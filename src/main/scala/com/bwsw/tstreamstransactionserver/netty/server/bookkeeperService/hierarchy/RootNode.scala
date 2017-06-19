package com.bwsw.tstreamstransactionserver.netty.server.bookkeeperService.hierarchy

import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.data.Stat

class RootNode(client: CuratorFramework,
               rootPath: String)
{

  private def init(): RootNodeData = {
    val metadataRetrieved: Stat = new Stat()
    scala.util.Try {
      client.getData
        .storingStatIn(metadataRetrieved)
        .forPath(rootPath)
    }.map { data =>
      if (data.isEmpty)
        RootNodeData(
          metadataRetrieved,
          Array.emptyByteArray,
          Array.emptyByteArray
        )
      else {
        val (delimiter, ids) =
          data.splitAt(RootNode.delimiterIndexFieldSize)

        val index =
          java.nio.ByteBuffer.wrap(delimiter).getInt

        val (firstID, secondID) =
          ids.splitAt(index)

        RootNodeData(
          metadataRetrieved,
          firstID,
          secondID
        )
      }
    } match {
      case scala.util.Success(nodeData) =>
        nodeData
      case scala.util.Failure(throwable) =>
        throwable match {
          case _: KeeperException.NoNodeException =>
            client.create()
              .forPath(rootPath, Array.emptyByteArray)

            val metadataRetrieved: Stat =
              client.checkExists()
                .forPath(rootPath)

            RootNodeData(
              metadataRetrieved,
              Array.emptyByteArray,
              Array.emptyByteArray
            )
        }
    }
  }

  private var nodeData = init()

  final def getData: RootNodeData = nodeData
  final def setFirstAndLastIDInRootNode(first: Array[Byte],
                                        second: Array[Byte]): Unit = {
    val buf = java.nio.ByteBuffer
      .allocate(RootNode.delimiterIndexFieldSize)
      .putInt(first.length)
    buf.flip()


    val binaryIndex = new Array[Byte](RootNode.delimiterIndexFieldSize)
    buf.get(binaryIndex)

    val data = first ++ binaryIndex ++ second

    client.setData()
      .withVersion(nodeData.metadata.getVersion)
      .forPath(rootPath, data)

    val retrievedMetadata = client.checkExists()
      .forPath(rootPath)

    nodeData = RootNodeData(
      retrievedMetadata,
      first,
      second
    )
  }

}

private object RootNode {
  val delimiterIndexFieldSize = java.lang.Integer.BYTES
}
