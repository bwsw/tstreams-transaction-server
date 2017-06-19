package com.bwsw.tstreamstransactionserver.netty.server.bookkeeperService.hierarchy

trait EntityToPathConverter[T]
{
  def entityToPath(entity: T): String
  def entityIDtoBytes(entity: T): Array[Byte]
  def bytesToEntityID(bytes: Array[Byte]): T
}
