package com.bwsw.tstreamstransactionserver.netty.server
import java.nio.ByteBuffer

import com.bwsw.tstreamstransactionserver.netty.server.consumerService.ConsumerTransactionRecord
import com.bwsw.tstreamstransactionserver.netty.server.transactionMetadataService.{CommitLogKey, ProducerTransactionRecord}
import org.slf4j.{Logger, LoggerFactory}

class BigCommit(transactionServer: TransactionServer,
                fileID: Long)
  extends Commitable {

  private val logger: Logger =
    LoggerFactory.getLogger(this.getClass)

  private val batch =
    transactionServer.rocksStorage.newBatch

  private lazy val notifications =
    new scala.collection.mutable.ListBuffer[Unit => Unit]


  override def putProducerTransactions(producerTransactions: Seq[ProducerTransactionRecord]): Unit = {
    if (logger.isDebugEnabled) {
      logger.debug(s"[batch ${batch.id}] " +
        s"Adding producer transactions to commit.")
    }

    notifications ++=
      transactionServer.putTransactions(
        producerTransactions,
        batch
      )
  }

  override def putConsumerTransactions(consumerTransactions: Seq[ConsumerTransactionRecord]): Unit = {
    if (logger.isDebugEnabled) {
      logger.debug(s"[batch ${batch.id}] " +
        s"Adding consumer transactions to commit.")
    }

    notifications ++=
      transactionServer.putConsumersCheckpoints(
        consumerTransactions,
        batch
      )
  }

  override def putProducerData(streamID: Int,
                               partition: Int,
                               transaction: Long,
                               data: Seq[ByteBuffer],
                               from: Int): Boolean = {
    val isDataPersisted = transactionServer.putTransactionData(
      streamID,
      partition,
      transaction,
      data,
      from
    )

    if (logger.isDebugEnabled) {
      logger.debug(s"[batch ${batch.id}] " +
        s"Persisting producer transactions data on stream: $streamID, partition: $partition, transaction: $transaction, " +
        s"from: $from.")
    }

    isDataPersisted
  }

  override def commit(): Boolean = {
    val key = CommitLogKey(fileID).toByteArray
    val value = Array[Byte]()

    batch.put(RocksStorage.COMMIT_LOG_STORE, key, value)
    if (batch.write()) {
      if (logger.isDebugEnabled) logger.debug(s"commit ${batch.id} is successfully fixed.")
      notifications foreach (notification => notification(()))
      true
    } else {
      false
    }
  }
}
