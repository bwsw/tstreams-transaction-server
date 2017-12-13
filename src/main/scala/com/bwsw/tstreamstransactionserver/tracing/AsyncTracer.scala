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

import java.util.concurrent.Executors

import brave.{Span, Tracing}
import com.bwsw.tstreamstransactionserver.netty.RequestMessage
import zipkin2.reporter.AsyncReporter
import zipkin2.reporter.okhttp3.OkHttpSender

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * @author Pavel Tomskikh
  */
class AsyncTracer(sender: OkHttpSender, reporter: AsyncReporter[zipkin2.Span], serviceName: String) {

  private implicit val executionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))
  private val tracing = Tracing.newBuilder()
    .spanReporter(reporter)
    .localServiceName(serviceName)
    .build()
  private val tracer = tracing.tracer()
  private val requestSpans = TrieMap.empty[Long, Span]
  private val spans = TrieMap.empty[Span, TrieMap[String, Span]]


  def startRequest(request: RequestMessage, timestamp: Long): Unit =
    Future(startRequest(request.id, timestamp))

  def finishRequest(request: RequestMessage, timestamp: Long): Unit = Future {
    requestSpans.get(request.id)
      .filter { span => spans.get(span).forall(_.isEmpty) }
      .foreach(_.finish(timestamp))
  }

  def invoke(request: RequestMessage, name: => String, timestamp: Long, stackTrace: Array[StackTraceElement]): Unit = Future {
    val requestSpan = requestSpans.getOrElse(request.id, startRequest(request.id, timestamp))

    val spansForRequest = spans(requestSpan)
    val parentSpan = stackTrace
      .collectFirst { case s if spansForRequest.contains(s.toString) => spansForRequest(s.toString) }
      .getOrElse(requestSpan)

    val span = tracer.newChild(parentSpan.context()).name(name).start(timestamp)
    spansForRequest += name -> span
  }

  def finish(request: RequestMessage, name: => String, timestamp: Long): Unit = Future {
    requestSpans.get(request.id).foreach(requestSpan =>
      spans.get(requestSpan)
        .foreach(_.remove(name)
          .foreach(_.finish(timestamp))))
  }

  def close(): Unit =
    Await.result(Future(tracing.close()), Duration.Inf)


  private def startRequest(requestId: Long, timestamp: Long): Span = {
    val span = tracer.newTrace().name(s"request-$requestId").start(timestamp)
    requestSpans += requestId -> span
    spans += span -> TrieMap.empty

    span
  }
}
