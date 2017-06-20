package com.bwsw.tstreamstransactionserver.netty.server.bookkeeperService.hierarchy


import org.apache.curator.framework.CuratorFramework

import scala.annotation.tailrec

abstract class ZookeeperTreeList[T](client: CuratorFramework,
                                    rootPath: String)
  extends EntityPathConverter[T]
    with  EntityIDSerializable[T]
{
  private val rootNode = new RootNode(client, rootPath)
  private def rootNodeData = rootNode.getData

  def firstEntryID: Option[T] = {
    val binaryID = rootNodeData.firstID
    if (binaryID.isEmpty)
      None
    else
      Some(bytesToEntityID(binaryID))
  }

  def lastEntryID: Option[T] = {
    val binaryID = rootNodeData.lastID
    if (binaryID.isEmpty)
      None
    else
      Some(bytesToEntityID(binaryID))
  }

  def createNode(entity: T): Unit = {
    val lastID = entityIDtoBytes(entity)
    val path = s"$rootPath/${buildPath(entity)}"

    if (rootNodeData.firstID.isEmpty) {
      client.create
        .creatingParentsIfNeeded()
        .forPath(
          path,
          lastID
        )
      rootNode.setFirstAndLastIDInRootNode(
        lastID, lastID
      )
    }
    else if (bytesToEntityID(rootNodeData.lastID) != entity) {
      client.create
        .creatingParentsIfNeeded()
        .forPath(
          path,
          Array.emptyByteArray
        )

      traverseToLastNode.foreach { id =>
        val pathPreviousNode = s"$rootPath/${buildPath(id)}"
        client.setData()
          .forPath(
            pathPreviousNode,
            lastID
          )
      }

      rootNode.setFirstAndLastIDInRootNode(
        rootNodeData.firstID, lastID
      )
    }
  }

  //  def updateNode(entity: Long, data: Array[Byte]): Unit = {
  //    client.setData()
  //      .forPath(
  //        s"$rootPath/${buildPath(entity)}",
  //        data
  //      )
  //  }


  private def buildPath(entity: T) = {
    entityToPath(entity).mkString("/")
  }


  private def traverseToLastNode: Option[T] = {
    @tailrec
    def go(node: Option[T]): Option[T] = {
      val nodeID = node.flatMap(id =>
        getNodeNext(id).filter(_ != id)
      )

      if (nodeID.isDefined)
        go(nodeID)
      else
        node
    }
    go(lastEntryID)
  }

  private def getNodeNext(entity: T): Option[T] = {
    val path = s"$rootPath/${buildPath(entity)}"
    val data = client.getData
      .forPath(path)

    if (data.isEmpty)
      None
    else
      Some(bytesToEntityID(data))
  }

}