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

import com.qwazr.server.configuration.ServerConfiguration;
import com.qwazr.utils.RandomUtils;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.SecurityInfo;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import java.io.IOException;

public class SecuredBasicServer implements BaseServer {

	public final static String CONTEXT_ATTRIBUTE_TEST = "test";

	public final String contextAttribute = RandomUtils.alphanumeric(5);

	public final String basicUsername = RandomUtils.alphanumeric(8);

	public final String basicPassword = RandomUtils.alphanumeric(12);

	public final String realm = RandomUtils.alphanumeric(6);

	private GenericServer server;

	public SecuredBasicServer() throws IOException {
		final MemoryIdentityManager identityManager = new MemoryIdentityManager();
		identityManager.addBasic(basicUsername, basicUsername, basicPassword, "secured");
		final GenericServerBuilder builder =
				GenericServer.of(ServerConfiguration.of().webAppAuthentication("BASIC").webAppRealm(realm).build())
						.contextAttribute(CONTEXT_ATTRIBUTE_TEST, contextAttribute)
						.identityManagerProvider(realm -> identityManager);

		builder.getWebServiceContext()
				.jaxrs(ApplicationBuilder.of("/*")
						.classes(RestApplication.JSON_CLASSES)
						.singletons(new WelcomeShutdownService()));

		builder.getWebAppContext()
				.servlet(SimpleServlet.class)
				.servlet(SecuredServlet.class)
				.jaxrs(TestJaxRsAppAuth.class)
				.jaxrs(new ApplicationBuilder("/jaxrs-app-auth-singletons/*").classes(RolesAllowedDynamicFeature.class)
						.singletons(new TestJaxRsAppAuth.ServiceAuth()))
				.addSecurityConstraint(Servlets.securityConstraint()
						.setEmptyRoleSemantic(SecurityInfo.EmptyRoleSemantic.AUTHENTICATE)
						.addWebResourceCollection(Servlets.webResourceCollection()
								.addUrlPatterns("/jaxrs-app-auth/*", "/jaxrs-app-auth-singletons/*")));

		server = builder.build();
	}

	@Override
	public GenericServer getServer() {
		return server;
	}

}
