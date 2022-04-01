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

import com.qwazr.utils.FileUtils;

import javax.servlet.ServletConfig;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(value = "/test", name = "ServletTest")
@MultipartConfig(maxFileSize = FileUtils.ONE_GB,
		maxRequestSize = FileUtils.ONE_MB,
		fileSizeThreshold = (int) FileUtils.ONE_MB)
public class SimpleServlet extends HttpServlet {

	private String testString;

	public static volatile String sessionId;
	public static volatile String lastSessionAttribute;

	@Override
	public void init(ServletConfig servletConfig) {
		testString = GenericServer.getContextAttribute(servletConfig.getServletContext(),
				SimpleServer.CONTEXT_ATTRIBUTE_TEST, String.class);
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse rep) throws IOException {
		HttpSession session = req.getSession();
		sessionId = session.getId();
		lastSessionAttribute = (String) session.getAttribute("AttributeTest");
		session.setAttribute("AttributeTest", sessionId);
		rep.getWriter().print(testString);
	}
}
