/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.LoggerUtils;
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
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LRUQueryCache;
import org.apache.lucene.search.QueryCache;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NRTCachingDirectory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class IndexStatus {

    private final static Logger LOGGER = LoggerUtils.getLogger(IndexStatus.class);

    @JsonProperty("num_docs")
    final public Long numDocs;

    @JsonProperty("num_deleted_docs")
    final public Long numDeletedDocs;

    @JsonProperty("has_pending_merges")
    final public Boolean hasPendingMerges;

    @JsonProperty("has_uncommitted_changes")
    final public Boolean hasUncommittedChanges;

    @JsonProperty("has_deletions")
    final public Boolean hasDeletions;

    @JsonProperty("ram_buffer_size_mb")
    final public Double ramBufferSizeMb;

    @JsonProperty("index_uuid")
    final public String indexUuid;

    @JsonProperty("master_uuid")
    final public String masterUuid;

    final public Long version;

    final public Set<String> analyzers;

    final public Set<String> fields;

    final public IndexSettingsDefinition settings;

    @JsonProperty("field_infos")
    final public SortedMap<String, Set<FieldInfoStatus>> fieldInfos;

    @JsonProperty("segment_count")
    final public Integer segmentCount;

    @JsonProperty("segments_bytes_size")
    final public Long segmentsBytesSize;

    @JsonProperty("segments_size")
    final public String segmentsSize;

    @JsonProperty("commit_filenames")
    final public Collection<String> commitFilenames;

    @JsonProperty("commit_generation")
    final public Long commitGeneration;

    @JsonProperty("merge_policy")
    final public MergePolicyStatus mergePolicy;

    @JsonProperty("query_cache")
    final public QueryCacheStats queryCache;

    @JsonProperty("commit_user_data")
    final public Map<String, String> commitUserData;

    @JsonProperty("directory_class")
    final public String directoryClass;

    @JsonProperty("directory_cached_files")
    final public String[] directoryCachedFiles;

    @JsonProperty("directory_cached_ram_used")
    final public String directoryCachedRamUsed;

    @JsonProperty("active_index_analyzers")
    final public Integer activeIndexAnalyzers;

    @JsonProperty("active_query_analyzers")
    final public Integer activeQueryAnalyzers;

    @JsonProperty("index_sort_fields")
    final public Set<String> indexSortFields;

    @JsonCreator
    IndexStatus(@JsonProperty("num_docs") Long numDocs, @JsonProperty("num_deleted_docs") Long numDeletedDocs,
                @JsonProperty("has_pending_merges") Boolean hasPendingMerges,
                @JsonProperty("has_uncommitted_changes") Boolean hasUncommittedChanges,
                @JsonProperty("has_deletions") Boolean hasDeletions,
                @JsonProperty("ram_buffer_size_mb") Double ramBufferSizeMb, @JsonProperty("index_uuid") String indexUuid,
                @JsonProperty("master_uuid") String masterUuid, @JsonProperty("version") Long version,
                @JsonProperty("analyzers") Set<String> analyzers, @JsonProperty("fields") Set<String> fields,
                @JsonProperty("settings") IndexSettingsDefinition settings,
                @JsonProperty("field_infos") SortedMap<String, Set<FieldInfoStatus>> fieldInfos,
                @JsonProperty("segment_count") Integer segmentCount,
                @JsonProperty("segments_bytes_size") Long segmentsBytesSize,
                @JsonProperty("segments_size") String segmentsSize,
                @JsonProperty("commit_filenames") Collection<String> commitFilenames,
                @JsonProperty("commit_generation") Long commitGeneration,
                @JsonProperty("merge_policy") MergePolicyStatus mergePolicy,
                @JsonProperty("query_cache") QueryCacheStats queryCache,
                @JsonProperty("commit_user_data") Map<String, String> commitUserData,
                @JsonProperty("directory_class") String directoryClass,
                @JsonProperty("directory_cached_files") String[] directoryCachedFiles,
                @JsonProperty("directory_cached_ram_used") String directoryCachedRamUsed,
                @JsonProperty("active_index_analyzers") Integer activeIndexAnalyzers,
                @JsonProperty("active_query_analyzers") Integer activeQueryAnalyzers,
                @JsonProperty("index_sort_fields") Set<String> indexSortFields) {
        this.numDocs = numDocs;
        this.numDeletedDocs = numDeletedDocs;
        this.mergePolicy = mergePolicy;
        this.hasPendingMerges = hasPendingMerges;
        this.hasUncommittedChanges = hasUncommittedChanges;
        this.ramBufferSizeMb = ramBufferSizeMb;
        this.hasDeletions = hasDeletions;
        this.indexUuid = indexUuid;
        this.masterUuid = masterUuid;
        this.version = version;
        this.analyzers = analyzers;
        this.fields = fields;
        this.settings = settings;
        this.fieldInfos = fieldInfos;
        this.segmentCount = segmentCount;
        this.segmentsBytesSize = segmentsBytesSize;
        this.segmentsSize = segmentsSize;
        this.commitFilenames = commitFilenames;
        this.commitGeneration = commitGeneration;
        this.queryCache = queryCache;
        this.commitUserData = commitUserData;
        this.directoryClass = directoryClass;
        this.directoryCachedFiles = directoryCachedFiles;
        this.directoryCachedRamUsed = directoryCachedRamUsed;
        this.activeIndexAnalyzers = activeIndexAnalyzers;
        this.activeQueryAnalyzers = activeQueryAnalyzers;
        this.indexSortFields = indexSortFields;
    }

    public IndexStatus(final UUID indexUuid, final UUID masterUuid, final Directory directory,
                       final IndexSearcher indexSearcher, final IndexWriter indexWriter, final IndexSettingsDefinition settings,
                       final Set<String> analyzers, final Set<String> fields, final int activeIndexAnalyzers,
                       final int activeQueryAnalyzers) throws IOException {
        final IndexReader indexReader = indexSearcher.getIndexReader();
        this.numDocs = (long) indexReader.numDocs();
        this.numDeletedDocs = (long) indexReader.numDeletedDocs();
        final TreeMap<String, Set<FieldInfoStatus>> m = new TreeMap<>();
        fillFieldInfos(m, indexReader.leaves());
        this.fieldInfos = Collections.unmodifiableSortedMap(m);

        if (indexWriter == null) {
            this.mergePolicy = null;
            this.hasPendingMerges = null;
            this.hasUncommittedChanges = null;
            this.hasDeletions = null;
            this.ramBufferSizeMb = null;
            this.indexSortFields = null;
        } else {
            final LiveIndexWriterConfig config = indexWriter.getConfig();
            final MergePolicy mergePolicy = config.getMergePolicy();
            this.mergePolicy = mergePolicy == null ? null : new MergePolicyStatus(mergePolicy);
            this.hasPendingMerges = indexWriter.hasPendingMerges();
            this.hasUncommittedChanges = indexWriter.hasUncommittedChanges();
            this.hasDeletions = indexWriter.hasDeletions();
            this.ramBufferSizeMb = config.getRAMBufferSizeMB();
            this.indexSortFields = config.getIndexSortFields();
        }

        final DirectoryReader directoryReader =
                indexReader instanceof DirectoryReader ? (DirectoryReader) indexReader : null;

        final IndexCommit indexCommit;
        if (directoryReader != null) {
            indexCommit = directoryReader.getIndexCommit();
            version = directoryReader.getVersion();
        } else {
            indexCommit = null;
            version = null;
        }

        if (indexCommit != null) {
            this.commitUserData = indexCommit.getUserData();
            this.segmentCount = indexCommit.getSegmentCount();
            this.commitFilenames = indexCommit.getFileNames();
            this.commitGeneration = indexCommit.getGeneration();
            if (directory != null) {
                long size = 0;
                for (String filename : this.commitFilenames) {
                    try {
                        size += directory.fileLength(filename);
                    }
                    catch (IOException e) {
                        LOGGER.log(Level.FINE, e, e::getMessage);
                    }
                }
                this.segmentsBytesSize = size;
                this.segmentsSize = FileUtils.byteCountToDisplaySize(size);
            } else {
                this.segmentsBytesSize = null;
                this.segmentsSize = null;
            }
        } else {
            this.commitUserData = null;
            this.segmentCount = null;
            this.segmentsBytesSize = null;
            this.segmentsSize = null;
            this.commitFilenames = null;
            this.commitGeneration = null;
        }

        this.indexUuid = indexUuid == null ? null : indexUuid.toString();
        this.masterUuid = masterUuid == null ? null : masterUuid.toString();
        this.settings = settings;
        this.analyzers = analyzers;
        this.activeIndexAnalyzers = activeIndexAnalyzers;
        this.activeQueryAnalyzers = activeQueryAnalyzers;
        this.fields = fields;

        final QueryCache queryCache = indexSearcher.getQueryCache();
        this.queryCache = queryCache instanceof LRUQueryCache ? new QueryCacheStats((LRUQueryCache) queryCache) : null;

        if (directory != null) {
            if (directory instanceof NRTCachingDirectory) {
                final NRTCachingDirectory nrtCachingDirectory = (NRTCachingDirectory) directory;
                this.directoryClass = nrtCachingDirectory.getDelegate().getClass().getName();
                this.directoryCachedFiles = nrtCachingDirectory.listCachedFiles();
                this.directoryCachedRamUsed = FileUtils.byteCountToDisplaySize(nrtCachingDirectory.ramBytesUsed());
            } else {
                this.directoryClass = directory.getClass().getName();
                this.directoryCachedFiles = null;
                this.directoryCachedRamUsed = null;
            }
        } else {
            this.directoryClass = null;
            this.directoryCachedFiles = null;
            this.directoryCachedRamUsed = null;
        }
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
    public int hashCode() {
        return Objects.hash(indexUuid, numDocs);
    }

    @Override
    final public boolean equals(final Object o) {
        if (!(o instanceof IndexStatus))
            return false;
        final IndexStatus s = (IndexStatus) o;
        if (!Objects.equals(numDocs, s.numDocs))
            return false;
        if (!Objects.equals(numDeletedDocs, s.numDeletedDocs))
            return false;
        if (!Objects.equals(indexUuid, s.indexUuid))
            return false;
        if (!Objects.equals(masterUuid, s.masterUuid))
            return false;
        if (!Objects.equals(version, s.version))
            return false;
        if (!Objects.equals(settings, s.settings))
            return false;
        if (!Objects.deepEquals(analyzers, s.analyzers))
            return false;
        if (!Objects.deepEquals(fields, s.fields))
            return false;
        if (!Objects.equals(activeIndexAnalyzers, s.activeIndexAnalyzers))
            return false;
        if (!Objects.equals(activeQueryAnalyzers, s.activeQueryAnalyzers))
            return false;
        if (!Objects.equals(indexSortFields, s.indexSortFields))
            return false;
        return true;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class MergePolicyStatus {

        final public String type;

        @JsonProperty("max_cfs_segment_size_mb")
        final public Double maxCfsSegmentSizeMb;

        @JsonProperty("no_cfs_ratio")
        final public Double noCfsRatio;

        //TieredMergePolicy
        @JsonProperty("max_merge_at_once")
        final public Integer maxMergeAtOnce;

        @JsonProperty("max_merged_segment_mb")
        final public Double maxMergedSegmentMb;

        @JsonProperty("segments_per_tier")
        final public Double segmentsPerTier;

        @JsonCreator
        MergePolicyStatus(@JsonProperty("type") String type,
                          @JsonProperty("max_cfs_segment_size_mb") Double max_cfs_segment_size_mb,
                          @JsonProperty("no_cfs_ratio") Double no_cfs_ratio,
                          @JsonProperty("max_merge_at_once") Integer max_merge_at_once,
                          @JsonProperty("max_merged_segment_mb") Double max_merged_segment_mb,
                          @JsonProperty("segments_per_tier") Double segments_per_tier) {
            this.type = type;
            this.maxCfsSegmentSizeMb = max_cfs_segment_size_mb;
            this.noCfsRatio = no_cfs_ratio;

            this.maxMergeAtOnce = max_merge_at_once;
            this.maxMergedSegmentMb = max_merged_segment_mb;
            this.segmentsPerTier = segments_per_tier;
        }

        MergePolicyStatus(final MergePolicy mergePolicy) {
            type = mergePolicy.getClass().getTypeName();
            maxCfsSegmentSizeMb = mergePolicy.getMaxCFSSegmentSizeMB();
            noCfsRatio = mergePolicy.getNoCFSRatio();
            if (mergePolicy instanceof TieredMergePolicy) {
                TieredMergePolicy tmp = (TieredMergePolicy) mergePolicy;
                maxMergeAtOnce = tmp.getMaxMergeAtOnce();
                maxMergedSegmentMb = tmp.getMaxMergedSegmentMB();
                segmentsPerTier = tmp.getSegmentsPerTier();
            } else {
                maxMergeAtOnce = null;
                maxMergedSegmentMb = null;
                segmentsPerTier = null;
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class FieldInfoStatus {

        public final Integer number;

        @JsonProperty("omit_norms")
        public final Boolean omitNorms;

        @JsonProperty("has_norms")
        public final Boolean hasNorms;

        @JsonProperty("has_payloads")
        public final Boolean hasPayloads;

        @JsonProperty("has_vectors")
        public final Boolean hasVectors;

        @JsonProperty("doc_values_gen")
        public final Long docValuesGen;

        @JsonProperty("doc_values_type")
        public final DocValuesType docValuesType;

        @JsonProperty("index_options")
        public final IndexOptions indexOptions;

        @JsonProperty("point_dimension_count")
        public final Integer pointDimensionCount;

        @JsonProperty("point_index_dimension_count")
        public final Integer pointIndexDimensionCount;

        @JsonProperty("point_num_bytes")
        public final Integer pointNumBytes;

        @JsonIgnore
        private final int hashCode;

        @JsonCreator
        FieldInfoStatus(@JsonProperty("number") Integer number, @JsonProperty("omit_norms") Boolean omitNorms,
                        @JsonProperty("has_norms") Boolean hasNorms, @JsonProperty("has_payloads") Boolean hasPayloads,
                        @JsonProperty("has_vectors") Boolean hasVectors, @JsonProperty("doc_values_gen") Long docValuesGen,
                        @JsonProperty("doc_values_type") DocValuesType docValuesType,
                        @JsonProperty("index_options") IndexOptions indexOptions,
                        @JsonProperty("point_dimension_count") Integer pointDimensionCount,
                        @JsonProperty("point_index_dimension_count") Integer pointIndexDimensionCount,
                        @JsonProperty("point_num_bytes") Integer pointNumBytes) {
            this.number = number;
            this.omitNorms = omitNorms;
            this.hasNorms = hasNorms;
            this.hasPayloads = hasPayloads;
            this.hasVectors = hasVectors;
            this.docValuesGen = docValuesGen;
            this.docValuesType = docValuesType;
            this.indexOptions = indexOptions;
            this.pointDimensionCount = pointDimensionCount;
            this.pointIndexDimensionCount = pointIndexDimensionCount;
            this.pointNumBytes = pointNumBytes;
            hashCode = buildHashCode();
        }

        private FieldInfoStatus(final FieldInfo info) {
            this(info.number, info.omitsNorms(), info.hasNorms(), info.hasPayloads(), info.hasVectors(),
                    info.getDocValuesGen(), info.getDocValuesType(), info.getIndexOptions(),
                    info.getPointDimensionCount(), info.getPointIndexDimensionCount(), info.getPointNumBytes());
        }

        private int buildHashCode() {
            return Objects.hash(number, omitNorms, hasNorms, hasPayloads, hasVectors, docValuesGen, docValuesType,
                    indexOptions, pointDimensionCount, pointIndexDimensionCount, pointNumBytes);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof FieldInfoStatus))
                return false;
            final FieldInfoStatus info = (FieldInfoStatus) o;
            if (!Objects.equals(number, info.number))
                return false;
            if (!Objects.equals(omitNorms, info.omitNorms))
                return false;
            if (!Objects.equals(hasNorms, info.hasNorms))
                return false;
            if (!Objects.equals(hasPayloads, info.hasPayloads))
                return false;
            if (!Objects.equals(hasVectors, info.hasVectors))
                return false;
            if (!Objects.equals(docValuesGen, info.docValuesGen))
                return false;
            if (!Objects.equals(docValuesType, info.docValuesType))
                return false;
            if (!Objects.equals(indexOptions, info.indexOptions))
                return false;
            if (!Objects.equals(pointDimensionCount, info.pointDimensionCount))
                return false;
            if (!Objects.equals(pointIndexDimensionCount, info.pointIndexDimensionCount))
                return false;
            if (!Objects.equals(pointNumBytes, info.pointNumBytes))
                return false;
            return true;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class QueryCacheStats {

        @JsonProperty("cache_count")
        public final Long cacheCount;

        @JsonProperty("cache_size")
        public final Long cacheSize;

        @JsonProperty("eviction_count")
        public final Long evictionCount;

        @JsonProperty("hit_count")
        public final Long hitCount;

        @JsonProperty("miss_count")
        public final Long missCount;

        @JsonProperty("total_count")
        public final Long totalCount;

        @JsonProperty("hit_rate")
        public final Float hitRate;

        @JsonProperty("miss_rate")
        public final Float missRate;

        private final int hashCode;

        @JsonCreator
        QueryCacheStats(@JsonProperty("cache_count") Long cacheCount, @JsonProperty("cache_size") Long cacheSize,
                        @JsonProperty("eviction_count") Long evictionCount, @JsonProperty("hit_count") Long hitCount,
                        @JsonProperty("miss_count") Long missCount, @JsonProperty("total_count") Long totalCount,
                        @JsonProperty("hit_rate") Float hitRate, @JsonProperty("miss_rate") Float missRate) {
            this.cacheCount = cacheCount;
            this.cacheSize = cacheSize;
            this.evictionCount = evictionCount;
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.totalCount = totalCount;
            this.hitRate = hitRate;
            this.missRate = missRate;
            this.hashCode = Objects.hash(cacheCount, cacheSize, evictionCount, hitCount, missCount, totalCount, hitRate,
                    missRate);
        }

        private QueryCacheStats(final LRUQueryCache queryCache) {
            this(queryCache.getCacheCount(), queryCache.getCacheSize(), queryCache.getEvictionCount(),
                    queryCache.getHitCount(), queryCache.getMissCount(), queryCache.getTotalCount(),
                    (float) (queryCache.getHitCount() * 100) / queryCache.getTotalCount(),
                    (float) (queryCache.getMissCount() * 100) / queryCache.getTotalCount());
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof QueryCacheStats))
                return false;
            final QueryCacheStats cache = (QueryCacheStats) o;
            if (!Objects.equals(cacheCount, cache.cacheCount))
                return false;
            if (!Objects.equals(cacheSize, cache.cacheSize))
                return false;
            if (!Objects.equals(evictionCount, cache.evictionCount))
                return false;
            if (!Objects.equals(hitCount, cache.hitCount))
                return false;
            if (!Objects.equals(missCount, cache.missCount))
                return false;
            if (!Objects.equals(totalCount, cache.totalCount))
                return false;
            if (!Objects.equals(hitRate, cache.hitRate))
                return false;
            if (!Objects.equals(missRate, cache.missRate))
                return false;
            return true;
        }
    }
}
