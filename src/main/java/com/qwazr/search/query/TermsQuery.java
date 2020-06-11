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
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.CollectionsUtils;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.util.BytesRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class TermsQuery extends AbstractMultiTermQuery<TermsQuery> {

    final public Collection<Object> terms;

    @JsonIgnore
    final private Collection<BytesRef> bytesRefCollection;

    @JsonCreator
    private TermsQuery(@JsonProperty("generic_field") final String genericField, @JsonProperty("field") final String field,
                       @JsonProperty("terms") final Collection<Object> terms) {
        super(TermsQuery.class, genericField, field, null);
        this.terms = Objects.requireNonNull(terms, "The term list is null");
        this.bytesRefCollection = null;
    }

    private TermsQuery(final Builder builder) {
        super(TermsQuery.class, builder);
        this.terms = builder.objects;
        this.bytesRefCollection = builder.bytesRefs;
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(TermsQuery q) {
        return super.isEqual(q) && CollectionsUtils.equals(terms, q.terms);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) {
        final Collection<BytesRef> bytesRefs;
        if (bytesRefCollection == null) {
            bytesRefs = new ArrayList<>();
            terms.forEach(term -> bytesRefs.add(BytesRefUtils.fromAny(term)));
        } else
            bytesRefs = bytesRefCollection;
        return new TermInSetQuery(resolveField(queryContext.getFieldMap(), FieldTypeInterface.LuceneFieldType.text), bytesRefs);
    }

    public static Builder of(final String genericField, final String field) {
        return new Builder(genericField, field);
    }

    public static Builder of(final String field) {
        return of(null, field);
    }

    public static class Builder extends MultiTermBuilder<TermsQuery, Builder> {

        private Builder(final String genericField, final String field) {
            super(genericField, field);
        }

        @Override
        protected Builder me() {
            return this;
        }

        final public TermsQuery build() {
            return new TermsQuery(this);
        }
    }
}
