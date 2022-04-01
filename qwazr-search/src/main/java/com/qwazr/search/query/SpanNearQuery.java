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
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.StringUtils;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.spans.SpanQuery;

public class SpanNearQuery extends AbstractFieldSpanQuery<SpanNearQuery> {

    final public List<AbstractSpanQuery<?>> clauses;
    @JsonProperty("in_order")
    final public Boolean inOrder;
    final public Integer slop;

    @JsonCreator
    public SpanNearQuery(@JsonProperty("generic_field") final String genericField,
                         @JsonProperty("clauses") final List<AbstractSpanQuery<?>> clauses,
                         @JsonProperty("field") final String field,
                         @JsonProperty("in_order") final Boolean inOrder,
                         @JsonProperty("slop") final Integer slop) {
        super(SpanNearQuery.class, genericField, field);
        this.clauses = clauses;
        this.inOrder = inOrder;
        this.slop = slop;
    }

    public SpanNearQuery(final List<AbstractSpanQuery<?>> clauses, @JsonProperty("field") final String field,
                         final Boolean in_order, final Integer slop) {
        this(null, clauses, field, in_order, slop);
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(final SpanNearQuery q) {
        return super.isEqual(q) && CollectionsUtils.equals(clauses, q.clauses) &&
            Objects.equals(inOrder, q.inOrder) && Objects.equals(slop, q.slop);
    }


    @Override
    protected int computeHashCode() {
        return Objects.hash(field, clauses, inOrder, slop);
    }

    @Override
    final public SpanQuery getQuery(final QueryContext queryContext)
        throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
        final org.apache.lucene.search.spans.SpanNearQuery.Builder builder =
            new org.apache.lucene.search.spans.SpanNearQuery.Builder(
                FieldResolver.resolveFullTextField(queryContext.getFieldMap(), genericField, field, StringUtils.EMPTY),
                inOrder != null && inOrder);
        if (slop != null)
            builder.setSlop(slop);
        if (clauses != null)
            for (final AbstractSpanQuery<?> clause : clauses)
                builder.addClause(clause.getQuery(queryContext));
        return builder.build();
    }
}
