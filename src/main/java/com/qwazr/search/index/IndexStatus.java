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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.lucene.index.*;

import java.util.*;

@JsonInclude(Include.NON_EMPTY)
public class IndexStatus {

	final public Long num_docs;
	final public Long num_deleted_docs;
	final public Boolean has_pending_merges;
	final public Boolean has_uncommitted_changes;
	final public Boolean has_deletions;
	final public Integer snapshot_deletion_count;
	final public List<String> snapshot_deletion_commits;
	final public Double ram_buffer_size_mb;
	final public String index_uuid;
	final public String master_uuid;
	final public Long version;
	final public Set<String> analyzers;
	final public Set<String> fields;
	final public IndexSettingsDefinition settings;
	final public Map<String, Set<FieldInfoStatus>> field_infos;

	public IndexStatus() {
		num_docs = null;
		num_deleted_docs = null;
		has_pending_merges = null;
		has_uncommitted_changes = null;
		snapshot_deletion_count = null;
		snapshot_deletion_commits = null;
		ram_buffer_size_mb = null;
		has_deletions = null;
		index_uuid = null;
		master_uuid = null;
		version = null;
		analyzers = null;
		fields = null;
		settings = null;
		field_infos = null;
	}

	public IndexStatus(final UUID indexUuid, final UUID masterUuid, final IndexReader indexReader,
			final IndexWriter indexWriter, final SnapshotDeletionPolicy snapshotDeletionPolicy,
			final IndexSettingsDefinition settings, final Set<String> analyzers, final Set<String> fields) {
		num_docs = (long) indexReader.numDocs();
		num_deleted_docs = (long) indexReader.numDeletedDocs();
		field_infos = new TreeMap<>();
		fillFieldInfos(field_infos, indexReader.leaves());
		if (indexWriter == null) {
			has_pending_merges = null;
			has_uncommitted_changes = null;
			has_deletions = null;
			ram_buffer_size_mb = null;
		} else {
			has_pending_merges = indexWriter.hasPendingMerges();
			has_uncommitted_changes = indexWriter.hasUncommittedChanges();
			has_deletions = indexWriter.hasDeletions();
			ram_buffer_size_mb = indexWriter.getConfig().getRAMBufferSizeMB();
		}
		if (snapshotDeletionPolicy != null) {
			snapshot_deletion_count = snapshotDeletionPolicy.getSnapshotCount();
			snapshot_deletion_commits = new ArrayList<>(snapshot_deletion_count);
			snapshotDeletionPolicy.getSnapshots()
					.forEach(indexCommit -> snapshot_deletion_commits.add(indexCommit.getSegmentsFileName()));
		} else {
			snapshot_deletion_count = null;
			snapshot_deletion_commits = null;
		}

		this.index_uuid = indexUuid == null ? null : indexUuid.toString();
		this.master_uuid = masterUuid == null ? null : masterUuid.toString();
		version = indexReader instanceof DirectoryReader ? ((DirectoryReader) indexReader).getVersion() : null;
		this.settings = settings;
		this.analyzers = analyzers;
		this.fields = fields;
	}

	private void fillFieldInfos(final Map<String, Set<FieldInfoStatus>> field_infos,
			final List<LeafReaderContext> leaves) {
		if (field_infos == null || leaves == null || leaves.isEmpty())
			return;
		leaves.forEach(leafReaderContext -> {
			final FieldInfos fieldInfos = leafReaderContext.reader().getFieldInfos();
			if (fieldInfos == null)
				return;
			fieldInfos.forEach(fieldInfo -> {
				final Set<FieldInfoStatus> set =
						field_infos.computeIfAbsent(fieldInfo.name, s -> new LinkedHashSet<>());
				set.add(new FieldInfoStatus(fieldInfo));
			});
		});
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

	public static class FieldInfoStatus {

		public final Integer number;
		public final Boolean omit_norms;
		public final Boolean has_norms;
		public final Boolean has_payloads;
		public final Boolean has_vectors;
		public final Long doc_values_gen;
		public final DocValuesType doc_values_type;
		public final IndexOptions index_options;
		public final Integer point_dimension_count;
		public final Integer point_num_bytes;

		@JsonIgnore
		private final int hashCode;

		public FieldInfoStatus() {
			number = null;
			omit_norms = null;
			has_norms = null;
			has_payloads = null;
			has_vectors = null;
			doc_values_gen = null;
			doc_values_type = null;
			index_options = null;
			point_dimension_count = null;
			point_num_bytes = null;
			hashCode = buildHashCode();
		}

		private FieldInfoStatus(final FieldInfo info) {
			number = info.number;
			omit_norms = info.omitsNorms();
			has_norms = info.hasNorms();
			has_payloads = info.hasPayloads();
			has_vectors = info.hasVectors();
			doc_values_gen = info.getDocValuesGen();
			doc_values_type = info.getDocValuesType();
			index_options = info.getIndexOptions();
			point_dimension_count = info.getPointDimensionCount();
			point_num_bytes = info.getPointNumBytes();
			hashCode = buildHashCode();
		}

		private final int buildHashCode() {
			final HashCodeBuilder builder = new HashCodeBuilder();
			builder.append(number);
			builder.append(omit_norms);
			builder.append(has_norms);
			builder.append(has_payloads);
			builder.append(has_vectors);
			builder.append(doc_values_gen);
			builder.append(doc_values_type);
			builder.append(index_options);
			builder.append(point_dimension_count);
			builder.append(point_num_bytes);
			return builder.toHashCode();
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(final Object o) {
			if (o == null || !(o instanceof FieldInfoStatus))
				return false;
			final FieldInfoStatus info = (FieldInfoStatus) o;
			if (!Objects.equals(number, info.number))
				return false;
			if (!Objects.equals(omit_norms, info.omit_norms))
				return false;
			if (!Objects.equals(has_norms, info.has_norms))
				return false;
			if (!Objects.equals(has_payloads, info.has_payloads))
				return false;
			if (!Objects.equals(has_vectors, info.has_vectors))
				return false;
			if (!Objects.equals(doc_values_gen, info.doc_values_gen))
				return false;
			if (!Objects.equals(doc_values_type, info.doc_values_type))
				return false;
			if (!Objects.equals(index_options, info.index_options))
				return false;
			if (!Objects.equals(point_dimension_count, info.point_dimension_count))
				return false;
			if (!Objects.equals(point_num_bytes, info.point_num_bytes))
				return false;
			return true;
		}
	}
}