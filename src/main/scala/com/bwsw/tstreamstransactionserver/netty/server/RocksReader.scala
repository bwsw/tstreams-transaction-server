package com.bwsw.tstreamstransactionserver.netty.server

import java.nio.ByteBuffer

import com.bwsw.tstreamstransactionserver.netty.server.consumerService.{ConsumerServiceWriter, ConsumerServiceRead}
import com.bwsw.tstreamstransactionserver.netty.server.multiNode.bookkeperService.metadata.LedgerIDAndItsLastRecordID
import com.bwsw.tstreamstransactionserver.netty.server.storage.MultiAndSingleNodeRockStorage
import com.bwsw.tstreamstransactionserver.netty.server.transactionDataService.TransactionDataService
import com.bwsw.tstreamstransactionserver.netty.server.transactionMetadataService._
import com.bwsw.tstreamstransactionserver.netty.server.transactionMetadataService.stateHandler.{LastTransaction, LastTransactionReader}
import com.bwsw.tstreamstransactionserver.rpc._

import scala.collection.Set

class RocksReader(rocksStorage: MultiAndSingleNodeRockStorage,
                  transactionDataService: TransactionDataService) {

  private val consumerServiceImpl =
    new ConsumerServiceRead(
      rocksStorage.getRocksStorage
    )

  private val lastTransactionReader =
    new LastTransactionReader(
      rocksStorage.getRocksStorage
    )

  private val oneNodeCommitLogServiceImpl =
    new singleNode.commitLogService.CommitLogService(
      rocksStorage.getRocksStorage
    )

  private val multiNodeCommitLogServiceImpl =
    new multiNode.commitLogService.CommitLogService(
      rocksStorage.getRocksStorage
    )

  private val transactionIDService =
    com.bwsw.tstreamstransactionserver.netty.server.transactionIDService.TransactionIdService

  private val transactionMetaServiceReaderImpl =
    new TransactionMetaServiceReader(
      rocksStorage.getRocksStorage
    )

  final def getLastProcessedCommitLogFileID: Long =
    oneNodeCommitLogServiceImpl.getLastProcessedCommitLogFileID.getOrElse(-1L)

  final def getLastProcessedLedgersAndRecordIDs: Option[Array[LedgerIDAndItsLastRecordID]] =
    multiNodeCommitLogServiceImpl.getLastProcessedLedgerAndRecordIDs

  final def getTransactionID: Long =
    transactionIDService.getTransaction()

  final def getTransactionIDByTimestamp(timestamp: Long): Long =
    transactionIDService.getTransaction(timestamp)

  final def getTransaction(streamID: Int,
                           partition: Int,
                           transaction: Long): TransactionInfo =
    transactionMetaServiceReaderImpl.getTransaction(
      streamID,
      partition,
      transaction
    )

  final def getLastCheckpointedTransaction(streamID: Int,
                                           partition: Int): Option[Long] =
    lastTransactionReader.getLastTransaction(streamID, partition)
      .flatMap(_.checkpointed.map(txn => txn.id)).orElse(Some(-1L))

  final def getLastTransactionIDAndCheckpointedID(streamID: Int,
                                                  partition: Int): Option[LastTransaction] =
    lastTransactionReader.getLastTransaction(
      streamID,
      partition
    )

  final def scanTransactions(streamID: Int,
                             partition: Int,
                             from: Long,
                             to: Long,
                             count: Int,
                             states: Set[TransactionStates]): ScanTransactionsInfo =
    transactionMetaServiceReaderImpl.scanTransactions(
      streamID,
      partition,
      from,
      to,
      count,
      states
    )

  final def getTransactionData(streamID: Int,
                               partition: Int,
                               transaction: Long,
                               from: Int,
                               to: Int): Seq[ByteBuffer] = {
    transactionDataService.getTransactionData(
      streamID,
      partition,
      transaction,
      from,
      to
    )
  }

  final def getConsumerState(name: String,
                             streamID: Int,
                             partition: Int): Long = {
    consumerServiceImpl.getConsumerState(
      name,
      streamID,
      partition
    )
  }
}