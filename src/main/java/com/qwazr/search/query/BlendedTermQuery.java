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
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.CollectionsUtils;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.search.Query;

public class BlendedTermQuery extends AbstractQuery<BlendedTermQuery> {

    public static class Term {

        @JsonProperty("generic_field")
        public final String genericField;
        public final String field;
        public final Object value;
        public final Float boost;

        @JsonCreator
        public Term(@JsonProperty("generic_field") final String genericField, @JsonProperty("field") final String field,
                    @JsonProperty("value") final Object value, @JsonProperty("boost") final Float boost) {
            this.genericField = genericField;
            this.field = field;
            this.value = value;
            this.boost = boost;
        }

        private void add(final FieldMap fieldMap,
                         final org.apache.lucene.search.BlendedTermQuery.Builder builder) {
            final org.apache.lucene.index.Term term =
                BytesRefUtils.toTerm(fieldMap == null ? field
                        : fieldMap.getFieldType(genericField, field, value).resolveFieldName(field, null, null),
                    value);
            if (boost == null)
                builder.add(term);
            else
                builder.add(term, boost);
        }

        @Override
        public int hashCode() {
            return Objects.hash(genericField, field, value, boost);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Term))
                return false;
            if (o == this)
                return true;
            final Term t = (Term) o;
            return Objects.equals(genericField, t.genericField) && Objects.equals(field, t.field) &&
                Objects.equals(value, t.value) && Objects.equals(boost, t.boost);
        }
    }

    final public Collection<Term> terms;

    @JsonCreator
    public BlendedTermQuery(@JsonProperty("terms") final Collection<Term> terms) {
        super(BlendedTermQuery.class);
        this.terms = terms;
    }

    private final static URI DOC = URI.create("core/org/apache/lucene/search/BlendedTermQuery.html");

    public BlendedTermQuery(final IndexSettingsDefinition indexSettingsDefinition,
                            final Map<String, AnalyzerDefinition> analyzers,
                            final Map<String, FieldDefinition> fields) {
        super(BlendedTermQuery.class, DOC);
        final String field = getFullTextField(fields,
            () -> getTextField(fields,
                () -> "text"));
        terms = List.of(new Term(null, field, "hello", 2.0f),
            new Term(null, field, "world", 1.0f));
    }

    public BlendedTermQuery term(final String genericField, final String field, final String value, final Float boost) {
        terms.add(new Term(genericField, field, value, boost));
        return this;
    }

    public BlendedTermQuery term(final String field, final String value, final Float boost) {
        return term(null, field, value, boost);
    }

    public BlendedTermQuery term(final String genericField, final String field, final String value) {
        return term(genericField, field, value, null);
    }

    public BlendedTermQuery term(final String field, final String value) {
        return term(null, field, value, null);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) {
        org.apache.lucene.search.BlendedTermQuery.Builder builder =
            new org.apache.lucene.search.BlendedTermQuery.Builder();
        if (terms != null)
            terms.forEach(term -> term.add(queryContext.getFieldMap(), builder));
        return builder.build();
    }

    @Override
    protected boolean isEqual(BlendedTermQuery query) {
        return CollectionsUtils.equals(terms, query.terms);
    }

}
