package com.bwsw.tstreamstransactionserver.netty.server

import com.bwsw.tstreamstransactionserver.configProperties.{ConfigFile, ServerConfig}
import org.apache.curator.retry.RetryNTimes
import org.apache.log4j.PropertyConfigurator
import org.slf4j.{Logger, LoggerFactory}
import com.bwsw.tstreamstransactionserver.zooKeeper.ZKLeaderClientToPutMaster
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.epoll.{EpollEventLoopGroup, EpollServerSocketChannel}
import io.netty.handler.logging.{LogLevel, LoggingHandler}

class Server(val config: ServerConfig = new ServerConfig(new ConfigFile("src/main/resources/serverProperties.properties"))) {

  import config._

  PropertyConfigurator.configure("src/main/resources/logServer.properties")
  private val logger: Logger = LoggerFactory.getLogger(classOf[Server])

  val zk = new ZKLeaderClientToPutMaster(zkEndpoints, zkTimeoutSession, zkTimeoutConnection,
    new RetryNTimes(zkRetriesMax, zkTimeoutBetweenRetries), zkPrefix)
  zk.putData(transactionServerAddress.getBytes())

  private val transactionServer = new TransactionServer(config)

  private val bossGroup = new EpollEventLoopGroup(1)
  private val workerGroup = new EpollEventLoopGroup()

  def start(): Unit = {
    try {
      val b = new ServerBootstrap()
      b.group(bossGroup, workerGroup)
        .channel(classOf[EpollServerSocketChannel])
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ServerInitializer(transactionServer, config.transactionServerPoolContext.getContext, logger))
        .option[java.lang.Integer](ChannelOption.SO_BACKLOG, 128)
        .childOption[java.lang.Boolean](ChannelOption.SO_KEEPALIVE, false)


      val f = b.bind(transactionServerListen, transactionServerPort).sync()
      f.channel().closeFuture().sync()
    } finally {
      zk.close()
      workerGroup.shutdownGracefully()
      bossGroup.shutdownGracefully()
      transactionServer.shutdown()
      config.shutdownThreadPools()
    }
  }

  def shutdown() = {
    zk.close()
    workerGroup.shutdownGracefully()
    bossGroup.shutdownGracefully()
    transactionServer.shutdown()
    config.shutdownThreadPools()
  }
}
