package it

import java.io.File
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.bwsw.commitlog.filesystem.CommitLogCatalogue
import com.bwsw.tstreamstransactionserver.netty.client.Client
import com.bwsw.tstreamstransactionserver.netty.server.{Server, Time}
import com.bwsw.tstreamstransactionserver.options.CommonOptions.ZookeeperOptions
import com.bwsw.tstreamstransactionserver.options.{ClientBuilder, CommonOptions, ServerOptions}
import com.bwsw.tstreamstransactionserver.rpc.{ConsumerTransaction, ProducerTransaction, TransactionStates}
import org.apache.commons.io.FileUtils
import org.apache.curator.test.TestingServer
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

import scala.concurrent.{Future => ScalaFuture}
import scala.concurrent.Await
import scala.concurrent.duration._


class ServerLastCheckpointedTransactionTest extends FlatSpec with Matchers with BeforeAndAfterEach {

  val clientsNum = 2

  var zkTestServer: TestingServer = _
  var clients: Array[Client] = new Array[Client](clientsNum)
  var transactionServer: Server = _

  private val clientBuilder = new ClientBuilder()

  private object TestTimer extends Time {
    private val initialTime = System.currentTimeMillis()
    private var currentTime = initialTime

    override def getCurrentTime: Long = currentTime

    def resetTimer(): Unit = currentTime = initialTime

    def updateTime(newTime: Long) = currentTime = newTime
  }

  private val maxIdleTimeBetweenRecordsMs = 10000

  private val serverAuthOptions = ServerOptions.AuthOptions()
  private val serverBootstrapOptions = ServerOptions.BootstrapOptions()
  private val serverReplicationOptions = ServerOptions.ServerReplicationOptions()
  private val serverStorageOptions = ServerOptions.StorageOptions()
  private val serverRocksStorageOptions = ServerOptions.RocksStorageOptions()
  private val serverCommitLogOptions = ServerOptions.CommitLogOptions(commitLogCloseDelayMs = Int.MaxValue)
  private val serverPackageTransmissionOptions = ServerOptions.TransportOptions()
  private val serverZookeeperSpecificOptions = ServerOptions.ZooKeeperOptions()


  def startTransactionServer(): Server = {
    val serverZookeeperOptions = CommonOptions.ZookeeperOptions(endpoints = zkTestServer.getConnectString)
    transactionServer = new Server(
      authOpts = serverAuthOptions,
      zookeeperOpts = serverZookeeperOptions,
      serverOpts = serverBootstrapOptions,
      serverReplicationOpts = serverReplicationOptions,
      storageOpts = serverStorageOptions,
      rocksStorageOpts = serverRocksStorageOptions,
      commitLogOptions = serverCommitLogOptions,
      packageTransmissionOpts = serverPackageTransmissionOptions,
      zookeeperSpecificOpts = serverZookeeperSpecificOptions,
      timer = TestTimer
    )

    val latch = new CountDownLatch(1)
    new Thread(() => {
      transactionServer.start(latch.countDown())
    }).start()

    latch.await()
    transactionServer
  }

  override def beforeEach(): Unit = {
    FileUtils.deleteDirectory(new File(serverStorageOptions.path + java.io.File.separatorChar + serverStorageOptions.metadataDirectory))
    FileUtils.deleteDirectory(new File(serverStorageOptions.path + java.io.File.separatorChar + serverStorageOptions.dataDirectory))
    FileUtils.deleteDirectory(new File(serverStorageOptions.path + java.io.File.separatorChar + serverStorageOptions.commitLogRocksDirectory))
    FileUtils.deleteDirectory(new File(serverStorageOptions.path + java.io.File.separatorChar + serverStorageOptions.commitLogDirectory))

    TestTimer.resetTimer()
    zkTestServer = new TestingServer(true)
    startTransactionServer()
    (0 until clientsNum) foreach (index => clients(index) = clientBuilder.withZookeeperOptions(ZookeeperOptions(endpoints = zkTestServer.getConnectString)).build())
  }

  override def afterEach() {
    TestTimer.resetTimer()
    clients.foreach(_.shutdown())
    transactionServer.shutdown()
    zkTestServer.close()

    FileUtils.deleteDirectory(new File(serverStorageOptions.path + java.io.File.separatorChar + serverStorageOptions.metadataDirectory))
    FileUtils.deleteDirectory(new File(serverStorageOptions.path + java.io.File.separatorChar + serverStorageOptions.dataDirectory))
    FileUtils.deleteDirectory(new File(serverStorageOptions.path + java.io.File.separatorChar + serverStorageOptions.commitLogRocksDirectory))
    FileUtils.deleteDirectory(new File(serverStorageOptions.path + java.io.File.separatorChar + serverStorageOptions.commitLogDirectory))
  }

  implicit object ProducerTransactionSortable extends Ordering[ProducerTransaction] {
    override def compare(x: ProducerTransaction, y: ProducerTransaction): Int = {
      if (x.stream > y.stream) 1
      else if (x.stream < y.stream) -1
      else if (x.partition > y.partition) 1
      else if (x.partition < y.partition) -1
      else if (x.transactionID > y.transactionID) 1
      else if (x.transactionID < y.transactionID) -1
      else 0
    }
  }

  private val rand = scala.util.Random

  private def getRandomStream =
    new com.bwsw.tstreamstransactionserver.rpc.StreamValue {
      override val name: String = rand.nextInt(10000).toString
      override val partitions: Int = rand.nextInt(10000)
      override val description: Option[String] = if (rand.nextBoolean()) Some(rand.nextInt(10000).toString) else None
      override val ttl: Long = Long.MaxValue
      override val zkPath: Option[String] = None
    }

  private def getRandomConsumerTransaction(streamID: Int, streamObj: com.bwsw.tstreamstransactionserver.rpc.StreamValue) =
    new ConsumerTransaction {
      override val transactionID: Long = scala.util.Random.nextLong()
      override val name: String = rand.nextInt(10000).toString
      override val stream: Int = streamID
      override val partition: Int = streamObj.partitions
    }


  val secondsWait = 5


  "One client" should "put a stream, then another client should put transactions on the stream on a partition. " +
    "After that the first client tries to put transactions on the stream on the partition and clients should get the same last checkpointed transaction." in {
    val stream = getRandomStream

    val firstClient = clients(0)
    val secondClient = clients(1)

    val streamID = Await.result(firstClient.putStream(stream), secondsWait.seconds)

    Await.result(secondClient.delStream(stream.name), secondsWait.seconds) shouldBe true

    val streamUpdated = stream.copy(description = Some("I replace a previous one."))
    Await.result(secondClient.putStream(streamUpdated), secondsWait.seconds) shouldBe (streamID + 1)



    TestTimer.updateTime(TestTimer.getCurrentTime + TimeUnit.SECONDS.toMillis(1))

    //transactions are processed in the async mode
    val producerTransaction1 = ProducerTransaction(streamID, stream.partitions, TestTimer.getCurrentTime, TransactionStates.Opened, -1, Long.MaxValue)

    Await.result(secondClient.putProducerState(producerTransaction1), secondsWait.seconds) shouldBe true
    Await.result(secondClient.putProducerState(producerTransaction1.copy(state = TransactionStates.Checkpointed)), secondsWait.seconds) shouldBe true
    Await.result(firstClient.getLastCheckpointedTransaction(streamID, stream.partitions), secondsWait.seconds) shouldBe -1L
    Await.result(secondClient.getLastCheckpointedTransaction(streamID, stream.partitions), secondsWait.seconds) shouldBe -1L

    //it's required to close a current commit log file
    TestTimer.updateTime(TestTimer.getCurrentTime + maxIdleTimeBetweenRecordsMs)
    Await.result(firstClient.putConsumerCheckpoint(getRandomConsumerTransaction(streamID, stream)), secondsWait.seconds)
    //it's required to a CommitLogToBerkeleyWriter writes the producer transactions to db
    transactionServer.scheduledCommitLogImpl.run()
    transactionServer.berkeleyWriter.run()

    Await.result(firstClient.getLastCheckpointedTransaction(streamID, stream.partitions), secondsWait.seconds) shouldBe producerTransaction1.transactionID
    Await.result(secondClient.getLastCheckpointedTransaction(streamID, stream.partitions), secondsWait.seconds) shouldBe producerTransaction1.transactionID

    val producerTransaction2 = ProducerTransaction(streamID, stream.partitions, TestTimer.getCurrentTime, TransactionStates.Opened, -1, Long.MaxValue)
    Await.result(secondClient.putProducerState(producerTransaction2.copy()), secondsWait.seconds) shouldBe true
    Await.result(secondClient.putProducerState(producerTransaction2.copy(state = TransactionStates.Checkpointed)), secondsWait.seconds) shouldBe true
    Await.result(secondClient.putProducerState(producerTransaction2.copy(state = TransactionStates.Opened, transactionID = TestTimer.getCurrentTime + 1L)), secondsWait.seconds) shouldBe true

    Await.result(firstClient.getLastCheckpointedTransaction(streamID, stream.partitions), secondsWait.seconds) shouldBe producerTransaction1.transactionID
    Await.result(secondClient.getLastCheckpointedTransaction(streamID, stream.partitions), secondsWait.seconds) shouldBe producerTransaction1.transactionID


    //it's required to close a current commit log file
    TestTimer.updateTime(TestTimer.getCurrentTime + maxIdleTimeBetweenRecordsMs)
    Await.result(firstClient.putConsumerCheckpoint(getRandomConsumerTransaction(streamID, stream)), secondsWait.seconds)
    //it's required to a CommitLogToBerkeleyWriter writes the producer transactions to db
    transactionServer.scheduledCommitLogImpl.run()
    transactionServer.berkeleyWriter.run()

    Await.result(firstClient.getLastCheckpointedTransaction(streamID, stream.partitions), secondsWait.seconds) shouldBe producerTransaction2.transactionID
    Await.result(secondClient.getLastCheckpointedTransaction(streamID, stream.partitions), secondsWait.seconds) shouldBe producerTransaction2.transactionID
  }

  it should "return last checkpointed transaction" in {
    val ALL = 100
    val transactions = for (i <- 0 until ALL) yield {
      TestTimer.updateTime(TestTimer.getCurrentTime + 1L)
      TestTimer.getCurrentTime
    }
    val transaction = transactions.head

    val client = clients(0)

    val stream = com.bwsw.tstreamstransactionserver.rpc.StreamValue("test_stream", 32, None, 360)
    val streamID = Await.result(client.putStream(stream), secondsWait.seconds)

    val partition = 1
    Await.result(client.putProducerState(ProducerTransaction(streamID, partition, transactions.head, TransactionStates.Opened, -1, 120)), secondsWait.seconds) shouldBe true
    Await.result(client.putProducerState(ProducerTransaction(streamID, partition, transactions.head, TransactionStates.Checkpointed, -1, 120)), secondsWait.seconds) shouldBe true

    Await.ready(
      ScalaFuture.sequence(
        transactions.drop(1).map(t => client.putProducerState(ProducerTransaction(streamID, partition, t, TransactionStates.Opened, -1, 120)))
      )(implicitly, scala.concurrent.ExecutionContext.Implicits.global), secondsWait.seconds)


    //it's required to close a current commit log file
    TestTimer.updateTime(TestTimer.getCurrentTime + maxIdleTimeBetweenRecordsMs)
    Await.result(client.putConsumerCheckpoint(getRandomConsumerTransaction(streamID, stream)), secondsWait.seconds)
    //it's required to a CommitLogToBerkeleyWriter writes the producer transactions to db
    transactionServer.scheduledCommitLogImpl.run()
    transactionServer.berkeleyWriter.run()


    val retrievedTransaction = Await.result(client.getLastCheckpointedTransaction(streamID, partition),secondsWait.seconds)
    retrievedTransaction shouldEqual transaction
  }
}
