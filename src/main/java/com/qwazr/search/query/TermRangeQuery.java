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
import com.qwazr.utils.StringUtils;
import java.util.Objects;
import org.apache.lucene.search.MultiTermQuery;

public class TermRangeQuery extends AbstractFieldQuery<TermRangeQuery> {

    final public String lower_term;
    final public String upper_term;
    final public Boolean include_lower;
    final public Boolean include_upper;

    @JsonCreator
    public TermRangeQuery(@JsonProperty("generic_field") final String genericField,
                          @JsonProperty("field") final String field, @JsonProperty("lower_term") final String lower_term,
                          @JsonProperty("upper_term") final String upper_term,
                          @JsonProperty("include_lower") final Boolean include_lower,
                          @JsonProperty("include_upper") final Boolean include_upper) {
        super(TermRangeQuery.class, genericField, field);
        this.lower_term = lower_term;
        this.upper_term = upper_term;
        this.include_lower = include_lower;
        this.include_upper = include_upper;
    }

    public TermRangeQuery(final String field, final String lower_term, final String upper_term,
                          final Boolean include_lower, final Boolean include_upper) {
        this(null, field, lower_term, upper_term, include_lower, include_upper);
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(final TermRangeQuery q) {
        return super.isEqual(q) && Objects.equals(lower_term, q.lower_term) &&
            Objects.equals(upper_term, q.upper_term) && Objects.equals(include_lower, q.include_lower) &&
            Objects.equals(include_upper, q.include_upper);
    }

    @Override
    final public MultiTermQuery getQuery(final QueryContext queryContext) {
        return org.apache.lucene.search.TermRangeQuery.newStringRange(
            resolveField(queryContext.getFieldMap(), StringUtils.EMPTY),
            lower_term, upper_term, include_lower == null ? true : include_lower,
            include_upper == null ? true : include_upper);
    }
}
