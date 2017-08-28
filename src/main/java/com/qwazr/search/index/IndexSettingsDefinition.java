/*
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.annotations.Index;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.StringUtils;
import org.apache.lucene.search.similarities.Similarity;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Supplier;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IndexSettingsDefinition {

	public enum Type {
		FSDirectory, RAMDirectory
	}

	public enum MergeScheduler {
		NO, CONCURRENT, SERIAL
	}

	public static final int DEFAULT_MAX_MERGE_AT_ONCE = 10;
	public static final int DEFAULT_SEGMENTS_PER_TIER = 10;
	public static final double DEFAULT_MAX_MERGED_SEGMENT_MB = 5 * 1024 * 1024;
	public static final double DEFAULT_NRT_CACHING_DIRECTORY_MERGE_SIZE_MB = 5;
	public static final double DEFAULT_NRT_CACHING_DIRECTORY_MAX_CACHED_MB = 60;

	@JsonProperty("similarity_class")
	final public String similarityClass;

	final public RemoteIndex master;

	@JsonProperty("directory_type")
	final public Type directoryType;

	@JsonProperty("merge_scheduler")
	final public MergeScheduler mergeScheduler;

	@JsonProperty("ram_buffer_size")
	final public Double ramBufferSize;

	@JsonProperty("max_merge_at_once")
	final public Integer maxMergeAtOnce;

	@JsonProperty("max_merged_segment_mb")
	final public Double maxMergedSegmentMB;

	@JsonProperty("segments_per_tier")
	final public Double segmentsPerTier;

	@JsonProperty("enable_taxonomy_index")
	final public Boolean enableTaxonomyIndex;

	@JsonProperty("sorted_set_facet_field")
	final public String sortedSetFacetField;

	@JsonProperty("index_reader_warmer")
	final public Boolean indexReaderWarmer;

	@JsonProperty("merged_segment_warmer")
	final public Boolean mergedSegmentWarmer;

	@JsonProperty("nrt_caching_directory_max_merge_size_mb")
	final public Double nrtCachingDirectoryMaxMergeSizeMB;

	@JsonProperty("nrt_caching_directory_max_cached_mb")
	final public Double nrtCachingDirectoryMaxCachedMB;

	public IndexSettingsDefinition() {
		directoryType = null;
		mergeScheduler = null;
		similarityClass = null;
		master = null;
		ramBufferSize = null;
		maxMergeAtOnce = null;
		maxMergedSegmentMB = null;
		segmentsPerTier = null;
		enableTaxonomyIndex = null;
		sortedSetFacetField = null;
		indexReaderWarmer = null;
		mergedSegmentWarmer = null;
		nrtCachingDirectoryMaxMergeSizeMB = null;
		nrtCachingDirectoryMaxCachedMB = null;
	}

	private IndexSettingsDefinition(final Builder builder) {
		this.directoryType = builder.directoryType;
		this.mergeScheduler = builder.mergeScheduler;
		this.similarityClass = builder.similarityClass;
		this.master = builder.master;
		this.ramBufferSize = builder.ramBufferSize;
		this.maxMergeAtOnce = builder.maxMergeAtOnce;
		this.maxMergedSegmentMB = builder.maxMergedSegmentMB;
		this.segmentsPerTier = builder.segmentsPerTier;
		this.enableTaxonomyIndex = builder.enableTaxonomyIndex;
		this.sortedSetFacetField = builder.sortedSetFacetField;
		this.indexReaderWarmer = builder.indexReaderWarmer;
		this.mergedSegmentWarmer = builder.mergedSegmentWarmer;
		this.nrtCachingDirectoryMaxMergeSizeMB = builder.nrtCachingDirectoryMaxMergeSizeMB;
		this.nrtCachingDirectoryMaxCachedMB = builder.nrtCachingDirectoryMaxCachedMB;
	}

	final static IndexSettingsDefinition EMPTY = new IndexSettingsDefinition();

	public static IndexSettingsDefinition newSettings(final String jsonString) throws IOException {
		if (StringUtils.isEmpty(jsonString))
			return null;
		return ObjectMappers.JSON.readValue(jsonString, IndexSettingsDefinition.class);
	}

	public static boolean useTaxonomyIndex(final IndexSettingsDefinition settings) {
		return settings != null && (settings.enableTaxonomyIndex == null ? false : settings.enableTaxonomyIndex);
	}

	@Override
	public final boolean equals(final Object o) {
		if (o == null || !(o instanceof IndexSettingsDefinition))
			return false;
		final IndexSettingsDefinition s = (IndexSettingsDefinition) o;
		if (!Objects.equals(directoryType, s.directoryType))
			return false;
		if (!Objects.equals(mergeScheduler, s.mergeScheduler))
			return false;
		if (!Objects.equals(similarityClass, s.similarityClass))
			return false;
		if (!Objects.equals(master, s.master))
			return false;
		if (!Objects.equals(ramBufferSize, s.ramBufferSize))
			return false;
		if (!Objects.equals(maxMergeAtOnce, s.maxMergeAtOnce))
			return false;
		if (!Objects.equals(maxMergedSegmentMB, s.maxMergedSegmentMB))
			return false;
		if (!Objects.equals(segmentsPerTier, s.segmentsPerTier))
			return false;
		if (!Objects.equals(enableTaxonomyIndex, s.enableTaxonomyIndex))
			return false;
		if (!Objects.equals(sortedSetFacetField, s.sortedSetFacetField))
			return false;
		if (!Objects.equals(indexReaderWarmer, s.indexReaderWarmer))
			return false;
		if (!Objects.equals(mergedSegmentWarmer, s.mergedSegmentWarmer))
			return false;
		if (!Objects.equals(nrtCachingDirectoryMaxMergeSizeMB, s.nrtCachingDirectoryMaxMergeSizeMB))
			return false;
		if (!Objects.equals(nrtCachingDirectoryMaxCachedMB, s.nrtCachingDirectoryMaxCachedMB))
			return false;
		return true;
	}

	public static Builder of(final Index index) throws URISyntaxException {
		return new Builder(index);
	}

	public static Builder of(final IndexSettingsDefinition indexSettings) {
		return new Builder(indexSettings);
	}

	public static Builder of() {
		return new Builder();
	}

	public static class Builder {

		private Type directoryType;
		private MergeScheduler mergeScheduler;
		private String similarityClass;
		private RemoteIndex master;
		private Double ramBufferSize;
		private Integer maxMergeAtOnce;
		private Double maxMergedSegmentMB;
		private Double segmentsPerTier;
		private Boolean enableTaxonomyIndex;
		private String sortedSetFacetField;
		private Boolean indexReaderWarmer;
		private Boolean mergedSegmentWarmer;
		private Double nrtCachingDirectoryMaxMergeSizeMB;
		private Double nrtCachingDirectoryMaxCachedMB;

		private Builder() {
		}

		private Builder(final Index annotatedIndex) throws URISyntaxException {
			directoryType = annotatedIndex.type();
			mergeScheduler = annotatedIndex.mergeScheduler();
			similarityClass(annotatedIndex.similarityClass());
			master(annotatedIndex.replicationMaster());
			ramBufferSize(annotatedIndex.ramBufferSize());
			maxMergeAtOnce(annotatedIndex.maxMergeAtOnce());
			maxMergedSegmentMB(annotatedIndex.maxMergedSegmentMB());
			segmentsPerTier(annotatedIndex.segmentsPerTier());
			enableTaxonomyIndex = annotatedIndex.enableTaxonomyIndex();
			sortedSetFacetField = annotatedIndex.sortedSetFacetField();
			indexReaderWarmer = annotatedIndex.indexReaderWarmer();
			mergedSegmentWarmer = annotatedIndex.mergedSegmentWarmer();
			nrtCachingDirectoryMaxMergeSizeMB = annotatedIndex.nrtCachingDirectoryMaxMergeSizeMB();
			nrtCachingDirectoryMaxCachedMB = annotatedIndex.nrtCachingDirectoryMaxCachedMB();
		}

		private Builder(final IndexSettingsDefinition settings) {
			this.directoryType = settings.directoryType;
			this.mergeScheduler = settings.mergeScheduler;
			this.similarityClass = settings.similarityClass;
			this.master = settings.master;
			this.ramBufferSize = settings.ramBufferSize;
			this.maxMergeAtOnce = settings.maxMergeAtOnce;
			this.maxMergedSegmentMB = settings.maxMergedSegmentMB;
			this.segmentsPerTier = settings.segmentsPerTier;
			this.enableTaxonomyIndex = settings.enableTaxonomyIndex;
			this.sortedSetFacetField = settings.sortedSetFacetField;
			this.indexReaderWarmer = settings.indexReaderWarmer;
			this.mergedSegmentWarmer = settings.mergedSegmentWarmer;
			this.nrtCachingDirectoryMaxMergeSizeMB = settings.nrtCachingDirectoryMaxMergeSizeMB;
			this.nrtCachingDirectoryMaxCachedMB = settings.nrtCachingDirectoryMaxCachedMB;
		}

		public Builder type(final Type directoryType) {
			this.directoryType = directoryType;
			return this;
		}

		public Builder mergeScheduler(final MergeScheduler mergeScheduler) {
			this.mergeScheduler = mergeScheduler;
			return this;
		}

		public Builder similarityClass(final Class<? extends Similarity> similarityClass) {
			this.similarityClass = similarityClass == null ? null : similarityClass.getName();
			return this;
		}

		public Builder master(final String master) throws URISyntaxException {
			if (master != null)
				master(RemoteIndex.build(master));
			return this;
		}

		public Builder master(final RemoteIndex master) {
			this.master = master;
			return this;
		}

		public Builder master(final String schema, final String index) {
			this.master = new RemoteIndex(schema, index);
			return this;
		}

		public Builder ramBufferSize(final Double ramBufferSize) {
			this.ramBufferSize = ramBufferSize;
			return this;
		}

		public Builder maxMergeAtOnce(final Integer maxMergeAtOnce) {
			this.maxMergeAtOnce = maxMergeAtOnce;
			return this;
		}

		public Builder maxMergedSegmentMB(final Double maxMergedSegmentMB) {
			this.maxMergedSegmentMB = maxMergedSegmentMB;
			return this;
		}

		public Builder segmentsPerTier(final Double segmentsPerTier) {
			this.segmentsPerTier = segmentsPerTier;
			return this;
		}

		public Builder enableTaxonomyIndex(final Boolean enableTaxonomyIndex) {
			this.enableTaxonomyIndex = enableTaxonomyIndex;
			return this;
		}

		public Builder sortedSetFacetField(final String sortedSetFacetField) {
			this.sortedSetFacetField = sortedSetFacetField;
			return this;
		}

		public Builder indexReaderWarmer(final Boolean indexReaderWarmer) {
			this.indexReaderWarmer = indexReaderWarmer;
			return this;
		}

		public Builder mergedSegmentWarmer(final Boolean mergedSegmentWarmer) {
			this.mergedSegmentWarmer = mergedSegmentWarmer;
			return this;
		}

		public Builder nrtCachingDirectoryMaxMergeSizeMB(final Double nrtCachingDirectoryMaxMergeSizeMB) {
			this.nrtCachingDirectoryMaxMergeSizeMB = nrtCachingDirectoryMaxMergeSizeMB;
			return this;
		}

		public Builder nrtCachingDirectoryMaxCachedMB(final Double nrtCachingDirectoryMaxCachedMB) {
			this.nrtCachingDirectoryMaxCachedMB = nrtCachingDirectoryMaxCachedMB;
			return this;
		}

		public IndexSettingsDefinition build() {
			return new IndexSettingsDefinition(this);
		}
	}

	static IndexSettingsDefinition load(final File settingsFile,
			final Supplier<IndexSettingsDefinition> defaultSettings) throws IOException {
		return settingsFile != null && settingsFile.exists() && settingsFile.isFile() ?
				ObjectMappers.JSON.readValue(settingsFile, IndexSettingsDefinition.class) :
				defaultSettings == null ? null : defaultSettings.get();
	}

	static void save(final IndexSettingsDefinition settings, final File settingsFile) throws IOException {
		if (settings == null)
			Files.deleteIfExists(settingsFile.toPath());
		else
			ObjectMappers.JSON.writeValue(settingsFile, settings);
	}

}
