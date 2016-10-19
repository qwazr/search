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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@JsonInclude(Include.NON_EMPTY)
public class IndexStatus {

	final public Long num_docs;
	final public Long num_deleted_docs;
	final public String index_uuid;
	final public String master_uuid;
	final public Long version;
	final public Set<String> analyzers;
	final public Set<String> fields;
	final public IndexSettingsDefinition settings;

	public IndexStatus() {
		num_docs = null;
		num_deleted_docs = null;
		index_uuid = null;
		master_uuid = null;
		version = null;
		analyzers = null;
		fields = null;
		settings = null;
	}

	public IndexStatus(final UUID indexUuid, final UUID masterUuid, final IndexReader indexReader,
			final IndexSettingsDefinition settings,
			final Set<String> analyzers, final Set<String> fields) {
		num_docs = (long) indexReader.numDocs();
		num_deleted_docs = (long) indexReader.numDeletedDocs();
		this.index_uuid = indexUuid == null ? null : indexUuid.toString();
		this.master_uuid = masterUuid == null ? null : masterUuid.toString();
		version = indexReader instanceof DirectoryReader ? ((DirectoryReader) indexReader).getVersion() : null;
		this.settings = settings;
		this.analyzers = analyzers;
		this.fields = fields;
	}

	@Override
	final public boolean equals(final Object o) {
		if (o == null || !(o instanceof IndexStatus))
			return false;
		final IndexStatus s = (IndexStatus) o;
		if (!Objects.equals(num_docs, s.num_docs))
			return false;
		if (!Objects.equals(num_deleted_docs, s.num_deleted_docs))
			return false;
		if (!Objects.equals(index_uuid, s.index_uuid))
			return false;
		if (!Objects.equals(master_uuid, s.master_uuid))
			return false;
		if (!Objects.equals(version, s.version))
			return false;
		if (!Objects.equals(settings, s.settings))
			return false;
		if (!Objects.deepEquals(analyzers, s.analyzers))
			return false;
		if (!Objects.deepEquals(fields, s.fields))
			return false;
		return true;
	}
}