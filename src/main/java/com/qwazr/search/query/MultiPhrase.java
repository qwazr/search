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
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.annotations.QuerySampleCreator;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.index.Term;

public class MultiPhrase extends AbstractFieldQuery<MultiPhrase> {

    final public List<String[]> terms;
    final public List<Integer> positions;
    final public Integer slop;

    @JsonCreator
    public MultiPhrase(@JsonProperty("generic_field") final String genericField,
                       @JsonProperty("field") final String field,
                       @JsonProperty("terms") final List<String[]> terms,
                       @JsonProperty("positions") final List<Integer> positions,
                       @JsonProperty("slop") final Integer slop) {
        super(MultiPhrase.class, genericField, field);
        this.terms = terms;
        this.positions = positions;
        this.slop = slop;
    }

    public MultiPhrase(final String field, final List<String[]> terms, final List<Integer> positions, Integer slop) {
        this(null, field, terms, positions, slop);
    }

    public MultiPhrase(final String field, final Integer slop) {
        this(field, new ArrayList<>(), new ArrayList<>(), slop);
    }

    private MultiPhrase(final Builder builder) {
        this(builder.field, builder.terms, builder.positions, builder.slop);
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/search/MultiPhraseQuery.html")
    public static MultiPhrase create(final IndexSettingsDefinition settings,
                                     final Map<String, AnalyzerDefinition> analyzers,
                                     final Map<String, FieldDefinition> fields) {
        final Builder builder = of(getFullTextField(fields, () -> getTextField(fields, () -> "text")));
        builder.addTermPosition(0, "Hello", "World");
        return builder.build();
    }

    public static Builder of(String field) {
        return new Builder(field);
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(final MultiPhrase q) {
        if (!super.isEqual(q) || !CollectionsUtils.equals(positions, q.positions) && !Objects.equals(slop, q.slop))
            return false;
        if (terms == q.terms)
            return true;
        if (terms == null || q.terms == null)
            return false;
        if (terms.size() != q.terms.size())
            return false;
        int i = 0;
        for (String[] t : terms)
            if (!Arrays.equals(t, q.terms.get(i++)))
                return false;
        return true;
    }

    @Override
    final public org.apache.lucene.search.MultiPhraseQuery getQuery(final QueryContext queryContext) {
        Objects.requireNonNull(field, "The field property should not be null");
        final String resolvedField = resolveIndexTextField(queryContext.getFieldMap(), StringUtils.EMPTY);
        final org.apache.lucene.search.MultiPhraseQuery.Builder builder =
            new org.apache.lucene.search.MultiPhraseQuery.Builder();
        if (slop != null)
            builder.setSlop(slop);
        if (terms != null) {
            if (positions == null || positions.isEmpty()) {
                for (String[] term : terms)
                    builder.add(toTerms(resolvedField, term));
            } else {
                int i = 0;
                for (String[] term : terms) {
                    builder.add(toTerms(resolvedField, term), positions.get(i++));
                }
            }
        }
        return builder.build();
    }

    private Term[] toTerms(final String resolvedField, final String[] termArray) {
        final Term[] terms = new Term[termArray.length];
        int i = 0;
        for (String term : termArray)
            terms[i++] = new Term(resolvedField, term);
        return terms;
    }

    public static class Builder {

        private final String field;
        private final List<String[]> terms;
        private List<Integer> positions;
        private Integer slop;

        private Builder(String field) {
            this.field = field;
            terms = new ArrayList<>();
            positions = new ArrayList<>();
        }

        public Builder setSlop(final int slop) {
            this.slop = slop;
            return this;
        }

        public Builder addTerm(final String... terms) {
            this.terms.add(terms);
            return this;
        }

        public Builder addTermPosition(final Integer position, final String... terms) {
            this.terms.add(terms);
            this.positions.add(position);
            return this;
        }

        public MultiPhrase build() {
            return new MultiPhrase(this);
        }
    }

}
