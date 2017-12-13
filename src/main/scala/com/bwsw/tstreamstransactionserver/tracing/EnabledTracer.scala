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

package com.bwsw.tstreamstransactionserver.tracing

import com.bwsw.tstreamstransactionserver.netty.Protocol.{OpenTransaction, PutTransactionData}
import com.bwsw.tstreamstransactionserver.netty.RequestMessage
import zipkin2.reporter.AsyncReporter
import zipkin2.reporter.okhttp3.OkHttpSender

/** Performs tracing
  *
  * @param endpoint OpenZipkin server address
  * @author Pavel Tomskikh
  */
class EnabledTracer(endpoint: String, serviceName: String) extends Tracer {
  private val sender = OkHttpSender.create(s"http://$endpoint/api/v2/spans")
  private val reporter = AsyncReporter.create(sender)
  private val tracers = Set(
    OpenTransaction,
    PutTransactionData)
    .map(method =>
      method.methodID -> new EnabledTracer.PerMethodTracer(sender, reporter, s"$serviceName-${method.name}"))
    .toMap


  override def withTracing[T](request: RequestMessage, name: => String = EnabledTracer.getStackTraceElement.toString)
                             (traced: => T): T = {
    tracers.get(request.methodId) match {
      case Some(tracer) => tracer.withTracing(request, name)(traced)
      case None => traced
    }
  }

  override def startRequest(request: RequestMessage): Unit =
    tracers.get(request.methodId).foreach(_.startRequest(request))

  override def finishRequest(request: RequestMessage): Unit =
    tracers.get(request.methodId).foreach(_.finishRequest(request))


  override def close(): Unit = {
    tracers.values.foreach(_.close())
    reporter.close()
    sender.close()
  }

  override def invoke(request: RequestMessage, name: => String): Unit =
    tracers.get(request.methodId).foreach(_.invoke(request, name))

  override def finish(request: RequestMessage, name: => String): Unit =
    tracers.get(request.methodId).foreach(_.finish(request, name))
}


object EnabledTracer {

  private val withTracingMethodPath = (classOf[EnabledTracer].getName, "withTracing")

  private def getStackTraceElement: StackTraceElement = {
    val stackTrace = Thread.currentThread().getStackTrace
    val index = stackTrace.indexWhere(s => (s.getClassName, s.getMethodName) == withTracingMethodPath)

    stackTrace(index + 1)
  }


  private class PerMethodTracer(sender: OkHttpSender, reporter: AsyncReporter[zipkin2.Span], serviceName: String) extends Tracer {
    private val asyncTracer = new AsyncTracer(sender, reporter, serviceName)

    override def withTracing[T](request: RequestMessage, name: => String = EnabledTracer.getStackTraceElement.toString)
                               (traced: => T): T = {
      val methodName = name
      invoke(request, methodName)
      val result = traced
      finish(request, methodName)

      result
    }

    override def startRequest(request: RequestMessage): Unit = {
      val timestamp = Clock.currentTimeMicroseconds
      asyncTracer.startRequest(request, timestamp)
    }

    override def finishRequest(request: RequestMessage): Unit = {
      val timestamp = Clock.currentTimeMicroseconds
      asyncTracer.finishRequest(request, timestamp)
    }

    override def close(): Unit =
      asyncTracer.close()

    override def invoke(request: RequestMessage, name: => String): Unit = {
      val timestamp = Clock.currentTimeMicroseconds
      val stackTrace = Thread.currentThread().getStackTrace
      asyncTracer.invoke(request, name, timestamp, stackTrace)
    }

    override def finish(request: RequestMessage, name: => String): Unit = {
      val timestamp = Clock.currentTimeMicroseconds
      asyncTracer.finish(request, name, timestamp)
    }
  }

  object Clock {
    private val start = System.currentTimeMillis() * 1000 - System.nanoTime() / 1000

    def currentTimeMicroseconds: Long = start + System.nanoTime() / 1000
  }

}