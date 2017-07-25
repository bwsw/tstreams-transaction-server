package com.bwsw.tstreamstransactionserver.netty.server

import java.nio.ByteBuffer

import com.bwsw.tstreamstransactionserver.exception.Throwable.StreamDoesNotExist
import com.bwsw.tstreamstransactionserver.netty.server.consumerService.{ConsumerServiceImpl, ConsumerTransactionRecord}
import com.bwsw.tstreamstransactionserver.netty.server.db.KeyValueDatabaseBatch
import com.bwsw.tstreamstransactionserver.netty.server.storage.AllInOneRockStorage
import com.bwsw.tstreamstransactionserver.netty.server.transactionDataService.TransactionDataServiceImpl
import com.bwsw.tstreamstransactionserver.netty.server.transactionMetadataService.{ProducerStateMachine, ProducerTransactionRecord, TransactionMetaServiceImpl}
import com.bwsw.tstreamstransactionserver.rpc.{ConsumerTransaction, ProducerTransaction}

import scala.collection.mutable.ListBuffer

class RocksWriter(rocksStorage: AllInOneRockStorage,
                  transactionDataService: TransactionDataServiceImpl) {

  private val consumerServiceImpl = new ConsumerServiceImpl(
    rocksStorage.getRocksStorage
  )

  private val producerStateMachine =
    new ProducerStateMachine(rocksStorage.getRocksStorage)

  private val transactionMetaServiceImpl = new TransactionMetaServiceImpl(
    rocksStorage.getRocksStorage,
    producerStateMachine
  )

  final def notifyProducerTransactionCompleted(onNotificationCompleted: ProducerTransaction => Boolean, func: => Unit): Long =
    transactionMetaServiceImpl.notifier.notifyProducerTransactionCompleted(onNotificationCompleted, func)

  final def removeProducerTransactionNotification(id: Long): Boolean =
    transactionMetaServiceImpl.notifier.removeProducerTransactionNotification(id)

  final def notifyConsumerTransactionCompleted(onNotificationCompleted: ConsumerTransaction => Boolean, func: => Unit): Long =
    consumerServiceImpl.notifyConsumerTransactionCompleted(onNotificationCompleted, func)

  final def removeConsumerTransactionNotification(id: Long): Boolean =
    consumerServiceImpl.removeConsumerTransactionNotification(id)

  @throws[StreamDoesNotExist]
  final def putTransactionData(streamID: Int, partition: Int, transaction: Long, data: Seq[ByteBuffer], from: Int): Boolean =
    transactionDataService.putTransactionData(streamID, partition, transaction, data, from)

  final def putTransactions(transactions: Seq[ProducerTransactionRecord],
                            batch: KeyValueDatabaseBatch): ListBuffer[Unit => Unit] = {
    transactionMetaServiceImpl.putTransactions(
      transactions,
      batch
    )
  }

  final def putConsumersCheckpoints(consumerTransactions: Seq[ConsumerTransactionRecord],
                                    batch: KeyValueDatabaseBatch): ListBuffer[(Unit) => Unit] = {
    consumerServiceImpl.putConsumersCheckpoints(consumerTransactions, batch)
  }

  final def getConsumerState(name: String, streamID: Int, partition: Int): Long = {
    consumerServiceImpl.getConsumerState(name, streamID, partition)
  }

  final def getNewBatch: KeyValueDatabaseBatch =
    rocksStorage.newBatch

  final def createAndExecuteTransactionsToDeleteTask(timestamp: Long): Unit =
    transactionMetaServiceImpl.createAndExecuteTransactionsToDeleteTask(timestamp)

  final def clearProducerTransactionCache(): Unit =
    producerStateMachine.clear()
}
