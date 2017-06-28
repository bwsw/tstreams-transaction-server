package com.bwsw.tstreamstransactionserver.netty.server.bookkeeperService

import java.util.concurrent.{BlockingQueue, TimeUnit}

import org.apache.bookkeeper.client.{BookKeeper, LedgerHandle}
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException
import Utils._

import scala.annotation.tailrec

class Slave(client: CuratorFramework,
            bookKeeper: BookKeeper,
            slave: Electable,
            ledgerLogPath: String,
            password: Array[Byte],
            timeBetweenCreationOfLedgers: Int,
            closedLedgers: BlockingQueue[LedgerHandle]

           )
{

  def follow(skipPast: LedgerID): Unit = {
    val ledgers =
      retrieveLedgersUntilNodeDoesntExist(skipPast)

      retrieveUpcomingLedgers(ledgers,  skipPast)
  }


  @tailrec
  private final def retrieveLedgersUntilNodeDoesntExist(lastLedgerAndItsLastRecordSeen: LedgerID): Array[Long] =
  {
    scala.util.Try {
      val ledgerIDsBinary = client.getData
        .forPath(ledgerLogPath)

      val ledgers = bytesToLongsArray(ledgerIDsBinary)

      processNewLedgersThatHaventSeenBefore(ledgers, lastLedgerAndItsLastRecordSeen)
    } match {
      case scala.util.Success(ledgers) =>
        ledgers
      case scala.util.Failure(throwable) => throwable match {
        case _: KeeperException.NoNodeException =>
          TimeUnit.MILLISECONDS.sleep(timeBetweenCreationOfLedgers)
          retrieveLedgersUntilNodeDoesntExist(lastLedgerAndItsLastRecordSeen)
        case _ =>
          throw throwable
      }
    }
  }

  private def processNewLedgersThatHaventSeenBefore(ledgers: Array[Long],
                                                    skipPast: LedgerID) = {
    if (skipPast.ledgerId != noLeadgerId) {
      val index = ledgers.indexWhere(id => id >= skipPast.ledgerId)
      ledgers.slice(index, ledgers.length)
    }
    else
      ledgers
  }

  @tailrec
  private final def monitorLedgerUntilItIsCompleted(ledger: Long,
                                                    lastLedgerAndItsLastRecordSeen: LedgerID,
                                                    isLedgerCompleted: Boolean
                                                   ): LedgerID = {
    if (isLedgerCompleted || slave.hasLeadership) {
      lastLedgerAndItsLastRecordSeen
    } else {
      val ledgerHandle = bookKeeper.openLedgerNoRecovery(
        ledger,
        BookKeeper.DigestType.MAC,
        password
      )


      val isLedgerCompleted = ledgerHandle.isClosed

      val lastProcessedLedger =
        if (isLedgerCompleted && (lastLedgerAndItsLastRecordSeen.ledgerId < ledgerHandle.getId)) {
          closedLedgers.add(ledgerHandle)
          LedgerID(ledgerHandle.getId)
        }
        else {
          lastLedgerAndItsLastRecordSeen
        }

      TimeUnit.MILLISECONDS.sleep(timeBetweenCreationOfLedgers)
      monitorLedgerUntilItIsCompleted(
        ledger,
        lastProcessedLedger,
        isLedgerCompleted
      )
    }
  }



  private final def readUntilWeAreSlave(ledgers: Array[Long],
                                        lastLedgerAndItsLastRecordSeen: LedgerID
                                       ): LedgerID = {
    ledgers.foldRight(lastLedgerAndItsLastRecordSeen)((ledger, lastLedgerAndItsLastRecordSeen) =>
      monitorLedgerUntilItIsCompleted(ledger,
        lastLedgerAndItsLastRecordSeen,
        isLedgerCompleted = false
      )
    )
  }

  @tailrec
  private final def retrieveUpcomingLedgers(ledgers: Array[Long], lastReadEntry: LedgerID): Unit = {
    if (!slave.hasLeadership) {
      val lastLedgerSeen =
        readUntilWeAreSlave(ledgers, lastReadEntry)

      val ledgersIDsBinary = client.getData
        .forPath(ledgerLogPath)

      val newLedgers = bytesToLongsArray(ledgersIDsBinary)

      val upcomingLedgers = {
        val index = newLedgers.indexWhere(id =>
          id > lastLedgerSeen.ledgerId)
        newLedgers.slice(index, newLedgers.length)
      }

      retrieveUpcomingLedgers(
        upcomingLedgers,
        lastLedgerSeen
      )
    }
  }
}