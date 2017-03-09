/**
 * Generated by Scrooge
 *   version: 4.14.0
 *   rev: 56cc6a6ed000a14f1a3fef4e3e5e60dab4478499
 *   built at: 20170203-164626
 */
package transactionService.rpc

import com.twitter.scrooge.{
  LazyTProtocol,
  HasThriftStructCodec3,
  TFieldBlob,
  ThriftService,
  ThriftStruct,
  ThriftStructCodec,
  ThriftStructCodec3,
  ThriftStructFieldInfo,
  ThriftResponse,
  ThriftUtil,
  ToThriftService
}
import java.nio.ByteBuffer
import java.util.Arrays
import org.apache.thrift.protocol._
import org.apache.thrift.transport.TTransport
import org.apache.thrift.TApplicationException
import org.apache.thrift.transport.TMemoryBuffer
import scala.collection.immutable.{Map => immutable$Map}
import scala.collection.mutable.{
  Builder,
  ArrayBuffer => mutable$ArrayBuffer, Buffer => mutable$Buffer,
  HashMap => mutable$HashMap, HashSet => mutable$HashSet}
import scala.collection.{Map, Set}
import scala.language.higherKinds


@javax.annotation.Generated(value = Array("com.twitter.scrooge.Compiler"))
trait AuthService[+MM[_]] extends ThriftService {
  
  def authenticate(authKey: String): MM[transactionService.rpc.AuthInfo]
  
  def isValid(token: Int): MM[Boolean]
}



object AuthService { self =>

  object Authenticate extends com.twitter.scrooge.ThriftMethod {
    
    object Args extends ThriftStructCodec3[Args] {
      private val NoPassthroughFields = immutable$Map.empty[Short, TFieldBlob]
      val Struct = new TStruct("authenticate_args")
      val AuthKeyField = new TField("authKey", TType.STRING, 1)
      val AuthKeyFieldManifest = implicitly[Manifest[String]]
    
      /**
       * Field information in declaration order.
       */
      lazy val fieldInfos: scala.List[ThriftStructFieldInfo] = scala.List[ThriftStructFieldInfo](
        new ThriftStructFieldInfo(
          AuthKeyField,
          false,
          false,
          AuthKeyFieldManifest,
          _root_.scala.None,
          _root_.scala.None,
          immutable$Map.empty[String, String],
          immutable$Map.empty[String, String],
          None
        )
      )
    
      lazy val structAnnotations: immutable$Map[String, String] =
        immutable$Map.empty[String, String]
    
      /**
       * Checks that all required fields are non-null.
       */
      def validate(_item: Args): Unit = {
      }
    
      def withoutPassthroughFields(original: Args): Args =
        new Args(
          authKey =
            {
              val field = original.authKey
              field
            }
        )
    
      override def encode(_item: Args, _oproto: TProtocol): Unit = {
        _item.write(_oproto)
      }
    
      override def decode(_iprot: TProtocol): Args = {
        var authKey: String = null
        var _passthroughFields: Builder[(Short, TFieldBlob), immutable$Map[Short, TFieldBlob]] = null
        var _done = false
    
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case 1 =>
                _field.`type` match {
                  case TType.STRING =>
                    authKey = readAuthKeyValue(_iprot)
                  case _actualType =>
                    val _expectedType = TType.STRING
                    throw new TProtocolException(
                      "Received wrong type for field 'authKey' (expected=%s, actual=%s).".format(
                        ttypeToString(_expectedType),
                        ttypeToString(_actualType)
                      )
                    )
                }
              case _ =>
                if (_passthroughFields == null)
                  _passthroughFields = immutable$Map.newBuilder[Short, TFieldBlob]
                _passthroughFields += (_field.id -> TFieldBlob.read(_field, _iprot))
            }
            _iprot.readFieldEnd()
          }
        }
        _iprot.readStructEnd()
    
        new Args(
          authKey,
          if (_passthroughFields == null)
            NoPassthroughFields
          else
            _passthroughFields.result()
        )
      }
    
      def apply(
        authKey: String
      ): Args =
        new Args(
          authKey
        )
    
      def unapply(_item: Args): _root_.scala.Option[String] = _root_.scala.Some(_item.authKey)
    
    
      @inline private def readAuthKeyValue(_iprot: TProtocol): String = {
        _iprot.readString()
      }
    
      @inline private def writeAuthKeyField(authKey_item: String, _oprot: TProtocol): Unit = {
        _oprot.writeFieldBegin(AuthKeyField)
        writeAuthKeyValue(authKey_item, _oprot)
        _oprot.writeFieldEnd()
      }
    
      @inline private def writeAuthKeyValue(authKey_item: String, _oprot: TProtocol): Unit = {
        _oprot.writeString(authKey_item)
      }
    
    
    }
    
    class Args(
        val authKey: String,
        val _passthroughFields: immutable$Map[Short, TFieldBlob])
      extends ThriftStruct
      with _root_.scala.Product1[String]
      with HasThriftStructCodec3[Args]
      with java.io.Serializable
    {
      import Args._
      def this(
        authKey: String
      ) = this(
        authKey,
        Map.empty
      )
    
      def _1 = authKey
    
    
    
      override def write(_oprot: TProtocol): Unit = {
        Args.validate(this)
        _oprot.writeStructBegin(Struct)
        if (authKey ne null) writeAuthKeyField(authKey, _oprot)
        if (_passthroughFields.nonEmpty) {
          _passthroughFields.values.foreach { _.write(_oprot) }
        }
        _oprot.writeFieldStop()
        _oprot.writeStructEnd()
      }
    
      def copy(
        authKey: String = this.authKey,
        _passthroughFields: immutable$Map[Short, TFieldBlob] = this._passthroughFields
      ): Args =
        new Args(
          authKey,
          _passthroughFields
        )
    
      override def canEqual(other: Any): Boolean = other.isInstanceOf[Args]
    
      private def _equals(x: Args, y: Args): Boolean =
          x.productArity == y.productArity &&
          x.productIterator.sameElements(y.productIterator)
    
      override def equals(other: Any): Boolean =
        canEqual(other) &&
          _equals(this, other.asInstanceOf[Args]) &&
          _passthroughFields == other.asInstanceOf[Args]._passthroughFields
    
      override def hashCode: Int = _root_.scala.runtime.ScalaRunTime._hashCode(this)
    
      override def toString: String = _root_.scala.runtime.ScalaRunTime._toString(this)
    
    
      override def productArity: Int = 1
    
      override def productElement(n: Int): Any = n match {
        case 0 => this.authKey
        case _ => throw new IndexOutOfBoundsException(n.toString)
      }
    
      override def productPrefix: String = "Args"
    
      def _codec: ThriftStructCodec3[Args] = Args
    }

    type SuccessType = transactionService.rpc.AuthInfo
    
    object Result extends ThriftStructCodec3[Result] {
      private val NoPassthroughFields = immutable$Map.empty[Short, TFieldBlob]
      val Struct = new TStruct("authenticate_result")
      val SuccessField = new TField("success", TType.STRUCT, 0)
      val SuccessFieldManifest = implicitly[Manifest[transactionService.rpc.AuthInfo]]
    
      /**
       * Field information in declaration order.
       */
      lazy val fieldInfos: scala.List[ThriftStructFieldInfo] = scala.List[ThriftStructFieldInfo](
        new ThriftStructFieldInfo(
          SuccessField,
          true,
          false,
          SuccessFieldManifest,
          _root_.scala.None,
          _root_.scala.None,
          immutable$Map.empty[String, String],
          immutable$Map.empty[String, String],
          None
        )
      )
    
      lazy val structAnnotations: immutable$Map[String, String] =
        immutable$Map.empty[String, String]
    
      /**
       * Checks that all required fields are non-null.
       */
      def validate(_item: Result): Unit = {
      }
    
      def withoutPassthroughFields(original: Result): Result =
        new Result(
          success =
            {
              val field = original.success
              field.map { field =>
                transactionService.rpc.AuthInfo.withoutPassthroughFields(field)
              }
            }
        )
    
      override def encode(_item: Result, _oproto: TProtocol): Unit = {
        _item.write(_oproto)
      }
    
      override def decode(_iprot: TProtocol): Result = {
        var success: _root_.scala.Option[transactionService.rpc.AuthInfo] = _root_.scala.None
        var _passthroughFields: Builder[(Short, TFieldBlob), immutable$Map[Short, TFieldBlob]] = null
        var _done = false
    
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case 0 =>
                _field.`type` match {
                  case TType.STRUCT =>
                    success = _root_.scala.Some(readSuccessValue(_iprot))
                  case _actualType =>
                    val _expectedType = TType.STRUCT
                    throw new TProtocolException(
                      "Received wrong type for field 'success' (expected=%s, actual=%s).".format(
                        ttypeToString(_expectedType),
                        ttypeToString(_actualType)
                      )
                    )
                }
              case _ =>
                if (_passthroughFields == null)
                  _passthroughFields = immutable$Map.newBuilder[Short, TFieldBlob]
                _passthroughFields += (_field.id -> TFieldBlob.read(_field, _iprot))
            }
            _iprot.readFieldEnd()
          }
        }
        _iprot.readStructEnd()
    
        new Result(
          success,
          if (_passthroughFields == null)
            NoPassthroughFields
          else
            _passthroughFields.result()
        )
      }
    
      def apply(
        success: _root_.scala.Option[transactionService.rpc.AuthInfo] = _root_.scala.None
      ): Result =
        new Result(
          success
        )
    
      def unapply(_item: Result): _root_.scala.Option[_root_.scala.Option[transactionService.rpc.AuthInfo]] = _root_.scala.Some(_item.success)
    
    
      @inline private def readSuccessValue(_iprot: TProtocol): transactionService.rpc.AuthInfo = {
        transactionService.rpc.AuthInfo.decode(_iprot)
      }
    
      @inline private def writeSuccessField(success_item: transactionService.rpc.AuthInfo, _oprot: TProtocol): Unit = {
        _oprot.writeFieldBegin(SuccessField)
        writeSuccessValue(success_item, _oprot)
        _oprot.writeFieldEnd()
      }
    
      @inline private def writeSuccessValue(success_item: transactionService.rpc.AuthInfo, _oprot: TProtocol): Unit = {
        success_item.write(_oprot)
      }
    
    
    }
    
    class Result(
        val success: _root_.scala.Option[transactionService.rpc.AuthInfo],
        val _passthroughFields: immutable$Map[Short, TFieldBlob])
      extends ThriftResponse[transactionService.rpc.AuthInfo] with ThriftStruct
      with _root_.scala.Product1[Option[transactionService.rpc.AuthInfo]]
      with HasThriftStructCodec3[Result]
      with java.io.Serializable
    {
      import Result._
      def this(
        success: _root_.scala.Option[transactionService.rpc.AuthInfo] = _root_.scala.None
      ) = this(
        success,
        Map.empty
      )
    
      def _1 = success
    
      def successField: Option[transactionService.rpc.AuthInfo] = success
      def exceptionFields: Iterable[Option[com.twitter.scrooge.ThriftException]] = Seq()
    
    
      override def write(_oprot: TProtocol): Unit = {
        Result.validate(this)
        _oprot.writeStructBegin(Struct)
        if (success.isDefined) writeSuccessField(success.get, _oprot)
        if (_passthroughFields.nonEmpty) {
          _passthroughFields.values.foreach { _.write(_oprot) }
        }
        _oprot.writeFieldStop()
        _oprot.writeStructEnd()
      }
    
      def copy(
        success: _root_.scala.Option[transactionService.rpc.AuthInfo] = this.success,
        _passthroughFields: immutable$Map[Short, TFieldBlob] = this._passthroughFields
      ): Result =
        new Result(
          success,
          _passthroughFields
        )
    
      override def canEqual(other: Any): Boolean = other.isInstanceOf[Result]
    
      private def _equals(x: Result, y: Result): Boolean =
          x.productArity == y.productArity &&
          x.productIterator.sameElements(y.productIterator)
    
      override def equals(other: Any): Boolean =
        canEqual(other) &&
          _equals(this, other.asInstanceOf[Result]) &&
          _passthroughFields == other.asInstanceOf[Result]._passthroughFields
    
      override def hashCode: Int = _root_.scala.runtime.ScalaRunTime._hashCode(this)
    
      override def toString: String = _root_.scala.runtime.ScalaRunTime._toString(this)
    
    
      override def productArity: Int = 1
    
      override def productElement(n: Int): Any = n match {
        case 0 => this.success
        case _ => throw new IndexOutOfBoundsException(n.toString)
      }
    
      override def productPrefix: String = "Result"
    
      def _codec: ThriftStructCodec3[Result] = Result
    }

    type FunctionType = Nothing
    type ServiceType = Nothing

    def functionToService(f: FunctionType): ServiceType = ???
    def serviceToFunction(svc: ServiceType): FunctionType = ???

    val name = "authenticate"
    val serviceName = "AuthService"
    val argsCodec = Args
    val responseCodec = Result
    val oneway = false
  }

  // Compatibility aliases.
  val authenticate$args = Authenticate.Args
  type authenticate$args = Authenticate.Args

  val authenticate$result = Authenticate.Result
  type authenticate$result = Authenticate.Result

  object IsValid extends com.twitter.scrooge.ThriftMethod {
    
    object Args extends ThriftStructCodec3[Args] {
      private val NoPassthroughFields = immutable$Map.empty[Short, TFieldBlob]
      val Struct = new TStruct("isValid_args")
      val TokenField = new TField("token", TType.I32, 1)
      val TokenFieldManifest = implicitly[Manifest[Int]]
    
      /**
       * Field information in declaration order.
       */
      lazy val fieldInfos: scala.List[ThriftStructFieldInfo] = scala.List[ThriftStructFieldInfo](
        new ThriftStructFieldInfo(
          TokenField,
          false,
          false,
          TokenFieldManifest,
          _root_.scala.None,
          _root_.scala.None,
          immutable$Map.empty[String, String],
          immutable$Map.empty[String, String],
          None
        )
      )
    
      lazy val structAnnotations: immutable$Map[String, String] =
        immutable$Map.empty[String, String]
    
      /**
       * Checks that all required fields are non-null.
       */
      def validate(_item: Args): Unit = {
      }
    
      def withoutPassthroughFields(original: Args): Args =
        new Args(
          token =
            {
              val field = original.token
              field
            }
        )
    
      override def encode(_item: Args, _oproto: TProtocol): Unit = {
        _item.write(_oproto)
      }
    
      override def decode(_iprot: TProtocol): Args = {
        var token: Int = 0
        var _passthroughFields: Builder[(Short, TFieldBlob), immutable$Map[Short, TFieldBlob]] = null
        var _done = false
    
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case 1 =>
                _field.`type` match {
                  case TType.I32 =>
                    token = readTokenValue(_iprot)
                  case _actualType =>
                    val _expectedType = TType.I32
                    throw new TProtocolException(
                      "Received wrong type for field 'token' (expected=%s, actual=%s).".format(
                        ttypeToString(_expectedType),
                        ttypeToString(_actualType)
                      )
                    )
                }
              case _ =>
                if (_passthroughFields == null)
                  _passthroughFields = immutable$Map.newBuilder[Short, TFieldBlob]
                _passthroughFields += (_field.id -> TFieldBlob.read(_field, _iprot))
            }
            _iprot.readFieldEnd()
          }
        }
        _iprot.readStructEnd()
    
        new Args(
          token,
          if (_passthroughFields == null)
            NoPassthroughFields
          else
            _passthroughFields.result()
        )
      }
    
      def apply(
        token: Int
      ): Args =
        new Args(
          token
        )
    
      def unapply(_item: Args): _root_.scala.Option[Int] = _root_.scala.Some(_item.token)
    
    
      @inline private def readTokenValue(_iprot: TProtocol): Int = {
        _iprot.readI32()
      }
    
      @inline private def writeTokenField(token_item: Int, _oprot: TProtocol): Unit = {
        _oprot.writeFieldBegin(TokenField)
        writeTokenValue(token_item, _oprot)
        _oprot.writeFieldEnd()
      }
    
      @inline private def writeTokenValue(token_item: Int, _oprot: TProtocol): Unit = {
        _oprot.writeI32(token_item)
      }
    
    
    }
    
    class Args(
        val token: Int,
        val _passthroughFields: immutable$Map[Short, TFieldBlob])
      extends ThriftStruct
      with _root_.scala.Product1[Int]
      with HasThriftStructCodec3[Args]
      with java.io.Serializable
    {
      import Args._
      def this(
        token: Int
      ) = this(
        token,
        Map.empty
      )
    
      def _1 = token
    
    
    
      override def write(_oprot: TProtocol): Unit = {
        Args.validate(this)
        _oprot.writeStructBegin(Struct)
        writeTokenField(token, _oprot)
        if (_passthroughFields.nonEmpty) {
          _passthroughFields.values.foreach { _.write(_oprot) }
        }
        _oprot.writeFieldStop()
        _oprot.writeStructEnd()
      }
    
      def copy(
        token: Int = this.token,
        _passthroughFields: immutable$Map[Short, TFieldBlob] = this._passthroughFields
      ): Args =
        new Args(
          token,
          _passthroughFields
        )
    
      override def canEqual(other: Any): Boolean = other.isInstanceOf[Args]
    
      private def _equals(x: Args, y: Args): Boolean =
          x.productArity == y.productArity &&
          x.productIterator.sameElements(y.productIterator)
    
      override def equals(other: Any): Boolean =
        canEqual(other) &&
          _equals(this, other.asInstanceOf[Args]) &&
          _passthroughFields == other.asInstanceOf[Args]._passthroughFields
    
      override def hashCode: Int = _root_.scala.runtime.ScalaRunTime._hashCode(this)
    
      override def toString: String = _root_.scala.runtime.ScalaRunTime._toString(this)
    
    
      override def productArity: Int = 1
    
      override def productElement(n: Int): Any = n match {
        case 0 => this.token
        case _ => throw new IndexOutOfBoundsException(n.toString)
      }
    
      override def productPrefix: String = "Args"
    
      def _codec: ThriftStructCodec3[Args] = Args
    }

    type SuccessType = Boolean
    
    object Result extends ThriftStructCodec3[Result] {
      private val NoPassthroughFields = immutable$Map.empty[Short, TFieldBlob]
      val Struct = new TStruct("isValid_result")
      val SuccessField = new TField("success", TType.BOOL, 0)
      val SuccessFieldManifest = implicitly[Manifest[Boolean]]
    
      /**
       * Field information in declaration order.
       */
      lazy val fieldInfos: scala.List[ThriftStructFieldInfo] = scala.List[ThriftStructFieldInfo](
        new ThriftStructFieldInfo(
          SuccessField,
          true,
          false,
          SuccessFieldManifest,
          _root_.scala.None,
          _root_.scala.None,
          immutable$Map.empty[String, String],
          immutable$Map.empty[String, String],
          None
        )
      )
    
      lazy val structAnnotations: immutable$Map[String, String] =
        immutable$Map.empty[String, String]
    
      /**
       * Checks that all required fields are non-null.
       */
      def validate(_item: Result): Unit = {
      }
    
      def withoutPassthroughFields(original: Result): Result =
        new Result(
          success =
            {
              val field = original.success
              field.map { field =>
                field
              }
            }
        )
    
      override def encode(_item: Result, _oproto: TProtocol): Unit = {
        _item.write(_oproto)
      }
    
      override def decode(_iprot: TProtocol): Result = {
        var success: _root_.scala.Option[Boolean] = _root_.scala.None
        var _passthroughFields: Builder[(Short, TFieldBlob), immutable$Map[Short, TFieldBlob]] = null
        var _done = false
    
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case 0 =>
                _field.`type` match {
                  case TType.BOOL =>
                    success = _root_.scala.Some(readSuccessValue(_iprot))
                  case _actualType =>
                    val _expectedType = TType.BOOL
                    throw new TProtocolException(
                      "Received wrong type for field 'success' (expected=%s, actual=%s).".format(
                        ttypeToString(_expectedType),
                        ttypeToString(_actualType)
                      )
                    )
                }
              case _ =>
                if (_passthroughFields == null)
                  _passthroughFields = immutable$Map.newBuilder[Short, TFieldBlob]
                _passthroughFields += (_field.id -> TFieldBlob.read(_field, _iprot))
            }
            _iprot.readFieldEnd()
          }
        }
        _iprot.readStructEnd()
    
        new Result(
          success,
          if (_passthroughFields == null)
            NoPassthroughFields
          else
            _passthroughFields.result()
        )
      }
    
      def apply(
        success: _root_.scala.Option[Boolean] = _root_.scala.None
      ): Result =
        new Result(
          success
        )
    
      def unapply(_item: Result): _root_.scala.Option[_root_.scala.Option[Boolean]] = _root_.scala.Some(_item.success)
    
    
      @inline private def readSuccessValue(_iprot: TProtocol): Boolean = {
        _iprot.readBool()
      }
    
      @inline private def writeSuccessField(success_item: Boolean, _oprot: TProtocol): Unit = {
        _oprot.writeFieldBegin(SuccessField)
        writeSuccessValue(success_item, _oprot)
        _oprot.writeFieldEnd()
      }
    
      @inline private def writeSuccessValue(success_item: Boolean, _oprot: TProtocol): Unit = {
        _oprot.writeBool(success_item)
      }
    
    
    }
    
    class Result(
        val success: _root_.scala.Option[Boolean],
        val _passthroughFields: immutable$Map[Short, TFieldBlob])
      extends ThriftResponse[Boolean] with ThriftStruct
      with _root_.scala.Product1[Option[Boolean]]
      with HasThriftStructCodec3[Result]
      with java.io.Serializable
    {
      import Result._
      def this(
        success: _root_.scala.Option[Boolean] = _root_.scala.None
      ) = this(
        success,
        Map.empty
      )
    
      def _1 = success
    
      def successField: Option[Boolean] = success
      def exceptionFields: Iterable[Option[com.twitter.scrooge.ThriftException]] = Seq()
    
    
      override def write(_oprot: TProtocol): Unit = {
        Result.validate(this)
        _oprot.writeStructBegin(Struct)
        if (success.isDefined) writeSuccessField(success.get, _oprot)
        if (_passthroughFields.nonEmpty) {
          _passthroughFields.values.foreach { _.write(_oprot) }
        }
        _oprot.writeFieldStop()
        _oprot.writeStructEnd()
      }
    
      def copy(
        success: _root_.scala.Option[Boolean] = this.success,
        _passthroughFields: immutable$Map[Short, TFieldBlob] = this._passthroughFields
      ): Result =
        new Result(
          success,
          _passthroughFields
        )
    
      override def canEqual(other: Any): Boolean = other.isInstanceOf[Result]
    
      private def _equals(x: Result, y: Result): Boolean =
          x.productArity == y.productArity &&
          x.productIterator.sameElements(y.productIterator)
    
      override def equals(other: Any): Boolean =
        canEqual(other) &&
          _equals(this, other.asInstanceOf[Result]) &&
          _passthroughFields == other.asInstanceOf[Result]._passthroughFields
    
      override def hashCode: Int = _root_.scala.runtime.ScalaRunTime._hashCode(this)
    
      override def toString: String = _root_.scala.runtime.ScalaRunTime._toString(this)
    
    
      override def productArity: Int = 1
    
      override def productElement(n: Int): Any = n match {
        case 0 => this.success
        case _ => throw new IndexOutOfBoundsException(n.toString)
      }
    
      override def productPrefix: String = "Result"
    
      def _codec: ThriftStructCodec3[Result] = Result
    }

    type FunctionType = Nothing
    type ServiceType = Nothing

    def functionToService(f: FunctionType): ServiceType = ???
    def serviceToFunction(svc: ServiceType): FunctionType = ???

    val name = "isValid"
    val serviceName = "AuthService"
    val argsCodec = Args
    val responseCodec = Result
    val oneway = false
  }

  // Compatibility aliases.
  val isValid$args = IsValid.Args
  type isValid$args = IsValid.Args

  val isValid$result = IsValid.Result
  type isValid$result = IsValid.Result


}