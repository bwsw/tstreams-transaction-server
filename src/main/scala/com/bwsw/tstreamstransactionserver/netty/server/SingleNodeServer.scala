/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.bwsw.tstreamstransactionserver.netty.server

import java.util
import java.util.concurrent.{Executors, PriorityBlockingQueue, TimeUnit}

import com.bwsw.commitlog.filesystem.{CommitLogCatalogue, CommitLogFile, CommitLogStorage}
import com.bwsw.tstreamstransactionserver.configProperties.ServerExecutionContextGrids
import com.bwsw.tstreamstransactionserver.exception.Throwable.ZkNoConnectionException
import com.bwsw.tstreamstransactionserver.netty.SocketHostPortPair
import com.bwsw.tstreamstransactionserver.netty.server.commitLogService._
import com.bwsw.tstreamstransactionserver.netty.server.db.rocks.RocksDbConnection
import com.bwsw.tstreamstransactionserver.netty.server.handler.RequestHandlerRouter
import com.bwsw.tstreamstransactionserver.netty.server.subscriber.{OpenTransactionStateNotifier, SubscriberNotifier, SubscribersObserver}
import com.bwsw.tstreamstransactionserver.options.CommonOptions
import com.bwsw.tstreamstransactionserver.options.ServerOptions._
import com.bwsw.tstreamstransactionserver.rpc.{ConsumerTransaction, ProducerTransaction}
import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.epoll.{EpollEventLoopGroup, EpollServerSocketChannel}
import io.netty.channel.{ChannelOption, SimpleChannelInboundHandler}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryForever
import org.slf4j.{Logger, LoggerFactory}


class SingleNodeServer(authenticationOpts: AuthenticationOptions,
                       zookeeperOpts: CommonOptions.ZookeeperOptions,
                       serverOpts: BootstrapOptions,
                       serverReplicationOpts: ServerReplicationOptions,
                       storageOpts: StorageOptions,
                       rocksStorageOpts: RocksStorageOptions,
                       commitLogOptions: CommitLogOptions,
                       packageTransmissionOpts: TransportOptions,
                       subscribersUpdateOptions: SubscriberUpdateOptions,
                       serverHandler: (RequestHandlerRouter, Logger) =>
               SimpleChannelInboundHandler[ByteBuf] = (handler, logger) => new ServerHandler(handler, logger),
                       timer: Time = new Time{}
            ) {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)
  @volatile private var isShutdown = false
  private val transactionServerSocketAddress = createTransactionServerAddress()
  private def createTransactionServerAddress() = {
    (System.getenv("HOST"), System.getenv("PORT0")) match {
      case (host, port) if host != null && port != null && scala.util.Try(port.toInt).isSuccess => (host, port.toInt)
      case _ => (serverOpts.bindHost, serverOpts.bindPort)
    }
  }

  SocketHostPortPair.validate(serverOpts.bindHost, serverOpts.bindPort)

  private val zk = scala.util.Try(
    new ZKClientServer(
      transactionServerSocketAddress._1,
      transactionServerSocketAddress._2,
      zookeeperOpts.endpoints,
      zookeeperOpts.sessionTimeoutMs,
      zookeeperOpts.connectionTimeoutMs,
      new RetryForever(zookeeperOpts.retryDelayMs)
    )) match {
    case scala.util.Success(client) =>
      client
    case scala.util.Failure(throwable) =>
      shutdown()
      throw throwable
  }

  private val executionContext = new ServerExecutionContextGrids(
    rocksStorageOpts.readThreadPool,
    rocksStorageOpts.writeThreadPool
  )

  private val zkStreamDatabase =
    zk.streamDatabase(s"${storageOpts.streamZookeeperDirectory}")
  private val transactionServer = new TransactionServer(
    executionContext,
    authenticationOpts,
    storageOpts,
    rocksStorageOpts,
    zkStreamDatabase
  )

  final def notifyProducerTransactionCompleted(onNotificationCompleted: ProducerTransaction => Boolean,
                                               func: => Unit): Long =
    transactionServer.notifyProducerTransactionCompleted(onNotificationCompleted, func)

  final def removeNotification(id: Long): Boolean =
    transactionServer.removeProducerTransactionNotification(id)

  final def notifyConsumerTransactionCompleted(onNotificationCompleted: ConsumerTransaction => Boolean,
                                               func: => Unit): Long =
    transactionServer.notifyConsumerTransactionCompleted(onNotificationCompleted, func)

  final def removeConsumerNotification(id: Long): Boolean =
    transactionServer.removeConsumerTransactionNotification(id)


  private val rocksDBCommitLog = new RocksDbConnection(
    rocksStorageOpts,
    s"${storageOpts.path}${java.io.File.separatorChar}${storageOpts.commitLogRocksDirectory}",
    commitLogOptions.expungeDelaySec
  )

  private val commitLogQueue = {
    val queue = new CommitLogQueueBootstrap(
      30,
      new CommitLogCatalogue(storageOpts.path + java.io.File.separatorChar + storageOpts.commitLogRawDirectory),
      transactionServer
    )
    val priorityQueue = queue.fillQueue()
    priorityQueue
  }

  /**
    * this variable is public for testing purposes only
    */
  val berkeleyWriter = new CommitLogToRocksWriter(
    rocksDBCommitLog,
    commitLogQueue,
    transactionServer,
    commitLogOptions.incompleteReadPolicy
  ) {
    override def getCurrentTime: Long = timer.getCurrentTime
  }



  private val fileIDGenerator = new zk.FileIDGenerator(
    commitLogOptions.zkFileIdGeneratorPath
  )

  val scheduledCommitLogImpl = new ScheduledCommitLog(commitLogQueue,
    storageOpts,
    commitLogOptions,
    fileIDGenerator.increment
  ) {
    override def getCurrentTime: Long = timer.getCurrentTime
  }


  private val databaseWriterExecutor =
    Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("DatabaseWriter-%d").build())
  private val commitLogCloseExecutor =
    Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("CommitLogClose-%d").build())

  private val bossGroup =
    new EpollEventLoopGroup(1)

  private val workerGroup =
    new EpollEventLoopGroup()

  private val orderedExecutionPool =
    new OrderedExecutionContextPool(serverOpts.openOperationsPoolSize)



  private val curatorSubscriberClient =
    if (subscribersUpdateOptions.monitoringZkEndpoints.isEmpty) {
      zk.client
    }
    else {
      val connection = CuratorFrameworkFactory.builder()
        .sessionTimeoutMs(zookeeperOpts.sessionTimeoutMs)
        .connectionTimeoutMs(zookeeperOpts.connectionTimeoutMs)
        .retryPolicy(new RetryForever(zookeeperOpts.retryDelayMs))
        .connectString(subscribersUpdateOptions.monitoringZkEndpoints.get)
        .build()

      connection.start()
      val isConnected = connection.blockUntilConnected(
        zookeeperOpts.connectionTimeoutMs,
        TimeUnit.MILLISECONDS
      )
      if (isConnected)
        connection
      else
        throw new ZkNoConnectionException(subscribersUpdateOptions.monitoringZkEndpoints.get)
    }

  private val openTransactionStateNotifier =
    new OpenTransactionStateNotifier(
      new SubscribersObserver(
        curatorSubscriberClient,
        zkStreamDatabase,
        subscribersUpdateOptions.updatePeriodMs
      ),
      new SubscriberNotifier
    )

  private val requestHandlerChooser: RequestHandlerRouter =
    new RequestHandlerRouter(
      transactionServer,
      scheduledCommitLogImpl,
      packageTransmissionOpts,
      authenticationOpts,
      orderedExecutionPool,
      openTransactionStateNotifier
    )

  def start(function: => Unit = ()): Unit = {
    try {
      val b = new ServerBootstrap()
      b.group(bossGroup, workerGroup)
        .channel(classOf[EpollServerSocketChannel])
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ServerInitializer(serverHandler(requestHandlerChooser, logger)))
        .option[java.lang.Integer](ChannelOption.SO_BACKLOG, 128)
        .childOption[java.lang.Boolean](ChannelOption.SO_KEEPALIVE, false)

      val f = b.bind(serverOpts.bindHost, serverOpts.bindPort).sync()

      databaseWriterExecutor.scheduleWithFixedDelay(
        scheduledCommitLogImpl,
        commitLogOptions.closeDelayMs,
        commitLogOptions.closeDelayMs,
        java.util.concurrent.TimeUnit.MILLISECONDS
      )
      commitLogCloseExecutor.scheduleWithFixedDelay(
        berkeleyWriter,
        0L,
        10L,
        java.util.concurrent.TimeUnit.MILLISECONDS
      )

      zk.putSocketAddress(zookeeperOpts.prefix)

      val channel = f.channel().closeFuture()
      function
      channel.sync()
    } finally {
      shutdown()
    }
  }

  def shutdown(): Unit = {
    if (!isShutdown) {
      isShutdown = true
      if (bossGroup != null) {
        bossGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).sync()
      }
      if (workerGroup != null) {
        workerGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).sync()
      }
      if (zk != null)
        zk.close()

      if (databaseWriterExecutor != null) {
        databaseWriterExecutor.shutdown()
        databaseWriterExecutor.awaitTermination(
          commitLogOptions.closeDelayMs * 5,
          TimeUnit.MILLISECONDS
        )
      }

      if (scheduledCommitLogImpl != null)
        scheduledCommitLogImpl.closeWithoutCreationNewFile()

      if (commitLogCloseExecutor != null) {
        commitLogCloseExecutor.shutdown()
        commitLogCloseExecutor.awaitTermination(
          commitLogOptions.closeDelayMs * 5,
          TimeUnit.MILLISECONDS
        )
      }

      if (berkeleyWriter != null) {
        berkeleyWriter.run()
        berkeleyWriter.closeRocksDB()
      }

      if (orderedExecutionPool != null) {
        orderedExecutionPool.close()
      }

      if (transactionServer != null) {
        transactionServer.stopAccessNewTasksAndAwaitAllCurrentTasksAreCompleted()
        transactionServer.closeAllDatabases()
      }
    }
  }
}

class CommitLogQueueBootstrap(queueSize: Int, commitLogCatalogue: CommitLogCatalogue, transactionServer: TransactionServer) {
  def fillQueue(): PriorityBlockingQueue[CommitLogStorage] = {
    val allFiles = commitLogCatalogue.listAllFilesAndTheirIDs().toMap


    val berkeleyProcessedFileIDMax = transactionServer.getLastProcessedCommitLogFileID
    val (allFilesIDsToProcess, allFilesToDelete: Map[Long, CommitLogFile]) =
      if (berkeleyProcessedFileIDMax > -1)
        (allFiles.filterKeys(_ > berkeleyProcessedFileIDMax), allFiles.filterKeys(_ <= berkeleyProcessedFileIDMax))
      else
        (allFiles, collection.immutable.Map())

    allFilesToDelete.values.foreach(_.delete())

    if (allFilesIDsToProcess.nonEmpty) {
      import scala.collection.JavaConverters.asJavaCollectionConverter
      val filesToProcess: util.Collection[CommitLogFile] = allFilesIDsToProcess
        .map{case (id, _) => new CommitLogFile(allFiles(id).getFile.getPath)}
        .asJavaCollection

      val maxSize = scala.math.max(filesToProcess.size, queueSize)
      val commitLogQueue = new PriorityBlockingQueue[CommitLogStorage](maxSize)

      if (filesToProcess.isEmpty) commitLogQueue
      else if (commitLogQueue.addAll(filesToProcess)) commitLogQueue
      else throw new Exception("Something goes wrong here")
    } else {
      new PriorityBlockingQueue[CommitLogStorage](queueSize)
    }
  }
}