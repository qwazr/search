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

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

@JsonInclude(Include.NON_NULL)
public class ClusterNodeJson {

	public final String address;
	@JsonProperty("node_live_id")
	public final String nodeLiveId;
	@JsonProperty("time_to_live")
	public final Integer timeToLive;
	public final Set<String> services;
	public final Set<String> groups;

	@JsonCreator
	private ClusterNodeJson(@JsonProperty("address") String address, @JsonProperty("node_live_id") String nodeLiveId,
			@JsonProperty("time_to_live") Integer timeToLive, @JsonProperty("services") Set<String> services,
			@JsonProperty("groups") Set<String> groups) {
		this.address = address;
		this.nodeLiveId = nodeLiveId;
		this.timeToLive = timeToLive;
		this.services = services;
		this.groups = groups;
	}

	ClusterNodeJson(final ClusterNode clusterNode, Integer timeToLive) {
		this(clusterNode.address.httpAddressKey,
				clusterNode.nodeLiveId == null ? null : clusterNode.nodeLiveId.toString(), timeToLive,
				toTreeSet(clusterNode.getServices()), toTreeSet(clusterNode.getGroups()));
	}

	private static TreeSet<String> toTreeSet(Collection<String> collection) {
		return collection == null ? null : new TreeSet<>(collection);
	}
}
