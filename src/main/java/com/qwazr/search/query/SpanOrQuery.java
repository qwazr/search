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
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.spans.SpanQuery;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class SpanOrQuery extends AbstractSpanQuery<SpanOrQuery> {

    final public List<AbstractSpanQuery<?>> clauses;

    @JsonCreator
    public SpanOrQuery(@JsonProperty("clauses") final List<AbstractSpanQuery<?>> clauses) {
        super(SpanOrQuery.class);
        this.clauses = Objects.requireNonNull(clauses, "The clauses are null");
    }

    @Override
    final public SpanQuery getQuery(final QueryContext queryContext)
        throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
        final SpanQuery[] spanQueries = new SpanQuery[clauses.size()];
        int i = 0;
        for (final AbstractSpanQuery<?> query : clauses)
            spanQueries[i++] = query.getQuery(queryContext);
        return new org.apache.lucene.search.spans.SpanOrQuery(spanQueries);
    }

    @Override
    protected boolean isEqual(SpanOrQuery q) {
        return Objects.equals(clauses, q.clauses);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hashCode(clauses);
    }
}
