package ut

import com.bwsw.tstreamstransactionserver.netty.server.bookkeeperService.hierarchy.{RootNode, ZookeeperTreeLongList}
import org.scalatest.{FlatSpec, Matchers}
import util.Utils

class ZookeeperTreeListTest
  extends FlatSpec
    with Matchers
{

  "asdas" should "asd" in {
    val (zkServer, zkClient) = Utils.startZkServerAndGetIt

//    val rootNode = new RootNode(zkClient, "/test")

    val longList = new ZookeeperTreeLongList(zkClient, "/test")
    longList.createNode(1)
    println(longList.lastRecordID)
    println(longList.firstRecordID)

    longList.createNode(5)
    println(longList.lastRecordID)
    println(longList.firstRecordID)

    zkClient.close()
    zkServer.close()
  }

}
