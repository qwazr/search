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
 **/
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.lucene.replicator.SessionToken;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ReplicationSessionDefinition {

	final public static ReplicationSessionDefinition EMPTY = new ReplicationSessionDefinition();

	final public String id;
	final public String version;
	final public Map<String, List<String>> sourceFiles;

	public ReplicationSessionDefinition() {
		id = null;
		version = null;
		sourceFiles = null;
	}

	private ReplicationSessionDefinition(final SessionToken token) {
		this.id = token.id;
		this.version = token.version;
		this.sourceFiles = new LinkedHashMap<>();
		token.sourceFiles.forEach((source, revisionFiles) -> {
			List<String> files = new ArrayList<>(revisionFiles.size());
			revisionFiles.forEach(revisionFile -> files.add(revisionFile.fileName));
			sourceFiles.put(source, files);
		});
	}

	/**
	 * Create a new instance. If the token is null the returned structure is empty.
	 *
	 * @param token
	 * @return
	 */
	final static ReplicationSessionDefinition newInstance(final SessionToken token) {
		return token == null ? EMPTY : new ReplicationSessionDefinition(token);
	}
}
