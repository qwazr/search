/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.annotations;

import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.search.field.SmartFieldDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface SmartField {

    String name() default StringUtils.EMPTY;

    SmartFieldDefinition.Type type() default SmartFieldDefinition.Type.TEXT;

    boolean facet() default false;

    boolean index() default false;

    int maxKeywordLength() default SmartFieldDefinition.DEFAULT_MAX_KEYWORD_LENGTH;

    SmartAnalyzerSet analyzer() default SmartAnalyzerSet.keyword;

    String indexAnalyzer() default StringUtils.EMPTY;

    String queryAnalyzer() default StringUtils.EMPTY;

    Class<? extends Analyzer> analyzerClass() default Analyzer.class;

    Class<? extends Analyzer> indexAnalyzerClass() default Analyzer.class;

    Class<? extends Analyzer> queryAnalyzerClass() default Analyzer.class;

    boolean sort() default false;

    boolean stored() default false;

    boolean multivalued() default false;

}
