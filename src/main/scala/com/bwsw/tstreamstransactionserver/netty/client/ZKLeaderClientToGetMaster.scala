package com.bwsw.tstreamstransactionserver.netty.client

import java.io.Closeable
import java.util.concurrent.TimeUnit

import com.bwsw.tstreamstransactionserver.exception.Throwable.ZkNoConnectionException
import com.bwsw.tstreamstransactionserver.netty.InetSocketAddressClass
import com.google.common.net.InetAddresses
import org.apache.curator.RetryPolicy
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.recipes.cache.{NodeCache, NodeCacheListener}
import org.apache.curator.framework.state.ConnectionStateListener
import org.slf4j.LoggerFactory


class ZKLeaderClientToGetMaster(endpoints: String,
                                sessionTimeoutMillis: Int,
                                connectionTimeoutMillis: Int,
                                policy: RetryPolicy,
                                prefix: String,
                                connectionStateListener: ConnectionStateListener,
                                onCoordinationPathChangeDo: => Unit
                               )
  extends NodeCacheListener with Closeable {

  private val logger = LoggerFactory.getLogger(this.getClass)
  @volatile private[client] var master: Option[InetSocketAddressClass] = None

  val connection = {
    val connection = CuratorFrameworkFactory.builder()
      .sessionTimeoutMs(sessionTimeoutMillis)
      .connectionTimeoutMs(connectionTimeoutMillis)
      .retryPolicy(policy)
      .connectString(endpoints)
      .build()

    connection.getConnectionStateListenable.addListener(connectionStateListener)

    connection.start()
    val isConnected = connection.blockUntilConnected(connectionTimeoutMillis, TimeUnit.MILLISECONDS)
    if (isConnected) connection else throw new ZkNoConnectionException(endpoints)
  }

  private val nodeToWatch = new NodeCache(connection, prefix, false)
  nodeToWatch.getListenable.addListener(this)

  def start(): Unit = nodeToWatch.start()

  override def close(): Unit = {
    nodeToWatch.close()
    connection.close()
  }

  override def nodeChanged(): Unit = {
    Option(nodeToWatch.getCurrentData) match {
      case Some(node) =>
        val addressPort = new String(node.getData)
        val splitIndex = addressPort.lastIndexOf(':')
        if (splitIndex != -1) {
          val (address, port) = addressPort.splitAt(splitIndex)
          val portToInt = scala.util.Try(port.tail.toInt)
          if (portToInt.isSuccess && InetSocketAddressClass.isValidSocketAddress(address, portToInt.get)) {
            master = Some(InetSocketAddressClass(address, portToInt.get))
            onCoordinationPathChangeDo
          }
          else {
            master = None
            onCoordinationPathChangeDo
            if (logger.isDebugEnabled) logger.debug(s"On Zookeeper server(s) ${connection.getZookeeperClient.getCurrentConnectionString} data(now it is $addressPort) in coordination path $prefix is corrupted!")
          }
        } else {
          master = None
          onCoordinationPathChangeDo
          if (logger.isDebugEnabled) logger.debug(s"On Zookeeper server(s) ${connection.getZookeeperClient.getCurrentConnectionString} data(now it is $addressPort) in coordination path $prefix is corrupted!")
        }
      case None =>
        master = None
    }
  }
}


