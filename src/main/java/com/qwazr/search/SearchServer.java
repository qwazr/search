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
package com.qwazr.search;

import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.cluster.ClusterManager;
import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.IndexServiceBuilder;
import com.qwazr.server.BaseServer;
import com.qwazr.server.GenericServer;
import com.qwazr.server.WelcomeShutdownService;
import com.qwazr.server.configuration.ServerConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchServer implements BaseServer {

	private final GenericServer server;
	private final IndexServiceBuilder serviceBuilder;

	private SearchServer(final ServerConfiguration configuration) throws IOException, URISyntaxException {
		final ExecutorService executorService = Executors.newCachedThreadPool();
		final GenericServer.Builder builder =
				GenericServer.of(configuration, executorService).webService(WelcomeShutdownService.class);
		final ClassLoaderManager classLoaderManager =
				new ClassLoaderManager(configuration.dataDirectory, Thread.currentThread());
		final ClusterManager clusterManager =
				new ClusterManager(executorService, configuration).registerHttpClientMonitoringThread(builder)
						.registerProtocolListener(builder)
						.registerWebService(builder);
		final IndexManager indexManager =
				new IndexManager(classLoaderManager, configuration.dataDirectory, executorService).registerWebService(
						builder).registerShutdownListener(builder);
		serviceBuilder = new IndexServiceBuilder(clusterManager, indexManager);
		server = builder.build();
	}

	@Override
	public GenericServer getServer() {
		return server;
	}

	private static volatile SearchServer INSTANCE;

	public static SearchServer getInstance() {
		return INSTANCE;
	}

	public IndexServiceBuilder getServiceBuilder() {
		return serviceBuilder;
	}

	public static synchronized void main(final String... args) throws Exception {
		shutdown();
		INSTANCE = new SearchServer(new ServerConfiguration(args));
		INSTANCE.start();
	}

	public static synchronized void shutdown() {
		if (INSTANCE == null)
			return;
		INSTANCE.stop();
		INSTANCE = null;
	}

}