
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

package com.bwsw.tstreamstransactionserver.options

import com.bwsw.tstreamstransactionserver.netty.server.SingleNodeServer
import com.bwsw.tstreamstransactionserver.options.CommonOptions.ZookeeperOptions
import com.bwsw.tstreamstransactionserver.options.ServerOptions._

class SingleNodeServerBuilder private(authenticationOpts: AuthenticationOptions,
                                      zookeeperOpts: CommonOptions.ZookeeperOptions,
                                      bootstrapOpts: BootstrapOptions,
                                      serverReplicationOpts: ServerReplicationOptions,
                                      storageOpts: StorageOptions,
                                      rocksStorageOpts: RocksStorageOptions,
                                      commitLogOpts: CommitLogOptions,
                                      packageTransmissionOpts: TransportOptions,
                                      subscriberUpdateOpts: SubscriberUpdateOptions) {

  private val authenticationOptions = authenticationOpts
  private val zookeeperOptions = zookeeperOpts
  private val bootstrapOptions = bootstrapOpts
  private val serverReplicationOptions = serverReplicationOpts
  private val storageOptions = storageOpts
  private val rocksStorageOptions = rocksStorageOpts
  private val commitLogOptions = commitLogOpts
  private val packageTransmissionOptions = packageTransmissionOpts
  private val subscribersUpdateOptions = subscriberUpdateOpts

  def this() = this(
    AuthenticationOptions(),
    CommonOptions.ZookeeperOptions(),
    BootstrapOptions(),
    ServerReplicationOptions(),
    StorageOptions(),
    RocksStorageOptions(),
    CommitLogOptions(),
    TransportOptions(),
    SubscriberUpdateOptions()
  )

  def withAuthenticationOptions(authenticationOptions: AuthenticationOptions) =
    new SingleNodeServerBuilder(authenticationOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, subscribersUpdateOptions)

  def withZookeeperOptions(zookeeperOptions: ZookeeperOptions) =
    new SingleNodeServerBuilder(authenticationOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, subscribersUpdateOptions)

  def withBootstrapOptions(bootstrapOptions: BootstrapOptions) =
    new SingleNodeServerBuilder(authenticationOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, subscribersUpdateOptions)

  def withServerReplicationOptions(serverReplicationOptions: ServerReplicationOptions) =
    new SingleNodeServerBuilder(authenticationOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, subscribersUpdateOptions)

  def withServerStorageOptions(serverStorageOptions: StorageOptions) =
    new SingleNodeServerBuilder(authenticationOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, serverStorageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, subscribersUpdateOptions)

  def withServerRocksStorageOptions(serverStorageRocksOptions: RocksStorageOptions) =
    new SingleNodeServerBuilder(authenticationOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, serverStorageRocksOptions, commitLogOptions, packageTransmissionOptions, subscribersUpdateOptions)

  def withCommitLogOptions(commitLogOptions: CommitLogOptions) =
    new SingleNodeServerBuilder(authenticationOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, subscribersUpdateOptions)

  def withPackageTransmissionOptions(packageTransmissionOptions: TransportOptions) =
    new SingleNodeServerBuilder(authenticationOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, subscribersUpdateOptions)

  def withSubscribersUpdateOptions(subscriberUpdateOptions: SubscriberUpdateOptions): SingleNodeServerBuilder =
    new SingleNodeServerBuilder(authenticationOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, subscriberUpdateOptions)


  def build() = new SingleNodeServer(
    authenticationOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions,
    storageOptions, rocksStorageOptions, commitLogOptions,
    packageTransmissionOptions,
    subscribersUpdateOptions
  )

  def getZookeeperOptions = zookeeperOptions.copy()

  def getAuthenticationOptions = authenticationOptions.copy()

  def getBootstrapOptions = bootstrapOptions.copy()

  def getServerReplicationOptions = serverReplicationOptions.copy()

  def getStorageOptions = storageOptions.copy()

  def getRocksStorageOptions = rocksStorageOptions.copy()

  def getPackageTransmissionOptions = packageTransmissionOptions.copy()

  def getCommitLogOptions = commitLogOptions.copy()

  def getSubscribersUpdateOptions = subscribersUpdateOptions.copy()
}