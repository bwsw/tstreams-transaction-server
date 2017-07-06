package it

import java.io.File
import java.util.concurrent.atomic.AtomicLong

import com.bwsw.tstreamstransactionserver.configProperties.ServerExecutionContextGrids
import com.bwsw.tstreamstransactionserver.netty.server.TransactionServer
import com.bwsw.tstreamstransactionserver.netty.server.db.zk.StreamDatabaseZK
import com.bwsw.tstreamstransactionserver.netty.server.transactionMetadataService.ProducerTransactionRecord
import com.bwsw.tstreamstransactionserver.netty.server.transactionMetadataService.stateHandler.LastOpenedAndCheckpointedTransaction
import com.bwsw.tstreamstransactionserver.options.ServerOptions.{RocksStorageOptions, StorageOptions}
import com.bwsw.tstreamstransactionserver.rpc.{ProducerTransaction, Transaction, TransactionStates}
import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import util.Utils.startZkServerAndGetIt

import scala.annotation.tailrec
import scala.language.reflectiveCalls

class ServerLastTransactionTestSuite extends FlatSpec with Matchers with BeforeAndAfterEach {

  private val rand = scala.util.Random

  private val nameGen = new AtomicLong(1L)
  private def getRandomStream = com.bwsw.tstreamstransactionserver.rpc.StreamValue(
    name = nameGen.getAndIncrement().toString,
    partitions = rand.nextInt(10000),
    description = if (rand.nextBoolean()) Some(rand.nextInt(10000).toString) else None,
    ttl = Long.MaxValue
  )

  private def getRandomProducerTransaction(streamID: Int, streamObj: com.bwsw.tstreamstransactionserver.rpc.StreamValue, txnID: Long, ttlTxn: Long) = ProducerTransaction(
    stream = streamID,
    partition = streamObj.partitions,
    transactionID = txnID,
    state = TransactionStates.Opened,
    quantity = -1,
    ttl = ttlTxn
  )

  private val storageOptions = StorageOptions(path = "/tmp")

  override def beforeEach(): Unit = {
    FileUtils.deleteDirectory(new File(storageOptions.path + java.io.File.separatorChar + storageOptions.metadataDirectory))
    FileUtils.deleteDirectory(new File(storageOptions.path + java.io.File.separatorChar + storageOptions.dataDirectory))
    FileUtils.deleteDirectory(new File(storageOptions.path + java.io.File.separatorChar + storageOptions.commitLogRocksDirectory))
    FileUtils.deleteDirectory(new File(storageOptions.path + java.io.File.separatorChar + storageOptions.commitLogRawDirectory))
  }

  override def afterEach() {
    beforeEach()
  }
  private val path = "/tts/test_path"
  it should "correctly return last transaction id per stream and partition" in {
    val authOptions = com.bwsw.tstreamstransactionserver.options.ServerOptions.AuthenticationOptions()
    val storageOptions = StorageOptions()
    val rocksStorageOptions = RocksStorageOptions()
    val serverExecutionContext = new ServerExecutionContextGrids(2, 2)

    val secondsAwait = 5

    val streamsNumber = 50
    val producerTxnPerStreamPartitionMaxNumber = 100

    val (zkServer, zkClient) = startZkServerAndGetIt
    val streamDatabaseZK = new StreamDatabaseZK(zkClient, path)
    val transactionServer = new TransactionServer(
      executionContext = serverExecutionContext,
      authOpts = authOptions,
      storageOpts = storageOptions,
      rocksStorageOpts = rocksStorageOptions,
      streamDatabaseZK
    ) {
      final def getLastTransactionIDWrapper(stream: Int, partition: Int): Option[LastOpenedAndCheckpointedTransaction] = {
        val lastTransaction = getLastTransactionIDAndCheckpointedID(stream, partition)
        lastTransaction
      }
    }

    val streams = Array.fill(streamsNumber)(getRandomStream)
    val streamsAndIDs = streams.map(stream =>
      (transactionServer.putStream(stream.name, stream.partitions, stream.description, stream.ttl), stream)
    )


    streamsAndIDs foreach {case (streamId, stream) =>
      val producerTransactionsNumber = rand.nextInt(producerTxnPerStreamPartitionMaxNumber)

      val producerTransactionsWithTimestamp: Array[(ProducerTransaction, Long)] = (0 to producerTransactionsNumber).map { transactionID =>
        val producerTransaction = getRandomProducerTransaction(streamId, stream, transactionID.toLong, Long.MaxValue)
        (producerTransaction, System.currentTimeMillis())
      }.toArray

      val maxTransactionID =
        producerTransactionsWithTimestamp.maxBy(_._1.transactionID)._1.transactionID

      val transactionsWithTimestamp =
        producerTransactionsWithTimestamp.map { case (producerTxn, timestamp) => ProducerTransactionRecord(producerTxn, timestamp) }

      val currentTime = System.currentTimeMillis()
      val bigCommit = transactionServer.getBigCommit(1L)
      bigCommit.putProducerTransactions(transactionsWithTimestamp)
      bigCommit.commit()

      val lastTransactionIDAndCheckpointedID = transactionServer.getLastTransactionIDWrapper(streamId, stream.partitions).get
      lastTransactionIDAndCheckpointedID.opened.id shouldBe maxTransactionID
    }
    transactionServer.stopAccessNewTasksAndAwaitAllCurrentTasksAreCompletedAndCloseDatabases()
    zkServer.close()
    zkClient.close()
  }

  it should "correctly return last transaction and last checkpointed transaction id per stream and partition" in {
    val authOptions = com.bwsw.tstreamstransactionserver.options.ServerOptions.AuthenticationOptions()
    val storageOptions = StorageOptions()
    val rocksStorageOptions = RocksStorageOptions()
    val serverExecutionContext = new ServerExecutionContextGrids(2, 2)

    val secondsAwait = 5

    val streamsNumber = 1
    val producerTxnPerStreamPartitionMaxNumber = 100

    val (zkServer, zkClient) = startZkServerAndGetIt
    val streamDatabaseZK = new StreamDatabaseZK(zkClient, path)
    val transactionServer = new TransactionServer(
      executionContext = serverExecutionContext,
      authOpts = authOptions,
      storageOpts = storageOptions,
      rocksStorageOpts = rocksStorageOptions,
      streamDatabaseZK
    ) {
      final def getLastTransactionIDWrapper(stream: Int, partition: Int): Option[LastOpenedAndCheckpointedTransaction] = {
        val lastTransaction = getLastTransactionIDAndCheckpointedID(stream, partition)
        lastTransaction
      }
    }

    val streams = Array.fill(streamsNumber)(getRandomStream)
    val streamsAndIDs = streams.map(stream =>
      (transactionServer.putStream(stream.name, stream.partitions, stream.description, stream.ttl), stream)
    )


    streamsAndIDs foreach {case (streamId, stream) =>
      val producerTransactionsNumber = rand.nextInt(producerTxnPerStreamPartitionMaxNumber) + 1

      var currentTimeInc = System.currentTimeMillis()
      val producerTransactionsWithTimestampWithoutChecpointed: Array[(ProducerTransaction, Long)] = (0 until producerTransactionsNumber).map { transactionID =>
        val producerTransaction = getRandomProducerTransaction(streamId, stream, transactionID.toLong, Long.MaxValue)
        currentTimeInc = currentTimeInc + 1
        (producerTransaction, currentTimeInc)
      }.toArray

      val transactionInCertainIndex = producerTransactionsWithTimestampWithoutChecpointed(producerTransactionsNumber - 1)
      val producerTransactionsWithTimestamp = producerTransactionsWithTimestampWithoutChecpointed :+ (transactionInCertainIndex._1.copy(state = TransactionStates.Checkpointed), currentTimeInc)

      val maxTransactionID = producerTransactionsWithTimestamp.maxBy(_._1.transactionID)._1.transactionID

      val transactionsWithTimestamp = producerTransactionsWithTimestamp.map{case (producerTxn, timestamp) => ProducerTransactionRecord(producerTxn, timestamp)}

      val currentTime = System.currentTimeMillis()
      val bigCommit = transactionServer.getBigCommit(1L)
      bigCommit.putProducerTransactions(transactionsWithTimestamp)
      bigCommit.commit()

      val lastTransactionIDAndCheckpointedID = transactionServer.getLastTransactionIDWrapper(streamId, stream.partitions).get

      lastTransactionIDAndCheckpointedID.opened.id shouldBe maxTransactionID
      lastTransactionIDAndCheckpointedID.checkpointed.get.id shouldBe maxTransactionID

    }
    transactionServer.stopAccessNewTasksAndAwaitAllCurrentTasksAreCompletedAndCloseDatabases()
    zkServer.close()
    zkClient.close()
  }

  it should "correctly return last transaction and last checkpointed transaction id per stream and partition and checkpointed transaction should be proccessed earlier" in {
    val authOptions = com.bwsw.tstreamstransactionserver.options.ServerOptions.AuthenticationOptions()
    val storageOptions = StorageOptions()
    val rocksStorageOptions = RocksStorageOptions()
    val serverExecutionContext = new ServerExecutionContextGrids(2, 2)

    val secondsAwait = 5
    val streamsNumber = 1

    val (zkServer, zkClient) = startZkServerAndGetIt
    val streamDatabaseZK = new StreamDatabaseZK(zkClient, path)
    val transactionServer = new TransactionServer(
      executionContext = serverExecutionContext,
      authOpts = authOptions,
      storageOpts = storageOptions,
      rocksStorageOpts = rocksStorageOptions,
      streamDatabaseZK
    ) {
      final def getLastTransactionIDWrapper(stream: Int, partition: Int): Option[LastOpenedAndCheckpointedTransaction] = {
        val lastTransaction = getLastTransactionIDAndCheckpointedID(stream, partition)
        lastTransaction
      }
    }

    val streams = Array.fill(streamsNumber)(getRandomStream)
    val streamsAndIDs = streams.map(stream =>
      (transactionServer.putStream(stream.name, stream.partitions, stream.description, stream.ttl), stream)
    )


    streamsAndIDs foreach {case (streamID, stream) =>
      val currentTimeInc = new AtomicLong(System.currentTimeMillis())
      val transactionRootChain = getRandomProducerTransaction(streamID, stream, 1, Long.MaxValue)
      val producerTransactionsWithTimestamp: Array[(ProducerTransaction, Long)] =
        Array(
          (transactionRootChain, currentTimeInc.getAndIncrement()),
          (transactionRootChain.copy(transactionID = 1L, state = TransactionStates.Checkpointed), currentTimeInc.getAndIncrement()),
          (transactionRootChain.copy(transactionID = 2L, state = TransactionStates.Updated), currentTimeInc.getAndIncrement()),
          (transactionRootChain.copy(transactionID = 3L, state = TransactionStates.Opened), currentTimeInc.getAndIncrement()),
          (transactionRootChain.copy(transactionID = 2L, state = TransactionStates.Opened), currentTimeInc.getAndIncrement())
        )

      val transactionsWithTimestamp =
        producerTransactionsWithTimestamp.map{case (producerTxn, timestamp) => ProducerTransactionRecord(producerTxn, timestamp)}

      val currentTime = System.currentTimeMillis()
      val bigCommit = transactionServer.getBigCommit(1L)
      bigCommit.putProducerTransactions(transactionsWithTimestamp)
      bigCommit.commit()

      val lastTransactionIDAndCheckpointedID = transactionServer.getLastTransactionIDWrapper(streamID, stream.partitions).get

      lastTransactionIDAndCheckpointedID.opened.id shouldBe 3L
      lastTransactionIDAndCheckpointedID.checkpointed.get.id shouldBe 1L

    }
    transactionServer.stopAccessNewTasksAndAwaitAllCurrentTasksAreCompletedAndCloseDatabases()
    zkServer.close()
    zkClient.close()
  }

  it should "correctly return last transaction id per stream and partition even if some transactions are out of order." in {
    val authOptions = com.bwsw.tstreamstransactionserver.options.ServerOptions.AuthenticationOptions()
    val storageOptions = StorageOptions()
    val rocksStorageOptions = RocksStorageOptions()
    val serverExecutionContext = new ServerExecutionContextGrids(2, 2)

    val secondsAwait = 5

    val streamsNumber = 2
    val producerTxnPerStreamPartitionMaxNumber = 50

    val (zkServer, zkClient) = startZkServerAndGetIt
    val streamDatabaseZK = new StreamDatabaseZK(zkClient, path)
    val transactionServer = new TransactionServer(
      executionContext = serverExecutionContext,
      authOpts = authOptions,
      storageOpts = storageOptions,
      rocksStorageOpts = rocksStorageOptions,
      streamDatabaseZK
    ) {
      final def getLastTransactionIDWrapper(stream: Int, partition: Int): Option[LastOpenedAndCheckpointedTransaction] = {
        val lastTransaction = getLastTransactionIDAndCheckpointedID(stream, partition)
        lastTransaction
      }
    }

    val streams = Array.fill(streamsNumber)(getRandomStream)

    @tailrec
    def getLastTransactionID(producerTransactions: List[ProducerTransaction], acc: Option[Long]): Option[Long] = {
      producerTransactions match {
        case Nil => acc
        case transaction :: otherTransactions =>
          if (acc.isEmpty) getLastTransactionID(otherTransactions, acc)
          else if (acc.get <= transaction.transactionID) getLastTransactionID(otherTransactions, Some(transaction.transactionID))
          else getLastTransactionID(otherTransactions, acc)
      }
    }

    val streamsAndIDs = streams.map(stream =>
      (transactionServer.putStream(stream.name, stream.partitions, stream.description, stream.ttl), stream)
    )

    streamsAndIDs foreach {case (streamID, stream) =>
      val producerTransactionsNumber = rand.nextInt(producerTxnPerStreamPartitionMaxNumber)


      val producerTransactions = scala.util.Random.shuffle(0 to producerTransactionsNumber).toArray.map { transactionID =>
        val transaction = getRandomProducerTransaction(streamID, stream, transactionID.toLong, Long.MaxValue)
        (transaction, System.currentTimeMillis() + rand.nextInt(100))
      }

      val producerTransactionsOrderedByTimestamp = producerTransactions.sortBy(_._2).toList
      val transactionsWithTimestamp =
        producerTransactionsOrderedByTimestamp.map { case (producerTxn, timestamp) => ProducerTransactionRecord(producerTxn, timestamp) }

      val currentTime = System.currentTimeMillis()
      val bigCommit = transactionServer.getBigCommit(1L)
      bigCommit.putProducerTransactions(transactionsWithTimestamp)
      bigCommit.commit()

      transactionServer.getLastTransactionIDWrapper(streamID, stream.partitions).get.opened.id shouldBe getLastTransactionID(producerTransactionsOrderedByTimestamp.map(_._1), Some(0L)).get
    }
    transactionServer.stopAccessNewTasksAndAwaitAllCurrentTasksAreCompletedAndCloseDatabases()
    zkServer.close()
    zkClient.close()
  }
}
