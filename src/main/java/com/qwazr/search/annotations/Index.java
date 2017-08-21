/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.search.annotations;

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.utils.StringUtils;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Index {

	String name();

	String schema();

	Class<? extends Similarity> similarityClass() default BM25Similarity.class;

	String replicationMaster() default StringUtils.EMPTY;

	IndexSettingsDefinition.Type type() default IndexSettingsDefinition.Type.FSDirectory;

	IndexSettingsDefinition.MergeScheduler mergeScheduler() default IndexSettingsDefinition.MergeScheduler.CONCURRENT;

	double ramBufferSize() default IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB;

	int maxMergeAtOnce() default IndexSettingsDefinition.DEFAULT_MAX_MERGE_AT_ONCE;

	double maxMergedSegmentMB() default IndexSettingsDefinition.DEFAULT_MAX_MERGED_SEGMENT_MB;

	double segmentsPerTier() default IndexSettingsDefinition.DEFAULT_SEGMENTS_PER_TIER;

	boolean enableTaxonomyIndex() default false;

	String sortedSetFacetField() default FieldDefinition.DEFAULT_SORTEDSET_FACET_FIELD;

	boolean indexReaderWarmer() default true;
}