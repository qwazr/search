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

public class SpanNotQuery extends AbstractSpanQuery<SpanNotQuery> {

    final public AbstractSpanQuery<?> include;
    final public AbstractSpanQuery<?> exclude;
    final public Integer dist;
    final public Integer pre;
    final public Integer post;

    @JsonCreator
    public SpanNotQuery(@JsonProperty("include") final AbstractSpanQuery<?> include,
                        @JsonProperty("exclude") final AbstractSpanQuery<?> exclude,
                        @JsonProperty("pre") final Integer pre,
                        @JsonProperty("post") final Integer post,
                        @JsonProperty("dist") final Integer dist) {
        super(SpanNotQuery.class);
        this.include = include;
        this.exclude = exclude;
        this.pre = pre;
        this.post = post;
        this.dist = dist;
    }

    @Override
    final public SpanQuery getQuery(final QueryContext queryContext)
        throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
        Objects.requireNonNull(include);
        Objects.requireNonNull(exclude);
        final SpanQuery includeQuery = include.getQuery(queryContext);
        final SpanQuery excludeQuery = exclude.getQuery(queryContext);
        if (dist != null)
            return new org.apache.lucene.search.spans.SpanNotQuery(includeQuery, excludeQuery, dist);
        else if (pre != null || post != null)
            return new org.apache.lucene.search.spans.SpanNotQuery(includeQuery, excludeQuery, pre == null ? 0 : pre,
                post == null ? 0 : post);
        return new org.apache.lucene.search.spans.SpanNotQuery(includeQuery, excludeQuery);
    }

    @Override
    protected boolean isEqual(SpanNotQuery q) {
        return Objects.equals(include, q.include) && Objects.equals(exclude, q.exclude) &&
            Objects.equals(dist, q.dist) && Objects.equals(pre, q.pre) && Objects.equals(post, q.post);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(include, exclude, dist, pre, post);
    }
}
