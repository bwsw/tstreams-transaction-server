package com.bwsw.tstreamstransactionserver.options


object CommonOptions {
  /**
    *
    * @param endpoints
    * @param prefix
    * @param sessionTimeoutMs
    * @param retryDelayMs
    * @param connectionTimeoutMs
    */
  case class ZookeeperOptions(endpoints: String = "127.0.0.1:2181", prefix: String = "/tts", sessionTimeoutMs: Int = 10000,
                              retryDelayMs: Int = 500, connectionTimeoutMs: Int = 10000)

}