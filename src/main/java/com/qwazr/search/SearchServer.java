/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

import com.qwazr.cluster.ClusterManager;
import com.qwazr.cluster.ClusterServiceInterface;
import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.IndexServiceBuilder;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.server.ApplicationBuilder;
import com.qwazr.server.BaseServer;
import com.qwazr.server.GenericServer;
import com.qwazr.server.GenericServerBuilder;
import com.qwazr.server.RestApplication;
import com.qwazr.server.WelcomeShutdownService;
import com.qwazr.server.configuration.ServerConfiguration;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.management.JMException;
import javax.servlet.ServletException;

public class SearchServer implements BaseServer {

    private final GenericServer server;
    private final IndexServiceBuilder serviceBuilder;
    private final ClusterManager clusterManager;
    private final IndexManager indexManager;

    private SearchServer(final ServerConfiguration configuration) throws IOException {

        final ExecutorService executorService = Executors.newCachedThreadPool();
        final GenericServerBuilder builder = GenericServer.of(configuration, executorService);

        final Set<String> services = new HashSet<>();
        services.add(ClusterServiceInterface.SERVICE_NAME);
        services.add(IndexServiceInterface.SERVICE_NAME);

        final ApplicationBuilder webServices = ApplicationBuilder.of("/*").classes(RestApplication.JSON_CLASSES).
            singletons(new WelcomeShutdownService());

        clusterManager = new ClusterManager(executorService, configuration).registerProtocolListener(builder, services);
        webServices.singletons(clusterManager.getService());

        final Path indexesDirectory = IndexManager.checkSubDirectory(configuration.dataDirectory, IndexManager.INDEXES_DIRECTORY);
        final Path backupsDirectory = IndexManager.checkSubDirectory(configuration.dataDirectory, IndexManager.BACKUPS_DIRECTORY);
        indexManager = new IndexManager(indexesDirectory, executorService, backupsDirectory);
        builder.shutdownListener(server -> indexManager.close());
        webServices.singletons(indexManager.getService());

        builder.getWebServiceContext().jaxrs(webServices);
        serviceBuilder = new IndexServiceBuilder(clusterManager, indexManager);
        server = builder.build();
    }

    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    public IndexManager getIndexManager() {
        return indexManager;
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

    public static synchronized void main(final String... args) throws IOException, ServletException, JMException {
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
