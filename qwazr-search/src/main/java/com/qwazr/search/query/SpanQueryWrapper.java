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
import com.qwazr.search.index.QueryContext;
import java.io.IOException;
import java.util.Objects;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanQuery;

public class SpanQueryWrapper extends AbstractSpanQuery<SpanQueryWrapper> {

    @JsonProperty("wrapped_query")
    final public AbstractQuery<?> wrappedQuery;

    @JsonCreator
    public SpanQueryWrapper(@JsonProperty("wrapped_query") final AbstractQuery<?> wrappedQuery) {
        super(SpanQueryWrapper.class);
        this.wrappedQuery = wrappedQuery;
    }

    @Override
    @JsonIgnore
    final public SpanQuery getQuery(final QueryContext queryContext)
        throws IOException, ReflectiveOperationException, ParseException, QueryNodeException {
        final Query subQuery = wrappedQuery.getQuery(queryContext);
        if (subQuery instanceof SpanQuery)
            return (SpanQuery) subQuery;
        else if (subQuery instanceof MultiTermQuery)
            return new SpanMultiTermQueryWrapper<>((MultiTermQuery) subQuery);
        else if (subQuery instanceof org.apache.lucene.search.TermQuery)
            return new org.apache.lucene.search.spans.SpanTermQuery(
                ((org.apache.lucene.search.TermQuery) subQuery).getTerm());
        throw new ParseException("Cannot convert " + subQuery.getClass().getName() + " as SpanQuery");
    }

    @Override
    protected boolean isEqual(SpanQueryWrapper q) {
        return Objects.equals(wrappedQuery, q.wrappedQuery);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hashCode(wrappedQuery);
    }
}
