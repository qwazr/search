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
package com.qwazr.cluster.test;

import com.google.common.io.Files;
import com.qwazr.cluster.ClusterServer;
import com.qwazr.cluster.ClusterServiceBuilder;
import com.qwazr.cluster.ClusterServiceInterface;
import com.qwazr.server.RemoteService;
import com.qwazr.server.configuration.ServerConfiguration;
import com.qwazr.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterTestServer {

    final ClusterServer server;
    final File dataDir;
    final ClusterServiceInterface client;
    final String address;
    final ClusterServiceBuilder serviceBuilder;

    final static List<ClusterTestServer> servers = new ArrayList<>();
    final static List<String> serverAdresses = new ArrayList<>();

    ClusterTestServer(final List<String> masters, final String hostname, final int tcpPort, final String multicastAddress,
                      final Integer multicastPort, final String... groups) throws Exception {

        dataDir = Files.createTempDir();
        address = "http://" + hostname + ':' + tcpPort;

        server = new ClusterServer(
                ServerConfiguration.of()
                .master(masters)
                        .data(dataDir.toPath())
                        .group(groups)
                        .publicAddress(hostname)
                        .listenAddress(hostname)
                        .webServicePort(tcpPort)
                        .multicastAddress(multicastAddress)
                        .multicastPort(multicastPort)
                        .build());

        serviceBuilder = server.getServiceBuilder();
        client = serviceBuilder.remote(RemoteService.of(this.address).build());
        servers.add(this);
        serverAdresses.add(this.address);

        server.start();
    }

    public void stop() {
        server.stop();
    }

    public static void stopServers() {
        servers.forEach(ClusterTestServer::stop);
        servers.clear();
    }
}
