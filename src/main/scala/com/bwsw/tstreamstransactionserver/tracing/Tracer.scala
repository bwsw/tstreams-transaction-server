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

import java.util.UUID

import com.bwsw.tstreamstransactionserver.netty.RequestMessage
import com.bwsw.tstreamstransactionserver.options.CommonOptions.TracingOptions

/** Provides methods to tracing requests
  *
  * @author Pavel Tomskikh
  */
trait Tracer {

  /** Traces some method if tracing enabled and request should be traced
    *
    * @param request request
    * @param name    span name
    * @param traced  method to trace
    * @tparam T traced method return type
    * @return traced method result
    */
  def withTracing[T](request: RequestMessage, name: => String = UUID.randomUUID().toString)
                    (traced: => T): T

  /** Reports request handling started if tracing enabled and request should be traced
    *
    * @param request request
    */
  def startRequest(request: RequestMessage): Unit = {}

  /** Reports request handling started if tracing enabled and request should be traced
    *
    * @param request request
    */
  def finishRequest(request: RequestMessage): Unit = {}


  def close(): Unit = {}
}


object Tracer {
  private var _tracer: Tracer = DisabledTracer
  private var initialized = false

  /** Initializes tracer
    *
    * @param options tracer options
    * @throws IllegalStateException if tracer already initialized
    */
  def init(options: TracingOptions): Unit = {
    if (initialized) throw new IllegalStateException("Tracer already initialized")
    if (options.enabled) {
      _tracer = new EnabledTracer(options.endpoint)
      initialized = true
    }
  }

  def tracer: Tracer = _tracer
}