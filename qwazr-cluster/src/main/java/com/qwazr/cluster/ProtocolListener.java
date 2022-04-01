/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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

import com.qwazr.server.UdpServerThread;
import com.qwazr.utils.concurrent.PeriodicThread;

import java.util.Collection;

abstract class ProtocolListener extends PeriodicThread implements UdpServerThread.PacketListener {

    protected final ClusterManager manager;

    final private static int DEFAULT_PERIOD_SEC = 120;
    final private static int TWICE_DEFAULT_PERIOD_MS = DEFAULT_PERIOD_SEC * 1000 * 2;

    protected ProtocolListener(final ClusterManager manager) {
        super(DEFAULT_PERIOD_SEC);
        this.manager = manager;
    }

    protected synchronized void joinCluster(final Collection<String> services) {
        if (services != null) {
            manager.myServices.clear();
            manager.myServices.addAll(services);
        }
    }

    protected ClusterNode registerNode(final AddressContent message) {
        final Long expirationTime = manager.isMe(message) ? null : System.currentTimeMillis() + TWICE_DEFAULT_PERIOD_MS;
        if (message instanceof FullContent)
            return manager.clusterNodeMap.registerFull((FullContent) message, expirationTime);
        else
            return manager.clusterNodeMap.registerAddress(message, expirationTime);
    }

    protected abstract void leaveCluster();

    @Override
    protected void runner() {
        manager.clusterNodeMap.removeExpired();
    }
}
