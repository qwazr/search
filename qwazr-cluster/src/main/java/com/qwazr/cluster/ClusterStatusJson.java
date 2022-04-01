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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.server.ServerException;

import java.util.Date;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

@JsonInclude(Include.NON_NULL)
public class ClusterStatusJson {

	public final String me;
	public final String uuid;
	public final String webapp;
	public final TreeMap<String, TreeSet<String>> groups;
	public final TreeMap<String, ClusterServiceStatusJson.StatusEnum> services;
	@JsonProperty("last_keep_alive_execution")
	public final Date lastKeepAliveExecution;
	@JsonProperty("active_nodes")
	public final TreeMap<String, ClusterNodeJson> activeNodes;
	public final TreeSet<String> masters;

	@JsonCreator
	private ClusterStatusJson(@JsonProperty("me") final String me, @JsonProperty("uuid") final String uuid,
			@JsonProperty("webapp") final String webapp,
			@JsonProperty("active_nodes") final TreeMap<String, ClusterNodeJson> activeNodes,
			@JsonProperty("groups") final TreeMap<String, TreeSet<String>> groups,
			@JsonProperty("services") final TreeMap<String, ClusterServiceStatusJson.StatusEnum> services,
			@JsonProperty("masters") final TreeSet<String> masters,
			@JsonProperty("last_keep_alive_execution") final Date lastKeepAliveExecution) throws ServerException {
		this.me = me;
		this.uuid = uuid;
		this.webapp = webapp;
		this.groups = groups;
		this.services = services;
		this.masters = masters;
		this.lastKeepAliveExecution = lastKeepAliveExecution;
		this.activeNodes = activeNodes;
	}

	ClusterStatusJson(final String me, final UUID uuid, final String webapp,
			final TreeMap<String, ClusterNodeJson> nodesMap, final TreeMap<String, TreeSet<String>> groups,
			final TreeMap<String, TreeSet<String>> services, final Set<String> masters,
			final Date lastKeepAliveExecution) throws ServerException {
		this(me, uuid.toString(), webapp, nodesMap, groups, toServices(services),
				masters == null ? null : new TreeSet<>(masters), lastKeepAliveExecution);
	}

	final static TreeMap<String, ClusterServiceStatusJson.StatusEnum> EMPTY = new TreeMap<>();

	private static TreeMap<String, ClusterServiceStatusJson.StatusEnum> toServices(
			final TreeMap<String, TreeSet<String>> services) {
		if (services == null || services.isEmpty())
			return EMPTY;
		final TreeMap<String, ClusterServiceStatusJson.StatusEnum> servicesTree = new TreeMap<>();
		services.forEach(
				(service, nodesSet) -> servicesTree.put(service, ClusterServiceStatusJson.StatusEnum.of(nodesSet)));
		return servicesTree;
	}

}
