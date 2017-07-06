package benchmark.utils.writer

import benchmark.utils.{CsvWriter, TimeMeasure, TransactionCreator}
import com.bwsw.tstreamstransactionserver.options.ClientBuilder
import com.bwsw.tstreamstransactionserver.options.ClientOptions.ConnectionOptions
import com.bwsw.tstreamstransactionserver.rpc.TransactionStates

import scala.concurrent.Await
import scala.concurrent.duration._

class TransactionMetadataWriter(streamID: Int, partition: Int = 1) extends TransactionCreator with CsvWriter with TimeMeasure {
  def run(txnCount: Int, filename: String) {
    val client = new ClientBuilder()
      .withAuthOptions(
        com.bwsw.tstreamstransactionserver.options.ClientOptions.AuthOptions(key = "pingstation")
      )
      .withConnectionOptions(ConnectionOptions(requestTimeoutMs = 100))
      .build()

    var globalProgress = 1
    val result = (1 to txnCount).map(x => {
      val localProgress = (x.toDouble / txnCount * 100).round
      if (globalProgress == localProgress) {
        println(localProgress + "%")
        globalProgress += 1
      }

      val openedProducerTransaction = createTransaction(streamID, partition, TransactionStates.Opened)
      (x, {
        time(Await.result(client.putProducerState(openedProducerTransaction), 5.seconds))
      })
    })

    println(s"Write to file $filename")
    writeMetadataTransactionsAndTime(filename, result)

    client.shutdown()
  }
}
