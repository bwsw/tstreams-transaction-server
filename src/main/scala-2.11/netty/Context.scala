package netty

import java.util.concurrent.{ExecutorService, Executors}

import com.google.common.util.concurrent.ThreadFactoryBuilder

import scala.concurrent.ExecutionContext


class Context(contextNum: Int, f: => ExecutorService) {
  require(contextNum > 0)

  private def newExecutionContext = ExecutionContext.fromExecutor(f)

  val contexts = Array.fill(contextNum)(newExecutionContext)

  def getContext(value: Long) = contexts((value % contextNum).toInt)

  val getContext = contexts(0)
}


object Context {
  def apply(contextNum: Int, f: => ExecutorService): Context = new Context(contextNum, f)
  def apply(contextNum: Int, nameFormat: String) = new Context(contextNum, Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(nameFormat).build()))
  def apply(f: => ExecutorService) = new Context(1, f)

  val serverPool = Context(Executors.newFixedThreadPool(configProperties.ServerConfig.transactionServerPool, new ThreadFactoryBuilder().setNameFormat("ServerPool-%d").build()))
  val clientPool = serverPool
  final val berkeleyWritePool = Context(1, "BerkeleyWritePool-%d")
  val berkeleyReadPool = Context(Executors.newFixedThreadPool(configProperties.ServerConfig.transactionServerBerkeleyReadPool, new ThreadFactoryBuilder().setNameFormat("BerkeleyReadPool-%d").build()))
  val rocksWritePool = Context(Executors.newFixedThreadPool(configProperties.ServerConfig.transactionServerRocksDBWritePool, new ThreadFactoryBuilder().setNameFormat("RocksWritePool-%d").build()))
  val rocksReadPool = Context(Executors.newFixedThreadPool(configProperties.ServerConfig.transactionServerRocksDBReadPool, new ThreadFactoryBuilder().setNameFormat("RocksReadPool-%d").build()))
}



