/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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
package com.qwazr.server.client;

import com.qwazr.utils.RandomUtils;

import javax.ws.rs.WebApplicationException;
import java.util.concurrent.atomic.AtomicInteger;

abstract class ClientExample {

	final AtomicInteger actionCounter = new AtomicInteger();
	final int id;

	ClientExample(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "ID " + id;
	}

	abstract Integer action() throws Exception;

	static class ErrorClient extends ClientExample {

		Exception exception;

		ErrorClient(int id) {
			super(id);
		}

		@Override
		public Integer action() throws Exception {
			actionCounter.incrementAndGet();
			switch (RandomUtils.nextInt(0, 3)) {
			case 0:
				exception = new WebApplicationException("I failed");
				break;
			case 1:
				exception = new RuntimeException("I failed");
				break;
			default:
			case 2:
				exception = new Exception("I failed");
				break;
			}
			throw exception;
		}
	}

	static class SuccessClient extends ClientExample {

		SuccessClient(int id) {
			super(id);
		}

		@Override
		public Integer action() {
			actionCounter.incrementAndGet();
			return id;
		}
	}

}
