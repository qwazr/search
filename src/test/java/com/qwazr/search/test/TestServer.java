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
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.IndexSingleClient;
import com.qwazr.utils.server.RemoteService;

import java.io.File;
import java.net.URISyntaxException;

public class TestServer {

	public static boolean serverStarted = false;

	private static final String BASE_URL = "http://localhost:9091";

	public static synchronized void startServer()
			throws Exception {
		if (serverStarted)
			return;
		final File dataDir = Files.createTempDir();
		System.setProperty("QWAZR_DATA", dataDir.getAbsolutePath());
		System.setProperty("PUBLIC_ADDR", "localhost");
		System.setProperty("LISTEN_ADDR", "localhost");
		SearchServer.main(new String[]{});
		serverStarted = true;
	}

	public static IndexSingleClient singleClient = null;

	public static synchronized IndexServiceInterface getSingleClient() throws URISyntaxException {
		if (singleClient != null)
			return singleClient;
		singleClient = new IndexSingleClient(new RemoteService(BASE_URL));
		return singleClient;
	}

	public static synchronized <T> AnnotatedIndexService<T> getService(final IndexServiceInterface indexService,
			final Class<T> indexClass, final String indexName,
			final IndexSettingsDefinition settings) throws URISyntaxException {
		return new AnnotatedIndexService(indexService, indexClass, null, indexName, settings);
	}

	public static synchronized <T> AnnotatedIndexService<T> getService(final IndexServiceInterface indexService,
			final Class<T> indexClass) throws URISyntaxException {
		return getService(indexService, indexClass, null, null);
	}

}
