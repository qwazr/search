/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SchemaSettingsDefinition {

	final public Integer max_simultaneous_write;
	final public Integer max_simultaneous_read;
	final public Long max_size;
	final public String backup_directory_path;

	public SchemaSettingsDefinition() {
		max_simultaneous_write = null;
		max_simultaneous_read = null;
		max_size = null;
		backup_directory_path = null;
	}

	public SchemaSettingsDefinition(final Integer max_simultaneous_write, final Integer max_simultaneous_read,
			final Long max_size, final String backupDirectoryPath) {
		this.max_simultaneous_write = max_simultaneous_write;
		this.max_simultaneous_read = max_simultaneous_read;
		this.max_size = max_size;
		this.backup_directory_path = backupDirectoryPath;
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

	static final SchemaSettingsDefinition EMPTY = new SchemaSettingsDefinition();

}
