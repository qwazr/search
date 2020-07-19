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
import java.util.Objects;
import org.apache.lucene.search.Query;

@Deprecated
/*
 * @see HasTerm
 */
public class TermQuery extends AbstractFieldQuery<TermQuery> {

    final public Object term;

    @JsonCreator
    public TermQuery(@JsonProperty("generic_field") final String genericField,
                     @JsonProperty("field") final String field,
                     @JsonProperty("term") final Object term) {
        super(TermQuery.class, genericField, field);
        this.term = term;
    }

    public TermQuery(final String field, final Object term) {
        this(null, field, term);
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(final TermQuery q) {
        return super.isEqual(q) && Objects.equals(term, q.term);
    }


    @Override
    @JsonIgnore
    protected int computeHashCode() {
        return Objects.hash(field, term);
    }

    @Override
    @JsonIgnore
    final public Query getQuery(final QueryContext queryContext) {
        return new org.apache.lucene.search.TermQuery(
            resolveIndexTextTerm(queryContext.getFieldMap(), term)
        );
    }
}
