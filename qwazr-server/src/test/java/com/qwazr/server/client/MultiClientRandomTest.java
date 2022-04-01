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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class MultiClientRandomTest extends MultiClientTest {

	static ExecutorService executorService;

	private final static Logger LOGGER = LoggerUtils.getLogger(MultiClientRandomTest.class);

	@BeforeClass
	public static void setup() {
		executorService = Executors.newCachedThreadPool();
	}

	@AfterClass
	public static void cleanup() {
		executorService.shutdown();
	}

	Integer firstRandom(ClientExample[] clients) {
		final MultiClient<ClientExample> multiClient = new MultiClient<>(clients, executorService);
		return multiClient.firstRandomSuccess(ClientExample::action, LOGGER);
	}

	void firstRandomTest(ClientExample[] clients, boolean isResult, Integer exceptionCount) {

		Assert.assertNotNull(clients);
		Assert.assertTrue(clients.length > 0);

		final MultiClient<ClientExample> multiClient = new MultiClient<>(clients, executorService);
		final List<WebApplicationException> exceptions = new ArrayList<>();
		final Integer result = multiClient.firstRandomSuccess(ClientExample::action, exceptions::add);
		if (exceptionCount != null)
			Assert.assertEquals((int) exceptionCount, exceptions.size());

		ClientExample clientFound = null;
		int successCount = 0;
		for (ClientExample client : clients) {
			if (result != null && client.id == result)
				clientFound = client;
			if (client.actionCounter.get() > 1)
				Assert.fail("Wrong counter: " + client.actionCounter.get());
			if (client.actionCounter.get() == 1 && client instanceof ClientExample.SuccessClient)
				successCount++;

		}
		if (isResult) {
			Assert.assertNotNull(clientFound);
			Assert.assertEquals(1, clientFound.actionCounter.get(), 0);
			Assert.assertEquals(ClientExample.SuccessClient.class, clientFound.getClass());
			Assert.assertEquals(1, successCount, 0);
		} else {
			Assert.assertNull(clientFound);
			Assert.assertEquals(0, successCount);
		}
	}

	@Test
	public void firstRandomTestsWithResult() {
		firstRandomTest(panel(Type.success), true, null);
		firstRandomTest(panel(Type.success, Type.success), true, null);
		firstRandomTest(panel(Type.success, Type.error), true, null);
		firstRandomTest(panel(Type.error, Type.success), true, null);
		firstRandomTest(panel(Type.error, Type.success, Type.error), true, null);
		firstRandomTest(panel(Type.success, Type.error, Type.error), true, null);
		firstRandomTest(panel(Type.error, Type.success, Type.error, Type.success), true, null);
		firstRandomTest(panel(Type.error, Type.error, Type.success, Type.success), true, null);
		firstRandomTest(panel(Type.success, Type.success, Type.error, Type.error), true, null);
	}

	void firstRandomTestEmpty(ClientExample[] clients) {

		final Integer result = firstRandom(clients);
		Assert.assertNull(result);
		if (clients != null)
			for (ClientExample client : clients)
				Assert.assertEquals(1, client.actionCounter.get(), 0);
	}

	@Test
	public void firstRandomTestsNoResult() {
		firstRandomTestEmpty(null);
		firstRandomTestEmpty(panel());
		firstRandomTest(panel(Type.error), false, 1);
		firstRandomTest(panel(Type.error, Type.error), false, 2);
		firstRandomTest(panel(Type.error, Type.error, Type.error), false, 3);
	}

	@Test(expected = MultiWebApplicationException.class)
	public void firstRandomFailError() {
		firstRandom(panel(Type.error));
	}

	@Test(expected = MultiWebApplicationException.class)
	public void firstRandomFailErrorError() {
		firstRandom(panel(Type.error, Type.error));
	}

}
