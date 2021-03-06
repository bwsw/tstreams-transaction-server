package benchmark.oneServer.multipleClients

import benchmark.utils.Installer

object Services extends Installer {
  private val streamName = "stream"
  private val clients = 16

  def main(args: Array[String]) {
    clearDB()
    startTransactionServer()
    Thread.sleep(1000L)
    createStream(streamName, clients)
  }
}
