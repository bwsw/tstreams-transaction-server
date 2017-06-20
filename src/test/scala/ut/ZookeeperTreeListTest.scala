package ut

import com.bwsw.tstreamstransactionserver.netty.server.bookkeeperService.hierarchy.ZookeeperTreeListLong
import org.scalatest.{FlatSpec, Matchers}
import util.Utils

class ZookeeperTreeListTest
  extends FlatSpec
    with Matchers
{

  "ZookeeperTreeListLong" should "return first entry id and last entry id as Nones" in {
    val (zkServer, zkClient) = Utils.startZkServerAndGetIt

    val treeListLong = new ZookeeperTreeListLong(zkClient, "/test")

    treeListLong.lastEntryID shouldBe None
    treeListLong.firstEntryID shouldBe None

    zkClient.close()
    zkServer.close()
  }

  it should "return first entry id and last entry id the same as only one entity id was persisted" in {
    val (zkServer, zkClient) = Utils.startZkServerAndGetIt

    val treeListLong = new ZookeeperTreeListLong(zkClient, "/test")

    val value = 1L
    treeListLong.createNode(value)

    treeListLong.firstEntryID shouldBe defined
    treeListLong.firstEntryID.get == value

    treeListLong.firstEntryID shouldBe treeListLong.lastEntryID

    zkClient.close()
    zkServer.close()
  }

  it should "return first entry id and last entry id properly" in {
    val (zkServer, zkClient) = Utils.startZkServerAndGetIt

    val treeListLong = new ZookeeperTreeListLong(zkClient, "/test")

    val startNumber = 0
    val maxNumbers  = 100

    (startNumber to maxNumbers)
      .foreach(number => treeListLong.createNode(number))

    treeListLong.firstEntryID shouldBe Some(startNumber)
    treeListLong.lastEntryID  shouldBe Some(maxNumbers)

    zkClient.close()
    zkServer.close()
  }

}
