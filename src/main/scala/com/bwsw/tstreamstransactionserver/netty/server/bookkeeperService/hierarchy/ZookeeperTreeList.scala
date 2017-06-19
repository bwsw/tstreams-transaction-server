package com.bwsw.tstreamstransactionserver.netty.server.bookkeeperService.hierarchy


import org.apache.curator.framework.CuratorFramework

abstract class ZookeeperTreeList[T](client: CuratorFramework,
                                    rootPath: String)
  extends EntityToPathConverter[T]
{

  private val rootNode = new RootNode(client, rootPath)

  def firstRecordID: Option[T] = {
    val binaryID = rootNode.getData.firstID
    if (binaryID.isEmpty)
      None
    else
      Some(bytesToEntityID(binaryID))
  }

  def lastRecordID: Option[T] = {
    val binaryID = rootNode.getData.lastID
    if (binaryID.isEmpty)
      None
    else
      Some(bytesToEntityID(binaryID))
  }


  def createNode(entity: T, data: Array[Byte] = Array.emptyByteArray) = {
    val lastID = entityIDtoBytes(entity)

    client.create
      .creatingParentsIfNeeded()
      .forPath(
        s"$rootPath/${entityToPath(entity)}",
        lastID
      )

    if (rootNode.getData.firstID.isEmpty) {
      rootNode.setFirstAndLastIDInRootNode(
        lastID, lastID
      )
    }
    else {
      rootNode.setFirstAndLastIDInRootNode(
        rootNode.getData.firstID, lastID
      )
    }
  }

}