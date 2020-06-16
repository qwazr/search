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
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.CollectionsUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DisjunctionMaxQuery extends AbstractQuery<DisjunctionMaxQuery> {

    final public List<AbstractQuery<?>> queries;

    @JsonProperty("tie_breaker_multiplier")
    final public Float tieBreakerMultiplier;

    @JsonCreator
    public DisjunctionMaxQuery(@JsonProperty("queries") final List<AbstractQuery<?>> queries,
                               @JsonProperty("tie_breaker_multiplier") final Float tieBreakerMultiplier) {
        super(DisjunctionMaxQuery.class);
        this.queries = queries;
        this.tieBreakerMultiplier = tieBreakerMultiplier;
    }

    public DisjunctionMaxQuery(final Float tieBreakerMultiplier, final AbstractQuery<?>... queries) {
        this(List.of(queries), tieBreakerMultiplier);
    }

    private final static URI DOC = URI.create("core/index.html?org/apache/lucene/search/DisjunctionMaxQuery.html");

    public DisjunctionMaxQuery(final IndexSettingsDefinition settings,
                               final Map<String, AnalyzerDefinition> analyzers,
                               final Map<String, FieldDefinition<?>> fields) {
        super(DisjunctionMaxQuery.class, DOC);
        final String field = getFullTextField(fields,
            () -> getTextField(fields,
                () -> "text"));
        queries = List.of(
            new TermQuery(field, "Hello"),
            new TermQuery(field, "World"));
        tieBreakerMultiplier = 0.1f;
    }

    @Override
    final public Query getQuery(final QueryContext queryContext)
        throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
        Objects.requireNonNull(queries, "The queries are missing");
        final List<Query> queryList = new ArrayList<>(queries.size());
        for (AbstractQuery<?> query : queries)
            queryList.add(query.getQuery(queryContext));
        return new org.apache.lucene.search.DisjunctionMaxQuery(queryList,
            tieBreakerMultiplier == null ? 0 : tieBreakerMultiplier);
    }

    @Override
    protected boolean isEqual(DisjunctionMaxQuery q) {
        return CollectionsUtils.equals(queries, q.queries);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(queries, tieBreakerMultiplier);
    }
}
