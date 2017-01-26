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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.annotations.Index;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IndexSettingsDefinition {

	public enum Type {
		FSDirectory, RAMDirectory
	}

	public static final int DEFAULT_MAX_MERGE_AT_ONCE = 10;
	public static final int DEFAULT_SEGMENTS_PER_TIER = 10;
	public static final double DEFAULT_MAX_MERGED_SEGMENT_MB = 5 * 1024 * 1024;

	@JsonProperty("similarity_class")
	final public String similarityClass;

	final public RemoteIndex master;

	@JsonProperty("directory_type")
	final public Type directoryType;

	@JsonProperty("ram_buffer_size")
	final public Double ramBufferSize;

	@JsonProperty("max_merge_at_once")
	final public Integer maxMergeAtOnce;

	@JsonProperty("max_merged_segment_mb")
	final public Double maxMergedSegmentMB;

	@JsonProperty("segments_per_tier")
	final public Double segmentsPerTier;

	public IndexSettingsDefinition() {
		directoryType = null;
		similarityClass = null;
		master = null;
		ramBufferSize = null;
		maxMergeAtOnce = null;
		maxMergedSegmentMB = null;
		segmentsPerTier = null;
	}

	private IndexSettingsDefinition(final Builder builder) {
		this.directoryType = builder.directoryType;
		this.similarityClass = builder.similarityClass;
		this.master = builder.master;
		this.ramBufferSize = builder.ramBufferSize;
		this.maxMergeAtOnce = builder.maxMergeAtOnce;
		this.maxMergedSegmentMB = builder.maxMergedSegmentMB;
		this.segmentsPerTier = builder.segmentsPerTier;
	}

	final static IndexSettingsDefinition EMPTY = new IndexSettingsDefinition();

	public static IndexSettingsDefinition newSettings(final String jsonString) throws IOException {
		if (StringUtils.isEmpty(jsonString))
			return null;
		return JsonMapper.MAPPER.readValue(jsonString, IndexSettingsDefinition.class);
	}

	@Override
	public final boolean equals(final Object o) {
		if (o == null || !(o instanceof IndexSettingsDefinition))
			return false;
		final IndexSettingsDefinition s = (IndexSettingsDefinition) o;
		if (!Objects.equals(directoryType, s.directoryType))
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
		private String similarityClass;
		private RemoteIndex master;
		private Double ramBufferSize;
		private Integer maxMergeAtOnce;
		private Double maxMergedSegmentMB;
		private Double segmentsPerTier;

		private Builder() {
		}

		private Builder(final Index annotatedIndex) throws URISyntaxException {
			directoryType = annotatedIndex.type();
			similarityClass(annotatedIndex.similarityClass());
			master(annotatedIndex.replicationMaster());
			ramBufferSize(annotatedIndex.ramBufferSize());
			maxMergeAtOnce(annotatedIndex.maxMergeAtOnce());
			maxMergedSegmentMB(annotatedIndex.maxMergedSegmentMB());
			segmentsPerTier(annotatedIndex.segmentsPerTier());
		}

		private Builder(final IndexSettingsDefinition settings) {
			this.directoryType = settings.directoryType;
			this.similarityClass = settings.similarityClass;
			this.master = settings.master;
			this.ramBufferSize = settings.ramBufferSize;
			this.maxMergeAtOnce = settings.maxMergeAtOnce;
			this.maxMergedSegmentMB = settings.maxMergedSegmentMB;
			this.segmentsPerTier = settings.segmentsPerTier;
		}

		public Builder type(final Type directoryType) {
			this.directoryType = directoryType;
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

		public IndexSettingsDefinition build() {
			return new IndexSettingsDefinition(this);
		}
	}
}
