package com.bwsw.tstreamstransactionserver.netty.server.multiNode

import com.bwsw.tstreamstransactionserver.options.CommonOptions
import com.bwsw.tstreamstransactionserver.options.CommonOptions.{TracingOptions, ZookeeperOptions}
import com.bwsw.tstreamstransactionserver.options.MultiNodeServerOptions.{BookkeeperOptions, CommonPrefixesOptions}
import com.bwsw.tstreamstransactionserver.options.SingleNodeServerOptions._

class CommonCheckpointGroupServerBuilder private(authenticationOpts: AuthenticationOptions,
                                                 packageTransmissionOpts: TransportOptions,
                                                 zookeeperOpts: CommonOptions.ZookeeperOptions,
                                                 bootstrapOpts: BootstrapOptions,
                                                 commonRoleOpts: CommonRoleOptions,
                                                 commonPrefixesOpts: CommonPrefixesOptions,
                                                 checkpointGroupRoleOpts: CheckpointGroupRoleOptions,
                                                 bookkeeperOpts: BookkeeperOptions,
                                                 storageOpts: StorageOptions,
                                                 rocksStorageOpts: RocksStorageOptions,
                                                 subscriberUpdateOpts: SubscriberUpdateOptions,
                                                 tracingOpts: TracingOptions) {

  private val authenticationOptions = authenticationOpts
  private val packageTransmissionOptions = packageTransmissionOpts
  private val zookeeperOptions = zookeeperOpts
  private val bootstrapOptions = bootstrapOpts
  private val commonRoleOptions = commonRoleOpts
  private val commonPrefixesOptions = commonPrefixesOpts
  private val checkpointGroupRoleOptions = checkpointGroupRoleOpts
  private val bookkeeperOptions = bookkeeperOpts
  private val storageOptions = storageOpts
  private val rocksStorageOptions = rocksStorageOpts
  private val subscribersUpdateOptions = subscriberUpdateOpts
  private val tracingOptions = tracingOpts

  def this() = this(
    AuthenticationOptions(),
    TransportOptions(),
    CommonOptions.ZookeeperOptions(),
    BootstrapOptions(),
    CommonRoleOptions(),
    CommonPrefixesOptions(),
    CheckpointGroupRoleOptions(),
    BookkeeperOptions(),
    StorageOptions(),
    RocksStorageOptions(),
    SubscriberUpdateOptions(),
    TracingOptions()
  )

  def withAuthenticationOptions(authenticationOptions: AuthenticationOptions) =
    new CommonCheckpointGroupServerBuilder(authenticationOptions, packageTransmissionOptions, zookeeperOptions, bootstrapOptions, commonRoleOptions, commonPrefixesOptions, checkpointGroupRoleOptions, bookkeeperOptions, storageOptions, rocksStorageOptions, subscribersUpdateOptions, tracingOptions)

  def withPackageTransmissionOptions(packageTransmissionOptions: TransportOptions) =
    new CommonCheckpointGroupServerBuilder(authenticationOptions, packageTransmissionOptions, zookeeperOptions, bootstrapOptions, commonRoleOptions, commonPrefixesOptions, checkpointGroupRoleOptions, bookkeeperOptions, storageOptions, rocksStorageOptions, subscribersUpdateOptions, tracingOptions)

  def withZookeeperOptions(zookeeperOptions: ZookeeperOptions) =
    new CommonCheckpointGroupServerBuilder(authenticationOptions, packageTransmissionOptions, zookeeperOptions, bootstrapOptions, commonRoleOptions, commonPrefixesOptions, checkpointGroupRoleOptions, bookkeeperOptions, storageOptions, rocksStorageOptions, subscribersUpdateOptions, tracingOptions)

  def withBootstrapOptions(bootstrapOptions: BootstrapOptions) =
    new CommonCheckpointGroupServerBuilder(authenticationOptions, packageTransmissionOptions, zookeeperOptions, bootstrapOptions, commonRoleOptions, commonPrefixesOptions, checkpointGroupRoleOptions, bookkeeperOptions, storageOptions, rocksStorageOptions, subscribersUpdateOptions, tracingOptions)

  def withCommonRoleOptions(commonRoleOptions: CommonRoleOptions) =
    new CommonCheckpointGroupServerBuilder(authenticationOptions, packageTransmissionOptions, zookeeperOptions, bootstrapOptions, commonRoleOptions, commonPrefixesOptions, checkpointGroupRoleOptions, bookkeeperOptions, storageOptions, rocksStorageOptions, subscribersUpdateOptions, tracingOptions)

  def withCommonPrefixesOptions(commonPrefixesOptions: CommonPrefixesOptions) =
    new CommonCheckpointGroupServerBuilder(authenticationOptions, packageTransmissionOptions, zookeeperOptions, bootstrapOptions, commonRoleOptions, commonPrefixesOptions, checkpointGroupRoleOptions, bookkeeperOptions, storageOptions, rocksStorageOptions, subscribersUpdateOptions, tracingOptions)

  def withCheckpointGroupRoleOptions(checkpointGroupRoleOptions: CheckpointGroupRoleOptions) =
    new CommonCheckpointGroupServerBuilder(authenticationOptions, packageTransmissionOptions, zookeeperOptions, bootstrapOptions, commonRoleOptions, commonPrefixesOptions, checkpointGroupRoleOptions, bookkeeperOptions, storageOptions, rocksStorageOptions, subscribersUpdateOptions, tracingOptions)

  def withBookkeeperOptions(bookkeeperOptions: BookkeeperOptions) =
    new CommonCheckpointGroupServerBuilder(authenticationOptions, packageTransmissionOptions, zookeeperOptions, bootstrapOptions, commonRoleOptions, commonPrefixesOptions, checkpointGroupRoleOptions, bookkeeperOptions, storageOptions, rocksStorageOptions, subscribersUpdateOptions, tracingOptions)

  def withServerStorageOptions(storageOptions: StorageOptions) =
    new CommonCheckpointGroupServerBuilder(authenticationOptions, packageTransmissionOptions, zookeeperOptions, bootstrapOptions, commonRoleOptions, commonPrefixesOptions, checkpointGroupRoleOptions, bookkeeperOptions, storageOptions, rocksStorageOptions, subscribersUpdateOptions, tracingOptions)

  def withServerRocksStorageOptions(rocksStorageOptions: RocksStorageOptions) =
    new CommonCheckpointGroupServerBuilder(authenticationOptions, packageTransmissionOptions, zookeeperOptions, bootstrapOptions, commonRoleOptions, commonPrefixesOptions, checkpointGroupRoleOptions, bookkeeperOptions, storageOptions, rocksStorageOptions, subscribersUpdateOptions, tracingOptions)

  def withSubscribersUpdateOptions(subscribersUpdateOptions: SubscriberUpdateOptions) =
    new CommonCheckpointGroupServerBuilder(authenticationOptions, packageTransmissionOptions, zookeeperOptions, bootstrapOptions, commonRoleOptions, commonPrefixesOptions, checkpointGroupRoleOptions, bookkeeperOptions, storageOptions, rocksStorageOptions, subscribersUpdateOptions, tracingOptions)

  def withTracingOptions(tracingOptions: TracingOptions) =
    new CommonCheckpointGroupServerBuilder(authenticationOptions, packageTransmissionOptions, zookeeperOptions, bootstrapOptions, commonRoleOptions, commonPrefixesOptions, checkpointGroupRoleOptions, bookkeeperOptions, storageOptions, rocksStorageOptions, subscribersUpdateOptions, tracingOptions)

  def build() = new CommonCheckpointGroupServer(
    authenticationOptions,
    packageTransmissionOptions,
    zookeeperOptions,
    bootstrapOptions,
    commonRoleOptions,
    commonPrefixesOptions,
    checkpointGroupRoleOptions,
    bookkeeperOptions,
    storageOptions,
    rocksStorageOptions,
    subscribersUpdateOptions,
    tracingOptions
  )

  def getAuthenticationOptions =
    authenticationOptions.copy()

  def getPackageTransmissionOptions =
    packageTransmissionOptions.copy()

  def getZookeeperOptions =
    zookeeperOptions.copy()

  def getBootstrapOptions =
    bootstrapOptions.copy()

  def getCommonRoleOptions =
    commonRoleOptions.copy()

  def getCommonPrefixesOptions =
    commonPrefixesOptions.copy()

  def getCheckpointGroupRoleOptions =
    checkpointGroupRoleOptions.copy()

  def getBookkeeperOptions =
    bookkeeperOptions.copy()

  def getStorageOptions =
    storageOptions.copy()

  def getRocksStorageOptions =
    rocksStorageOptions.copy()

  def getSubscribersUpdateOptions =
    subscribersUpdateOptions.copy()

  def getTracingOptions = tracingOptions
}


