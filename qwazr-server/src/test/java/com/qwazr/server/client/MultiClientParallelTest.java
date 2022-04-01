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

import com.qwazr.utils.LoggerUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class MultiClientParallelTest extends MultiClientTest {

	private final static Logger LOGGER = LoggerUtils.getLogger(MultiClientParallelTest.class);

	static ExecutorService executor;

	@Before
	public void setup() {
		executor = Executors.newCachedThreadPool();
	}

	@After
	public void cleanup() {
		executor.shutdown();
	}

	void parallel(ClientExample[] clients, int resultCount, int exceptionCount) {
		final MultiClient<ClientExample> multiClient = new MultiClient<>(clients, executor);
		final List<WebApplicationException> exceptions = new ArrayList<>();
		final List<Integer> results = multiClient.forEachParallel(ClientExample::action, exceptions::add);

		Assert.assertEquals(resultCount, results.size());
		Assert.assertEquals(exceptionCount, exceptions.size());

		Assert.assertEquals(clients == null ? 0 : clients.length, results.size() + exceptions.size());
		if (clients == null || clients.length == 0) {
			Assert.assertTrue(results.isEmpty());
			return;
		}

		for (ClientExample client : clients) {
			Assert.assertEquals(1, client.actionCounter.get(), 0);
			if (client instanceof ClientExample.ErrorClient) {
				Assert.assertFalse(results.contains(client.id));
				Assert.assertNotNull(((ClientExample.ErrorClient) client).exception);

				final Exception expectionToFind = ((ClientExample.ErrorClient) client).exception;
				boolean expectionFound = false;
				for (Exception exception : exceptions)
					if (exception == expectionToFind || exception.getCause() == expectionToFind)
						expectionFound = true;
				Assert.assertTrue("Exception not found", expectionFound);
			}
			if (client instanceof ClientExample.SuccessClient)
				Assert.assertTrue(results.contains(client.id));
		}

	}

	@Test
	public void parallelTestsWithResults() {
		parallel(panel(Type.success), 1, 0);
		parallel(panel(Type.success, Type.success), 2, 0);

		parallel(panel(Type.success), 1, 0);
		parallel(panel(Type.success, Type.success), 2, 0);
		parallel(panel(Type.success, Type.success, Type.success), 3, 0);
	}

	@Test
	public void parallelTestsWithOnlyErrors() {
		parallel(panel(Type.error), 0, 1);
		parallel(panel(Type.error, Type.error), 0, 2);
		parallel(panel(Type.error, Type.error, Type.error), 0, 3);

	}

	@Test
	public void parallelTestsNoClients() {
		parallel(null, 0, 0);
		parallel(panel(), 0, 0);
	}

	@Test
	public void parallelTestsMixSuccessError() {
		parallel(panel(Type.success, Type.error), 1, 1);
		parallel(panel(Type.error, Type.success), 1, 1);
		parallel(panel(Type.error, Type.success, Type.error), 1, 2);
		parallel(panel(Type.success, Type.error, Type.error), 1, 2);
		parallel(panel(Type.error, Type.success, Type.error, Type.success), 2, 2);
		parallel(panel(Type.error, Type.error, Type.success, Type.success), 2, 2);
		parallel(panel(Type.success, Type.success, Type.error, Type.error), 2, 2);
		parallel(panel(Type.error, Type.success, Type.error, Type.error), 1, 3);
	}

	void parallelSuccess(ClientExample[] clients, int resultCount) {
		final MultiClient<ClientExample> multiClient = new MultiClient<>(clients, executor);
		List<Integer> results = multiClient.forEachParallel(ClientExample::action, LOGGER);
		Assert.assertNotNull(results);
		Assert.assertEquals(resultCount, results.size());
	}

	@Test
	public void parallelTestSuccess() {
		parallelSuccess(null, 0);
		parallelSuccess(panel(), 0);
		parallelSuccess(panel(Type.success), 1);
		parallelSuccess(panel(Type.success, Type.success), 2);
		parallelSuccess(panel(Type.success, Type.success, Type.success), 3);
	}

	void parallelFail(ClientExample[] clients, int exceptionCount) {
		final MultiClient<ClientExample> multiClient = new MultiClient<>(clients, executor);
		try {
			multiClient.forEachParallel(ClientExample::action, LOGGER);
			Assert.fail("MultiWebApplicationException not thrown");
		} catch (MultiWebApplicationException e) {
			Assert.assertNotNull(e.getCauses());
			Assert.assertEquals(exceptionCount, e.getCauses().size());
		}
	}

	@Test
	public void parallelTestFail() {
		parallelFail(panel(Type.error), 1);
		parallelFail(panel(Type.error, Type.error), 2);
		parallelFail(panel(Type.error, Type.error, Type.error), 3);
		parallelFail(panel(Type.error, Type.success), 1);
		parallelFail(panel(Type.error, Type.success, Type.error), 2);
		parallelFail(panel(Type.success, Type.error, Type.success), 1);
	}
}
