package com.bwsw.tstreamstransactionserver.netty.server.bookkeeperService

import java.io.IOException
import java.net.ServerSocket

object Utils {
  final val noLeadgerId: Int = -1

  def bytesToLongsArray(bytes: Array[Byte]): Array[Long] = {
    val buffer = java.nio.ByteBuffer
      .allocate(bytes.length)
      .put(bytes)
    buffer.flip()

    val size =  bytes.length / java.lang.Long.BYTES
    val longs = Array.fill[Long](size)(buffer.getLong)

    longs
  }

  def bytesToIntsArray(bytes: Array[Byte]): Array[Int] = {
    val buffer = java.nio.ByteBuffer
      .allocate(bytes.length)
      .put(bytes)
    buffer.flip()

    val size = bytes.length / java.lang.Integer.BYTES
    val ints = Array.fill[Int](size)(buffer.getInt)

    ints
  }

  def longArrayToBytes(longs: Array[Long]): Array[Byte] = {
    val size = longs.length * java.lang.Long.BYTES
    val buffer = java.nio.ByteBuffer.allocate(size)

    longs.foreach(longValue =>
      buffer.putLong(longValue)
    )

    buffer.flip()
    val bytes = new Array[Byte](size)
    buffer.get(bytes)

    bytes
  }

  def getRandomPort: Int = {
    scala.util.Try {
      val server = new ServerSocket(0)
      val port = server.getLocalPort
      server.close()
      port
    } match {
      case scala.util.Success(port) =>
        port
      case scala.util.Failure(throwable) =>
        throw throwable
    }
  }
}