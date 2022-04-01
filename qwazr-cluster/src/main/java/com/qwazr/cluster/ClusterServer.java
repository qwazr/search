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
package com.qwazr.cluster;

import com.qwazr.server.ApplicationBuilder;
import com.qwazr.server.BaseServer;
import com.qwazr.server.GenericServer;
import com.qwazr.server.GenericServerBuilder;
import com.qwazr.server.RestApplication;
import com.qwazr.server.WelcomeShutdownService;
import com.qwazr.server.configuration.ServerConfiguration;

import javax.management.JMException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClusterServer implements BaseServer {

    private final GenericServer server;
    private final ClusterManager clusterManager;
    private final ClusterServiceBuilder serviceBuilder;

    public ClusterServer(final ServerConfiguration serverConfiguration) throws IOException {

        final ExecutorService executorService = Executors.newCachedThreadPool();

        final GenericServerBuilder builder = GenericServer.of(serverConfiguration, executorService);

        final ApplicationBuilder webServices = ApplicationBuilder.of("/*")
                .classes(RestApplication.JSON_CLASSES)
                .singletons(new WelcomeShutdownService());

        final Set<String> services = new HashSet<>();
        services.add(ClusterServiceInterface.SERVICE_NAME);

        clusterManager =
                new ClusterManager(executorService, serverConfiguration).registerProtocolListener(builder, services);
        webServices.singletons(clusterManager.getService());

        builder.getWebServiceContext().jaxrs(webServices);
        server = builder.build();
        serviceBuilder = new ClusterServiceBuilder(clusterManager);
    }

    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    public ClusterServiceBuilder getServiceBuilder() {
        return serviceBuilder;
    }

    @Override
    public GenericServer getServer() {
        return server;
    }

    private static volatile ClusterServer INSTANCE;

    public static ClusterServer getInstance() {
        return INSTANCE;
    }

    public static synchronized void main(final String... args)
            throws IOException, ServletException, JMException {
        shutdown();
        INSTANCE = new ClusterServer(
                ServerConfiguration.of()
                        .applyEnvironmentVariables()
                        .applySystemProperties()
                        .applyCommandLineArgs(args)
                        .build());
        INSTANCE.start();
    }

    public static synchronized void shutdown() {
        if (INSTANCE == null)
            return;
        INSTANCE.stop();
        INSTANCE = null;
    }

}
