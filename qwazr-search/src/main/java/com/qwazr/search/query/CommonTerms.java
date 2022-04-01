/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.annotations.QuerySampleCreator;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.Equalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.queries.CommonTermsQuery;
import org.apache.lucene.search.Query;

public class CommonTerms extends AbstractQuery<CommonTerms> {

    @JsonProperty("high_freq_occur")
    public final Bool.Occur highFreqOccur;

    @JsonProperty("low_freq_occur")
    public final Bool.Occur lowFreqOccur;

    @JsonProperty("max_term_frequency")
    public final Float maxTermFrequency;

    @JsonProperty("terms")
    public final List<Term> terms;

    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class Term extends Equalizer.Immutable<Term> {

        public final String field;
        public final Object value;

        @JsonCreator
        protected Term(@JsonProperty("field") final String field,
                       @JsonProperty("value") final Object value) {
            super(Term.class);
            this.field = field;
            this.value = value;
        }

        private org.apache.lucene.index.Term toTerm() {
            return BytesRefUtils.toTerm(field, value);
        }

        @Override
        protected boolean isEqual(Term term) {
            return Objects.equals(field, term.field) && Objects.equals(value, term.value);
        }

        @Override
        protected int computeHashCode() {
            return Objects.hash(field, value);
        }
    }

    @JsonCreator
    private CommonTerms(@JsonProperty("high_freq_occur") final Bool.Occur highFreqOccur,
                        @JsonProperty("low_freq_occur") final Bool.Occur lowFreqOccur,
                        @JsonProperty("max_term_frequency") final Float maxTermFrequency,
                        @JsonProperty("terms") final List<Term> terms) {
        super(CommonTerms.class);
        this.highFreqOccur = highFreqOccur;
        this.lowFreqOccur = lowFreqOccur;
        this.maxTermFrequency = maxTermFrequency;
        this.terms = terms;
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "queries/org/apache/lucene/queries/CommonTermsQuery.html")
    public CommonTerms(final IndexSettingsDefinition settings,
                       final Map<String, AnalyzerDefinition> analyzers,
                       final Map<String, FieldDefinition> fields) {
        super(CommonTerms.class);
        final String field = getFullTextField(fields,
            () -> getTextField(fields,
                () -> "text"));
        highFreqOccur = Bool.Occur.must;
        lowFreqOccur = Bool.Occur.should;
        maxTermFrequency = 0.5f;
        terms = List.of(new Term(field, "a"), new Term(field, "of"));
    }

    @Override
    protected boolean isEqual(final CommonTerms other) {
        return Objects.equals(highFreqOccur, other.highFreqOccur) && Objects.equals(lowFreqOccur, other.lowFreqOccur) &&
            Objects.equals(maxTermFrequency, other.maxTermFrequency) && Objects.equals(terms, other.terms);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(highFreqOccur, lowFreqOccur, maxTermFrequency, terms);
    }

    @Override
    public Query getQuery(final QueryContext queryContext) {
        final CommonTermsQuery commonTermsQuery = new CommonTermsQuery(
            highFreqOccur.occur, lowFreqOccur.occur,
            maxTermFrequency == null ? 1f : maxTermFrequency
        );
        if (terms != null)
            terms.forEach(term -> commonTermsQuery.add(term.toTerm()));
        return commonTermsQuery;
    }

    public static Builder of() {
        return new Builder();
    }

    public static class Builder {

        private Bool.Occur highFreqOccur;
        private Bool.Occur lowFreqOccur;
        private float maxTermFrequency;
        private final List<Term> terms;

        private Builder() {
            terms = new ArrayList<>();
            maxTermFrequency = 1;
        }

        public Builder highFreqOccur(Bool.Occur highFreqOccur) {
            this.highFreqOccur = highFreqOccur;
            return this;
        }

        public Builder lowFreqOccur(Bool.Occur lowFreqOccur) {
            this.lowFreqOccur = lowFreqOccur;
            return this;
        }

        public Builder maxTermFrequency(float maxTermFrequency) {
            this.maxTermFrequency = maxTermFrequency;
            return this;
        }

        public Builder term(String field, Object value) {
            terms.add(new Term(field, value));
            return this;
        }

        public CommonTerms build() {
            return new CommonTerms(highFreqOccur, lowFreqOccur, maxTermFrequency, new ArrayList<>(terms));
        }

    }
}
