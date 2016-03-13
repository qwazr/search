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

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SchemaSettingsDefinition {

	final public Integer max_simultaneous_write;
	final public Integer max_simultaneous_read;
	final public Long max_size;

	public SchemaSettingsDefinition() {
		max_simultaneous_write = null;
		max_simultaneous_read = null;
		max_size = null;
	}

	public SchemaSettingsDefinition(Integer max_simultaneous_write, Integer max_simultaneous_read, Long max_size) {
		this.max_simultaneous_write = max_simultaneous_write;
		this.max_simultaneous_read = max_simultaneous_read;
		this.max_size = max_size;
	}

	static final SchemaSettingsDefinition EMPTY = new SchemaSettingsDefinition();

}
