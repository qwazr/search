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

import org.junit.Assert;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.Principal;

@WebServlet("/secured")
@ServletSecurity(value = @HttpConstraint(rolesAllowed = { "secured" }),
		httpMethodConstraints = @HttpMethodConstraint("POST"))
public class SecuredServlet extends SimpleServlet {

	public static String HEADER_USER = "X-QWAZR-USER";

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse rep) throws IOException {
		final Principal principal = req.getUserPrincipal();
		if (principal != null)
			rep.setHeader(HEADER_USER, principal.getName());
		super.doGet(req, rep);
	}

	static Response check(Response response, String username) {
		Assert.assertEquals(200, response.getStatus());
		Assert.assertEquals(username, response.getHeaderString(HEADER_USER));
		return response;
	}
}
