/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface IndexField {

    String name() default StringUtils.EMPTY;

    FieldDefinition.Template template() default FieldDefinition.Template.NONE;

    String analyzer() default StringUtils.EMPTY;

    String queryAnalyzer() default StringUtils.EMPTY;

    Class<? extends Analyzer> analyzerClass() default Analyzer.class;

    Class<? extends Analyzer> queryAnalyzerClass() default Analyzer.class;

    boolean tokenized() default false;

    boolean stored() default false;

    boolean storeTermVectors() default false;

    boolean storeTermVectorOffsets() default false;

    boolean storeTermVectorPositions() default false;

    boolean storeTermVectorPayloads() default false;

    boolean omitNorms() default false;

    IndexOptions indexOptions() default IndexOptions.NONE;

    DocValuesType docValuesType() default DocValuesType.NONE;

    int dimensionCount() default 0;

    int dimensionNumBytes() default 0;

    boolean facetMultivalued() default false;

    boolean facetHierarchical() default false;

    boolean facetRequireDimCount() default false;

}
