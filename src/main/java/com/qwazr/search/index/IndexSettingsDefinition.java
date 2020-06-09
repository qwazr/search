/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.annotations.Index;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.StringUtils;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.similarities.Similarity;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Supplier;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(
    creatorVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    fieldVisibility = JsonAutoDetect.Visibility.NONE)
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

    @JsonProperty("primary_key")
    final public String primaryKey;

    @JsonProperty("similarity")
    final public String similarity;

    @JsonProperty("similarity_class")
    final public String similarityClass;

    @JsonProperty("sort")
    final public String sort;

    @JsonProperty("sort_class")
    final public String sortClass;

    @JsonProperty("master")
    final public RemoteIndex master;

    @JsonProperty("directory_type")
    final public Type directoryType;

    @JsonProperty("merge_scheduler")
    final public MergeScheduler mergeScheduler;

    @JsonProperty("ram_buffer_size")
    final public Double ramBufferSize;

    @JsonProperty("use_compound_file")
    final public Boolean useCompoundFile;

    @JsonProperty("use_simple_text_codec")
    final public Boolean useSimpleTextCodec;

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

    @JsonProperty("source_field")
    final public String sourceField;

    @JsonProperty("index_reader_warmer")
    final public Boolean indexReaderWarmer;

    @JsonProperty("merged_segment_warmer")
    final public Boolean mergedSegmentWarmer;

    @JsonProperty("nrt_caching_directory_max_merge_size_mb")
    final public Double nrtCachingDirectoryMaxMergeSizeMB;

    @JsonProperty("nrt_caching_directory_max_cached_mb")
    final public Double nrtCachingDirectoryMaxCachedMB;

    @JsonCreator
    private IndexSettingsDefinition(
        @JsonProperty("primary_key") final String primaryKey,
        @JsonProperty("similarity") final String similarity,
        @JsonProperty("similarity_class") final String similarityClass,
        @JsonProperty("sort") final String sort,
        @JsonProperty("sort_class") final String sortClass,
        @JsonProperty("master") final RemoteIndex master,
        @JsonProperty("directory_type") final Type directoryType,
        @JsonProperty("merge_scheduler") final MergeScheduler mergeScheduler,
        @JsonProperty("ram_buffer_size") final Double ramBufferSize,
        @JsonProperty("use_compound_file") final Boolean useCompoundFile,
        @JsonProperty("use_simple_text_codec") final Boolean useSimpleTextCodec,
        @JsonProperty("max_merge_at_once") final Integer maxMergeAtOnce,
        @JsonProperty("max_merged_segment_mb") final Double maxMergedSegmentMB,
        @JsonProperty("segments_per_tier") final Double segmentsPerTier,
        @JsonProperty("enable_taxonomy_index") final Boolean enableTaxonomyIndex,
        @JsonProperty("sorted_set_facet_field") final String sortedSetFacetField,
        @JsonProperty("source_field") final String sourceField,
        @JsonProperty("index_reader_warmer") final Boolean indexReaderWarmer,
        @JsonProperty("merged_segment_warmer") final Boolean mergedSegmentWarmer,
        @JsonProperty("nrt_caching_directory_max_merge_size_mb") final Double nrtCachingDirectoryMaxMergeSizeMB,
        @JsonProperty("nrt_caching_directory_max_cached_mb") final Double nrtCachingDirectoryMaxCachedMB) {
        this.primaryKey = primaryKey;
        this.directoryType = directoryType;
        this.mergeScheduler = mergeScheduler;
        this.similarity = similarity;
        this.similarityClass = similarityClass;
        this.sort = sort;
        this.sortClass = sortClass;
        this.master = master;
        this.ramBufferSize = ramBufferSize;
        this.useCompoundFile = useCompoundFile;
        this.useSimpleTextCodec = useSimpleTextCodec;
        this.maxMergeAtOnce = maxMergeAtOnce;
        this.maxMergedSegmentMB = maxMergedSegmentMB;
        this.segmentsPerTier = segmentsPerTier;
        this.enableTaxonomyIndex = enableTaxonomyIndex;
        this.sortedSetFacetField = sortedSetFacetField;
        this.sourceField = sourceField;
        this.indexReaderWarmer = indexReaderWarmer;
        this.mergedSegmentWarmer = mergedSegmentWarmer;
        this.nrtCachingDirectoryMaxMergeSizeMB = nrtCachingDirectoryMaxMergeSizeMB;
        this.nrtCachingDirectoryMaxCachedMB = nrtCachingDirectoryMaxCachedMB;
    }

    private IndexSettingsDefinition(final Builder builder) {
        this.primaryKey = builder.primaryKey;
        this.directoryType = builder.directoryType;
        this.mergeScheduler = builder.mergeScheduler;
        this.similarity = builder.similarity;
        this.similarityClass = builder.similarityClass;
        this.sort = builder.sort;
        this.sortClass = builder.sortClass;
        this.master = builder.master;
        this.ramBufferSize = builder.ramBufferSize;
        this.useCompoundFile = builder.useCompoundFile;
        this.useSimpleTextCodec = builder.useSimpleTextCodec;
        this.maxMergeAtOnce = builder.maxMergeAtOnce;
        this.maxMergedSegmentMB = builder.maxMergedSegmentMB;
        this.segmentsPerTier = builder.segmentsPerTier;
        this.enableTaxonomyIndex = builder.enableTaxonomyIndex;
        this.sortedSetFacetField = builder.sortedSetFacetField;
        this.sourceField = builder.sourceField;
        this.indexReaderWarmer = builder.indexReaderWarmer;
        this.mergedSegmentWarmer = builder.mergedSegmentWarmer;
        this.nrtCachingDirectoryMaxMergeSizeMB = builder.nrtCachingDirectoryMaxMergeSizeMB;
        this.nrtCachingDirectoryMaxCachedMB = builder.nrtCachingDirectoryMaxCachedMB;
    }

    final static IndexSettingsDefinition EMPTY = new IndexSettingsDefinition(new Builder());

    public static IndexSettingsDefinition newSettings(final String jsonString) throws IOException {
        if (StringUtils.isEmpty(jsonString))
            return null;
        return ObjectMappers.JSON.readValue(jsonString, IndexSettingsDefinition.class);
    }

    public static boolean useTaxonomyIndex(final IndexSettingsDefinition settings) {
        return settings != null && (settings.enableTaxonomyIndex == null ? false : settings.enableTaxonomyIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryKey, directoryType, ramBufferSize, useCompoundFile, similarityClass, sortedSetFacetField, sourceField);
    }

    @Override
    public final boolean equals(final Object o) {
        if (!(o instanceof IndexSettingsDefinition))
            return false;
        final IndexSettingsDefinition s = (IndexSettingsDefinition) o;
        if (!Objects.equals(primaryKey, s.primaryKey))
            return false;
        if (!Objects.equals(directoryType, s.directoryType))
            return false;
        if (!Objects.equals(mergeScheduler, s.mergeScheduler))
            return false;
        if (!Objects.equals(similarity, s.similarity))
            return false;
        if (!Objects.equals(similarityClass, s.similarityClass))
            return false;
        if (!Objects.equals(sort, s.sort))
            return false;
        if (!Objects.equals(sortClass, s.sortClass))
            return false;
        if (!Objects.equals(master, s.master))
            return false;
        if (!Objects.equals(ramBufferSize, s.ramBufferSize))
            return false;
        if (!Objects.equals(useCompoundFile, s.useCompoundFile))
            return false;
        if (!Objects.equals(useSimpleTextCodec, s.useSimpleTextCodec))
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
        if (!Objects.equals(sourceField, s.sourceField))
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

        private String primaryKey;
        private Type directoryType;
        private MergeScheduler mergeScheduler;
        private String similarity;
        private String similarityClass;
        private String sort;
        private String sortClass;
        private RemoteIndex master;
        private Double ramBufferSize;
        private Boolean useCompoundFile;
        private Boolean useSimpleTextCodec;
        private Integer maxMergeAtOnce;
        private Double maxMergedSegmentMB;
        private Double segmentsPerTier;
        private Boolean enableTaxonomyIndex;
        private String sortedSetFacetField;
        private String sourceField;
        private Boolean indexReaderWarmer;
        private Boolean mergedSegmentWarmer;
        private Double nrtCachingDirectoryMaxMergeSizeMB;
        private Double nrtCachingDirectoryMaxCachedMB;

        private Builder() {
        }

        private Builder(final Index annotatedIndex) throws URISyntaxException {
            primaryKey(annotatedIndex.primaryKey());
            type(annotatedIndex.type());
            mergeScheduler(annotatedIndex.mergeScheduler());
            similarity(annotatedIndex.similarity());
            similarityClass(annotatedIndex.similarityClass());
            sort(annotatedIndex.sort());
            master(annotatedIndex.replicationMaster());
            ramBufferSize(annotatedIndex.ramBufferSize());
            useCompoundFile(annotatedIndex.useCompoundFile());
            useSimpleTextCodec(annotatedIndex.useSimpleTextCodec());
            maxMergeAtOnce(annotatedIndex.maxMergeAtOnce());
            maxMergedSegmentMB(annotatedIndex.maxMergedSegmentMB());
            segmentsPerTier(annotatedIndex.segmentsPerTier());
            enableTaxonomyIndex(annotatedIndex.enableTaxonomyIndex());
            sortedSetFacetField(annotatedIndex.sortedSetFacetField());
            sourceField(annotatedIndex.sourceField());
            indexReaderWarmer(annotatedIndex.indexReaderWarmer());
            mergedSegmentWarmer(annotatedIndex.mergedSegmentWarmer());
            nrtCachingDirectoryMaxMergeSizeMB(annotatedIndex.nrtCachingDirectoryMaxMergeSizeMB());
            nrtCachingDirectoryMaxCachedMB(annotatedIndex.nrtCachingDirectoryMaxCachedMB());
        }

        private Builder(final IndexSettingsDefinition settings) {
            this.primaryKey = settings.primaryKey;
            this.directoryType = settings.directoryType;
            this.mergeScheduler = settings.mergeScheduler;
            this.similarity = settings.similarity;
            this.similarityClass = settings.similarityClass;
            this.sort = settings.sort;
            this.sortClass = settings.sortClass;
            this.master = settings.master;
            this.ramBufferSize = settings.ramBufferSize;
            this.useCompoundFile = settings.useCompoundFile;
            this.useSimpleTextCodec = settings.useSimpleTextCodec;
            this.maxMergeAtOnce = settings.maxMergeAtOnce;
            this.maxMergedSegmentMB = settings.maxMergedSegmentMB;
            this.segmentsPerTier = settings.segmentsPerTier;
            this.enableTaxonomyIndex = settings.enableTaxonomyIndex;
            this.sortedSetFacetField = settings.sortedSetFacetField;
            this.sourceField = settings.sourceField;
            this.indexReaderWarmer = settings.indexReaderWarmer;
            this.mergedSegmentWarmer = settings.mergedSegmentWarmer;
            this.nrtCachingDirectoryMaxMergeSizeMB = settings.nrtCachingDirectoryMaxMergeSizeMB;
            this.nrtCachingDirectoryMaxCachedMB = settings.nrtCachingDirectoryMaxCachedMB;
        }

        public Builder primaryKey(final String primaryKey) {
            this.primaryKey = primaryKey;
            return this;
        }

        public Builder type(final Type directoryType) {
            this.directoryType = directoryType;
            return this;
        }

        public Builder mergeScheduler(final MergeScheduler mergeScheduler) {
            this.mergeScheduler = mergeScheduler;
            return this;
        }

        public Builder similarity(final String similarity) {
            this.similarity = similarity;
            return this;
        }

        public Builder similarityClass(final Class<? extends Similarity> similarityClass) {
            this.similarityClass = similarityClass == null ? null : similarityClass.getName();
            return this;
        }

        public Builder sort(final String sort) {
            this.sort = sort;
            return this;
        }

        public Builder sortClass(final Class<? extends Sort> sortClass) {
            this.sortClass = sortClass == null ? null : sortClass.getName();
            return this;
        }

        public Builder master(final String master) throws URISyntaxException {
            if (master != null)
                master(RemoteIndex.build(master));
            else
                this.master = null;
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

        public Builder useCompoundFile(final Boolean useCompoundFile) {
            this.useCompoundFile = useCompoundFile;
            return this;
        }

        public Builder useSimpleTextCodec(final Boolean useSimpleTextCodec) {
            this.useSimpleTextCodec = useSimpleTextCodec;
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

        public Builder sourceField(final String sourceField) {
            this.sourceField = sourceField;
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
        return settingsFile != null && settingsFile.exists() && settingsFile.isFile() && settingsFile.length() > 0 ?
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
