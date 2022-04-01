/*
 * Copyright 2016-2020 Emmanuel Keller / QWAZR
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

import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class TestListener implements ServletContextListener {

	public static final Set<TestListener> initializedListeners = new LinkedHashSet<>();
	public static final Set<TestListener> destroyedListeners = new LinkedHashSet<>();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		synchronized (initializedListeners) {
			initializedListeners.add(this);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		synchronized (destroyedListeners) {
			destroyedListeners.add(this);
		}
	}
}
