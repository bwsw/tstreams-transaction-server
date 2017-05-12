package com.bwsw.tstreamstransactionserver.netty.server.db.zk


import com.bwsw.tstreamstransactionserver.exception.Throwable.StreamOverwriteProhibited
import com.bwsw.tstreamstransactionserver.netty.server.streamService.{StreamCRUD, StreamKey, StreamRecord, StreamValue}
import org.apache.curator.framework.CuratorFramework

final class StreamDatabaseZK(client: CuratorFramework, path: String) extends StreamCRUD {
  private val streamNamePath = new StreamNamePath(client, s"$path/names")
  private val streamIDPath = new streamIDPath(client, s"$path/ids")

  override def putStream(streamValue: StreamValue): StreamKey = {
    if (!streamNamePath.checkExists(streamValue.name)) {
      val streamRecord = streamIDPath.put(streamValue)
      streamNamePath.put(streamRecord)
      streamRecord.key
    } else throw new StreamOverwriteProhibited(streamValue.name)
  }

  override def checkStreamExists(name: String): Boolean = streamNamePath.checkExists(name)

  override def delStream(name: String): Boolean = streamNamePath.delete(name)

  override def getStream(name: String): Option[StreamRecord] = streamNamePath.get(name)

  override def getStream(streamKey: StreamKey): Option[StreamRecord] = streamIDPath.get(streamKey)
}
