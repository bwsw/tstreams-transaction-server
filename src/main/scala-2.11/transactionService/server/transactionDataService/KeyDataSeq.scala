package transactionService.server.transactionDataService

import transactionService.server.`implicit`.Implicits._

case class KeyDataSeq(key: Key, dataSeq: Int) {
  def toBinary: Array[Byte] = key.toBinary ++ intToByteArray(dataSeq)
  override def toString: String = s"${key.toString} $dataSeq"
}
