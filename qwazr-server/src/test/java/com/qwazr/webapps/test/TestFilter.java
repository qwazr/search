/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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
 **/
package com.qwazr.webapps.test;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class TestFilter implements Filter {

	public static final Set<TestFilter> initializedFilters = new LinkedHashSet<>();
	public static final Set<TestFilter> destroyedFilters = new LinkedHashSet<>();
	public static final Set<TestFilter> calledFilters = new LinkedHashSet<>();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		initializedFilters.add(this);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		calledFilters.add(this);
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		destroyedFilters.add(this);
	}
}
