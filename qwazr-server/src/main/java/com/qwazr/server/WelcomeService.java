/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.IOException;

@RolesAllowed(WelcomeService.SERVICE_NAME)
@Path("/")
public class WelcomeService extends AbstractServiceImpl {

	public final static String SERVICE_NAME = "welcome";

	@GET
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	public WelcomeStatus welcome(@QueryParam("properties") Boolean properties, @QueryParam("env") Boolean env)
			throws IOException {
		return new WelcomeStatus(getContextAttribute(GenericServer.class), properties, env);
	}

}
