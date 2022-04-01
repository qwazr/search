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
package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.ArrayUtils;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.StringUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;

public class MultiFieldQueryParser extends AbstractClassicQueryParser<MultiFieldQueryParser> {

    final public String[] fields;
    final public LinkedHashMap<String, Float> boosts;

    @JsonCreator
    private MultiFieldQueryParser(final @JsonProperty("enable_position_increments") Boolean enablePositionIncrements,
                                  final @JsonProperty("auto_generate_multi_term_synonyms_phrase_query") Boolean autoGenerateMultiTermSynonymsPhraseQuery,
                                  final @JsonProperty("enable_graph_queries") Boolean enableGraphQueries,
                                  final @JsonProperty("query_string") String queryString,
                                  final @JsonProperty("analyzer") String analyzer) {
        super(MultiFieldQueryParser.class, enablePositionIncrements, autoGenerateMultiTermSynonymsPhraseQuery, enableGraphQueries, queryString, analyzer);
        this.fields = null;
        this.boosts = null;
    }

    public MultiFieldQueryParser(Builder builder) {
        super(MultiFieldQueryParser.class, builder);
        this.fields = builder.fields == null ? null : ArrayUtils.toArray(builder.fields);
        this.boosts = builder.boosts;
    }

    @JsonIgnore
    @Override
    protected boolean isEqual(MultiFieldQueryParser q) {
        return super.isEqual(q)
            && Arrays.equals(fields, q.fields)
            && CollectionsUtils.equals(boosts, q.boosts);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(super.computeHashCode(), fields, boosts);
    }

    protected Map<String, Float> resolvedBoosts(final FieldMap fieldMap) {
        return boosts != null && fieldMap != null ?
            FieldMap.resolveFieldNames(boosts, new HashMap<>(),
                f -> fieldMap.getFieldType(f, f, StringUtils.EMPTY).resolveFieldName(f, null, null)) :
            boosts;
    }

    protected String[] resolveFields(final FieldMap fieldMap) {
        return fields != null && fieldMap != null ?
            FieldMap.resolveFieldNames(fields,
                f -> fieldMap.getFieldType(f, f, StringUtils.EMPTY).resolveFieldName(f, null, null)) :
            fields;
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) throws ParseException {
        final FieldMap fieldMap = queryContext.getFieldMap();
        final org.apache.lucene.queryparser.classic.MultiFieldQueryParser parser =
            new org.apache.lucene.queryparser.classic.MultiFieldQueryParser(resolveFields(fieldMap),
                null, resolvedBoosts(fieldMap));
        setParserParameters(queryContext, parser);
        return parser.parse(Objects.requireNonNull(queryString, "The query string is missing"));
    }

    public static Builder of() {
        return new Builder();
    }

    public static class Builder extends AbstractParserBuilder<Builder, MultiFieldQueryParser> {

        private LinkedHashSet<String> fields;
        private LinkedHashMap<String, Float> boosts;

        public Builder addField(Collection<String> fieldSet) {
            if (fieldSet != null) {
                if (fields == null)
                    fields = new LinkedHashSet<>();
                this.fields.addAll(fieldSet);
            }
            return me();
        }

        public Builder addField(String... fieldSet) {
            if (fields == null)
                fields = new LinkedHashSet<>();
            Collections.addAll(fields, fieldSet);
            return me();
        }

        public Builder addBoost(String field, Float boost) {
            if (boosts == null)
                boosts = new LinkedHashMap<>();
            boosts.put(field, boost);
            return me();
        }

        @Override
        protected Builder me() {
            return this;
        }

        @Override
        public MultiFieldQueryParser build() {
            return new MultiFieldQueryParser(this);
        }

    }
}
