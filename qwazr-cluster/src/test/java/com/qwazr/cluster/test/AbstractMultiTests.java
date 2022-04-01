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
package com.qwazr.cluster.test;

import com.qwazr.cluster.ClusterServiceInterface;
import com.qwazr.utils.LoggerUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.ws.rs.WebApplicationException;
import java.net.URISyntaxException;
import java.util.SortedSet;
import java.util.logging.Logger;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractMultiTests {

	private static final Logger LOGGER = LoggerUtils.getLogger(AbstractMultiTests.class);

	static ClusterTestServer master1;
	static ClusterTestServer master2;
	static ClusterTestServer front1;
	static ClusterTestServer front2;
	static ClusterTestServer front3;

	final static String GROUP_MASTER = "master";
	final static String GROUP_FRONT = "front";

	protected abstract void startServers() throws Exception;

	@Test
	public void test00_startInstances() throws Exception {
		startServers();
		Assert.assertNotNull(master1);
		Assert.assertNotNull(master2);
		Assert.assertNotNull(front1);
		Assert.assertNotNull(front2);
		Assert.assertNotNull(front3);
	}

	@Test
	public void test10_findClusters() throws InterruptedException {
		final long end = System.currentTimeMillis() + 120 * 1000 * 2;
		while (System.currentTimeMillis() < end) {
			int found = 0;
			try {
				for (ClusterTestServer server : ClusterTestServer.servers) {
					final SortedSet<String> founds = server.client.getActiveNodesByService("cluster", null);
					if (founds.containsAll(ClusterTestServer.serverAdresses))
						found++;
					else
						LOGGER.warning(() -> "Failed on " + server.address + " => " + founds.toString());
				}
				if (found == ClusterTestServer.servers.size())
					return;
			} catch (WebApplicationException e) {
			}
			Thread.sleep(10000);
		}
		Assert.fail();
	}

	@Test
	public void test70_serviceBuilder() throws URISyntaxException {
		ClusterServiceInterface leader;
		ClusterServiceInterface master;
		ClusterServiceInterface front;
		ClusterServiceInterface dummy;
		ClusterServiceInterface random;
		for (ClusterTestServer server : ClusterTestServer.servers) {

			leader = server.serviceBuilder.getLeader(null);
			Assert.assertNotNull(leader);

			master = server.serviceBuilder.getLeader("master");
			Assert.assertNotNull(master);
			front = server.serviceBuilder.getLeader("front");
			Assert.assertNotNull(front);
			Assert.assertNotEquals(master, front);

			dummy = server.serviceBuilder.getLeader("dummy");
			Assert.assertNull(dummy);

			random = server.serviceBuilder.getRandom(null);
			Assert.assertNotNull(random);
			random = server.serviceBuilder.getRandom("master");
			Assert.assertNotNull(random);
			random = server.serviceBuilder.getRandom("front");
			Assert.assertNotNull(random);
			dummy = server.serviceBuilder.getRandom("dummy");
			Assert.assertNull(dummy);
		}
	}

	@Test(expected = NotImplementedException.class)
	public void test71_ServiceBuilderNotImplemented() throws URISyntaxException {
		ClusterTestServer.servers.get(0).serviceBuilder.getActive(null);
	}

	@AfterClass
	public static void after() {
		ClusterTestServer.stopServers();
	}
}
