package com.bwsw.tstreamstransactionserver.netty.server.handler.metadata

import com.bwsw.tstreamstransactionserver.netty.Descriptors
import com.bwsw.tstreamstransactionserver.netty.server.TransactionServer
import com.bwsw.tstreamstransactionserver.netty.server.commitLogService.{CommitLogToBerkeleyWriter, ScheduledCommitLog}
import com.bwsw.tstreamstransactionserver.netty.server.handler.RequestHandler
import com.bwsw.tstreamstransactionserver.rpc._

class PutSimpleTransactionAndDataHandler(server: TransactionServer,
                                         scheduledCommitLog: ScheduledCommitLog)
  extends RequestHandler {

  private val descriptor = Descriptors.PutSimpleTransactionAndData

  private def process(requestBody: Array[Byte]) = {
    val txn = descriptor.decodeRequest(requestBody)
    server.putTransactionData(
      txn.streamID,
      txn.partition,
      txn.transaction,
      txn.data,
      0
    )

    val transactions = collection.immutable.Seq(
      Transaction(Some(
        ProducerTransaction(
          txn.streamID,
          txn.partition,
          txn.transaction,
          TransactionStates.Opened,
          txn.data.size, 3L
        )), None
      ),
      Transaction(Some(
        ProducerTransaction(
          txn.streamID,
          txn.partition,
          txn.transaction,
          TransactionStates.Checkpointed,
          txn.data.size,
          120L)), None
      )
    )
    val messageForPutTransactions = Descriptors.PutTransactions.encodeRequest(
      TransactionService.PutTransactions.Args(transactions)
    )
    scheduledCommitLog.putData(
      CommitLogToBerkeleyWriter.putTransactionsType,
      messageForPutTransactions
    )
  }

  override def handleAndSendResponse(requestBody: Array[Byte]): Array[Byte] = {
    val isPutted = process(requestBody)
//    logSuccessfulProcession(Descriptors.PutSimpleTransactionAndData.name)
    Descriptors.PutSimpleTransactionAndData.encodeResponse(
      TransactionService.PutSimpleTransactionAndData.Result(
        Some(isPutted)
      )
    )
  }

  override def handle(requestBody: Array[Byte]): Unit = {
    process(requestBody)
  }

  override def createErrorResponse(message: String): Array[Byte] = {
    descriptor.encodeResponse(
      TransactionService.PutSimpleTransactionAndData.Result(
        None,
        Some(ServerException(message)
        )
      )
    )
  }

  override def getName: String = descriptor.name
}
