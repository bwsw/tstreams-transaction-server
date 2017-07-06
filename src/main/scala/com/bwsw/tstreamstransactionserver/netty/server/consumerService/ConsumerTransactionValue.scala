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
package com.bwsw.tstreamstransactionserver.netty.server.consumerService

case class ConsumerTransactionValue(transactionId: Long,
                                    timestamp:Long) {
  def toByteArray: Array[Byte] = {
    val buffer = java.nio.ByteBuffer
      .allocate(ConsumerTransactionValue.size)
      .putLong(transactionId)
      .putLong(timestamp)
    buffer.flip()

    val bytes = new Array[Byte](ConsumerTransactionValue.size)
    buffer.get(bytes)
    bytes
  }
}

object ConsumerTransactionValue {
  private val size = java.lang.Long.BYTES + java.lang.Long.BYTES

  def fromByteArray(bytes: Array[Byte]): ConsumerTransactionValue = {
    val buffer = java.nio.ByteBuffer.wrap(bytes)
    val transactionId = buffer.getLong
    val timestamp = buffer.getLong
    ConsumerTransactionValue(transactionId, timestamp)
  }
}
