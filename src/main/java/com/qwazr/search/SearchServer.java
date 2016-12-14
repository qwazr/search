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
import com.qwazr.cluster.manager.ClusterManager;
import com.qwazr.search.index.IndexManager;
import com.qwazr.server.GenericServer;
import com.qwazr.server.ServerBuilder;
import com.qwazr.server.WelcomeShutdownService;
import com.qwazr.server.configuration.ServerConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

public class SearchServer extends GenericServer {

	private SearchServer(final ServerConfiguration serverConfiguration) throws IOException {
		super(serverConfiguration);
	}

	@Override
	protected void build(final ExecutorService executorService, final ServerBuilder builder,
			final ServerConfiguration configuration, final Collection<File> etcFiles) throws IOException {
		builder.registerWebService(WelcomeShutdownService.class);
		ClassLoaderManager.load(configuration.dataDirectory, null);
		ClusterManager.load(builder, configuration);
		IndexManager.load(builder, configuration, executorService);
	}

	public static void main(final String... args) throws Exception {
		new SearchServer(new ServerConfiguration(args)).start(true);
	}

}