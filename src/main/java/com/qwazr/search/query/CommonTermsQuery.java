/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.Equalizer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommonTermsQuery extends AbstractQuery<CommonTermsQuery> {

    @JsonProperty("high_freq_occur")
    public final BooleanQuery.Occur highFreqOccur;

    @JsonProperty("low_freq_occur")
    public final BooleanQuery.Occur lowFreqOccur;

    @JsonProperty("max_term_frequency")
    public final Float maxTermFrequency;

    @JsonProperty("disable_coord")
    public final Boolean disableCoord;

    @JsonProperty("terms")
    public final List<Term> terms;

    @JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
            setterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    public static class Term extends Equalizer<Term> {

        public final String field;
        public final Object value;

        @JsonCreator
        protected Term(@JsonProperty("field") final String field, @JsonProperty("value") final Object value) {
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
        public int hashCode() {
            return Objects.hash(field, value);
        }
    }

    @JsonCreator
    private CommonTermsQuery(@JsonProperty("high_freq_occur") final BooleanQuery.Occur highFreqOccur,
            @JsonProperty("low_freq_occur") final BooleanQuery.Occur lowFreqOccur,
            @JsonProperty("max_term_frequency") final Float maxTermFrequency,
            @JsonProperty("disable_coord") final Boolean disableCoord, @JsonProperty("terms") final List<Term> terms) {
        super(CommonTermsQuery.class);
        this.highFreqOccur = highFreqOccur;
        this.lowFreqOccur = lowFreqOccur;
        this.maxTermFrequency = maxTermFrequency;
        this.disableCoord = disableCoord;
        this.terms = terms;
    }

    @Override
    protected boolean isEqual(final CommonTermsQuery other) {
        return Objects.equals(highFreqOccur, other.highFreqOccur) && Objects.equals(lowFreqOccur, other.lowFreqOccur) &&
                Objects.equals(maxTermFrequency, other.maxTermFrequency) &&
                Objects.equals(disableCoord, other.disableCoord) && Objects.equals(terms, other.terms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(highFreqOccur, lowFreqOccur, maxTermFrequency, disableCoord, terms);
    }

    @Override
    public Query getQuery(final QueryContext queryContext)
            throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
        final org.apache.lucene.queries.CommonTermsQuery commonTermsQuery =
                new org.apache.lucene.queries.CommonTermsQuery(highFreqOccur.occur, lowFreqOccur.occur,
                        maxTermFrequency == null ? 1f : maxTermFrequency, disableCoord == null ? false : disableCoord);
        if (terms != null)
            terms.forEach(term -> commonTermsQuery.add(term.toTerm()));
        return commonTermsQuery;
    }

    public static Builder of() {
        return new Builder();
    }

    public static class Builder {

        private BooleanQuery.Occur highFreqOccur;
        private BooleanQuery.Occur lowFreqOccur;
        private float maxTermFrequency;
        private boolean disableCoord;
        private final List<Term> terms;

        private Builder() {
            terms = new ArrayList<>();
            disableCoord = false;
            maxTermFrequency = 1;
        }

        public Builder highFreqOccur(BooleanQuery.Occur highFreqOccur) {
            this.highFreqOccur = highFreqOccur;
            return this;
        }

        public Builder lowFreqOccur(BooleanQuery.Occur lowFreqOccur) {
            this.lowFreqOccur = lowFreqOccur;
            return this;
        }

        public Builder maxTermFrequency(float maxTermFrequency) {
            this.maxTermFrequency = maxTermFrequency;
            return this;
        }

        public Builder disableCoord(boolean disableCoord) {
            this.disableCoord = disableCoord;
            return this;
        }

        public Builder term(String field, Object value) {
            terms.add(new Term(field, value));
            return this;
        }

        public CommonTermsQuery build() {
            return new CommonTermsQuery(highFreqOccur, lowFreqOccur, maxTermFrequency, disableCoord,
                    new ArrayList<>(terms));
        }

    }
}
