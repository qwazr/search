/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qwazr.search.replication;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
		getterVisibility = JsonAutoDetect.Visibility.NONE,
		setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ReplicationSession {

	public final String masterUuid;
	public final String sessionUuid;
	public final Map<String, Map<String, Item>> items;
	public final long size;

	@JsonCreator
	ReplicationSession(@JsonProperty("master_uuid") final String masterUuid,
			@JsonProperty("session_uuid") final String sessionUuid,
			@JsonProperty("files") final Map<String, Map<String, Item>> items, @JsonProperty("size") final long size) {
		this.masterUuid = masterUuid;
		this.sessionUuid = sessionUuid;
		this.items = items;
		this.size = size;
	}

	ReplicationSession(final String masterUuid, final String sessionUuid, final Map<String, Map<String, Item>> files) {
		this(masterUuid, sessionUuid, files, computeTotalSize(files));
	}

	static long computeTotalSize(final Map<String, Map<String, Item>> items) {
		long totalSize = 0;
		for (final Map<String, Item> sourceItems : items.values())
			for (final Item item : sourceItems.values())
				if (item != null && item.size != null)
					totalSize += item.size;
		return totalSize;
	}

	@JsonIgnore
	public Map<String, Item> getSourceFiles(final ReplicationProcess.Source source) {
		return source == null ? null : items.get(source.name());
	}

	@JsonIgnore
	public Item getItem(final ReplicationProcess.Source source, final String name) {
		final Map<String, Item> sourceItems = getSourceFiles(source);
		return sourceItems == null ? null : sourceItems.get(name);
	}

	public static final class Item {

		final Long size;
		final Long version;

		@JsonCreator
		Item(@JsonProperty("size") final Long size, @JsonProperty("version") final Long version) {
			this.size = size;
			this.version = version;
		}

		Item(Path itemPath) throws IOException {
			this(Files.size(itemPath), Objects.requireNonNull(Files.getLastModifiedTime(itemPath),
					"Cannot extract last modified on: " + itemPath).toMillis());
		}

		@Override
		public boolean equals(final Object object) {
			if (object == this)
				return true;
			if (object == null || !(object instanceof Item))
				return false;
			final Item item = (Item) object;
			return Objects.equals(size, item.size) && Objects.equals(version, item.version);
		}
	}

}
