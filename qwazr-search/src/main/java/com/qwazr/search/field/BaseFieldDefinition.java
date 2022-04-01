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
package com.qwazr.search.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qwazr.search.annotations.Copy;
import com.qwazr.utils.ArrayUtils;
import com.qwazr.utils.Equalizer;
import com.qwazr.utils.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;

abstract class BaseFieldDefinition<T extends BaseFieldDefinition<T>>
    extends Equalizer.Immutable<T> implements FieldDefinition {

    protected final SmartFieldDefinition.Type type;
    protected final String analyzer;
    protected final String indexAnalyzer;
    protected final String queryAnalyzer;
    protected final String[] copyFrom;

    protected BaseFieldDefinition(final Class<T> fieldClass,
                                  final SmartFieldDefinition.Type type,
                                  final String analyzer,
                                  final String indexAnalyzer,
                                  final String queryAnalyzer,
                                  final String[] copyFrom) {
        super(fieldClass);
        this.type = type;
        this.analyzer = StringUtils.isBlank(analyzer) ? null : analyzer;
        this.indexAnalyzer = StringUtils.isBlank(indexAnalyzer) ? null : indexAnalyzer;
        this.queryAnalyzer = StringUtils.isBlank(queryAnalyzer) ? null : queryAnalyzer;
        this.copyFrom = copyFrom == null ? ArrayUtils.EMPTY_STRING_ARRAY : copyFrom;
    }

    protected BaseFieldDefinition(final Class<T> fieldClass,
                                  final AbstractBuilder<? extends AbstractBuilder<?>> builder) {
        super(fieldClass);
        this.type = builder.type;
        this.analyzer = builder.analyzer;
        this.indexAnalyzer = builder.indexAnalyzer;
        this.queryAnalyzer = builder.queryAnalyzer;
        this.copyFrom = builder.copyFrom == null || builder.copyFrom.isEmpty() ?
            ArrayUtils.EMPTY_STRING_ARRAY :
            builder.copyFrom.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(type, analyzer, indexAnalyzer, queryAnalyzer);
    }

    @Override
    protected boolean isEqual(final T f) {
        return Objects.equals(type, f.type)
            && Objects.equals(analyzer, f.analyzer)
            && Objects.equals(indexAnalyzer, f.indexAnalyzer)
            && Objects.equals(queryAnalyzer, f.queryAnalyzer)
            && Arrays.equals(copyFrom, f.copyFrom);
    }

    @Override
    final public SmartFieldDefinition.Type getType() {
        return type;
    }

    @Override
    final public String getAnalyzer() {
        return analyzer;
    }

    @Override
    final public String getIndexAnalyzer() {
        return indexAnalyzer;
    }

    @Override
    final public String getQueryAnalyzer() {
        return queryAnalyzer;
    }

    @Override
    final public String[] getCopyFrom() {
        return copyFrom;
    }

    private final static String KEYWORD_ANALYZER_SIMPLE_NAME = KeywordAnalyzer.class.getSimpleName();

    @Override
    public boolean hasFullTextAnalyzer() {
        if (analyzer == null && queryAnalyzer == null && indexAnalyzer == null)
            return false;
        final String alzr = analyzer != null ?
            analyzer : queryAnalyzer != null ? queryAnalyzer : indexAnalyzer;
        return !(alzr.endsWith(KEYWORD_ANALYZER_SIMPLE_NAME));
    }

    @JsonIgnore
    public String resolvedIndexAnalyzer() {
        return StringUtils.isBlank(indexAnalyzer) ? analyzer : indexAnalyzer;
    }

    @JsonIgnore
    public String resolvedQueryAnalyzer() {
        return StringUtils.isBlank(queryAnalyzer) ? analyzer : queryAnalyzer;
    }

    protected static String from(String analyzerName, Class<? extends Analyzer> analyzerClass) {
        return analyzerClass != Analyzer.class ?
            analyzerClass.getName() :
            StringUtils.isEmpty(analyzerName) ? null : analyzerName;
    }

    protected static String[] from(final String fieldName, final Map<String, Copy> copyMap) {
        if (copyMap == null || copyMap.isEmpty())
            return null;
        final TreeMap<Integer, List<String>> map = new TreeMap<>();
        copyMap.forEach((name, copy) -> {
            for (Copy.To to : copy.to())
                if (fieldName.equals(to.field()))
                    map.computeIfAbsent(to.order(), order -> new ArrayList<>()).add(name);
        });

        final List<String> globalCopyFromList = new ArrayList<>();
        map.forEach((order, copyFromList) -> globalCopyFromList.addAll(copyFromList));
        return globalCopyFromList.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public static abstract class AbstractBuilder<B extends AbstractBuilder<B>> {

        SmartFieldDefinition.Type type;
        String analyzer;
        String indexAnalyzer;
        String queryAnalyzer;
        LinkedHashSet<String> copyFrom;

        protected abstract B me();

        public B type(SmartFieldDefinition.Type type) {
            this.type = type;
            return me();
        }

        final public B analyzer(String analyzer) {
            this.analyzer = analyzer;
            return me();
        }

        final public B indexAnalyzer(String indexAnalyzer) {
            this.indexAnalyzer = indexAnalyzer;
            return me();
        }

        final public B queryAnalyzer(String queryAnalyzer) {
            this.queryAnalyzer = queryAnalyzer;
            return me();
        }

        final public B copyFrom(String field) {
            if (copyFrom == null)
                copyFrom = new LinkedHashSet<>();
            copyFrom.add(field);
            return me();
        }
    }

    final public static class Builder extends AbstractBuilder<Builder> {

        @Override
        protected Builder me() {
            return this;
        }
    }

}
