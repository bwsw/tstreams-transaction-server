package com.bwsw.tstreamstransactionserver.netty.server.handler

import com.bwsw.tstreamstransactionserver.netty.Descriptors
import com.bwsw.tstreamstransactionserver.netty.server.TransactionServer
import com.bwsw.tstreamstransactionserver.rpc.{ServerException, TransactionService}

class GetTransactionDataHandler(server: TransactionServer)
  extends RequestHandler{

  private val descriptor = Descriptors.GetTransactionData

  override def handleAndSendResponse(requestBody: Array[Byte]): Array[Byte] = {
    val args = descriptor.decodeRequest(requestBody)
    val result = server.getTransactionData(
      args.streamID,
      args.partition,
      args.transaction,
      args.from,
      args.to
    )
    //    logSuccessfulProcession(Descriptors.GetStream.name)
    descriptor.encodeResponse(
      TransactionService.GetTransactionData.Result(Some(result))
    )
  }

  override def handle(requestBody: Array[Byte]): Unit = {
    //    throw new UnsupportedOperationException(
    //      "It doesn't make any sense to get transaction data according to fire and forget policy"
    //    )
  }

  override def createErrorResponse(message: String): Array[Byte] = {
    descriptor.encodeResponse(
      TransactionService.GetTransactionData.Result(
        None,
        Some(ServerException(message)
        )
      )
    )
  }
}
