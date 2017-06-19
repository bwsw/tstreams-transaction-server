package com.bwsw.tstreamstransactionserver.netty.server.bookkeeperService.hierarchy

import org.apache.zookeeper.data.Stat

private[bookkeeperService] case class RootNodeData(metadata: Stat,
                                                   firstID: Array[Byte],
                                                   lastID: Array[Byte])
