/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.server.logs;

import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.function.Consumer;

final public class LogContext implements ExchangeCompletionListener {

	private final Consumer<LogContext> logger;

	HttpServerExchange exchange;
	HeaderMap requestHeaders;

	InetSocketAddress destinationAddress;
	InetSocketAddress sourceAddress;

	long nanoStartTime;
	long nanoEndTime;
	LocalDateTime logDateTime;

	LogContext(final Consumer<LogContext> logger) {
		this.logger = logger;
	}

	@Override
	final public void exchangeEvent(final HttpServerExchange exchange, final NextListener nextListener) {
		try {
			this.exchange = exchange;
			this.requestHeaders = exchange.getRequestHeaders();
			this.destinationAddress = exchange.getDestinationAddress();
			this.sourceAddress = exchange.getSourceAddress();
			this.nanoStartTime = exchange.getRequestStartTime();
			this.nanoEndTime = System.nanoTime();
			this.logDateTime = LocalDateTime.now();
			logger.accept(this);
		} finally {
			nextListener.proceed();
		}
	}

}