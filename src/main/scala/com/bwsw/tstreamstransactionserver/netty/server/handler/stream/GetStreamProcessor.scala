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
package com.bwsw.tstreamstransactionserver.netty.server.handler.stream

import com.bwsw.tstreamstransactionserver.netty.{Message, Protocol}
import com.bwsw.tstreamstransactionserver.netty.server.TransactionServer
import com.bwsw.tstreamstransactionserver.netty.server.handler.SomeNameRequestProcessor
import com.bwsw.tstreamstransactionserver.rpc.{ServerException, TransactionService}
import GetStreamProcessor.descriptor
import com.bwsw.tstreamstransactionserver.netty.server.authService.AuthService
import com.bwsw.tstreamstransactionserver.netty.server.transportService.TransportService
import io.netty.channel.ChannelHandlerContext

import scala.concurrent.{ExecutionContext, Future}

private object GetStreamProcessor {
  val descriptor = Protocol.GetStream
}

class GetStreamProcessor(server: TransactionServer,
                         context: ExecutionContext,
                         authService: AuthService,
                         transportService: TransportService)
  extends SomeNameRequestProcessor(
    authService,
    transportService) {

  override val name: String = descriptor.name

  override val id: Byte = descriptor.methodID

  private def process(requestBody: Array[Byte]) = {
    val args = descriptor.decodeRequest(requestBody)
    server.getStream(args.name)
  }

  override protected def handle(message: Message,
                                ctx: ChannelHandlerContext): Unit = {
//    throw new UnsupportedOperationException(
//      "It doesn't make any sense to get stream according to fire and forget policy"
//    )
  }

  override protected def handleAndGetResponse(message: Message,
                                              ctx: ChannelHandlerContext): Unit = {
    val exceptionOpt = validate(message, ctx)
    if (exceptionOpt.isEmpty) {
      Future {
        val response = descriptor.encodeResponse(
          TransactionService.GetStream.Result(
            process(message.body)
          )
        )
        val responseMessage = message.copy(
          bodyLength = response.length,
          body = response
        )
        sendResponseToClient(responseMessage, ctx)
      }(context)
        .recover { case error =>
          logUnsuccessfulProcessing(name, error, message, ctx)
          val response = createErrorResponse(error.getMessage)
          val responseMessage = message.copy(
            bodyLength = response.length,
            body = response
          )
          sendResponseToClient(responseMessage, ctx)
        }(context)
    } else {
      val error = exceptionOpt.get
      logUnsuccessfulProcessing(name, error, message, ctx)
      val response = createErrorResponse(error.getMessage)
      val responseMessage = message.copy(
        bodyLength = response.length,
        body = response
      )
      sendResponseToClient(responseMessage, ctx)
    }

  }

  override def createErrorResponse(message: String): Array[Byte] = {
    descriptor.encodeResponse(
      TransactionService.GetStream.Result(
        None,
        Some(ServerException(message)
        )
      )
    )
  }
}
