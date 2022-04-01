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
import java.util.SortedSet;

@JsonInclude(Include.NON_NULL)
public class ClusterServiceStatusJson {

	public enum StatusEnum {
		ok, failure;

		static StatusEnum of(Collection<String> nodesSet) {
			return nodesSet == null || nodesSet.isEmpty() ? failure : ok;
		}
	}

	public final String leader;
	public final StatusEnum status;
	@JsonProperty("active_count")
	public final int activeCount;
	public final SortedSet<String> active;

	@JsonCreator
	private ClusterServiceStatusJson(@JsonProperty("leader") String leader, @JsonProperty("status") StatusEnum status,
			@JsonProperty("active_count") int activeCount, @JsonProperty("active") SortedSet<String> active) {
		this.leader = leader;
		this.status = status;
		this.activeCount = activeCount;
		this.active = active;
	}

	static ClusterServiceStatusJson of(SortedSet<String> nodes) {
		if (nodes == null || nodes.isEmpty())
			return new ClusterServiceStatusJson(null, StatusEnum.failure, 0, null);
		else
			return new ClusterServiceStatusJson(nodes.first(), StatusEnum.ok, nodes.size(), nodes);
	}
}
