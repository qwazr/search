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
package com.qwazr.search.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.search.annotations.Copy;
import com.qwazr.search.annotations.SmartField;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.analysis.Analyzer;

import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SmartFieldDefinition extends BaseFieldDefinition<SmartFieldDefinition> {

    public final static int DEFAULT_MAX_KEYWORD_LENGTH = 2048;

    @JsonProperty("index")
    final public Boolean index;
    @JsonProperty("facet")
    final public Boolean facet;
    @JsonProperty("sort")
    final public Boolean sort;
    @JsonProperty("stored")
    final public Boolean stored;
    @JsonProperty("multivalued")
    final public Boolean multivalued;
    @JsonProperty("maxKeywordLength")
    final public Integer maxKeywordLength;

    public enum Type {
        TEXT, LONG, INTEGER, DOUBLE, FLOAT
    }

    @JsonCreator
    SmartFieldDefinition(@JsonProperty("type") final Type type,
                         @JsonProperty("facet") final Boolean facet,
                         @JsonProperty("index") final Boolean index,
                         @JsonProperty("max_keyword_length") final Integer maxKeywordLength,
                         @JsonProperty("analyzer") final SmartAnalyzerSet analyzer,
                         @JsonProperty("index_analyzer") final String indexAnalyzer,
                         @JsonProperty("query_analyzer") final String queryAnalyzer,
                         @JsonProperty("sort") final Boolean sort,
                         @JsonProperty("stored") final Boolean stored,
                         @JsonProperty("multivalued") final Boolean multivalued,
                         @JsonProperty("copy_from") final String[] copyFrom) {
        super(SmartFieldDefinition.class,
            type,
            analyzer != null && analyzer != SmartAnalyzerSet.keyword ? analyzer.name() : null,
            indexAnalyzer,
            queryAnalyzer,
            copyFrom);
        this.facet = facet;
        this.index = index;
        this.sort = sort;
        this.stored = stored;
        this.multivalued = multivalued;
        this.maxKeywordLength = maxKeywordLength;
    }

    private SmartFieldDefinition(final SmartBuilder builder) {
        super(SmartFieldDefinition.class, builder);
        facet = builder.facet;
        index = builder.index;
        sort = builder.sort;
        stored = builder.stored;
        multivalued = builder.multivalued;
        maxKeywordLength = builder.maxKeywordLength;
    }

    public SmartFieldDefinition(final String fieldName,
                                final SmartField smartField,
                                final Map<String, Copy> copyMap) {
        super(SmartFieldDefinition.class,
            smartField.type(),
            smartField.analyzerClass() != Analyzer.class ? smartField.analyzerClass().getName() : smartField.analyzer().name(),
            from(smartField.indexAnalyzer(), smartField.indexAnalyzerClass()),
            from(smartField.queryAnalyzer(), smartField.queryAnalyzerClass()),
            from(fieldName, copyMap));
        this.facet = smartField.facet();
        this.index = smartField.index();
        this.sort = smartField.sort();
        this.stored = smartField.stored();
        this.multivalued = smartField.multivalued();
        this.maxKeywordLength = smartField.maxKeywordLength();
    }

    @Override
    protected boolean isEqual(final SmartFieldDefinition f) {
        return super.isEqual(f)
            && Objects.equals(facet, f.facet)
            && Objects.equals(index, f.index)
            && Objects.equals(sort, f.sort)
            && Objects.equals(stored, f.stored)
            && Objects.equals(maxKeywordLength, f.maxKeywordLength);
    }

    @Override
    @JsonIgnore
    public FieldTypeInterface newFieldType(final String genericFieldName,
                                           final WildcardMatcher wildcardMatcher,
                                           final String primaryKey) {
        return new SmartFieldType(genericFieldName, wildcardMatcher, primaryKey, this);
    }

    public static SmartBuilder of() {
        return new SmartBuilder();
    }

    public static class SmartBuilder extends AbstractBuilder<SmartBuilder> {

        public Boolean facet;
        public Boolean index;
        public Boolean sort;
        public Boolean stored;
        public Boolean multivalued;
        public Integer maxKeywordLength;

        public SmartBuilder facet(Boolean facet) {
            this.facet = facet;
            return this;
        }

        public SmartBuilder index(Boolean index) {
            this.index = index;
            return this;
        }

        public SmartBuilder sort(Boolean sort) {
            this.sort = sort;
            return this;
        }

        public SmartBuilder stored(Boolean stored) {
            this.stored = stored;
            return this;
        }

        public SmartBuilder multivalued(Boolean multivalued) {
            this.stored = multivalued;
            return this;
        }

        public SmartBuilder maxKeywordLength(Integer maxKeywordLength) {
            this.maxKeywordLength = maxKeywordLength;
            return this;
        }

        public SmartFieldDefinition build() {
            return new SmartFieldDefinition(this);
        }

        @Override
        protected SmartBuilder me() {
            return this;
        }
    }

}
