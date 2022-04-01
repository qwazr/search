/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.server;

import com.fasterxml.jackson.jaxrs.smile.SmileMediaTypes;
import com.qwazr.server.configuration.ServerConfiguration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.management.JMException;
import javax.servlet.ServletException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SimpleServerTest extends BaseServerTest {

	private static SimpleServer server;

	@Test
	public void test100createServer() throws IOException {
		server = new SimpleServer(null, ServerConfiguration.of().publicAddress("localhost").build());
		Assert.assertNotNull(server.getServer());
	}

	@Test
	public void test200startServer() throws JMException, ServletException, IOException {
		server.start();
		Assert.assertNotNull(server.contextAttribute);
		Assert.assertEquals(200, getClient().target("http://localhost:9091/").request().get().getStatus());
		Assert.assertEquals(404,
				getClient().target("http://localhost:9091/sdflksjflskdfj").request().get().getStatus());
	}

	@Test
	public void test250welcomeStatus() {
		final WelcomeStatus welcomeStatus =
				getClient().target("http://localhost:9091/").request().get().readEntity(WelcomeStatus.class);
		Assert.assertNotNull(welcomeStatus);
		Assert.assertNotNull(welcomeStatus.webapp_endpoints);
		Assert.assertNotNull(welcomeStatus.webservice_endpoints);
		Assert.assertEquals(2, welcomeStatus.webapp_endpoints.size());
		Assert.assertEquals(4, welcomeStatus.webservice_endpoints.size());
	}

	@Test
	public void test300SimpleServletWithFilter() {
		Response response = getClient().target("http://localhost:9090/test").request().get();
		try {
			Assert.assertEquals(SimpleFilter.TEST_VALUE, response.getHeaderString(SimpleFilter.HEADER_NAME));
			Assert.assertEquals(server.contextAttribute, response.readEntity(String.class));
		} finally {
			response.close();
		}
	}

	@Test
	public void test301SimpleServletUrlMappingBis() {
		Assert.assertEquals(server.contextAttribute,
				getClient().target("http://localhost:9090/test_bis").request().get().readEntity(String.class));
	}

	@Test
	public void test400LoadedService() {
		Assert.assertEquals(LoadedService.TEXT,
				getClient().target("http://localhost:9091/loaded").request().get().readEntity(String.class));
	}

	@Test
	public void test401LoadedServiceMapSmile() {
		Map<String, String> map = getClient().target("http://localhost:9091/loaded/map")
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.get()
				.readEntity(LoadedService.mapType);
		Assert.assertEquals(LoadedService.TEXT, map.get(LoadedService.SERVICE_NAME));
	}

	@Test
	public void test401LoadedServiceMapJson() {
		Map<String, String> map = getClient().target("http://localhost:9091/loaded/map")
				.request(MediaType.APPLICATION_JSON)
				.get()
				.readEntity(LoadedService.mapType);
		Assert.assertEquals(LoadedService.TEXT, map.get(LoadedService.SERVICE_NAME));
	}

	@Test
	public void test404() {
		Assert.assertEquals(404,
				getClient().target("http://localhost:9091/sd404flsfjskdfj").request().get().getStatus());
	}

	@Test
	public void test500ErrorJson() {
		try {
			getClient().target("http://localhost:9091/error").request(MediaType.APPLICATION_JSON).get(String.class);
			Assert.fail("WebApplicationException not thrown");
		} catch (WebApplicationException e) {
			e = ServerException.from(e);
			Assert.assertEquals("Not Acceptable Error", e.getMessage());
		}
	}

	@Test
	public void test500ErrorText() {
		try {
			getClient().target("http://localhost:9091/error").request(MediaType.TEXT_PLAIN).get(String.class);
			Assert.fail("WebApplicationException not thrown");
		} catch (WebApplicationException e) {
			e = ServerException.from(e);
			Assert.assertTrue(e.getMessage().startsWith("Not Acceptable Error"));
		}
	}

	@Test
	public void test500ErrorHtml() {
		try {
			getClient().target("http://localhost:9091/error").request(MediaType.TEXT_HTML).get(String.class);
			Assert.fail("WebApplicationException not thrown");
		} catch (WebApplicationException e) {
			e = ServerException.from(e);
			Assert.assertTrue(e.getMessage().startsWith("<html><body><h2>Error 406</h2>"));
		}
	}

	@AfterClass
	public static void cleanupTest() {
		server.stop();
	}

}
