/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Objects;

public class NGramPhraseQuery extends AbstractQuery {

	@JsonProperty("phrase_query")
	final public PhraseQuery phraseQuery;

	@JsonProperty("ngram_size")
	final public Integer nGramSize;

	@JsonCreator
	public NGramPhraseQuery(@JsonProperty("phrase_query") final PhraseQuery phraseQuery,
			@JsonProperty("ngram_size") final Integer ngramSize) {
		this.phraseQuery = phraseQuery;
		this.nGramSize = ngramSize;
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) throws IOException {
		Objects.requireNonNull(phraseQuery, "The phrase_query should not be null");
		Objects.requireNonNull(phraseQuery, "The ngram_size should not be null");
		return new org.apache.lucene.search.NGramPhraseQuery(nGramSize, phraseQuery.getQuery(queryContext));
	}
}
