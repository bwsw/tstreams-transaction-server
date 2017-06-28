/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.bwsw.tstreamstransactionserver.netty.server.db.rocks

import java.io.{Closeable, File}
import java.util.concurrent.atomic.AtomicLong

import com.bwsw.tstreamstransactionserver.netty.server.db.KeyValueDatabaseManager
import com.bwsw.tstreamstransactionserver.options.ServerOptions.RocksStorageOptions
import org.apache.commons.io.FileUtils
import org.rocksdb._

import scala.collection.{JavaConverters, mutable}

class RocksDBALL(absolutePath: String,
                 rocksStorageOpts: RocksStorageOptions,
                 descriptors: Seq[RocksDatabaseDescriptor],
                 readMode: Boolean = false)
  extends KeyValueDatabaseManager
    with Closeable {
  RocksDB.loadLibrary()

  private val batchIDGen = new AtomicLong(-1L)

  private val options = rocksStorageOpts.createDBOptions()

  private[rocks] val (client, descriptorsWorkWith, databaseHandlers) = {
    val descriptorsWithDefaultDescriptor =
      (new RocksDatabaseDescriptor("default".getBytes(), new ColumnFamilyOptions()) +: descriptors).toArray

    val (columnFamilyDescriptors, ttls) = descriptorsWithDefaultDescriptor
      .map(descriptor => (new ColumnFamilyDescriptor(descriptor.name, descriptor.options), descriptor.ttl)
      ).unzip

    val databaseHandlers = new java.util.ArrayList[ColumnFamilyHandle](columnFamilyDescriptors.length)

    val file = new File(absolutePath)
    FileUtils.forceMkdir(file)

    val connection = TtlDB.open(
      options,
      file.getAbsolutePath,
      JavaConverters.seqAsJavaList(columnFamilyDescriptors),
      databaseHandlers,
      JavaConverters.seqAsJavaList(ttls),
      readMode
    )
    (connection, descriptorsWithDefaultDescriptor.toBuffer, JavaConverters.asScalaBuffer(databaseHandlers))
  }

  def getDatabasesNamesAndIndex: mutable.Seq[(Int, Array[Byte])] =
    descriptorsWorkWith.zipWithIndex.map { case (descriptor, index) => (index, descriptor.name) }

  def getDatabase(index: Int): RocksDBPartitionDatabase =
    new RocksDBPartitionDatabase(client, databaseHandlers(index))

  def getRecordFromDatabase(index: Int, key: Array[Byte]): Array[Byte] =
    client.get(databaseHandlers(index), key)

  def newBatch =
    new Batch(client, databaseHandlers, batchIDGen)

  override def close(): Unit =
    client.close()
}

