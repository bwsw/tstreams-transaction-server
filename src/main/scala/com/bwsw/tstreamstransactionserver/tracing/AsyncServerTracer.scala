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

import java.net.InetAddress

import com.bwsw.tstreamstransactionserver.netty.RequestMessage
import zipkin2.Span

import scala.collection.concurrent.TrieMap
import scala.util.Random

/** Asynchronous tracer for TTS server
  *
  * @param zipkinAddress OpenZipkin address
  * @param serviceName   TTS server name
  * @param serverAddress TTS server address
  * @param serverPort    TTS server port
  * @author Pavel Tomskikh
  */
class AsyncServerTracer(zipkinAddress: String,
                        serviceName: String,
                        serverAddress: InetAddress = InetAddress.getLocalHost,
                        serverPort: Int = 0)
  extends AsyncTracer(
    zipkinAddress,
    serviceName,
    serverAddress,
    serverPort,
    "server-tracer")
    with ServerTracer {

  private val rootSpans = TrieMap.empty[Long, SpanBuilderWrapper]
  private val spans: scala.collection.concurrent.Map[(Long, String), SpanBuilderWrapper] =
    TrieMap.empty[(Long, String), SpanBuilderWrapper]
  private val className = getClass.getName

  override def withTracing[T](request: RequestMessage,
                              maybeName: Option[String],
                              parentName: Option[String])
                             (traced: => T): T = {

    if (request.tracingInfo.isDefined) {
      val name = invoke(request, maybeName, parentName)
      val result = traced
      name.foreach(s => finish(request, s))

      result
    }
    else traced
  }

  override def invoke(request: RequestMessage,
                      maybeName: Option[String],
                      parentName: Option[String]): Option[String] = {
    request.tracingInfo.map { _ =>
      val stackTrace = Thread.currentThread().getStackTrace
      val name = maybeName.getOrElse {
        val index = stackTrace.indexWhere(s => s.getClassName == className && s.getMethodName == "withTracing") + 1
        stackTrace(index).toString
      }
      val timestamp = Clock.currentTimeMicroseconds
      events.put(Invoke(request, name, parentName, stackTrace, timestamp))

      name
    }
  }

  override def finish(request: RequestMessage, name: String): Unit = {
    if (request.tracingInfo.isDefined) {
      val timestamp = Clock.currentTimeMicroseconds

      events.put(Finish(request, name, timestamp))
    }
  }

  override def serverSend(request: RequestMessage): Unit = {
    if (request.tracingInfo.isDefined) {
      val timestamp = Clock.currentTimeMicroseconds

      events.put(ServerSend(request, timestamp))
    }
  }

  override def serverReceive(request: RequestMessage): Unit = {
    if (request.tracingInfo.isDefined) {
      val timestamp = Clock.currentTimeMicroseconds

      events.put(ServerReceive(request, timestamp))
    }
  }


  private def withEndpoint(builder: Span.Builder) =
    builder.kind(Span.Kind.SERVER).localEndpoint(endpoint)


  case class SpanBuilderWrapper(traceId: String,
                                timestamp: Long,
                                name: String,
                                id: String = Random.nextLong().toHexString,
                                builder: Span.Builder = Span.newBuilder()) {
    builder
      .traceId(traceId)
      .id(id)
      .timestamp(timestamp)
      .name(name)

    def report(endTimestamp: Long): Unit =
      reporter.report(builder.duration(endTimestamp - timestamp).build())
  }


  private def findParent(request: RequestMessage, stackTrace: Array[StackTraceElement]): Option[String] = {
    stackTrace.collectFirst {
      case s if spans.keySet.filter(_._1 == request.id).map(_._2).contains(s.toString) => s.toString
    }
  }


  case class ServerSend(request: RequestMessage, timestamp: Long) extends AsyncTracer.Event {
    override def handle(): Unit = {
      rootSpans.remove(request.id).foreach(_.report(timestamp))

      val span = withEndpoint(request.tracingInfo.get.responseSpanBuilder)
        .name("response")
        .addAnnotation(timestamp, "ss")
        .build()

      reporter.report(span)
    }
  }

  case class ServerReceive(request: RequestMessage, timestamp: Long) extends AsyncTracer.Event {
    override def handle(): Unit = {
      val tracingInfo = request.tracingInfo.get
      val span = withEndpoint(tracingInfo.requestSpanBuilder)
        .addAnnotation(timestamp, "sr")
        .build()

      reporter.report(span)

      val rootSpan = SpanBuilderWrapper(
        traceId = tracingInfo.traceId.toHexString,
        timestamp = timestamp,
        name = tracedMethods.getOrElse(request.methodId, "request") + s" (${request.id})")

      rootSpans += request.id -> rootSpan
    }
  }

  case class Invoke(request: RequestMessage,
                    name: String,
                    parentName: Option[String],
                    stackTrace: Array[StackTraceElement],
                    timestamp: Long) extends AsyncTracer.Event {
    override def handle(): Unit = {
      val parentSpan = parentName.orElse(findParent(request, stackTrace))
        .map(n => spans.get((request.id, n)))
        .getOrElse(rootSpans.get(request.id))

      val span = SpanBuilderWrapper(
        traceId = request.tracingInfo.get.traceId.toHexString,
        timestamp = timestamp,
        name = name)

      parentSpan.map(_.id).foreach(span.builder.parentId)

      spans += (request.id, name) -> span
    }
  }

  case class Finish(request: RequestMessage, name: String, timestamp: Long) extends AsyncTracer.Event {
    override def handle(): Unit =
      spans.remove((request.id, name)).foreach(_.report(timestamp))
  }

}