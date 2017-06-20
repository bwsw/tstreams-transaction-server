package com.bwsw.tstreamstransactionserver.netty.server.bookkeeperService.hierarchy

trait EntitySerializable[T]
{
  def entityIDtoBytes(entity: T): Array[Byte]
  def bytesToEntityID(bytes: Array[Byte]): T
}
