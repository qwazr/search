/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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
package com.qwazr.search.test;

import com.google.common.io.Files;
import com.qwazr.search.SearchServer;
import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSingleClient;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class TestServer {

	public static boolean serverStarted = false;

	private static final String BASE_URL = "http://localhost:9091";

	public static synchronized void startServer()
			throws InstantiationException, IllegalAccessException, ServletException, IOException {
		if (serverStarted)
			return;
		final File dataDir = Files.createTempDir();
		System.setProperty("QWAZR_DATA", dataDir.getAbsolutePath());
		SearchServer.main(new String[] {});
		serverStarted = true;
	}

	public static IndexSingleClient singleClient = null;

	public static synchronized IndexServiceInterface getSingleClient() throws URISyntaxException {
		if (singleClient != null)
			return singleClient;
		singleClient = new IndexSingleClient(BASE_URL, 60000);
		return singleClient;
	}

	public static Map<Class<?>, AnnotatedIndexService> serviceMap = new HashMap<>();

	public static synchronized <T> AnnotatedIndexService<T> getService(Class<T> indexClass) throws URISyntaxException {
		AnnotatedIndexService service = serviceMap.get(indexClass);
		if (service != null)
			return service;
		service = new AnnotatedIndexService(IndexServiceInterface.getClient(true, null, null), indexClass);
		serviceMap.put(indexClass, service);
		return service;
	}

}
