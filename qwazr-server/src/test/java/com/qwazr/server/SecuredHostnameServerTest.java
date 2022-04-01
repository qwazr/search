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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.management.JMException;
import javax.servlet.ServletException;
import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SecuredHostnameServerTest extends BaseServerTest {

	private static SecuredHostnameServer server;

	@BeforeClass
	public static void setupClass() throws IOException {
		server = new SecuredHostnameServer();
		Assert.assertNotNull(server.getServer());
	}

	@Test
	public void test200startServer() throws JMException, ServletException, IOException {
		server.start();
		Assert.assertNotNull(server.contextAttribute);

		;

		Assert.assertEquals(200, getClient().target("http://localhost:9091").request().get().getStatus());
		Assert.assertEquals(404,
				getClient().target("http://localhost:9091/sdflksjflskdfj").request().get().getStatus());
	}

	@Test
	public void test300SimpleServlet() {
		Assert.assertEquals(server.contextAttribute,
				getClient().target("http://localhost:9090/test").request().get().readEntity(String.class));
	}

	@Test
	public void test400SecuredHostnameLoginSuccessfulServlet() throws IOException {
		server.principalResolver.put("localhost", server.externalUsername);
		SecuredServlet.check(getClient().target("http://localhost:9090/secured").request().get(),
				server.externalUsername);
	}

	@Test
	public void test500SecuredHostnameLoginUnSuccessfulNoUserServlet() throws IOException {
		server.principalResolver.remove("localhost");
		Assert.assertEquals(401, getClient().target("http://localhost:9090/secured").request().get().getStatus());
	}

	@Test
	public void test510SecuredHostnameLoginUnSuccessfulUknownUserServlet() throws IOException {
		server.principalResolver.put("localhost", "1234");
		Assert.assertEquals(401, getClient().target("http://localhost:9090/secured").request().get().getStatus());
	}

	@AfterClass
	public static void cleanupClass() {
		server.stop();
	}
}
