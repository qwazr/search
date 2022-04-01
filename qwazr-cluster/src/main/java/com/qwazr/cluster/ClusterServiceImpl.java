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

import com.qwazr.server.AbstractServiceImpl;
import com.qwazr.server.ServerException;
import com.qwazr.utils.LoggerUtils;

import javax.ws.rs.NotAcceptableException;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

class ClusterServiceImpl extends AbstractServiceImpl implements ClusterServiceInterface {

    private final static Logger LOGGER = LoggerUtils.getLogger(ClusterServiceImpl.class);

    final ClusterManager manager;

    public ClusterServiceImpl(final ClusterManager manager) {
        this.manager = manager;
    }

    @Override
    public ClusterStatusJson getStatus() {
        try {
            return manager.getStatus();
        } catch (ServerException e) {
            throw e.warnIfCause(LOGGER).getJsonException(false);
        }
    }

    @Override
    public SortedSet<String> getNodes() {
        try {
            return new TreeSet<>(manager.getNodes());
        } catch (ServerException e) {
            throw e.warnIfCause(LOGGER).getJsonException(false);
        }
    }

    @Override
    public SortedSet<String> getActiveNodesByService(final String serviceName, final String group) {
        try {
            if (serviceName == null)
                throw new NotAcceptableException();
            return manager.getNodesByGroupByService(group, serviceName);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public String getActiveNodeRandomByService(final String serviceName, final String group) {
        try {
            if (serviceName == null)
                throw new NotAcceptableException();
            return manager.getRandomNode(group, serviceName);
        } catch (Exception e) {
            throw ServerException.getTextException(LOGGER, e);
        }
    }

    @Override
    public String getActiveNodeLeaderByService(final String serviceName, final String group) {
        try {
            if (serviceName == null)
                throw new NotAcceptableException();
            return manager.getLeaderNode(group, serviceName);
        } catch (ServerException e) {
            throw ServerException.getTextException(LOGGER, e);
        }
    }

    @Override
    public SortedMap<String, ClusterServiceStatusJson.StatusEnum> getServiceMap(final String group) {
        try {
            return manager.getServicesStatus(group);
        } catch (ServerException e) {
            throw ServerException.getTextException(LOGGER, e);
        }
    }

    @Override
    public ClusterServiceStatusJson getServiceStatus(final String serviceName, final String group) {
        try {
            return manager.getServiceStatus(group, serviceName);
        } catch (ServerException e) {
            throw ServerException.getTextException(LOGGER, e);
        }
    }

    @Override
    public int hashCode() {
        return manager.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (o instanceof ClusterServiceImpl)
            return manager.nodeLiveId.equals(((ClusterServiceImpl) o).manager.nodeLiveId);
        if (o instanceof ClusterSingleClient)
            return manager.me.httpAddressKey.equals(((ClusterSingleClient) o).serverAddress);
        return false;
    }

}
