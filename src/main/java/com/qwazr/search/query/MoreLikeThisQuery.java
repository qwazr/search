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
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.StringUtils;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;

public class MoreLikeThisQuery extends AbstractQuery<MoreLikeThisQuery> {

    final public String like_text;
    final public String fieldname;
    final public float percent_terms_to_match;

    final public Integer doc_num;

    final public Boolean is_boost;
    final public Float boost_factor;
    final public String[] fieldnames;
    final public Integer max_doc_freq;
    final public Integer max_doc_freq_pct;
    final public Integer max_num_tokens_parsed;
    final public Integer max_query_terms;
    final public Integer max_word_len;
    final public Integer min_doc_freq;
    final public Integer min_term_freq;
    final public Integer min_word_len;
    final public Set<String> stop_words;

    @JsonCreator
    private MoreLikeThisQuery() {
        super(MoreLikeThisQuery.class);
        like_text = null;
        fieldname = null;
        percent_terms_to_match = 0;
        doc_num = null;
        is_boost = null;
        boost_factor = null;
        fieldnames = null;
        max_doc_freq = null;
        max_doc_freq_pct = null;
        max_num_tokens_parsed = null;
        max_query_terms = null;
        max_word_len = null;
        min_doc_freq = null;
        min_term_freq = null;
        min_word_len = null;
        stop_words = null;
    }

    private MoreLikeThisQuery(final Builder builder) {
        super(MoreLikeThisQuery.class);
        this.like_text = builder.likeText;
        this.fieldname = builder.fieldname;
        this.percent_terms_to_match = builder.percentTermsToMatch;
        this.doc_num = builder.docNum;
        this.is_boost = builder.isBoost;
        this.boost_factor = builder.boostFactor;
        this.fieldnames = builder.fieldnames;
        this.max_doc_freq = builder.maxDocFreq;
        this.max_doc_freq_pct = builder.maxDocFreqPct;
        this.max_num_tokens_parsed = builder.maxNumTokensParsed;
        this.max_query_terms = builder.maxQueryTerms;
        this.max_word_len = builder.maxWordLen;
        this.min_doc_freq = builder.minDocFreq;
        this.min_term_freq = builder.minTermFreq;
        this.min_word_len = builder.minWordLen;
        this.stop_words = builder.stopWords;
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(like_text, fieldname, percent_terms_to_match, max_doc_freq, max_query_terms);
    }

    @Override
    protected boolean isEqual(MoreLikeThisQuery q) {
        return Objects.equals(like_text, q.like_text) && Objects.equals(fieldname, q.fieldname) &&
            Objects.equals(percent_terms_to_match, q.percent_terms_to_match) &&
            Objects.equals(doc_num, q.doc_num) && Objects.equals(is_boost, q.is_boost) &&
            Objects.equals(boost_factor, q.boost_factor) && Arrays.equals(fieldnames, q.fieldnames) &&
            Objects.equals(max_doc_freq, q.max_doc_freq) && Objects.equals(max_doc_freq_pct, q.max_doc_freq_pct) &&
            Objects.equals(max_num_tokens_parsed, q.max_num_tokens_parsed) &&
            Objects.equals(max_query_terms, q.max_query_terms) && Objects.equals(max_word_len, q.max_word_len) &&
            Objects.equals(min_doc_freq, q.min_doc_freq) && Objects.equals(min_term_freq, q.min_term_freq) &&
            Objects.equals(min_word_len, q.min_word_len) && CollectionsUtils.equals(stop_words, q.stop_words);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) throws IOException, ParseException {

        final FieldMap fieldMap = queryContext.getFieldMap();

        final MoreLikeThis mlt = new MoreLikeThis(queryContext.getIndexReader());
        if (is_boost != null)
            mlt.setBoost(is_boost);
        if (boost_factor != null)
            mlt.setBoostFactor(boost_factor);

        if (fieldnames != null)
            mlt.setFieldNames(fieldMap == null ?
                fieldnames :
                FieldMap.resolveFieldNames(fieldnames,
                    f -> fieldMap.getFieldType(f, f, StringUtils.EMPTY).resolveFieldName(f, null, null)));
        if (max_doc_freq != null)
            mlt.setMaxDocFreq(max_doc_freq);
        if (max_doc_freq_pct != null)
            mlt.setMaxDocFreqPct(max_doc_freq_pct);
        if (max_num_tokens_parsed != null)
            mlt.setMaxNumTokensParsed(max_num_tokens_parsed);
        if (max_query_terms != null)
            mlt.setMaxQueryTerms(max_query_terms);
        if (max_word_len != null)
            mlt.setMaxWordLen(max_word_len);
        if (min_doc_freq != null)
            mlt.setMinDocFreq(min_doc_freq);
        if (min_term_freq != null)
            mlt.setMinTermFreq(min_term_freq);
        if (min_word_len != null)
            mlt.setMinWordLen(min_word_len);
        if (stop_words != null)
            mlt.setStopWords(stop_words);
        mlt.setAnalyzer(queryContext.getQueryAnalyzer());

        if (doc_num != null)
            return mlt.like(doc_num);
        if (StringUtils.isEmpty(like_text) || StringUtils.isEmpty(fieldname))
            throw new ParseException("Either doc_num or like_text/fieldname are missing");

        final org.apache.lucene.search.BooleanQuery bq = (org.apache.lucene.search.BooleanQuery) mlt.like(
            resolveFullTextField(fieldMap, fieldname, fieldname, StringUtils.EMPTY), new StringReader(like_text));
        final org.apache.lucene.search.BooleanQuery.Builder newBq = new org.apache.lucene.search.BooleanQuery.Builder();
        for (BooleanClause clause : bq)
            newBq.add(clause);
        //make at least half the terms match
        newBq.setMinimumNumberShouldMatch((int) (bq.clauses().size() * percent_terms_to_match));
        return newBq.build();
    }

    public static Builder of(int docNum) {
        return new Builder(docNum);
    }

    public static Builder of(String likeText, String fieldName) {
        return of(likeText, fieldName, null);
    }

    public static Builder of(String likeText, String fieldName, Float percentTermsToMatch) {
        return new Builder(likeText, fieldName, percentTermsToMatch);
    }

    public static class Builder {

        public final String likeText;
        public final String fieldname;
        public final float percentTermsToMatch;
        public final Integer docNum;
        public Boolean isBoost;
        public Float boostFactor;
        public String[] fieldnames;
        public Integer maxDocFreq;
        public Integer maxDocFreqPct;
        public Integer maxNumTokensParsed;
        public Integer maxQueryTerms;
        public Integer maxWordLen;
        public Integer minDocFreq;
        public Integer minTermFreq;
        public Integer minWordLen;
        public LinkedHashSet<String> stopWords;

        private Builder(String likeText, String fieldname, Float percentTermsToMatch) {
            this.likeText = likeText;
            this.fieldname = fieldname;
            this.percentTermsToMatch = percentTermsToMatch == null ? 0.3F : percentTermsToMatch;
            this.docNum = null;
        }

        private Builder(Integer docNum) {
            this.likeText = null;
            this.fieldname = null;
            this.percentTermsToMatch = 0;
            this.docNum = docNum;
        }

        public Builder isBoost(Boolean isBoost) {
            this.isBoost = isBoost;
            return this;
        }

        public Builder boostFactor(Float boostFactor) {
            this.boostFactor = boostFactor;
            return this;
        }

        public Builder maxDocFreq(Integer maxDocFreq) {
            this.maxDocFreq = maxDocFreq;
            return this;
        }

        public Builder maxDocFreqPct(Integer maxDocFreqPct) {
            this.maxDocFreqPct = maxDocFreqPct;
            return this;
        }

        public Builder maxNumTokensParsed(Integer maxNumTokensParsed) {
            this.maxNumTokensParsed = maxNumTokensParsed;
            return this;
        }

        public Builder maxQueryTerms(Integer maxQueryTerms) {
            this.maxQueryTerms = maxQueryTerms;
            return this;
        }

        public Builder maxWordLen(Integer maxWordLen) {
            this.maxWordLen = maxWordLen;
            return this;
        }

        public Builder minDocFreq(Integer minDocFreq) {
            this.minDocFreq = minDocFreq;
            return this;
        }

        public Builder minTermFreq(Integer minTermFreq) {
            this.minTermFreq = minTermFreq;
            return this;
        }

        public Builder minWordLen(Integer minWordLen) {
            this.minWordLen = minWordLen;
            return this;
        }

        public Builder stopWord(String... stopWords) {
            if (this.stopWords == null)
                this.stopWords = new LinkedHashSet<>();
            if (stopWords != null)
                Collections.addAll(this.stopWords, stopWords);
            return this;
        }

        public Builder fieldnames(String... fieldnames) {
            this.fieldnames = fieldnames;
            return this;
        }

        public MoreLikeThisQuery build() {
            return new MoreLikeThisQuery(this);
        }
    }

}
