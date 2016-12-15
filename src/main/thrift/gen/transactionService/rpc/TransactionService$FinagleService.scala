/**
 * Generated by Scrooge
 *   version: 4.12.0
 *   rev: f7190e7f6b92684107b8cebf853d0d2403473022
 *   built at: 20161122-154730
 */
package transactionService.rpc

import com.twitter.finagle.Thrift
import com.twitter.finagle.stats.{NullStatsReceiver, StatsReceiver}
import com.twitter.scrooge.{ThriftStruct, TReusableMemoryTransport}
import com.twitter.util.{Future, Return, Throw, Throwables}
import java.nio.ByteBuffer
import java.util.Arrays
import org.apache.thrift.protocol._
import org.apache.thrift.TApplicationException
import org.apache.thrift.transport.TMemoryInputTransport
import scala.collection.mutable.{
  ArrayBuffer => mutable$ArrayBuffer, HashMap => mutable$HashMap}
import scala.collection.{Map, Set}

import scala.language.higherKinds


@javax.annotation.Generated(value = Array("com.twitter.scrooge.Compiler"))
class TransactionService$FinagleService(
  iface: TransactionService[Future],
  protocolFactory: TProtocolFactory,
  stats: StatsReceiver,
  maxThriftBufferSize: Int,
  serviceName: String
) extends com.twitter.finagle.Service[Array[Byte], Array[Byte]] {
  import TransactionService._

  def this(
    iface: TransactionService[Future],
    protocolFactory: TProtocolFactory,
    stats: StatsReceiver,
    maxThriftBufferSize: Int
  ) = this(iface, protocolFactory, stats, maxThriftBufferSize, "TransactionService")

  def this(
    iface: TransactionService[Future],
    protocolFactory: TProtocolFactory
  ) = this(iface, protocolFactory, NullStatsReceiver, Thrift.maxThriftBufferSize)

  private[this] val tlReusableBuffer = new ThreadLocal[TReusableMemoryTransport] {
    override def initialValue() = TReusableMemoryTransport(512)
  }

  private[this] def reusableBuffer: TReusableMemoryTransport = {
    val buf = tlReusableBuffer.get()
    buf.reset()
    buf
  }

  private[this] val resetCounter = stats.scope("buffer").counter("resetCount")

  private[this] def resetBuffer(trans: TReusableMemoryTransport): Unit = {
    if (trans.currentCapacity > maxThriftBufferSize) {
      resetCounter.incr()
      tlReusableBuffer.remove()
    }
  }

  protected val functionMap = new mutable$HashMap[String, (TProtocol, Int) => Future[Array[Byte]]]()

  protected def addFunction(name: String, f: (TProtocol, Int) => Future[Array[Byte]]): Unit = {
    functionMap(name) = f
  }

  protected def exception(name: String, seqid: Int, code: Int, message: String): Future[Array[Byte]] = {
    try {
      val x = new TApplicationException(code, message)
      val memoryBuffer = reusableBuffer
      try {
        val oprot = protocolFactory.getProtocol(memoryBuffer)

        oprot.writeMessageBegin(new TMessage(name, TMessageType.EXCEPTION, seqid))
        x.write(oprot)
        oprot.writeMessageEnd()
        oprot.getTransport().flush()
        Future.value(Arrays.copyOfRange(memoryBuffer.getArray(), 0, memoryBuffer.length()))
      } finally {
        resetBuffer(memoryBuffer)
      }
    } catch {
      case e: Exception => Future.exception(e)
    }
  }

  protected def reply(name: String, seqid: Int, result: ThriftStruct): Future[Array[Byte]] = {
    try {
      val memoryBuffer = reusableBuffer
      try {
        val oprot = protocolFactory.getProtocol(memoryBuffer)

        oprot.writeMessageBegin(new TMessage(name, TMessageType.REPLY, seqid))
        result.write(oprot)
        oprot.writeMessageEnd()

        Future.value(Arrays.copyOfRange(memoryBuffer.getArray(), 0, memoryBuffer.length()))
      } finally {
        resetBuffer(memoryBuffer)
      }
    } catch {
      case e: Exception => Future.exception(e)
    }
  }

  final def apply(request: Array[Byte]): Future[Array[Byte]] = {
    val inputTransport = new TMemoryInputTransport(request)
    val iprot = protocolFactory.getProtocol(inputTransport)

    try {
      val msg = iprot.readMessageBegin()
      val func = functionMap.get(msg.name)
      func match {
        case _root_.scala.Some(fn) =>
          fn(iprot, msg.seqid)
        case _ =>
          TProtocolUtil.skip(iprot, TType.STRUCT)
          exception(msg.name, msg.seqid, TApplicationException.UNKNOWN_METHOD,
            "Invalid method name: '" + msg.name + "'")
      }
    } catch {
      case e: Exception => Future.exception(e)
    }
  }

  // ---- end boilerplate.

  private[this] val scopedStats = if (serviceName != "") stats.scope(serviceName) else stats
  private[this] object __stats_putStream {
    val RequestsCounter = scopedStats.scope("putStream").counter("requests")
    val SuccessCounter = scopedStats.scope("putStream").counter("success")
    val FailuresCounter = scopedStats.scope("putStream").counter("failures")
    val FailuresScope = scopedStats.scope("putStream").scope("failures")
  }
  addFunction("putStream", { (iprot: TProtocol, seqid: Int) =>
    try {
      __stats_putStream.RequestsCounter.incr()
      val args = PutStream.Args.decode(iprot)
      iprot.readMessageEnd()
      (try {
        iface.putStream(args.token, args.stream, args.partitions, args.description, args.ttl)
      } catch {
        case e: Exception => Future.exception(e)
      }).flatMap { value: Boolean =>
        reply("putStream", seqid, PutStream.Result(success = Some(value)))
      }.rescue {
        case e => Future.exception(e)
      }.respond {
        case Return(_) =>
          __stats_putStream.SuccessCounter.incr()
        case Throw(ex) =>
          __stats_putStream.FailuresCounter.incr()
          __stats_putStream.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
      }
    } catch {
      case e: TProtocolException => {
        iprot.readMessageEnd()
        exception("putStream", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
      }
      case e: Exception => Future.exception(e)
    }
  })
  private[this] object __stats_doesStreamExist {
    val RequestsCounter = scopedStats.scope("doesStreamExist").counter("requests")
    val SuccessCounter = scopedStats.scope("doesStreamExist").counter("success")
    val FailuresCounter = scopedStats.scope("doesStreamExist").counter("failures")
    val FailuresScope = scopedStats.scope("doesStreamExist").scope("failures")
  }
  addFunction("doesStreamExist", { (iprot: TProtocol, seqid: Int) =>
    try {
      __stats_doesStreamExist.RequestsCounter.incr()
      val args = DoesStreamExist.Args.decode(iprot)
      iprot.readMessageEnd()
      (try {
        iface.doesStreamExist(args.token, args.stream)
      } catch {
        case e: Exception => Future.exception(e)
      }).flatMap { value: Boolean =>
        reply("doesStreamExist", seqid, DoesStreamExist.Result(success = Some(value)))
      }.rescue {
        case e => Future.exception(e)
      }.respond {
        case Return(_) =>
          __stats_doesStreamExist.SuccessCounter.incr()
        case Throw(ex) =>
          __stats_doesStreamExist.FailuresCounter.incr()
          __stats_doesStreamExist.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
      }
    } catch {
      case e: TProtocolException => {
        iprot.readMessageEnd()
        exception("doesStreamExist", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
      }
      case e: Exception => Future.exception(e)
    }
  })
  private[this] object __stats_getStream {
    val RequestsCounter = scopedStats.scope("getStream").counter("requests")
    val SuccessCounter = scopedStats.scope("getStream").counter("success")
    val FailuresCounter = scopedStats.scope("getStream").counter("failures")
    val FailuresScope = scopedStats.scope("getStream").scope("failures")
  }
  addFunction("getStream", { (iprot: TProtocol, seqid: Int) =>
    try {
      __stats_getStream.RequestsCounter.incr()
      val args = GetStream.Args.decode(iprot)
      iprot.readMessageEnd()
      (try {
        iface.getStream(args.token, args.stream)
      } catch {
        case e: Exception => Future.exception(e)
      }).flatMap { value: transactionService.rpc.Stream =>
        reply("getStream", seqid, GetStream.Result(success = Some(value)))
      }.rescue {
        case e => Future.exception(e)
      }.respond {
        case Return(_) =>
          __stats_getStream.SuccessCounter.incr()
        case Throw(ex) =>
          __stats_getStream.FailuresCounter.incr()
          __stats_getStream.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
      }
    } catch {
      case e: TProtocolException => {
        iprot.readMessageEnd()
        exception("getStream", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
      }
      case e: Exception => Future.exception(e)
    }
  })
  private[this] object __stats_delStream {
    val RequestsCounter = scopedStats.scope("delStream").counter("requests")
    val SuccessCounter = scopedStats.scope("delStream").counter("success")
    val FailuresCounter = scopedStats.scope("delStream").counter("failures")
    val FailuresScope = scopedStats.scope("delStream").scope("failures")
  }
  addFunction("delStream", { (iprot: TProtocol, seqid: Int) =>
    try {
      __stats_delStream.RequestsCounter.incr()
      val args = DelStream.Args.decode(iprot)
      iprot.readMessageEnd()
      (try {
        iface.delStream(args.token, args.stream)
      } catch {
        case e: Exception => Future.exception(e)
      }).flatMap { value: Boolean =>
        reply("delStream", seqid, DelStream.Result(success = Some(value)))
      }.rescue {
        case e => Future.exception(e)
      }.respond {
        case Return(_) =>
          __stats_delStream.SuccessCounter.incr()
        case Throw(ex) =>
          __stats_delStream.FailuresCounter.incr()
          __stats_delStream.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
      }
    } catch {
      case e: TProtocolException => {
        iprot.readMessageEnd()
        exception("delStream", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
      }
      case e: Exception => Future.exception(e)
    }
  })
  private[this] object __stats_putTransaction {
    val RequestsCounter = scopedStats.scope("putTransaction").counter("requests")
    val SuccessCounter = scopedStats.scope("putTransaction").counter("success")
    val FailuresCounter = scopedStats.scope("putTransaction").counter("failures")
    val FailuresScope = scopedStats.scope("putTransaction").scope("failures")
  }
  addFunction("putTransaction", { (iprot: TProtocol, seqid: Int) =>
    try {
      __stats_putTransaction.RequestsCounter.incr()
      val args = PutTransaction.Args.decode(iprot)
      iprot.readMessageEnd()
      (try {
        iface.putTransaction(args.token, args.transaction)
      } catch {
        case e: Exception => Future.exception(e)
      }).flatMap { value: Boolean =>
        reply("putTransaction", seqid, PutTransaction.Result(success = Some(value)))
      }.rescue {
        case e => Future.exception(e)
      }.respond {
        case Return(_) =>
          __stats_putTransaction.SuccessCounter.incr()
        case Throw(ex) =>
          __stats_putTransaction.FailuresCounter.incr()
          __stats_putTransaction.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
      }
    } catch {
      case e: TProtocolException => {
        iprot.readMessageEnd()
        exception("putTransaction", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
      }
      case e: Exception => Future.exception(e)
    }
  })
  private[this] object __stats_putTransactions {
    val RequestsCounter = scopedStats.scope("putTransactions").counter("requests")
    val SuccessCounter = scopedStats.scope("putTransactions").counter("success")
    val FailuresCounter = scopedStats.scope("putTransactions").counter("failures")
    val FailuresScope = scopedStats.scope("putTransactions").scope("failures")
  }
  addFunction("putTransactions", { (iprot: TProtocol, seqid: Int) =>
    try {
      __stats_putTransactions.RequestsCounter.incr()
      val args = PutTransactions.Args.decode(iprot)
      iprot.readMessageEnd()
      (try {
        iface.putTransactions(args.token, args.transactions)
      } catch {
        case e: Exception => Future.exception(e)
      }).flatMap { value: Boolean =>
        reply("putTransactions", seqid, PutTransactions.Result(success = Some(value)))
      }.rescue {
        case e => Future.exception(e)
      }.respond {
        case Return(_) =>
          __stats_putTransactions.SuccessCounter.incr()
        case Throw(ex) =>
          __stats_putTransactions.FailuresCounter.incr()
          __stats_putTransactions.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
      }
    } catch {
      case e: TProtocolException => {
        iprot.readMessageEnd()
        exception("putTransactions", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
      }
      case e: Exception => Future.exception(e)
    }
  })
  private[this] object __stats_scanTransactions {
    val RequestsCounter = scopedStats.scope("scanTransactions").counter("requests")
    val SuccessCounter = scopedStats.scope("scanTransactions").counter("success")
    val FailuresCounter = scopedStats.scope("scanTransactions").counter("failures")
    val FailuresScope = scopedStats.scope("scanTransactions").scope("failures")
  }
  addFunction("scanTransactions", { (iprot: TProtocol, seqid: Int) =>
    try {
      __stats_scanTransactions.RequestsCounter.incr()
      val args = ScanTransactions.Args.decode(iprot)
      iprot.readMessageEnd()
      (try {
        iface.scanTransactions(args.token, args.stream, args.partition, args.from, args.to)
      } catch {
        case e: Exception => Future.exception(e)
      }).flatMap { value: Seq[transactionService.rpc.Transaction] =>
        reply("scanTransactions", seqid, ScanTransactions.Result(success = Some(value)))
      }.rescue {
        case e => Future.exception(e)
      }.respond {
        case Return(_) =>
          __stats_scanTransactions.SuccessCounter.incr()
        case Throw(ex) =>
          __stats_scanTransactions.FailuresCounter.incr()
          __stats_scanTransactions.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
      }
    } catch {
      case e: TProtocolException => {
        iprot.readMessageEnd()
        exception("scanTransactions", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
      }
      case e: Exception => Future.exception(e)
    }
  })
  private[this] object __stats_putTransactionData {
    val RequestsCounter = scopedStats.scope("putTransactionData").counter("requests")
    val SuccessCounter = scopedStats.scope("putTransactionData").counter("success")
    val FailuresCounter = scopedStats.scope("putTransactionData").counter("failures")
    val FailuresScope = scopedStats.scope("putTransactionData").scope("failures")
  }
  addFunction("putTransactionData", { (iprot: TProtocol, seqid: Int) =>
    try {
      __stats_putTransactionData.RequestsCounter.incr()
      val args = PutTransactionData.Args.decode(iprot)
      iprot.readMessageEnd()
      (try {
        iface.putTransactionData(args.token, args.stream, args.partition, args.transaction, args.data, args.from)
      } catch {
        case e: Exception => Future.exception(e)
      }).flatMap { value: Boolean =>
        reply("putTransactionData", seqid, PutTransactionData.Result(success = Some(value)))
      }.rescue {
        case e => Future.exception(e)
      }.respond {
        case Return(_) =>
          __stats_putTransactionData.SuccessCounter.incr()
        case Throw(ex) =>
          __stats_putTransactionData.FailuresCounter.incr()
          __stats_putTransactionData.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
      }
    } catch {
      case e: TProtocolException => {
        iprot.readMessageEnd()
        exception("putTransactionData", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
      }
      case e: Exception => Future.exception(e)
    }
  })
  private[this] object __stats_getTransactionData {
    val RequestsCounter = scopedStats.scope("getTransactionData").counter("requests")
    val SuccessCounter = scopedStats.scope("getTransactionData").counter("success")
    val FailuresCounter = scopedStats.scope("getTransactionData").counter("failures")
    val FailuresScope = scopedStats.scope("getTransactionData").scope("failures")
  }
  addFunction("getTransactionData", { (iprot: TProtocol, seqid: Int) =>
    try {
      __stats_getTransactionData.RequestsCounter.incr()
      val args = GetTransactionData.Args.decode(iprot)
      iprot.readMessageEnd()
      (try {
        iface.getTransactionData(args.token, args.stream, args.partition, args.transaction, args.from, args.to)
      } catch {
        case e: Exception => Future.exception(e)
      }).flatMap { value: Seq[ByteBuffer] =>
        reply("getTransactionData", seqid, GetTransactionData.Result(success = Some(value)))
      }.rescue {
        case e => Future.exception(e)
      }.respond {
        case Return(_) =>
          __stats_getTransactionData.SuccessCounter.incr()
        case Throw(ex) =>
          __stats_getTransactionData.FailuresCounter.incr()
          __stats_getTransactionData.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
      }
    } catch {
      case e: TProtocolException => {
        iprot.readMessageEnd()
        exception("getTransactionData", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
      }
      case e: Exception => Future.exception(e)
    }
  })
  private[this] object __stats_setConsumerState {
    val RequestsCounter = scopedStats.scope("setConsumerState").counter("requests")
    val SuccessCounter = scopedStats.scope("setConsumerState").counter("success")
    val FailuresCounter = scopedStats.scope("setConsumerState").counter("failures")
    val FailuresScope = scopedStats.scope("setConsumerState").scope("failures")
  }
  addFunction("setConsumerState", { (iprot: TProtocol, seqid: Int) =>
    try {
      __stats_setConsumerState.RequestsCounter.incr()
      val args = SetConsumerState.Args.decode(iprot)
      iprot.readMessageEnd()
      (try {
        iface.setConsumerState(args.token, args.name, args.stream, args.partition, args.transaction)
      } catch {
        case e: Exception => Future.exception(e)
      }).flatMap { value: Boolean =>
        reply("setConsumerState", seqid, SetConsumerState.Result(success = Some(value)))
      }.rescue {
        case e => Future.exception(e)
      }.respond {
        case Return(_) =>
          __stats_setConsumerState.SuccessCounter.incr()
        case Throw(ex) =>
          __stats_setConsumerState.FailuresCounter.incr()
          __stats_setConsumerState.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
      }
    } catch {
      case e: TProtocolException => {
        iprot.readMessageEnd()
        exception("setConsumerState", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
      }
      case e: Exception => Future.exception(e)
    }
  })
  private[this] object __stats_getConsumerState {
    val RequestsCounter = scopedStats.scope("getConsumerState").counter("requests")
    val SuccessCounter = scopedStats.scope("getConsumerState").counter("success")
    val FailuresCounter = scopedStats.scope("getConsumerState").counter("failures")
    val FailuresScope = scopedStats.scope("getConsumerState").scope("failures")
  }
  addFunction("getConsumerState", { (iprot: TProtocol, seqid: Int) =>
    try {
      __stats_getConsumerState.RequestsCounter.incr()
      val args = GetConsumerState.Args.decode(iprot)
      iprot.readMessageEnd()
      (try {
        iface.getConsumerState(args.token, args.name, args.stream, args.partition)
      } catch {
        case e: Exception => Future.exception(e)
      }).flatMap { value: Long =>
        reply("getConsumerState", seqid, GetConsumerState.Result(success = Some(value)))
      }.rescue {
        case e => Future.exception(e)
      }.respond {
        case Return(_) =>
          __stats_getConsumerState.SuccessCounter.incr()
        case Throw(ex) =>
          __stats_getConsumerState.FailuresCounter.incr()
          __stats_getConsumerState.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
      }
    } catch {
      case e: TProtocolException => {
        iprot.readMessageEnd()
        exception("getConsumerState", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
      }
      case e: Exception => Future.exception(e)
    }
  })
  private[this] object __stats_authenticate {
    val RequestsCounter = scopedStats.scope("authenticate").counter("requests")
    val SuccessCounter = scopedStats.scope("authenticate").counter("success")
    val FailuresCounter = scopedStats.scope("authenticate").counter("failures")
    val FailuresScope = scopedStats.scope("authenticate").scope("failures")
  }
  addFunction("authenticate", { (iprot: TProtocol, seqid: Int) =>
    try {
      __stats_authenticate.RequestsCounter.incr()
      val args = Authenticate.Args.decode(iprot)
      iprot.readMessageEnd()
      (try {
        iface.authenticate(args.login, args.password)
      } catch {
        case e: Exception => Future.exception(e)
      }).flatMap { value: Int =>
        reply("authenticate", seqid, Authenticate.Result(success = Some(value)))
      }.rescue {
        case e => Future.exception(e)
      }.respond {
        case Return(_) =>
          __stats_authenticate.SuccessCounter.incr()
        case Throw(ex) =>
          __stats_authenticate.FailuresCounter.incr()
          __stats_authenticate.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
      }
    } catch {
      case e: TProtocolException => {
        iprot.readMessageEnd()
        exception("authenticate", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
      }
      case e: Exception => Future.exception(e)
    }
  })
  private[this] object __stats_isValid {
    val RequestsCounter = scopedStats.scope("isValid").counter("requests")
    val SuccessCounter = scopedStats.scope("isValid").counter("success")
    val FailuresCounter = scopedStats.scope("isValid").counter("failures")
    val FailuresScope = scopedStats.scope("isValid").scope("failures")
  }
  addFunction("isValid", { (iprot: TProtocol, seqid: Int) =>
    try {
      __stats_isValid.RequestsCounter.incr()
      val args = IsValid.Args.decode(iprot)
      iprot.readMessageEnd()
      (try {
        iface.isValid(args.token)
      } catch {
        case e: Exception => Future.exception(e)
      }).flatMap { value: Boolean =>
        reply("isValid", seqid, IsValid.Result(success = Some(value)))
      }.rescue {
        case e => Future.exception(e)
      }.respond {
        case Return(_) =>
          __stats_isValid.SuccessCounter.incr()
        case Throw(ex) =>
          __stats_isValid.FailuresCounter.incr()
          __stats_isValid.FailuresScope.counter(Throwables.mkString(ex): _*).incr()
      }
    } catch {
      case e: TProtocolException => {
        iprot.readMessageEnd()
        exception("isValid", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
      }
      case e: Exception => Future.exception(e)
    }
  })
}