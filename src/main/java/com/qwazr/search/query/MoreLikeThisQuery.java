/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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

import com.qwazr.search.index.QueryContext;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class MoreLikeThisQuery extends AbstractQuery {

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

	public MoreLikeThisQuery() {
		super(null);
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

	public MoreLikeThisQuery(Integer doc_num, Boolean is_boost, Float boost_factor, String[] fieldnames,
					Integer max_doc_freq, Integer max_doc_freq_pct, Integer max_num_tokens_parsed,
					Integer max_query_terms, Integer max_word_len, Integer min_doc_freq, Integer min_term_freq,
					Integer min_word_len, Set<String> stop_words) {
		super(null);
		this.doc_num = doc_num;
		this.is_boost = is_boost;
		this.boost_factor = boost_factor;
		this.fieldnames = fieldnames;
		this.max_doc_freq = max_doc_freq;
		this.max_doc_freq_pct = max_doc_freq_pct;
		this.max_num_tokens_parsed = max_num_tokens_parsed;
		this.max_query_terms = max_query_terms;
		this.max_word_len = max_word_len;
		this.min_doc_freq = min_doc_freq;
		this.min_term_freq = min_term_freq;
		this.min_word_len = min_word_len;
		this.stop_words = stop_words;
	}

	@Override
	final protected Query getQuery(QueryContext queryContext) throws IOException, ParseException {
		Objects.requireNonNull(doc_num, "The doc_num field is missing");
		final MoreLikeThis mlt = new MoreLikeThis(queryContext.indexSearcher.getIndexReader());
		if (is_boost != null)
			mlt.setBoost(is_boost);
		if (boost_factor != null)
			mlt.setBoostFactor(boost_factor);
		if (fieldnames != null)
			mlt.setFieldNames(fieldnames);
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
		mlt.setAnalyzer(queryContext.analyzer);
		return mlt.like(doc_num);
	}
}
