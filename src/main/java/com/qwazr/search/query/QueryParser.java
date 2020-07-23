/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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
import com.qwazr.utils.StringUtils;
import java.util.Objects;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;

public class QueryParser extends AbstractClassicQueryParser<QueryParser> {

    @JsonProperty("default_field")
    public final String defaulField;

    @JsonCreator
    private QueryParser(@JsonProperty("default_field") String defaulField,
                        @JsonProperty("enable_position_increments") final Boolean enablePositionIncrements,
                        @JsonProperty("auto_generate_multi_term_synonyms_phrase_query") final Boolean autoGenerateMultiTermSynonymsPhraseQuery,
                        @JsonProperty("enable_graph_queries") final Boolean enableGraphQueries,
                        @JsonProperty("query_string") final String queryString,
                        @JsonProperty("analyzer") final String analyzer) {
        super(QueryParser.class, enablePositionIncrements, autoGenerateMultiTermSynonymsPhraseQuery, enableGraphQueries, queryString, analyzer);
        this.defaulField = defaulField;
    }

    private QueryParser(Builder builder) {
        super(QueryParser.class, builder);
        this.defaulField = builder.defaulField;
    }

    @JsonIgnore
    @Override
    protected boolean isEqual(QueryParser q) {
        return super.isEqual(q) && Objects.equals(defaulField, q.defaulField);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) throws ParseException {
        final FieldMap fieldMap = queryContext.getFieldMap();
        final org.apache.lucene.queryparser.classic.QueryParser parser =
            new org.apache.lucene.queryparser.classic.QueryParser(
                resolveFullTextField(fieldMap, defaulField, defaulField, StringUtils.EMPTY), null);
        setParserParameters(queryContext, parser);
        return parser.parse(Objects.requireNonNull(queryString, "The query string is missing"));
    }

    public static Builder of(String defaulField) {
        return new Builder().setDefaultField(defaulField);
    }

    public static class Builder extends AbstractParserBuilder<Builder, QueryParser> {

        private String defaulField;

        protected Builder() {
            super(Builder.class);
        }

        @Override
        public QueryParser build() {
            return new QueryParser(this);
        }

        public Builder setDefaultField(String defaulField) {
            this.defaulField = defaulField;
            return this;
        }

    }
}
