package ut.multiNodeServer.ZkTreeListTest

import java.util.concurrent.atomic.AtomicLong

import com.bwsw.tstreamstransactionserver.netty.server.multiNode.bookkeperService.{LedgerHandle, StorageManager}

class StorageManagerInMemory
  extends StorageManager{

  private val ledgerIDGen = new AtomicLong(0L)

  private val storage =
    new java.util.concurrent.ConcurrentHashMap[Long, LedgerHandle]()

  override def createLedger(): LedgerHandle = {
    val id = ledgerIDGen.getAndIncrement()

    val previousLedger = storage.computeIfAbsent(id, {id =>
      new LedgerHandleInMemory(id)
    })

    if (previousLedger != null)
      previousLedger
    else
      throw new IllegalArgumentException()
  }

  override def openLedger(id: Long): Option[LedgerHandle] = {
    Option(storage.get(id))
  }

  override def deleteLedger(id: Long): Boolean = {
    Option(storage.remove(id)).isDefined
  }

  override def isClosed(id: Long): Boolean = true
}
