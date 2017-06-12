// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.bwsw.tstreamstransactionserver.protocol



@SerialVersionUID(0L)
final case class TransactionState(
    transactionID: Long = 0L,
    partition: Int = 0,
    masterID: Int = 0,
    orderID: Long = 0L,
    count: Int = 0,
    status: com.bwsw.tstreamstransactionserver.protocol.TransactionState.Status = com.bwsw.tstreamstransactionserver.protocol.TransactionState.Status.Opened,
    ttlMs: Long = 0L,
    isNotReliable: Boolean = false,
    authKey: String = ""
    ) extends com.trueaccord.scalapb.GeneratedMessage with com.trueaccord.scalapb.Message[TransactionState] with com.trueaccord.lenses.Updatable[TransactionState] {
    @transient
    private[this] var __serializedSizeCachedValue: Int = 0
    private[this] def __computeSerializedValue(): Int = {
      var __size = 0
      if (transactionID != 0L) { __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(1, transactionID) }
      if (partition != 0) { __size += _root_.com.google.protobuf.CodedOutputStream.computeInt32Size(2, partition) }
      if (masterID != 0) { __size += _root_.com.google.protobuf.CodedOutputStream.computeInt32Size(3, masterID) }
      if (orderID != 0L) { __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(4, orderID) }
      if (count != 0) { __size += _root_.com.google.protobuf.CodedOutputStream.computeInt32Size(5, count) }
      if (status != com.bwsw.tstreamstransactionserver.protocol.TransactionState.Status.Opened) { __size += _root_.com.google.protobuf.CodedOutputStream.computeEnumSize(6, status.value) }
      if (ttlMs != 0L) { __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(7, ttlMs) }
      if (isNotReliable != false) { __size += _root_.com.google.protobuf.CodedOutputStream.computeBoolSize(8, isNotReliable) }
      if (authKey != "") { __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(9, authKey) }
      __size
    }
    final override def serializedSize: Int = {
      var read = __serializedSizeCachedValue
      if (read == 0) {
        read = __computeSerializedValue()
        __serializedSizeCachedValue = read
      }
      read
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): Unit = {
      {
        val __v = transactionID
        if (__v != 0L) {
          _output__.writeInt64(1, __v)
        }
      };
      {
        val __v = partition
        if (__v != 0) {
          _output__.writeInt32(2, __v)
        }
      };
      {
        val __v = masterID
        if (__v != 0) {
          _output__.writeInt32(3, __v)
        }
      };
      {
        val __v = orderID
        if (__v != 0L) {
          _output__.writeInt64(4, __v)
        }
      };
      {
        val __v = count
        if (__v != 0) {
          _output__.writeInt32(5, __v)
        }
      };
      {
        val __v = status
        if (__v != com.bwsw.tstreamstransactionserver.protocol.TransactionState.Status.Opened) {
          _output__.writeEnum(6, __v.value)
        }
      };
      {
        val __v = ttlMs
        if (__v != 0L) {
          _output__.writeInt64(7, __v)
        }
      };
      {
        val __v = isNotReliable
        if (__v != false) {
          _output__.writeBool(8, __v)
        }
      };
      {
        val __v = authKey
        if (__v != "") {
          _output__.writeString(9, __v)
        }
      };
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.bwsw.tstreamstransactionserver.protocol.TransactionState = {
      var __transactionID = this.transactionID
      var __partition = this.partition
      var __masterID = this.masterID
      var __orderID = this.orderID
      var __count = this.count
      var __status = this.status
      var __ttlMs = this.ttlMs
      var __isNotReliable = this.isNotReliable
      var __authKey = this.authKey
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 8 =>
            __transactionID = _input__.readInt64()
          case 16 =>
            __partition = _input__.readInt32()
          case 24 =>
            __masterID = _input__.readInt32()
          case 32 =>
            __orderID = _input__.readInt64()
          case 40 =>
            __count = _input__.readInt32()
          case 48 =>
            __status = com.bwsw.tstreamstransactionserver.protocol.TransactionState.Status.fromValue(_input__.readEnum())
          case 56 =>
            __ttlMs = _input__.readInt64()
          case 64 =>
            __isNotReliable = _input__.readBool()
          case 74 =>
            __authKey = _input__.readString()
          case tag => _input__.skipField(tag)
        }
      }
      com.bwsw.tstreamstransactionserver.protocol.TransactionState(
          transactionID = __transactionID,
          partition = __partition,
          masterID = __masterID,
          orderID = __orderID,
          count = __count,
          status = __status,
          ttlMs = __ttlMs,
          isNotReliable = __isNotReliable,
          authKey = __authKey
      )
    }
    def withTransactionID(__v: Long): TransactionState = copy(transactionID = __v)
    def withPartition(__v: Int): TransactionState = copy(partition = __v)
    def withMasterID(__v: Int): TransactionState = copy(masterID = __v)
    def withOrderID(__v: Long): TransactionState = copy(orderID = __v)
    def withCount(__v: Int): TransactionState = copy(count = __v)
    def withStatus(__v: com.bwsw.tstreamstransactionserver.protocol.TransactionState.Status): TransactionState = copy(status = __v)
    def withTtlMs(__v: Long): TransactionState = copy(ttlMs = __v)
    def withIsNotReliable(__v: Boolean): TransactionState = copy(isNotReliable = __v)
    def withAuthKey(__v: String): TransactionState = copy(authKey = __v)
    def getFieldByNumber(__fieldNumber: Int): scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = transactionID
          if (__t != 0L) __t else null
        }
        case 2 => {
          val __t = partition
          if (__t != 0) __t else null
        }
        case 3 => {
          val __t = masterID
          if (__t != 0) __t else null
        }
        case 4 => {
          val __t = orderID
          if (__t != 0L) __t else null
        }
        case 5 => {
          val __t = count
          if (__t != 0) __t else null
        }
        case 6 => {
          val __t = status.javaValueDescriptor
          if (__t.getNumber() != 0) __t else null
        }
        case 7 => {
          val __t = ttlMs
          if (__t != 0L) __t else null
        }
        case 8 => {
          val __t = isNotReliable
          if (__t != false) __t else null
        }
        case 9 => {
          val __t = authKey
          if (__t != "") __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PLong(transactionID)
        case 2 => _root_.scalapb.descriptors.PInt(partition)
        case 3 => _root_.scalapb.descriptors.PInt(masterID)
        case 4 => _root_.scalapb.descriptors.PLong(orderID)
        case 5 => _root_.scalapb.descriptors.PInt(count)
        case 6 => _root_.scalapb.descriptors.PEnum(status.scalaValueDescriptor)
        case 7 => _root_.scalapb.descriptors.PLong(ttlMs)
        case 8 => _root_.scalapb.descriptors.PBoolean(isNotReliable)
        case 9 => _root_.scalapb.descriptors.PString(authKey)
      }
    }
    override def toString: String = _root_.com.trueaccord.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion = com.bwsw.tstreamstransactionserver.protocol.TransactionState
}

object TransactionState extends com.trueaccord.scalapb.GeneratedMessageCompanion[com.bwsw.tstreamstransactionserver.protocol.TransactionState] {
  implicit def messageCompanion: com.trueaccord.scalapb.GeneratedMessageCompanion[com.bwsw.tstreamstransactionserver.protocol.TransactionState] = this
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.bwsw.tstreamstransactionserver.protocol.TransactionState = {
    require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    com.bwsw.tstreamstransactionserver.protocol.TransactionState(
      __fieldsMap.getOrElse(__fields.get(0), 0L).asInstanceOf[Long],
      __fieldsMap.getOrElse(__fields.get(1), 0).asInstanceOf[Int],
      __fieldsMap.getOrElse(__fields.get(2), 0).asInstanceOf[Int],
      __fieldsMap.getOrElse(__fields.get(3), 0L).asInstanceOf[Long],
      __fieldsMap.getOrElse(__fields.get(4), 0).asInstanceOf[Int],
      com.bwsw.tstreamstransactionserver.protocol.TransactionState.Status.fromValue(__fieldsMap.getOrElse(__fields.get(5), com.bwsw.tstreamstransactionserver.protocol.TransactionState.Status.Opened.javaValueDescriptor).asInstanceOf[_root_.com.google.protobuf.Descriptors.EnumValueDescriptor].getNumber),
      __fieldsMap.getOrElse(__fields.get(6), 0L).asInstanceOf[Long],
      __fieldsMap.getOrElse(__fields.get(7), false).asInstanceOf[Boolean],
      __fieldsMap.getOrElse(__fields.get(8), "").asInstanceOf[String]
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.bwsw.tstreamstransactionserver.protocol.TransactionState] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      com.bwsw.tstreamstransactionserver.protocol.TransactionState(
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[Long]).getOrElse(0L),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[Int]).getOrElse(0),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[Int]).getOrElse(0),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).map(_.as[Long]).getOrElse(0L),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(5).get).map(_.as[Int]).getOrElse(0),
        com.bwsw.tstreamstransactionserver.protocol.TransactionState.Status.fromValue(__fieldsMap.get(scalaDescriptor.findFieldByNumber(6).get).map(_.as[_root_.scalapb.descriptors.EnumValueDescriptor]).getOrElse(com.bwsw.tstreamstransactionserver.protocol.TransactionState.Status.Opened.scalaValueDescriptor).number),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(7).get).map(_.as[Long]).getOrElse(0L),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(8).get).map(_.as[Boolean]).getOrElse(false),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(9).get).map(_.as[String]).getOrElse("")
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = ProtocolProto.javaDescriptor.getMessageTypes.get(2)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ProtocolProto.scalaDescriptor.messages(2)
  def messageCompanionForFieldNumber(__fieldNumber: Int): _root_.com.trueaccord.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__fieldNumber)
  def enumCompanionForFieldNumber(__fieldNumber: Int): _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[_] = {
    (__fieldNumber: @_root_.scala.unchecked) match {
      case 6 => com.bwsw.tstreamstransactionserver.protocol.TransactionState.Status
    }
  }
  lazy val defaultInstance = com.bwsw.tstreamstransactionserver.protocol.TransactionState(
  )
  sealed trait Status extends _root_.com.trueaccord.scalapb.GeneratedEnum {
    type EnumType = Status
    def isOpened: Boolean = false
    def isCheckpointed: Boolean = false
    def isCancelled: Boolean = false
    def isUpdated: Boolean = false
    def isInvalid: Boolean = false
    def isInstant: Boolean = false
    def companion: _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[Status] = com.bwsw.tstreamstransactionserver.protocol.TransactionState.Status
  }
  
  object Status extends _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[Status] {
    implicit def enumCompanion: _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[Status] = this
    @SerialVersionUID(0L)
    case object Opened extends Status {
      val value = 0
      val index = 0
      val name = "Opened"
      override def isOpened: Boolean = true
    }
    
    @SerialVersionUID(0L)
    case object Checkpointed extends Status {
      val value = 1
      val index = 1
      val name = "Checkpointed"
      override def isCheckpointed: Boolean = true
    }
    
    @SerialVersionUID(0L)
    case object Cancelled extends Status {
      val value = 2
      val index = 2
      val name = "Cancelled"
      override def isCancelled: Boolean = true
    }
    
    @SerialVersionUID(0L)
    case object Updated extends Status {
      val value = 3
      val index = 3
      val name = "Updated"
      override def isUpdated: Boolean = true
    }
    
    @SerialVersionUID(0L)
    case object Invalid extends Status {
      val value = 4
      val index = 4
      val name = "Invalid"
      override def isInvalid: Boolean = true
    }
    
    @SerialVersionUID(0L)
    case object Instant extends Status {
      val value = 5
      val index = 5
      val name = "Instant"
      override def isInstant: Boolean = true
    }
    
    @SerialVersionUID(0L)
    case class Unrecognized(value: Int) extends Status with _root_.com.trueaccord.scalapb.UnrecognizedEnum
    
    lazy val values = scala.collection.Seq(Opened, Checkpointed, Cancelled, Updated, Invalid, Instant)
    def fromValue(value: Int): Status = value match {
      case 0 => Opened
      case 1 => Checkpointed
      case 2 => Cancelled
      case 3 => Updated
      case 4 => Invalid
      case 5 => Instant
      case __other => Unrecognized(__other)
    }
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.EnumDescriptor = com.bwsw.tstreamstransactionserver.protocol.TransactionState.javaDescriptor.getEnumTypes.get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.EnumDescriptor = com.bwsw.tstreamstransactionserver.protocol.TransactionState.scalaDescriptor.enums(0)
  }
  implicit class TransactionStateLens[UpperPB](_l: _root_.com.trueaccord.lenses.Lens[UpperPB, com.bwsw.tstreamstransactionserver.protocol.TransactionState]) extends _root_.com.trueaccord.lenses.ObjectLens[UpperPB, com.bwsw.tstreamstransactionserver.protocol.TransactionState](_l) {
    def transactionID: _root_.com.trueaccord.lenses.Lens[UpperPB, Long] = field(_.transactionID)((c_, f_) => c_.copy(transactionID = f_))
    def partition: _root_.com.trueaccord.lenses.Lens[UpperPB, Int] = field(_.partition)((c_, f_) => c_.copy(partition = f_))
    def masterID: _root_.com.trueaccord.lenses.Lens[UpperPB, Int] = field(_.masterID)((c_, f_) => c_.copy(masterID = f_))
    def orderID: _root_.com.trueaccord.lenses.Lens[UpperPB, Long] = field(_.orderID)((c_, f_) => c_.copy(orderID = f_))
    def count: _root_.com.trueaccord.lenses.Lens[UpperPB, Int] = field(_.count)((c_, f_) => c_.copy(count = f_))
    def status: _root_.com.trueaccord.lenses.Lens[UpperPB, com.bwsw.tstreamstransactionserver.protocol.TransactionState.Status] = field(_.status)((c_, f_) => c_.copy(status = f_))
    def ttlMs: _root_.com.trueaccord.lenses.Lens[UpperPB, Long] = field(_.ttlMs)((c_, f_) => c_.copy(ttlMs = f_))
    def isNotReliable: _root_.com.trueaccord.lenses.Lens[UpperPB, Boolean] = field(_.isNotReliable)((c_, f_) => c_.copy(isNotReliable = f_))
    def authKey: _root_.com.trueaccord.lenses.Lens[UpperPB, String] = field(_.authKey)((c_, f_) => c_.copy(authKey = f_))
  }
  final val TRANSACTIONID_FIELD_NUMBER = 1
  final val PARTITION_FIELD_NUMBER = 2
  final val MASTERID_FIELD_NUMBER = 3
  final val ORDERID_FIELD_NUMBER = 4
  final val COUNT_FIELD_NUMBER = 5
  final val STATUS_FIELD_NUMBER = 6
  final val TTLMS_FIELD_NUMBER = 7
  final val ISNOTRELIABLE_FIELD_NUMBER = 8
  final val AUTHKEY_FIELD_NUMBER = 9
}