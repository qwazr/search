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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

final class ClusterNode {

	final ClusterNodeAddress address;
	final UUID nodeLiveId;

	private final Set<String> groups;
	private final Set<String> services;

	private volatile Long expirationTimeMs;

	private volatile List<String> groupsCache;
	private volatile List<String> servicesCache;

	ClusterNode(final ClusterNodeAddress address, final UUID nodeLiveId, final Long expirationTimeMs) {
		this.nodeLiveId = nodeLiveId;
		this.address = address;
		this.expirationTimeMs = expirationTimeMs;
		this.groups = new HashSet<>();
		this.services = new HashSet<>();
		groupsCache = null;
		servicesCache = null;
	}

	final void setExpirationTime(final Long expirationTimeMs) {
		this.expirationTimeMs = expirationTimeMs;
	}

	final Long getExpirationTimeMs() {
		return expirationTimeMs;
	}

	final boolean isExpired(final long currentTimeMs) {
		final Long ns = expirationTimeMs;
		return ns != null && currentTimeMs > expirationTimeMs;
	}

	final void registerGroups(final Collection<String> newGroups) {
		if (newGroups == null)
			return;
		groups.clear();
		groups.addAll(newGroups);
		groupsCache = new ArrayList<>(groups);
	}

	final void registerServices(final Collection<String> newServices) {
		if (newServices == null)
			return;
		services.clear();
		services.addAll(newServices);
		servicesCache = new ArrayList<>(services);
	}

	final Collection<String> getGroups() {
		return groupsCache;
	}

	final Collection<String> getServices() {
		return servicesCache;
	}

}
