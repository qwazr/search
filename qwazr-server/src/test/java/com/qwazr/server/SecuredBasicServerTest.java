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

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.management.JMException;
import javax.servlet.ServletException;
import javax.ws.rs.client.Client;
import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SecuredBasicServerTest extends BaseServerTest {

	private static SecuredBasicServer server;

	@BeforeClass
	public static void setupClass() throws IOException {
		server = new SecuredBasicServer();
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
	public void test300SimpleServlet() {
		Assert.assertEquals(server.contextAttribute,
				getClient().target("http://localhost:9090/test").request().get().readEntity(String.class));
	}

	@Test
	public void test400SecuredNonAuthServlet() {
		Assert.assertEquals(401, getClient().target("http://localhost:9090/secured").request().get().getStatus());
	}

	@Test
	public void test410SecuredBasicLoginSuccessfulServlet() {
		SecuredServlet.check(
				getClient(HttpAuthenticationFeature.basic(server.basicUsername, server.basicPassword)).target(
						"http://localhost:9090/secured").request().get(), server.basicUsername).close();
	}

	@Test
	public void test415SecuredBasicLoginFailureServlet() {
		final Client client = getClient(HttpAuthenticationFeature.basic(server.basicUsername, "--"));
		Assert.assertEquals(401, client.target("http://localhost:9090/secured").request().get().getStatus());
	}

	private void checkAppAuth(String path) {
		Assert.assertEquals(401, getClient().target(path + "auth/test").request().get().getStatus());
		Assert.assertEquals(401,
				getClient(HttpAuthenticationFeature.basic(server.basicUsername, "--")).target(path + "auth/test")
						.request()
						.get()
						.getStatus());

		final Client client = getClient(HttpAuthenticationFeature.basic(server.basicUsername, server.basicPassword));
		Assert.assertEquals(403, client.target(path + "auth/wrong-role").request().get().getStatus());
		Assert.assertEquals(200, client.target(path + "auth/test").request().get().getStatus());
	}

	@Test
	public void test500AppJaxRsAuth() {
		checkAppAuth("http://localhost:9090/jaxrs-app-auth/");
	}

	@Test
	public void test505AppJaxRsAuthSingletons() {
		checkAppAuth("http://localhost:9090/jaxrs-app-auth-singletons/");
	}

	@AfterClass
	public static void cleanupClass() {
		server.stop();
	}
}
