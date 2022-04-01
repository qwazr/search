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

import com.qwazr.server.ConnectorStatisticsMXBean;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.concurrent.atomic.AtomicInteger;

final public class LogMetricsHandler implements HttpHandler, ConnectorStatisticsMXBean {

	private final String address;
	private final int port;
	private final String name;
	private final HttpHandler next;
	private final AccessLogger accessLogger;
	private final AtomicInteger active;
	private final AtomicInteger maxActive;

	public LogMetricsHandler(final HttpHandler next, final String address, final int port, final String name,
			final AccessLogger accessLogger) {
		this.next = next;
		this.active = new AtomicInteger();
		this.maxActive = new AtomicInteger();
		this.address = address;
		this.port = port;
		this.name = name;
		this.accessLogger = accessLogger;
	}

	@Override
	final public void handleRequest(final HttpServerExchange exchange) throws Exception {
		if (accessLogger != null)
			exchange.addExchangeCompleteListener(new LogContext(accessLogger));
		final int act = active.incrementAndGet();
		if (act > maxActive.get())
			maxActive.set(act);
		try {
			next.handleRequest(exchange);
		} finally {
			active.decrementAndGet();
		}
	}

	@Override
	final public int getActiveCount() {
		return active.get();
	}

	@Override
	final public int getMaxActiveCount() {
		return maxActive.get();
	}

	@Override
	final public String getAddress() {
		return this.address;
	}

	@Override
	final public int getPort() {
		return this.port;
	}

	@Override
	final public String getName() {
		return this.name;
	}

	@Override
	final public void reset() {
		maxActive.set(0);
	}

}
