/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.bwsw.tstreamstransactionserver.netty.server.transactionMetadataService

import java.util.concurrent.TimeUnit

import com.bwsw.tstreamstransactionserver.netty.server.{BigCommit, RocksStorage}
import com.bwsw.tstreamstransactionserver.netty.server.consumerService.{ConsumerServiceImpl, ConsumerTransactionRecord}
import com.bwsw.tstreamstransactionserver.netty.server.db.{KeyValueDatabaseBatch, KeyValueDatabaseManager}
import com.bwsw.tstreamstransactionserver.netty.server.multiNode.bookkeperService.metadata.{LedgerIDAndItsLastRecordID, MetadataRecord}
import com.bwsw.tstreamstransactionserver.netty.server.streamService.StreamKey
import com.bwsw.tstreamstransactionserver.netty.server.transactionMetadataService.stateHandler.{KeyStreamPartition, LastTransactionStreamPartition, TransactionStateHandler}
import com.bwsw.tstreamstransactionserver.rpc._
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}



class TransactionMetaServiceImpl(rocksMetaServiceDB: KeyValueDatabaseManager,
                                 lastTransactionStreamPartition: LastTransactionStreamPartition,
                                 consumerService: ConsumerServiceImpl)
  extends TransactionStateHandler
    with ProducerTransactionStateNotifier
{
  import lastTransactionStreamPartition._

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val producerTransactionsDatabase =
    rocksMetaServiceDB.getDatabase(RocksStorage.TRANSACTION_ALL_STORE)
  private val producerTransactionsWithOpenedStateDatabase =
    rocksMetaServiceDB.getDatabase(RocksStorage.TRANSACTION_OPEN_STORE)

  private def fillOpenedTransactionsRAMTable: com.google.common.cache.Cache[ProducerTransactionKey, ProducerTransactionValue] = {
    if (logger.isDebugEnabled)
      logger.debug("Filling cache with Opened Transactions table.")

    val secondsToLive = 180
    val threadsToWriteNumber = 1
    val cache = com.google.common.cache.CacheBuilder.newBuilder()
      .concurrencyLevel(threadsToWriteNumber)
      .expireAfterAccess(secondsToLive, TimeUnit.SECONDS)
      .build[ProducerTransactionKey, ProducerTransactionValue]()

    val iterator = producerTransactionsWithOpenedStateDatabase.iterator
    iterator.seekToFirst()
    while (iterator.isValid) {
      cache.put(
        ProducerTransactionKey.fromByteArray(iterator.key()),
        ProducerTransactionValue.fromByteArray(iterator.value())
      )
      iterator.next()
    }
    iterator.close()

    cache
  }

  private val transactionsRamTable: com.google.common.cache.Cache[ProducerTransactionKey, ProducerTransactionValue] =
    fillOpenedTransactionsRAMTable

  def getOpenedTransaction(key: ProducerTransactionKey): Option[ProducerTransactionValue] = {
    Option(transactionsRamTable.getIfPresent(key))
      .orElse {
        val keyFound = key.toByteArray
        Option(producerTransactionsWithOpenedStateDatabase.get(keyFound)).map { data =>
          val producerTransactionValue =
            ProducerTransactionValue.fromByteArray(data)
          transactionsRamTable.put(key, producerTransactionValue)
          producerTransactionValue
        }
      }
  }

  private final def selectInOrderProducerTransactions(transactions: Seq[ProducerTransactionRecord],
                                                                       batch: KeyValueDatabaseBatch) = {
    val producerTransactions = ArrayBuffer[ProducerTransactionRecord]()
    transactions foreach { txn =>
      val key = KeyStreamPartition(txn.stream, txn.partition)
      if (txn.state != TransactionStates.Opened) {
        producerTransactions += txn
      } else if (!isThatTransactionOutOfOrder(key, txn.transactionID)) {
        // updating RAM table, and last opened transaction database.
        updateLastTransactionStreamPartitionRamTable(
          key,
          txn.transactionID,
          isOpenedTransaction = true
        )

        putLastTransaction(key,
          txn.transactionID,
          isOpenedTransaction = true,
          batch
        )

        if (logger.isDebugEnabled)
          logger.debug(
            s"On stream:${key.stream} partition:${key.partition} " +
              s"last opened transaction is ${txn.transactionID} now."
          )
        producerTransactions += txn
      }
    }
    producerTransactions
  }


  private final def groupProducerTransactionsByStreamPartitionTransactionID(producerTransactions: Seq[ProducerTransactionRecord]) =
    producerTransactions.groupBy(txn => txn.key)

  private final def updateLastCheckpointedTransactionAndPutToDatabase(key: stateHandler.KeyStreamPartition,
                                                                      producerTransactionWithNewState: ProducerTransactionRecord,
                                                                      batch: KeyValueDatabaseBatch): Unit = {
    updateLastTransactionStreamPartitionRamTable(
      key,
      producerTransactionWithNewState.transactionID,
      isOpenedTransaction = false
    )

    putLastTransaction(
      key,
      producerTransactionWithNewState.transactionID,
      isOpenedTransaction = false,
      batch
    )

    if (logger.isDebugEnabled())
      logger.debug(
        s"On stream:${key.stream} partition:${key.partition} " +
        s"last checkpointed transaction is ${producerTransactionWithNewState.transactionID} now."
      )
  }

  private def putTransactionToAllAndOpenedTables(producerTransactionRecord: ProducerTransactionRecord,
                                                 notifications: scala.collection.mutable.ListBuffer[Unit => Unit],
                                                 batch: KeyValueDatabaseBatch) =
  {
    val binaryTxn = producerTransactionRecord.producerTransaction.toByteArray
    val binaryKey = producerTransactionRecord.key.toByteArray

    if (producerTransactionRecord.state == TransactionStates.Checkpointed) {
      updateLastCheckpointedTransactionAndPutToDatabase(
        stateHandler.KeyStreamPartition(
          producerTransactionRecord.stream,
          producerTransactionRecord.partition
        ),
        producerTransactionRecord,
        batch
      )
    }

    transactionsRamTable.put(producerTransactionRecord.key, producerTransactionRecord.producerTransaction)
    if (producerTransactionRecord.state == TransactionStates.Opened) {
      batch.put(RocksStorage.TRANSACTION_OPEN_STORE, binaryKey, binaryTxn)
    }
    else {
      batch.remove(RocksStorage.TRANSACTION_OPEN_STORE, binaryKey)
    }

    if (areThereAnyProducerNotifies)
      notifications += tryCompleteProducerNotify(producerTransactionRecord)

    batch.put(RocksStorage.TRANSACTION_ALL_STORE, binaryKey, binaryTxn)
    if (logger.isDebugEnabled)
      logger.debug(s"Producer transaction on stream: ${producerTransactionRecord.stream}" +
        s"partition ${producerTransactionRecord.partition}, transactionId ${producerTransactionRecord.transactionID} " +
        s"with state ${producerTransactionRecord.state} is ready for commit[commit id: ${batch.id}]"
    )
  }


  def putTransactions(transactions: Seq[ProducerTransactionRecord],
                      batch: KeyValueDatabaseBatch): ListBuffer[Unit => Unit] = {

    val notifications = new scala.collection.mutable.ListBuffer[Unit => Unit]()

    val producerTransactions =
      selectInOrderProducerTransactions(transactions, batch)

    val groupedProducerTransactions =
      groupProducerTransactionsByStreamPartitionTransactionID(producerTransactions)

    groupedProducerTransactions foreach { case (key, txns) =>
      //retrieving an opened transaction from opened transaction database if it exist
      val openedTransactionOpt = getOpenedTransaction(key)

      openedTransactionOpt match {
        case Some(data) =>
          val persistedProducerTransactionRocks = ProducerTransactionRecord(key, data)
          if (logger.isDebugEnabled)
            logger.debug(
              s"Transiting producer transaction on stream: ${persistedProducerTransactionRocks.stream}" +
                s"partition ${persistedProducerTransactionRocks.partition}, " +
                s"transaction ${persistedProducerTransactionRocks.transactionID} " +
                s"with state ${persistedProducerTransactionRocks.state} to new state"
            )


          val producerTransaction =
            transitProducerTransactionToNewState(persistedProducerTransactionRocks, txns)

          producerTransaction.foreach { transaction =>
            putTransactionToAllAndOpenedTables(transaction, notifications, batch)
          }

        case None =>
          if (logger.isDebugEnabled)
            logger.debug(s"Trying to put new producer transaction on stream ${key.stream}.")

          val producerTransaction = transitProducerTransactionToNewState(txns)

          producerTransaction.foreach { transaction =>
            putTransactionToAllAndOpenedTables(transaction, notifications, batch)
          }
      }
    }

    notifications
  }


  private val commitLogDatabase = rocksMetaServiceDB.getDatabase(RocksStorage.COMMIT_LOG_STORE)
  private[server] final def getLastProcessedCommitLogFileID: Option[Long] = {
    val iterator = commitLogDatabase.iterator
    iterator.seekToLast()

    val id = if (iterator.isValid)
      Some(CommitLogKey.fromByteArray(iterator.key()).id)
    else
      None

    iterator.close()
    id
  }

//  private[server] final def getProcessedCommitLogFiles: ArrayBuffer[Long] = {
//    val processedCommitLogFiles = scala.collection.mutable.ArrayBuffer[Long]()
//
//    val iterator = commitLogDatabase.iterator
//    iterator.seekToFirst()
//
//    while (iterator.isValid) {
//      processedCommitLogFiles += CommitLogKey.fromByteArray(iterator.key()).id
//      iterator.next()
//    }
//    iterator.close()
//
//    processedCommitLogFiles
//  }

  private val bookkeeperLogDatabase = rocksMetaServiceDB.getDatabase(RocksStorage.BOOKKEEPER_LOG_STORE)
  private[server] final def getLastProcessedLedgerAndRecordIDs: Option[Array[LedgerIDAndItsLastRecordID]] = {
    val iterator = bookkeeperLogDatabase.iterator
    iterator.seek(BigCommit.bookkeeperKey)

    val records = if (iterator.isValid)
      Some(MetadataRecord.fromByteArray(iterator.value()).records)
    else
      None

    iterator.close()
    records
  }


  final def getTransaction(streamID: Int, partition: Int, transaction: Long): com.bwsw.tstreamstransactionserver.rpc.TransactionInfo = {
    val lastTransaction = getLastTransactionIDAndCheckpointedID(streamID, partition)
    if (lastTransaction.isEmpty || transaction > lastTransaction.get.opened.id) {
      TransactionInfo(exists = false, None)
    } else {
      val searchKey = new ProducerTransactionKey(streamID, partition, transaction).toByteArray

      Option(producerTransactionsDatabase.get(searchKey)).map(searchData =>
        new ProducerTransactionRecord(ProducerTransactionKey.fromByteArray(searchKey), ProducerTransactionValue.fromByteArray(searchData))
      ) match {
        case None =>
          TransactionInfo(exists = true, None)
        case Some(producerTransactionRecord) =>
          TransactionInfo(exists = true, Some(producerTransactionRecord))
      }
    }
  }

//  final def getLastCheckpointedTransaction(streamID: Int, partition: Int): Option[Long] = {
//    val result = getLastTransactionIDAndCheckpointedID(streamID, partition) match {
//      case Some(last) => last.checkpointed match {
//        case Some(checkpointed) => Some(checkpointed.id)
//        case None => None
//      }
//      case None => None
//    }
//    result
//  }

  private val comparator = com.bwsw.tstreamstransactionserver.`implicit`.Implicits.ByteArray
  def scanTransactions(streamID: Int, partition: Int, from: Long, to: Long, count: Int, states: collection.Set[TransactionStates]): com.bwsw.tstreamstransactionserver.rpc.ScanTransactionsInfo =
    {
      val (lastOpenedTransactionID, toTransactionID) = getLastTransactionIDAndCheckpointedID(streamID, partition) match {
        case Some(lastTransaction) => lastTransaction.opened.id match {
          case lt if lt < from => (lt, from - 1L)
          case lt if from <= lt && lt < to => (lt, lt)
          case lt if lt >= to => (lt, to)
        }
        case None => (-1L, from - 1L)
      }

      if (logger.isDebugEnabled) logger.debug(s"Trying to retrieve transactions on stream $streamID, partition: $partition in range [$from, $to]." +
        s"Actually as lt ${if (lastOpenedTransactionID == -1) "doesn't exist" else s"is $lastOpenedTransactionID"} the range is [$from, $toTransactionID].")

      if (toTransactionID < from || count == 0) ScanTransactionsInfo(lastOpenedTransactionID, Seq())
      else {
        val iterator = producerTransactionsDatabase.iterator

        val lastTransactionID = new ProducerTransactionKey(streamID, partition, toTransactionID).toByteArray
        def moveCursorToKey: Option[ProducerTransactionRecord] = {
          val keyFrom = new ProducerTransactionKey(streamID, partition, from)

          iterator.seek(keyFrom.toByteArray)
          val startKey = if (iterator.isValid && comparator.compare(iterator.key(), lastTransactionID) <= 0) {
            Some(
              new ProducerTransactionRecord(
                ProducerTransactionKey.fromByteArray(iterator.key()),
                ProducerTransactionValue.fromByteArray(iterator.value())
              )
            )
          } else None

          iterator.next()

          startKey
        }

        moveCursorToKey match {
          case None =>
            iterator.close()
            ScanTransactionsInfo(lastOpenedTransactionID, Seq())

          case Some(producerTransactionKey) =>
            val producerTransactions = ArrayBuffer[ProducerTransactionRecord](producerTransactionKey)

            var txnState: TransactionStates = producerTransactionKey.state
            while (
              iterator.isValid &&
                producerTransactions.length < count &&
                !states.contains(txnState) &&
                (comparator.compare(iterator.key(), lastTransactionID) <= 0)
            ) {
              val producerTransaction =
                ProducerTransactionRecord(
                  ProducerTransactionKey.fromByteArray(iterator.key()),
                  ProducerTransactionValue.fromByteArray(iterator.value())
                )
              txnState = producerTransaction.state
              producerTransactions += producerTransaction
              iterator.next()
            }

            iterator.close()

            val result = if (states.contains(txnState))
              producerTransactions.init
            else
              producerTransactions

            ScanTransactionsInfo(lastOpenedTransactionID, result)
        }
      }
    }


  def transactionsToDeleteTask(timestampToDeleteTransactions: Long) {
    def doesProducerTransactionExpired(producerTransactionWithoutKey: ProducerTransactionValue): Boolean = {
      scala.math.abs(
        producerTransactionWithoutKey.timestamp +
          producerTransactionWithoutKey.ttl
      ) <= timestampToDeleteTransactions
    }


    if (logger.isDebugEnabled)
      logger.debug(s"Cleaner[time: $timestampToDeleteTransactions] of expired transactions is running.")
    val batch = rocksMetaServiceDB.newBatch

    val iterator = producerTransactionsWithOpenedStateDatabase.iterator
    iterator.seekToFirst()

    val notifications = new ListBuffer[Unit => Unit]()
    while (iterator.isValid) {
      val producerTransactionValue = ProducerTransactionValue.fromByteArray(iterator.value())
      if (doesProducerTransactionExpired(producerTransactionValue)) {
        if (logger.isDebugEnabled)
          logger.debug(s"Cleaning $producerTransactionValue as it's expired.")

        val producerTransactionValueTimestampUpdated = producerTransactionValue.copy(timestamp = timestampToDeleteTransactions)
        val key = iterator.key()
        val producerTransactionKey = ProducerTransactionKey.fromByteArray(key)

        val canceledTransactionRecordDueExpiration =
          transitProducerTransactionToInvalidState(ProducerTransactionRecord(producerTransactionKey, producerTransactionValueTimestampUpdated))
        if (areThereAnyProducerNotifies)
          notifications += tryCompleteProducerNotify(ProducerTransactionRecord(producerTransactionKey, canceledTransactionRecordDueExpiration.producerTransaction))

        transactionsRamTable.invalidate(producerTransactionKey)
        batch.put(RocksStorage.TRANSACTION_ALL_STORE, key, canceledTransactionRecordDueExpiration.producerTransaction.toByteArray)

        batch.remove(RocksStorage.TRANSACTION_OPEN_STORE, key)
      }
      iterator.next()
    }
    iterator.close()
    batch.write()

    notifications.foreach(notification => notification(()))
  }

  final def createAndExecuteTransactionsToDeleteTask(timestampToDeleteTransactions: Long): Unit = {
    transactionsToDeleteTask(timestampToDeleteTransactions)
  }
}
