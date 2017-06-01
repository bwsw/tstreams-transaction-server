package com.bwsw.tstreamstransactionserver.options

import com.bwsw.tstreamstransactionserver.netty.server.Server
import com.bwsw.tstreamstransactionserver.options.CommonOptions.ZookeeperOptions
import com.bwsw.tstreamstransactionserver.options.ServerOptions._

class ServerBuilder private(authOpts: AuthOptions, zookeeperOpts: CommonOptions.ZookeeperOptions,
                            bootstrapOpts: BootstrapOptions, serverReplicationOpts: ServerReplicationOptions,
                            storageOpts: StorageOptions, rocksStorageOpts: RocksStorageOptions, commitLogOpts: CommitLogOptions,
                            packageTransmissionOpts: TransportOptions, zookeeperSpecificOpt: ServerOptions.ZooKeeperOptions,
                            subscriberOpts: SubscriberOptions) {
  private val authOptions = authOpts
  private val zookeeperOptions = zookeeperOpts
  private val bootstrapOptions = bootstrapOpts
  private val serverReplicationOptions = serverReplicationOpts
  private val storageOptions = storageOpts
  private val rocksStorageOptions = rocksStorageOpts
  private val commitLogOptions = commitLogOpts
  private val packageTransmissionOptions = packageTransmissionOpts
  private val zookeeperSpecificOptions = zookeeperSpecificOpt
  private val subscriberOptions = subscriberOpts

  def this() = this(
    AuthOptions(), CommonOptions.ZookeeperOptions(),
    BootstrapOptions(), ServerReplicationOptions(),
    StorageOptions(), RocksStorageOptions(), CommitLogOptions(),
    TransportOptions(), ServerOptions.ZooKeeperOptions(),
    SubscriberOptions()
  )

  def withAuthOptions(authOptions: AuthOptions) =
    new ServerBuilder(authOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, zookeeperSpecificOptions, subscriberOptions)

  def withZookeeperOptions(zookeeperOptions: ZookeeperOptions) =
    new ServerBuilder(authOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, zookeeperSpecificOptions, subscriberOptions)

  def withBootstrapOptions(bootstrapOptions: BootstrapOptions) =
    new ServerBuilder(authOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, zookeeperSpecificOptions, subscriberOptions)

  def withServerReplicationOptions(serverReplicationOptions: ServerReplicationOptions) =
    new ServerBuilder(authOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, zookeeperSpecificOptions, subscriberOptions)

  def withServerStorageOptions(serverStorageOptions: StorageOptions) =
    new ServerBuilder(authOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, serverStorageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, zookeeperSpecificOptions, subscriberOptions)

  def withServerRocksStorageOptions(serverStorageRocksOptions: RocksStorageOptions) =
    new ServerBuilder(authOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, serverStorageRocksOptions, commitLogOptions, packageTransmissionOptions, zookeeperSpecificOptions, subscriberOptions)

  def withCommitLogOptions(commitLogOptions: CommitLogOptions) =
    new ServerBuilder(authOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, zookeeperSpecificOptions, subscriberOptions)

  def withPackageTransmissionOptions(packageTransmissionOptions: TransportOptions) =
    new ServerBuilder(authOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, zookeeperSpecificOptions, subscriberOptions)

  def withZooKeeperSpecificOption(zookeeperSpecificOptions: ServerOptions.ZooKeeperOptions) =
    new ServerBuilder(authOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, zookeeperSpecificOptions, subscriberOptions)

  def withSubscriberOptions(subscriberOptions: SubscriberOptions): ServerBuilder =
    new ServerBuilder(authOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions, storageOptions, rocksStorageOptions, commitLogOptions, packageTransmissionOptions, zookeeperSpecificOptions, subscriberOptions)


  def build() = new Server(
    authOptions, zookeeperOptions, bootstrapOptions, serverReplicationOptions,
    storageOptions, rocksStorageOptions, commitLogOptions,
    packageTransmissionOptions, zookeeperSpecificOptions
  )

  def getZookeeperOptions = zookeeperOptions.copy()

  def getAuthOptions = authOptions.copy()

  def getBootstrapOptions = bootstrapOptions.copy()

  def getServerReplicationOptions = serverReplicationOptions.copy()

  def getStorageOptions = storageOptions.copy()

  def getRocksStorageOptions = rocksStorageOptions.copy()

  def getPackageTransmissionOptions = packageTransmissionOptions.copy()

  def getCommitLogOptions = commitLogOptions.copy()

  def getZookeeperSpecificOptions = zookeeperSpecificOptions.copy()

  def getSubscriberOptions = subscriberOptions.copy()
}