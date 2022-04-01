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

public class SpanWithinQuery extends AbstractSpanQuery<SpanWithinQuery> {

    final public AbstractSpanQuery<?> big;
    final public AbstractSpanQuery<?> little;

    @JsonCreator
    public SpanWithinQuery(@JsonProperty("big") final AbstractSpanQuery<?> big,
                           @JsonProperty("little") final AbstractSpanQuery<?> little) {
        super(SpanWithinQuery.class);
        this.big = Objects.requireNonNull(big, "the big query is missing");
        this.little = Objects.requireNonNull(little, "the little query is missing");
    }

    @Override
    final public SpanQuery getQuery(final QueryContext queryContext)
        throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {
        return new org.apache.lucene.search.spans.SpanWithinQuery(big.getQuery(queryContext),
            little.getQuery(queryContext));
    }

    @Override
    protected boolean isEqual(SpanWithinQuery q) {
        return Objects.equals(big, q.big) && Objects.equals(little, q.little);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(big, little);
    }
}
