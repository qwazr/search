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
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SchemaSettingsDefinition {

	@JsonProperty("max_simultaneous_write")
	final public Integer maxSimultaneousWrite;
	@JsonProperty("max_simultaneous_read")
	final public Integer maxSimultaneousRead;
	@JsonProperty("max_size")
	final public Long maxSize;
	@JsonProperty("backup_directory_path")
	final public String backupDirectoryPath;

	@JsonCreator
	private SchemaSettingsDefinition(@JsonProperty("max_simultaneous_write") final Integer maxSimultaneousWrite,
			@JsonProperty("max_simultaneous_read") final Integer maxSimultaneousRead,
			@JsonProperty("max_size") final Long maxSize,
			@JsonProperty("backup_directory_path") final String backupDirectoryPath) {
		this.maxSimultaneousWrite = maxSimultaneousWrite;
		this.maxSimultaneousRead = maxSimultaneousRead;
		this.maxSize = maxSize;
		this.backupDirectoryPath = backupDirectoryPath;
	}

	private SchemaSettingsDefinition(Builder builder) {
		this(builder.maxSimultaneousWrite, builder.maxSimultaneousRead, builder.maxSize, builder.backupDirectoryPath);
	}

	@Override
	public int hashCode() {
		return Objects.hash(maxSimultaneousWrite, maxSimultaneousRead, maxSize, backupDirectoryPath);
	}

	@Override
	public boolean equals(final Object e) {
		if (!(e instanceof SchemaSettingsDefinition))
			return false;
		final SchemaSettingsDefinition def = (SchemaSettingsDefinition) e;
		if (!Objects.equals(maxSimultaneousWrite, def.maxSimultaneousWrite))
			return false;
		if (!Objects.equals(maxSimultaneousRead, def.maxSimultaneousRead))
			return false;
		if (!Objects.equals(maxSize, def.maxSize))
			return false;
		if (!Objects.equals(backupDirectoryPath, def.backupDirectoryPath))
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
