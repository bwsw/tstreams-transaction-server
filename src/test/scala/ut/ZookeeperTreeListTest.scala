package ut

import com.bwsw.tstreamstransactionserver.netty.server.bookkeeperService.hierarchy.ZookeeperTreeList
import org.scalatest.{FlatSpec, Matchers}
import util.Utils

class ZookeeperTreeListTest
  extends FlatSpec
    with Matchers
{

  "asdas" should "asd" in {
    val (zkServer, zkClient) = Utils.startZkServerAndGetIt

    val longList = new ZookeeperTreeList(zkClient, "/test")

    (0 to 1000) foreach{i =>
      longList.createNode(i)
    }

    println(longList.getNodeNext(3))
    println(longList.lastRecordID)
    println(longList.firstRecordID)

    zkClient.close()
    zkServer.close()
  }

}
