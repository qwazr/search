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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ReplicationSession {

	public final String id;

	public final Map<String, Map<String, Long>> files;

	@JsonCreator
	ReplicationSession(@JsonProperty("id") final String id,
			@JsonProperty("files") final Map<String, Map<String, Long>> files) {
		this.id = id;
		this.files = files;
	}

	@JsonIgnore
	public Map<String, Long> getSourceFiles(final ReplicationProcess.Source source) {
		return source == null ? null : files.get(source.name());
	}

	@JsonIgnore
	public Long getFileLength(final ReplicationProcess.Source source, final String fileName) {
		final Map<String, Long> sourceFiles = files.get(source.name());
		return sourceFiles == null ? null : sourceFiles.get(fileName);
	}

}
