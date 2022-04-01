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
package com.qwazr.cluster;

import com.qwazr.server.GenericServerBuilder;
import com.qwazr.server.ServerException;
import com.qwazr.server.configuration.ServerConfiguration;
import com.qwazr.utils.ArrayUtils;
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.StringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class ClusterManager {

    private static final Logger LOGGER = LoggerUtils.getLogger(ClusterManager.class);

    final ClusterNodeMap clusterNodeMap;

    final ClusterNodeAddress me;

    final ClusterNodeAddress webApp;

    final Set<String> myServices;
    final Set<String> myGroups;

    final UUID nodeLiveId;

    private final Set<String> masters;

    private final ProtocolListener protocolListener;

    private final ExecutorService executorService;

    private final ClusterServiceImpl service;

    public ClusterManager(final ExecutorService executorService, final ServerConfiguration configuration) {

        this.executorService = executorService;
        this.nodeLiveId = HashUtils.newTimeBasedUUID();

        me = new ClusterNodeAddress(configuration.webServiceConnector.addressPort,
                configuration.webServiceConnector.port);
        webApp = new ClusterNodeAddress(configuration.webAppConnector.addressPort, configuration.webAppConnector.port);

        LOGGER.info(() -> "Server: " + me.httpAddressKey + " Groups: " + ArrayUtils.prettyPrint(configuration.groups));
        this.myGroups = configuration.groups != null ? new HashSet<>(configuration.groups) : null;
        this.myServices = new HashSet<>();
        if (configuration.masters != null && !configuration.masters.isEmpty()) {
            this.masters = new HashSet<>();
            configuration.masters.forEach(master -> this.masters.add(
                    new ClusterNodeAddress(master, configuration.webServiceConnector.port).httpAddressKey));
        } else
            this.masters = null;
        clusterNodeMap = new ClusterNodeMap(this, me.address);
        clusterNodeMap.register(me.httpAddressKey);
        clusterNodeMap.register(masters);

        if (configuration.multicastConnector.address != null && configuration.multicastConnector.port != -1)
            protocolListener = new MulticastListener(this, configuration.multicastConnector.address,
                    configuration.multicastConnector.port);
        else
            protocolListener = new DatagramListener(this);

        service = new ClusterServiceImpl(this);
    }

    public ClusterServiceInterface getService() {
        return service;
    }

    public ClusterManager registerProtocolListener(final GenericServerBuilder builder, final Set<String> services) {
        builder.packetListener(protocolListener);
        builder.startedListener(server -> protocolListener.joinCluster(services));
        builder.shutdownListener(server -> protocolListener.leaveCluster());
        builder.shutdownListener(server -> protocolListener.shutdown());
        executorService.submit(protocolListener);
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodeLiveId);
    }

    public boolean isGroup(String group) {
        if (group == null)
            return true;
        if (myGroups == null)
            return true;
        if (group.isEmpty())
            return true;
        return myGroups.contains(group);
    }

    public boolean isLeader(final String service, final String group) throws ServerException {
        final SortedSet<String> nodes = clusterNodeMap.getGroupService(group, service);
        if (nodes == null || nodes.isEmpty()) {
            LOGGER.warning(() -> "No node available for this service/group: " + service + '/' + group);
            return false;
        }
        return me.httpAddressKey.equals(nodes.first());
    }

    final ClusterStatusJson getStatus() {
        final Map<String, ClusterNode> nodesMap = clusterNodeMap.getNodesMap();
        final TreeMap<String, ClusterNodeJson> nodesJsonMap = new TreeMap<>();
        if (nodesMap != null) {
            final long currentMs = System.currentTimeMillis();
            nodesMap.forEach((address, clusterNode) -> {
                final Integer timeToLive;
                final Long expirationTimeMs = clusterNode.getExpirationTimeMs();
                if (expirationTimeMs != null)
                    timeToLive = (int) ((expirationTimeMs - currentMs) / 1000);
                else
                    timeToLive = null;
                final ClusterNodeJson clusterNodeJson = new ClusterNodeJson(clusterNode, timeToLive);
                nodesJsonMap.put(address, clusterNodeJson);
            });
        }
        return new ClusterStatusJson(me.httpAddressKey, nodeLiveId,
                myServices.contains("webapps") ? webApp.httpAddressKey : null, nodesJsonMap, clusterNodeMap.getGroups(),
                clusterNodeMap.getServices(), masters, protocolListener.getLastExecutionDate());
    }

    final Set<String> getNodes() {
        final Map<String, ClusterNode> nodesMap = clusterNodeMap.getNodesMap();
        return nodesMap == null ? Collections.emptySet() : nodesMap.keySet();
    }

    final TreeMap<String, ClusterServiceStatusJson.StatusEnum> getServicesStatus(final String group) {
        final TreeMap<String, ClusterServiceStatusJson.StatusEnum> servicesStatus = new TreeMap<>();
        final Set<String> services = clusterNodeMap.getServices().keySet();
        if (services.isEmpty())
            return servicesStatus;
        services.forEach(service -> {
            final SortedSet<String> nodes = getNodesByGroupByService(group, service);
            if (nodes != null && !nodes.isEmpty())
                servicesStatus.put(service, ClusterServiceStatusJson.StatusEnum.of(nodes));
        });
        return servicesStatus;
    }

    final ClusterServiceStatusJson getServiceStatus(final String group, final String service) {
        final SortedSet<String> nodes = getNodesByGroupByService(group, service);
        return ClusterServiceStatusJson.of(nodes);
    }

    final SortedSet<String> getNodesByGroupByService(final String group, final String service) {
        if (StringUtils.isEmpty(group))
            return clusterNodeMap.getByService(service);
        else if (StringUtils.isEmpty(service))
            return clusterNodeMap.getByGroup(group);
        else
            return clusterNodeMap.getGroupService(group, service);
    }

    final String getLeaderNode(final String group, final String service) {
        final SortedSet<String> nodes = getNodesByGroupByService(group, service);
        if (nodes == null || nodes.isEmpty())
            return null;
        return nodes.first();
    }

    final String getRandomNode(final String group, final String service) {
        final SortedSet<String> nodes = getNodesByGroupByService(group, service);
        if (nodes == null || nodes.isEmpty())
            return null;
        int rand = RandomUtils.nextInt(0, nodes.size());
        Iterator<String> it = nodes.iterator();
        for (; ; ) {
            final String node = it.next();
            if (rand == 0)
                return node;
            rand--;
        }
    }

    final boolean isMe(final AddressContent message) {
        if (message == null)
            return false;
        if (nodeLiveId.equals(message.getNodeLiveId()))
            return true;
        if (me.httpAddressKey.equals(message.getAddress()))
            return true;
        return false;
    }

    final boolean isMaster(final ClusterNodeAddress nodeAddress) {
        if (nodeAddress == null || masters == null)
            return false;
        return masters.contains(nodeAddress.httpAddressKey);
    }
}
