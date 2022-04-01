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

import com.qwazr.cluster.ClusterManager;
import com.qwazr.cluster.ClusterServer;
import com.qwazr.cluster.ClusterServiceBuilder;
import com.qwazr.cluster.ClusterServiceInterface;
import com.qwazr.cluster.ClusterServiceStatusJson;
import com.qwazr.cluster.ClusterStatusJson;
import com.qwazr.server.RemoteService;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AllTest {

	private static final Logger logger = LoggerUtils.getLogger(AllTest.class);

	private final static String[] GROUPS = { "group1", "group2" };

	private final static String ADDRESS = "http://localhost:9091";

	private static ClusterServiceInterface client;
	private static ClusterServiceBuilder serviceBuilder;
	private static ClusterManager clusterManager;

	@Test
	public void test00_startServer() throws Exception {
		ClusterServer.main("--LISTEN_ADDR=localhost", "--PUBLIC_ADDR=localhost", "--WEBSERVICE_PORT:9091",
				"--QWAZR_GROUPS=" + StringUtils.join(GROUPS, ","), "--QWAZR_MASTERS=localhost:9091");
		clusterManager = ClusterServer.getInstance().getClusterManager();
		Assert.assertNotNull(clusterManager);
		serviceBuilder = ClusterServer.getInstance().getServiceBuilder();
		Assert.assertNotNull(serviceBuilder);
		client = serviceBuilder.remote(RemoteService.of(ADDRESS).build());
	}

	/**
	 * We wait 30 seconds until the service is visible as active.
	 *
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	@Test
	public void test15_check_service_activation() throws URISyntaxException, InterruptedException {
		int count = 0;
		int activated_services_count = 0;
		int activated_groups_count = 0;
		while (count++ < 20) {
			activated_services_count = 0;
			activated_groups_count = 0;

			logger.info("Check service activation: " + count);
			ClusterServiceStatusJson result = client.getServiceStatus("cluster", null);
			if (result.active != null && result.active.contains(ADDRESS))
				activated_services_count++;
			for (String group : GROUPS) {
				logger.info("Check group activation: " + count);
				ClusterServiceStatusJson resultGroup = client.getServiceStatus("cluster", group);
				if (resultGroup.active != null && resultGroup.active.contains(ADDRESS))
					activated_groups_count++;
			}
			if (activated_services_count == 1 && activated_groups_count == GROUPS.length) {
				logger.info("Check activation succeed");
				break;
			}
			Thread.sleep(5000);
		}
		Assert.assertEquals(1, activated_services_count);
		Assert.assertEquals(GROUPS.length, activated_groups_count);
	}

	@Test
	public void test20_get_node_list() throws URISyntaxException {
		Set<String> result = client.getNodes();
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
	}

	@Test
	public void test22_get_active_list_by_service() throws URISyntaxException {
		final Set<String> result = client.getActiveNodesByService("cluster", null);
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(ADDRESS, result.iterator().next());
		for (String group : GROUPS) {
			Set<String> resultGroup = client.getActiveNodesByService("cluster", group);
			Assert.assertNotNull(resultGroup);
			Assert.assertEquals(1, resultGroup.size());
			Assert.assertEquals(ADDRESS, resultGroup.iterator().next());
		}
	}

	@Test
	public void test25_active_random_service() throws URISyntaxException {
		final String result = client.getActiveNodeRandomByService("cluster", null);
		Assert.assertNotNull(result);
		Assert.assertEquals(ADDRESS, result);
		for (String group : GROUPS) {
			String resultGroup = client.getActiveNodeRandomByService("cluster", group);
			Assert.assertNotNull(resultGroup);
			Assert.assertEquals(ADDRESS, resultGroup);
		}
	}

	@Test
	public void test30_active_leader() throws URISyntaxException {
		String result = client.getActiveNodeLeaderByService("cluster", null);
		Assert.assertNotNull(result);
		Assert.assertEquals(ADDRESS, result);
		for (String group : GROUPS) {
			String resultGroup = client.getActiveNodeLeaderByService("cluster", group);
			Assert.assertNotNull(resultGroup);
			Assert.assertEquals(ADDRESS, resultGroup);
		}
	}

	@Test
	public void test35_get_service_map() throws URISyntaxException {
		Map<String, ClusterServiceStatusJson.StatusEnum> result = client.getServiceMap(null);
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(ClusterServiceStatusJson.StatusEnum.ok, result.get("cluster"));
		for (String group : GROUPS) {
			result = client.getServiceMap(group);
			Assert.assertNotNull(result);
			Assert.assertEquals(1, result.size());
			Assert.assertEquals(ClusterServiceStatusJson.StatusEnum.ok, result.get("cluster"));
		}
	}

	@Test
	public void test40_status() throws URISyntaxException {
		ClusterStatusJson status = client.getStatus();
		Assert.assertNotNull(status);
		Assert.assertEquals(1, status.activeNodes.size());
		Assert.assertEquals(ADDRESS, status.activeNodes.values().iterator().next().address);
		Assert.assertEquals(1, status.masters.size());
		Assert.assertEquals("http://localhost:9091", status.masters.first());
	}

	@Test
	public void test50_getActive() throws URISyntaxException {
		ClusterServiceInterface local = serviceBuilder.local();
		ClusterServiceInterface service = serviceBuilder.getActive(GROUPS[0]);
		Assert.assertNotNull(local);
		Assert.assertEquals(local, service);
	}

	@Test
	public void test51_getRandom() throws URISyntaxException {
		ClusterServiceInterface local = serviceBuilder.local();
		ClusterServiceInterface service = serviceBuilder.getRandom(GROUPS[0]);
		Assert.assertNotNull(local);
		Assert.assertEquals(local, service);
	}

	@Test
	public void test52_getLeader() throws URISyntaxException {
		ClusterServiceInterface local = serviceBuilder.local();
		ClusterServiceInterface service = serviceBuilder.getLeader(GROUPS[0]);
		Assert.assertNotNull(local);
		Assert.assertEquals(local, service);
	}

	@Test
	public void test60_isLeader() {
		Assert.assertTrue(clusterManager.isLeader(ClusterServiceInterface.SERVICE_NAME, GROUPS[0]));
	}

	@Test
	public void test62_isGroup() {
		Assert.assertTrue(clusterManager.isGroup(GROUPS[0]));
		Assert.assertTrue(clusterManager.isGroup(GROUPS[1]));
	}

	@Test
	public void test70_serviceBuilder() throws URISyntaxException {
		Assert.assertNotNull(serviceBuilder.local());
		ClusterServiceInterface clusterService;
		clusterService = serviceBuilder.getActive(GROUPS[0]);
		Assert.assertEquals(serviceBuilder.local(), clusterService);
		clusterService = serviceBuilder.getActive(GROUPS[1]);
		Assert.assertEquals(serviceBuilder.local(), clusterService);
	}

	@AfterClass
	public static void cleanup() {
		ClusterServer.shutdown();
	}

}
