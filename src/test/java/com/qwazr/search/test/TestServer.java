/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import com.qwazr.search.index.IndexServiceBuilder;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.server.RemoteService;
import com.qwazr.utils.FileUtils;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class TestServer {

	public static IndexServiceInterface service;
	public static IndexServiceInterface remote;

	static final String BASE_URL = "http://localhost:9091";

	static final File dataDir = Files.createTempDir();

	public static final AtomicInteger injectedAnalyzerCount = new AtomicInteger();

	public static synchronized void startServer() throws Exception {
		if (service != null)
			return;
		FileUtils.copyDirectory(new File("src/test/data"), dataDir);
		System.setProperty("QWAZR_DATA", dataDir.getAbsolutePath());
		System.setProperty("PUBLIC_ADDR", "localhost");
		System.setProperty("LISTEN_ADDR", "localhost");
		SearchServer.main();
		SearchServer.getInstance()
				.getIndexManager()
				.registerAnalyzerFactory(AnnotatedRecord.INJECTED_ANALYZER_NAME,
						resourceLoader -> new AnnotatedRecord.TestAnalyzer(injectedAnalyzerCount));
		IndexServiceBuilder indexServiceBuilder = SearchServer.getInstance().getServiceBuilder();
		service = indexServiceBuilder.local();
		remote = indexServiceBuilder.remote(RemoteService.of(BASE_URL).build());
	}

}
