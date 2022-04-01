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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "FilterTest",
		servletNames = "ServletTest",
		initParams = { @WebInitParam(name = "param1", value = SimpleFilter.TEST_VALUE) })
public class SimpleFilter implements Filter {

	final static String TEST_VALUE = "filterValue1";
	final static String HEADER_NAME = "X-FILTER-PARAM1";

	private String param;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		param = filterConfig.getInitParameter("param1");
		Assert.assertEquals(TEST_VALUE, param);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		((HttpServletResponse) response).addHeader(HEADER_NAME, param);
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}
}
