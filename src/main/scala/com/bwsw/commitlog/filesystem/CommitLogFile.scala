package com.bwsw.commitlog.filesystem

import java.io.{BufferedInputStream, File, FileInputStream, FileNotFoundException}
import java.math.BigInteger
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{Files, Paths}
import java.security.MessageDigest

import scala.io.Source

/** Represents commitlog file with data.
  *
  * @param path full path to file
  */
class CommitLogFile(path: String) {
  private val file = new File(path)
  private val md5File = new File(file.toString.split('.')(0) + ".md5")

  class Attributes {
    private val attr = Files.readAttributes(Paths.get(file.getAbsolutePath), classOf[BasicFileAttributes])
    def creationTime = attr.creationTime()
    def lastAccessTime = attr.lastAccessTime()
    def lastModifiedTime = attr.lastModifiedTime()
  }

  def attributes = new Attributes()

  /** Returns underlying file. */
  def getFile(): File = file


  /** Returns an iterator over records */
  def getIterator(): CommitLogFileIterator = new CommitLogFileIterator(file.toString)


  /** bytes to read from this file */
  private val chunkSize = 100000

  /** Returns calculated MD5 of this file. */
  def calculateMD5(): Array[Byte] = {
    val fileInputStream = new FileInputStream(file)
    val stream = new BufferedInputStream(fileInputStream)

    val md5: MessageDigest = MessageDigest.getInstance("MD5")
    md5.reset()
    while (stream.available() > 0) {
      val chunk = new Array[Byte](chunkSize)
      val bytesReaded = stream.read(chunk)
      md5.update(chunk.take(bytesReaded))
    }

    stream.close()
    fileInputStream.close()

    new BigInteger(1, md5.digest()).toByteArray
  }

  /** Returns a MD5 sum from MD5 FIle */
  private def getContentOfMD5File = {
    val fileInputStream = new FileInputStream(md5File)
    val md5Sum = new  Array[Byte](17)
    fileInputStream.read(md5Sum)
    fileInputStream.close()
    md5Sum
  }


  /** Returns existing MD5 of this file. Throws an exception otherwise. */
  def getMD5(): Array[Byte] = if (!md5Exists()) throw new FileNotFoundException("No MD5 file for " + path) else getContentOfMD5File

  /** Checks md5 sum of file with existing md5 sum. Throws an exception when no MD5 exists. */
  def checkMD5(): Boolean = getMD5 sameElements calculateMD5()

  /** Returns true if md5-file exists. */
  def md5Exists(): Boolean = md5File.exists()
}
