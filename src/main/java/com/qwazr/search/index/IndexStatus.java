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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.FileUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.LiveIndexWriterConfig;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.index.SegmentCommitInfo;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LRUQueryCache;
import org.apache.lucene.search.QueryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IndexStatus {

	private final static Logger LOGGER = LoggerFactory.getLogger(IndexStatus.class);

	final public Long num_docs;
	final public Long num_deleted_docs;
	final public Boolean has_pending_merges;
	final public Boolean has_uncommitted_changes;
	final public Boolean has_deletions;
	final public Double ram_buffer_size_mb;
	final public String index_uuid;
	final public String master_uuid;
	final public Long version;
	final public Set<String> analyzers;
	final public Set<String> fields;
	final public IndexSettingsDefinition settings;
	final public Map<String, Set<FieldInfoStatus>> field_infos;
	final public Integer number_of_segment;
	final public List<SegmentInfoStatus> segment_infos;
	final public Long segments_bytes_size;
	final public String segments_size;
	final public MergePolicyStatus merge_policy;
	final public QueryCacheStats query_cache;
	final public Map<String, String> commit_user_data;

	@JsonCreator
	IndexStatus(@JsonProperty("num_docs") Long num_docs, @JsonProperty("num_deleted_docs") Long num_deleted_docs,
			@JsonProperty("has_pending_merges") Boolean has_pending_merges,
			@JsonProperty("has_uncommitted_changes") Boolean has_uncommitted_changes,
			@JsonProperty("has_deletions") Boolean has_deletions,
			@JsonProperty("ram_buffer_size_mb") Double ram_buffer_size_mb,
			@JsonProperty("index_uuid") String index_uuid, @JsonProperty("master_uuid") String master_uuid,
			@JsonProperty("version") Long version, @JsonProperty("analyzers") Set<String> analyzers,
			@JsonProperty("fields") Set<String> fields, @JsonProperty("settings") IndexSettingsDefinition settings,
			@JsonProperty("field_infos") Map<String, Set<FieldInfoStatus>> field_infos,
			@JsonProperty("number_of_segment") Integer number_of_segment,
			@JsonProperty("segment_infos") List<SegmentInfoStatus> segment_infos,
			@JsonProperty("segments_bytes_size") Long segments_bytes_size,
			@JsonProperty("segments_size") String segments_size,
			@JsonProperty("merge_policy") MergePolicyStatus merge_policy,
			@JsonProperty("query_cache") QueryCacheStats query_cache,
			@JsonProperty("commit_user_data") Map<String, String> commit_user_data) {
		this.num_docs = num_docs;
		this.num_deleted_docs = num_deleted_docs;
		this.merge_policy = merge_policy;
		this.has_pending_merges = has_pending_merges;
		this.has_uncommitted_changes = has_uncommitted_changes;
		this.ram_buffer_size_mb = ram_buffer_size_mb;
		this.has_deletions = has_deletions;
		this.index_uuid = index_uuid;
		this.master_uuid = master_uuid;
		this.version = version;
		this.analyzers = analyzers;
		this.fields = fields;
		this.settings = settings;
		this.field_infos = field_infos;
		this.number_of_segment = number_of_segment;
		this.segment_infos = segment_infos;
		this.segments_bytes_size = segments_bytes_size;
		this.segments_size = segments_size;
		this.query_cache = query_cache;
		this.commit_user_data = commit_user_data;
	}

	public IndexStatus(final UUID indexUuid, final UUID masterUuid, final IndexSearcher indexSearcher,
			final IndexWriter indexWriter, final IndexSettingsDefinition settings, final Set<String> analyzers,
			final Set<String> fields) {
		final IndexReader indexReader = indexSearcher.getIndexReader();
		num_docs = (long) indexReader.numDocs();
		num_deleted_docs = (long) indexReader.numDeletedDocs();
		field_infos = new TreeMap<>();
		fillFieldInfos(field_infos, indexReader.leaves());
		if (indexWriter == null) {
			merge_policy = null;
			has_pending_merges = null;
			has_uncommitted_changes = null;
			has_deletions = null;
			ram_buffer_size_mb = null;
			commit_user_data = null;
		} else {
			final LiveIndexWriterConfig config = indexWriter.getConfig();
			final MergePolicy mergePolicy = config.getMergePolicy();
			merge_policy = mergePolicy == null ? null : new MergePolicyStatus(mergePolicy);
			has_pending_merges = indexWriter.hasPendingMerges();
			has_uncommitted_changes = indexWriter.hasUncommittedChanges();
			has_deletions = indexWriter.hasDeletions();
			ram_buffer_size_mb = config.getRAMBufferSizeMB();
			final Iterable<Map.Entry<String, String>> commitData = indexWriter.getLiveCommitData();
			if (commitData != null) {
				commit_user_data = new LinkedHashMap<>();
				commitData.forEach(entry -> commit_user_data.put(entry.getKey(), entry.getValue()));
			} else
				commit_user_data = null;
		}

		this.index_uuid = indexUuid == null ? null : indexUuid.toString();
		this.master_uuid = masterUuid == null ? null : masterUuid.toString();
		this.version = indexReader instanceof DirectoryReader ? ((DirectoryReader) indexReader).getVersion() : null;
		this.settings = settings;
		this.analyzers = analyzers;
		this.fields = fields;

		final SegmentInfos segmentInfos = getSegmentInfos(indexReader);
		if (segmentInfos != null) {
			number_of_segment = segmentInfos.size();
			final AtomicLong segmentsBytesSize = new AtomicLong();
			segment_infos = getSegmentsInfoStatus(segmentInfos, segmentsBytesSize);
			segments_bytes_size = segmentsBytesSize.get();
			segments_size = FileUtils.byteCountToDisplaySize(segments_bytes_size);
		} else {
			number_of_segment = null;
			segment_infos = null;
			segments_bytes_size = null;
			segments_size = null;
		}
		final QueryCache queryCache = indexSearcher.getQueryCache();
		this.query_cache = queryCache != null && queryCache instanceof LRUQueryCache ? new QueryCacheStats(
				(LRUQueryCache) queryCache) : null;
	}

	private static SegmentInfos getSegmentInfos(final IndexReader indexReader) {
		if (!(indexReader instanceof DirectoryReader))
			return null;
		try {
			final IndexCommit indexCommit = ((DirectoryReader) indexReader).getIndexCommit();
			if (indexCommit == null)
				return null;
			return SegmentInfos.readCommit(indexCommit.getDirectory(), indexCommit.getSegmentsFileName());
		} catch (IOException e) {
			LOGGER.warn("Fail while extracting Segment information", e);
			return null;
		}
	}

	private static ArrayList<SegmentInfoStatus> getSegmentsInfoStatus(final SegmentInfos segmentInfos,
			final AtomicLong totalBytesSize) {
		if (segmentInfos == null)
			return null;
		final ArrayList<SegmentInfoStatus> segmentInfoStatuses = new ArrayList<>(segmentInfos.size());
		for (SegmentCommitInfo segmentInfo : segmentInfos) {
			try {
				final SegmentInfoStatus status = new SegmentInfoStatus(segmentInfo);
				segmentInfoStatuses.add(status);
				totalBytesSize.addAndGet(status.sizeInBytes);
			} catch (IOException e) {
				LOGGER.warn("Fail while extracting Segment information", e);
			}
		}
		return segmentInfoStatuses;
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
				final Set<FieldInfoStatus> set = field_infos.computeIfAbsent(fieldInfo.name,
						s -> new LinkedHashSet<>());
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

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public static class MergePolicyStatus {

		final public String type;
		final public Double max_cfs_segment_size_mb;
		final public Double no_cfs_ratio;

		//TieredMergePolicy
		final public Integer max_merge_at_once;
		final public Double max_merged_segment_mb;
		final public Double segments_per_tier;

		@JsonCreator
		MergePolicyStatus(@JsonProperty("type") String type,
				@JsonProperty("max_cfs_segment_size_mb") Double max_cfs_segment_size_mb,
				@JsonProperty("no_cfs_ratio") Double no_cfs_ratio,
				@JsonProperty("max_merge_at_once") Integer max_merge_at_once,
				@JsonProperty("max_merged_segment_mb") Double max_merged_segment_mb,
				@JsonProperty("segments_per_tier") Double segments_per_tier) {
			this.type = type;
			this.max_cfs_segment_size_mb = max_cfs_segment_size_mb;
			this.no_cfs_ratio = no_cfs_ratio;

			this.max_merge_at_once = max_merge_at_once;
			this.max_merged_segment_mb = max_merged_segment_mb;
			this.segments_per_tier = segments_per_tier;
		}

		MergePolicyStatus(final MergePolicy mergePolicy) {
			type = mergePolicy.getClass().getTypeName();
			max_cfs_segment_size_mb = mergePolicy.getMaxCFSSegmentSizeMB();
			no_cfs_ratio = mergePolicy.getNoCFSRatio();
			if (mergePolicy instanceof TieredMergePolicy) {
				TieredMergePolicy tmp = (TieredMergePolicy) mergePolicy;
				max_merge_at_once = tmp.getMaxMergeAtOnce();
				max_merged_segment_mb = tmp.getMaxMergedSegmentMB();
				segments_per_tier = tmp.getSegmentsPerTier();
			} else {
				max_merge_at_once = null;
				max_merged_segment_mb = null;
				segments_per_tier = null;
			}
		}
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

		@JsonCreator
		FieldInfoStatus(@JsonProperty("number") Integer number, @JsonProperty("omit_norms") Boolean omit_norms,
				@JsonProperty("has_norms") Boolean has_norms, @JsonProperty("has_payloads") Boolean has_payloads,
				@JsonProperty("has_vectors") Boolean has_vectors, @JsonProperty("doc_values_gen") Long doc_values_gen,
				@JsonProperty("doc_values_type") DocValuesType doc_values_type,
				@JsonProperty("index_options") IndexOptions index_options,
				@JsonProperty("point_dimension_count") Integer point_dimension_count,
				@JsonProperty("point_num_bytes") Integer point_num_bytes) {
			this.number = number;
			this.omit_norms = omit_norms;
			this.has_norms = has_norms;
			this.has_payloads = has_payloads;
			this.has_vectors = has_vectors;
			this.doc_values_gen = doc_values_gen;
			this.doc_values_type = doc_values_type;
			this.index_options = index_options;
			this.point_dimension_count = point_dimension_count;
			this.point_num_bytes = point_num_bytes;
			hashCode = buildHashCode();
		}

		private FieldInfoStatus(final FieldInfo info) {
			this(info.number, info.omitsNorms(), info.hasNorms(), info.hasPayloads(), info.hasVectors(),
					info.getDocValuesGen(), info.getDocValuesType(), info.getIndexOptions(),
					info.getPointDimensionCount(), info.getPointNumBytes());
		}

		private int buildHashCode() {
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

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public static class SegmentInfoStatus {

		@JsonProperty("size_in_bytes")
		final public Long sizeInBytes;

		final public String size;

		final public Collection<String> files;

		@JsonCreator
		SegmentInfoStatus(@JsonProperty("size_in_bytes") Long sizeInBytes, @JsonProperty("size") String size,
				@JsonProperty("files") Collection<String> files) {
			this.sizeInBytes = sizeInBytes;
			this.size = size;
			this.files = files;
		}

		SegmentInfoStatus(final SegmentCommitInfo segmentInfo) throws IOException {
			this(segmentInfo.sizeInBytes(), FileUtils.byteCountToDisplaySize(segmentInfo.sizeInBytes()),
					segmentInfo.files());
		}
	}

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public static class QueryCacheStats {

		public final Long cache_count;
		public final Long cache_size;
		public final Long eviction_count;
		public final Long hit_count;
		public final Long miss_count;
		public final Long total_count;
		public final Float hit_rate;
		public final Float miss_rate;

		@JsonCreator
		QueryCacheStats(@JsonProperty("cache_count") Long cache_count, @JsonProperty("cache_size") Long cache_size,
				@JsonProperty("eviction_count") Long eviction_count, @JsonProperty("hit_count") Long hit_count,
				@JsonProperty("miss_count") Long miss_count, @JsonProperty("total_count") Long total_count,
				@JsonProperty("hit_rate") Float hit_rate, @JsonProperty("miss_rate") Float miss_rate) {
			this.cache_count = cache_count;
			this.cache_size = cache_size;
			this.eviction_count = eviction_count;
			this.hit_count = hit_count;
			this.miss_count = miss_count;
			this.total_count = total_count;
			this.hit_rate = hit_rate;
			this.miss_rate = miss_rate;
		}

		private QueryCacheStats(final LRUQueryCache queryCache) {
			this(queryCache.getCacheCount(), queryCache.getCacheSize(), queryCache.getEvictionCount(),
					queryCache.getHitCount(), queryCache.getMissCount(), queryCache.getTotalCount(),
					(float) (queryCache.getHitCount() * 100) / queryCache.getTotalCount(),
					(float) (queryCache.getMissCount() * 100) / queryCache.getTotalCount());
		}
	}
}