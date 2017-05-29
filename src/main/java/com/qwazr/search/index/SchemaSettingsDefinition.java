/**
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
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SchemaSettingsDefinition {

	final public Integer max_simultaneous_write;
	final public Integer max_simultaneous_read;
	final public Long max_size;
	final public String backup_directory_path;

	@JsonCreator
	private SchemaSettingsDefinition(@JsonProperty("max_simultaneous_write") final Integer maxSimultaneousWrite,
			@JsonProperty("max_simultaneous_read") final Integer maxSimultaneousRead,
			@JsonProperty("max_size") final Long maxSize,
			@JsonProperty("backupDirectoryPath") final String backupDirectoryPath) {
		this.max_simultaneous_write = maxSimultaneousWrite;
		this.max_simultaneous_read = maxSimultaneousRead;
		this.max_size = maxSize;
		this.backup_directory_path = backupDirectoryPath;
	}

	private SchemaSettingsDefinition(Builder builder) {
		this(builder.maxSimultaneousWrite, builder.maxSimultaneousRead, builder.maxSize, builder.backupDirectoryPath);
	}

	@Override
	public boolean equals(final Object e) {
		if (e == null || !(e instanceof SchemaSettingsDefinition))
			return false;
		final SchemaSettingsDefinition def = (SchemaSettingsDefinition) e;
		if (!Objects.equals(max_simultaneous_read, def.max_simultaneous_read))
			return false;
		if (!Objects.equals(max_simultaneous_write, def.max_simultaneous_write))
			return false;
		if (!Objects.equals(max_size, def.max_size))
			return false;
		if (!Objects.equals(backup_directory_path, def.backup_directory_path))
			return false;
		return true;
	}

	static final SchemaSettingsDefinition EMPTY = SchemaSettingsDefinition.of().build();

	public static Builder of() {
		return new Builder();
	}

	public static class Builder {

		public Integer maxSimultaneousWrite;
		public Integer maxSimultaneousRead;
		public Long maxSize;
		public String backupDirectoryPath;

		public Builder maxSimultaneousWrite(Integer maxSimultaneousWrite) {
			this.maxSimultaneousWrite = maxSimultaneousWrite;
			return this;
		}

		public Builder maxSimultaneousRead(Integer maxSimultaneousRead) {
			this.maxSimultaneousRead = maxSimultaneousRead;
			return this;
		}

		public Builder maxSize(Long maxSize) {
			this.maxSize = maxSize;
			return this;
		}

		public Builder backupDirectoryPath(String backupDirectoryPath) {
			this.backupDirectoryPath = backupDirectoryPath;
			return this;
		}

		public SchemaSettingsDefinition build() {
			return new SchemaSettingsDefinition(this);
		}
	}
}
