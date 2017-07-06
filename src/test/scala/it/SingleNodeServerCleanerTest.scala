package it


import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import com.bwsw.tstreamstransactionserver.configProperties.ServerExecutionContextGrids
import com.bwsw.tstreamstransactionserver.netty.server.TransactionServer
import com.bwsw.tstreamstransactionserver.netty.server.db.zk.StreamDatabaseZK
import com.bwsw.tstreamstransactionserver.netty.server.transactionMetadataService.{ProducerTransactionKey, ProducerTransactionRecord}
import com.bwsw.tstreamstransactionserver.options.ServerOptions._
import com.bwsw.tstreamstransactionserver.rpc.{ProducerTransaction, Transaction, TransactionStates}
import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import util.Utils._

import scala.language.reflectiveCalls

class SingleNodeServerCleanerTest extends FlatSpec
  with Matchers
  with BeforeAndAfterEach {

  private val rand = scala.util.Random

  private def getRandomStream = com.bwsw.tstreamstransactionserver.rpc.StreamValue(
    name = rand.nextInt(10000).toString,
    partitions = rand.nextInt(10000),
    description = if (rand.nextBoolean()) Some(rand.nextInt(10000).toString) else None,
    ttl = Long.MaxValue
  )

  private val txnCounter = new AtomicLong(0)

  private def getRandomProducerTransaction(streamID: Int, streamObj: com.bwsw.tstreamstransactionserver.rpc.StreamValue, ttlTxn: Long) = ProducerTransaction(
    stream = streamID,
    partition = streamObj.partitions,
    transactionID = txnCounter.getAndIncrement(),
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


  "Cleaner" should "remove all expired transactions from OpenedTransactions table and invalidate them in AllTransactions table" in {
    val authOptions = com.bwsw.tstreamstransactionserver.options.ServerOptions.AuthenticationOptions()
    val storageOptions = StorageOptions()
    val rocksStorageOptions = RocksStorageOptions()
    val serverExecutionContext = new ServerExecutionContextGrids(2, 2)

    val maxTTLForProducerTransactionSec = 5

    val producerTxnNumber = 100

    val path = "/tts/streams"
    val (zkServer, zkClient) = startZkServerAndGetIt
    val streamDatabaseZK = new StreamDatabaseZK(zkClient, path)


    val transactionService = new TransactionServer(
      executionContext = serverExecutionContext,
      authOpts = authOptions,
      storageOpts = storageOptions,
      rocksStorageOpts = rocksStorageOptions,
      streamDatabaseZK
    ) {
      def checkTransactionExistInOpenedTable(stream: Int, partition: Int, transactionId: Long): Boolean = {
        val txn = getOpenedTransaction(ProducerTransactionKey(stream, partition, transactionId))
        txn.isDefined
      }
    }
    def ttlSec = TimeUnit.SECONDS.toMillis(rand.nextInt(maxTTLForProducerTransactionSec))

    val stream = getRandomStream

    val streamID = transactionService.putStream(stream.name, stream.partitions, stream.description, stream.ttl)

    val currentTime = System.currentTimeMillis()
    val producerTransactionsWithTimestamp: Array[(ProducerTransaction, Long)] = Array.fill(producerTxnNumber) {
      val producerTransaction = getRandomProducerTransaction(streamID, stream, ttlSec)
      (producerTransaction, System.currentTimeMillis())
    }
    val minTransactionID = producerTransactionsWithTimestamp.minBy(_._1.transactionID)._1.transactionID
    val maxTransactionID = producerTransactionsWithTimestamp.maxBy(_._1.transactionID)._1.transactionID

    val transactionsWithTimestamp = producerTransactionsWithTimestamp.map { case (producerTxn, timestamp) => ProducerTransactionRecord(producerTxn, timestamp) }

    val bigCommit = transactionService.getBigCommit(1L)
    bigCommit.putProducerTransactions(transactionsWithTimestamp)
    bigCommit.commit()

    transactionService.createAndExecuteTransactionsToDeleteTask(currentTime + TimeUnit.SECONDS.toMillis(maxTTLForProducerTransactionSec))
    val expiredTransactions = producerTransactionsWithTimestamp.map { case (producerTxn, _) =>
      ProducerTransaction(producerTxn.stream, producerTxn.partition, producerTxn.transactionID, TransactionStates.Invalid, 0, 0L)
    }

    transactionService.scanTransactions(streamID, stream.partitions, minTransactionID, maxTransactionID, Int.MaxValue, Set(TransactionStates.Opened)).producerTransactions should contain theSameElementsAs expiredTransactions

    (minTransactionID to maxTransactionID) foreach { transactionID =>
      transactionService.checkTransactionExistInOpenedTable(streamID, stream.partitions, transactionID) shouldBe false
    }

    transactionService.stopAccessNewTasksAndAwaitAllCurrentTasksAreCompletedAndCloseDatabases()
    zkClient.close()
    zkServer.close()
  }

}
