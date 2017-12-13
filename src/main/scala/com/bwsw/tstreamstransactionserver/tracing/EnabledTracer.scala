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

import brave.{Span, Tracing}
import com.bwsw.tstreamstransactionserver.netty.Protocol.OpenTransaction
import com.bwsw.tstreamstransactionserver.netty.RequestMessage
import zipkin2.reporter.AsyncReporter
import zipkin2.reporter.okhttp3.OkHttpSender

import scala.collection.concurrent.TrieMap

/** Performs tracing
  *
  * @param endpoint OpenZipkin server address
  * @author Pavel Tomskikh
  */
class EnabledTracer(endpoint: String) extends Tracer {
  private val sender = OkHttpSender.create(s"http://$endpoint/api/v2/spans")
  private val reporter = AsyncReporter.create(sender)
  private val tracers = Set(OpenTransaction).map(method =>
    method.methodID -> new EnabledTracer.PerMethodTracer(sender, reporter, s"TTS-${method.name}")).toMap


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
}


object EnabledTracer {

  private val withTracingMethodPath = (classOf[EnabledTracer].getName, "withTracing")

  private def getStackTraceElement: StackTraceElement = {
    val stackTrace = Thread.currentThread().getStackTrace
    val index = stackTrace.indexWhere(s => (s.getClassName, s.getMethodName) == withTracingMethodPath)

    stackTrace(index + 1)
  }


  private class PerMethodTracer(sender: OkHttpSender, reporter: AsyncReporter[zipkin2.Span], serviceName: String) extends Tracer {
    private val tracing = Tracing.newBuilder()
      .spanReporter(reporter)
      .localServiceName(serviceName)
      .build()
    private val tracer = tracing.tracer()
    private val requestSpans = TrieMap.empty[Long, Span]
    private val spans = TrieMap.empty[Span, TrieMap[String, Span]]

    override def withTracing[T](request: RequestMessage, name: => String = EnabledTracer.getStackTraceElement.toString)
                               (traced: => T): T = {
      val methodName = name
      invoke(request.id, methodName)
      val result = traced
      finish(request.id, methodName)

      result
    }

    override def startRequest(request: RequestMessage): Unit = startRequest(request.id)

    override def finishRequest(request: RequestMessage): Unit = {
      requestSpans.get(request.id).filter(span =>
        spans.get(span).forall(_.isEmpty))
        .foreach(_.finish())
    }

    override def close(): Unit = tracing.close()

    /** Reports request handling started
      *
      * @param requestId request id
      * @return created span
      */
    private def startRequest(requestId: Long): Span = {
      val span = tracer.newTrace().name(s"request-$requestId").start()
      requestSpans += requestId -> span
      spans += span -> TrieMap.empty

      span
    }


    private def invoke(requestId: Long, name: String): Span = {
      val requestSpan = requestSpans.getOrElse(requestId, startRequest(requestId))
      val spansForRequest = spans(requestSpan)
      val parentSpan = Thread.currentThread().getStackTrace
        .collectFirst { case s if spansForRequest.contains(s.toString) => spansForRequest(s.toString) }
        .getOrElse(requestSpan)

      val span = tracer.newChild(parentSpan.context()).name(name.toString).start()
      spansForRequest += name -> span

      span
    }

    private def finish(requestId: Long, name: String): Unit = {
      requestSpans.get(requestId).foreach(requestSpan =>
        spans.get(requestSpan)
          .foreach(_.remove(name)
            .foreach(_.finish())))
    }
  }

}