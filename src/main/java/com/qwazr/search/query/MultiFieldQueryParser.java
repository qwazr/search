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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;

import java.util.Objects;

public class MultiFieldQueryParser extends AbstractClassicQueryParser<MultiFieldQueryParser> {

    @JsonCreator
    private MultiFieldQueryParser(final @JsonProperty("enable_position_increments") Boolean enablePositionIncrements,
                                  final @JsonProperty("auto_generate_multi_term_synonyms_phrase_query") Boolean autoGenerateMultiTermSynonymsPhraseQuery,
                                  final @JsonProperty("enable_graph_queries") Boolean enableGraphQueries,
                                  final @JsonProperty("query_string") String queryString,
                                  final @JsonProperty("analyzer") String analyzer) {
        super(MultiFieldQueryParser.class, enablePositionIncrements, autoGenerateMultiTermSynonymsPhraseQuery, enableGraphQueries, queryString, analyzer);
    }

    public MultiFieldQueryParser(Builder builder) {
        super(MultiFieldQueryParser.class, builder);
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

        protected Builder() {
            super(Builder.class);
        }

        @Override
        public MultiFieldQueryParser build() {
            return new MultiFieldQueryParser(this);
        }

    }
}
