package com.bwsw.tstreamstransactionserver.netty.server.bookkeeperService.hierarchy


import org.apache.curator.framework.CuratorFramework

import scala.annotation.tailrec

class ZookeeperTreeList(client: CuratorFramework,
                        rootPath: String)
  extends EntityToPathConverter[Long]
{

  private val rootNode = new RootNode(client, rootPath)

  def firstRecordID: Option[Long] = {
    val binaryID = rootNode.getData.firstID
    if (binaryID.isEmpty)
      None
    else
      Some(bytesToEntityID(binaryID))
  }

  def lastRecordID: Option[Long] = {
    val binaryID = rootNode.getData.lastID
    if (binaryID.isEmpty)
      None
    else
      Some(bytesToEntityID(binaryID))
  }

//  def updateNode(entity: Long, data: Array[Byte]): Unit = {
//    client.setData()
//      .forPath(
//        s"$rootPath/${entityToPath(entity)}",
//        data
//      )
//  }

  private def traverseToLastNode: Option[Long] = {
    @tailrec
    def go(node: Option[Long]): Option[Long] = {
      val nodeID = node.flatMap(id =>
        getNodeNext(id).filter(_ != id)
      )

      if (nodeID.isDefined)
        go(nodeID)
      else
        node
    }
    go(lastRecordID)
  }

  def createNode(entity: Long): Unit = {
    val lastID = entityIDtoBytes(entity)
    val path = s"$rootPath/${entityToPath(entity)}"

    if (rootNode.getData.firstID.isEmpty) {
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
    else if (bytesToEntityID(rootNode.getData.lastID) != entity) {
      client.create
        .creatingParentsIfNeeded()
        .forPath(
          path,
          Array.emptyByteArray
        )

      traverseToLastNode.foreach { id =>
        val pathPreviousNode = s"$rootPath/${entityToPath(id)}"
        client.setData()
          .forPath(
            pathPreviousNode,
            lastID
          )
      }

      rootNode.setFirstAndLastIDInRootNode(
        rootNode.getData.firstID, lastID
      )
    }
  }


  def getNodeNext(entity: Long): Option[Long] = {
    val path = s"$rootPath/${entityToPath(entity)}"
    val data = client.getData
      .forPath(path)

    if (data.isEmpty)
      None
    else
      Some(bytesToEntityID(data))
  }

  override def entityToPath(entity: Long): String = {
    def splitLongToHexes: Array[String] = {
      val size = java.lang.Long.BYTES
      val buffer = java.nio.ByteBuffer.allocate(
        size
      )
      buffer.putLong(entity)
      buffer.flip()

      val bytes = Array.fill(4)(
        f"${buffer.getShort & 0xffff}%x"
      )
      bytes
    }
    splitLongToHexes.mkString("/")
  }

  override def entityIDtoBytes(entity: Long): Array[Byte] = {
    val size = java.lang.Long.BYTES
    val buffer = java.nio.ByteBuffer.allocate(size)

    buffer.putLong(entity)
    buffer.flip()

    val bytes = new Array[Byte](size)
    buffer.get(bytes)
    bytes
  }

  override def bytesToEntityID(bytes: Array[Byte]): Long = {
    val buffer = java.nio.ByteBuffer.wrap(bytes)
    buffer.getLong()
  }

}