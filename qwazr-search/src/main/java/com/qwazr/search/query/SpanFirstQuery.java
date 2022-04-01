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
import java.util.Objects;

public class SpanFirstQuery extends AbstractSpanQuery<SpanFirstQuery> {

    final public AbstractSpanQuery<?> spanQuery;
    final public Integer end;

    @JsonCreator
    public SpanFirstQuery(@JsonProperty("spanQuery") final AbstractSpanQuery<?> spanQuery,
                          @JsonProperty("end") final Integer end) {
        super(SpanFirstQuery.class);
        this.spanQuery = spanQuery;
        this.end = end;
    }

    @Override
    final public SpanQuery getQuery(final QueryContext queryContext)
        throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
        return new org.apache.lucene.search.spans.SpanFirstQuery(spanQuery.getQuery(queryContext),
            end == null ? 0 : end);
    }

    @Override
    protected boolean isEqual(SpanFirstQuery q) {
        return Objects.equals(spanQuery, q.spanQuery) && Objects.equals(end, q.end);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(spanQuery, end);
    }
}
