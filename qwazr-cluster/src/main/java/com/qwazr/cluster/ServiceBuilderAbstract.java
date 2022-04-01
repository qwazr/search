/*
 * Copyright 2016 Emmanuel Keller / QWAZR
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

import com.qwazr.server.RemoteService;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Objects;
import java.util.SortedSet;

public class ServiceBuilderAbstract<T> implements ServiceBuilderInterface<T> {

	final protected ClusterManager clusterManager;
	final protected String serviceName;
	final protected T local;

	protected ServiceBuilderAbstract(final ClusterManager clusterManager, final String serviceName, final T local) {
		this.clusterManager = clusterManager;
		this.serviceName = serviceName;
		this.local = local;
	}

	@Override
	final public T getService(final String node) throws URISyntaxException {
		if (node == null)
			return null;
		if (local != null && clusterManager != null && node.equals(clusterManager.me.httpAddressKey))
			return local;
		return remote(RemoteService.of(node).build());
	}

	@Override
	final public T getService(final Collection<String> nodes) throws URISyntaxException {
		if (nodes == null || nodes.isEmpty())
			return null;
		return nodes.size() == 1 ? getService(nodes.iterator().next()) : remotes(RemoteService.build(nodes));
	}

	@Override
	public T getActive(final String group) throws URISyntaxException {
		Objects.requireNonNull(serviceName, "The service name is missing");
		final SortedSet<String> nodes = clusterManager.getNodesByGroupByService(group, serviceName);
		if (nodes == null || nodes.isEmpty())
			return null;
		return nodes.size() == 1 ? getService(nodes.first()) : getService(nodes);
	}

	@Override
	final public T getRandom(final String group) throws URISyntaxException {
		Objects.requireNonNull(serviceName, "The service name is missing");
		return getService(clusterManager.getRandomNode(group, serviceName));
	}

	@Override
	final public T getLeader(final String group) throws URISyntaxException {
		Objects.requireNonNull(serviceName, "The service name is missing");
		return getService(clusterManager.getLeaderNode(group, serviceName));
	}

	@Override
	final public T local() {
		return local;
	}
}
