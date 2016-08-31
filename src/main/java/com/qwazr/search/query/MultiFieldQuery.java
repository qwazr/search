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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.analysis.CustomAnalyzer;
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.UnicodeWhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.*;

public class MultiFieldQuery extends AbstractQuery {

	@JsonProperty("fields_boosts")
	final public Map<String, Float> fieldsBoosts;

	@JsonProperty("default_operator")
	final public QueryParserOperator defaultOperator;

	@JsonProperty("query_string")
	final public String queryString;

	@JsonProperty("tokenizer")
	final public LinkedHashMap<String, String> tokenizerDefinition;

	public MultiFieldQuery() {
		fieldsBoosts = null;
		defaultOperator = null;
		tokenizerDefinition = null;
		queryString = null;
	}

	public MultiFieldQuery(final Map<String, Float> fieldsBoosts, final QueryParserOperator defaultOperator,
			final LinkedHashMap<String, String> tokenizerDefinition, final String queryString) {
		this.fieldsBoosts = fieldsBoosts;
		this.defaultOperator = defaultOperator;
		this.tokenizerDefinition = tokenizerDefinition;
		this.queryString = queryString;
	}

	final static Analyzer DEFAULT_TOKEN_ANALYZER = new UnicodeWhitespaceAnalyzer();

	@Override
	final public Query getQuery(final QueryContext queryContext) throws IOException, ReflectiveOperationException {
		Objects.requireNonNull(fieldsBoosts, "Fields boosts is missing");

		final String queryString = StringUtils.isEmpty(this.queryString) ? queryContext.queryString : this.queryString;
		if (StringUtils.isEmpty(queryString))
			return new org.apache.lucene.search.MatchNoDocsQuery();

		// Build the analyzer used to tokenize the query string
		final Analyzer tokenAnalyzer = tokenizerDefinition != null ?
				new CustomAnalyzer(new AnalyzerDefinition(tokenizerDefinition, null)) : DEFAULT_TOKEN_ANALYZER;

		// Determine the top level occur operator
		final BooleanClause.Occur topLevelOccur =
				defaultOperator == null || defaultOperator.queryParseroperator == QueryParser.Operator.AND ?
						BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD;

		final org.apache.lucene.search.BooleanQuery.Builder topLevelQuery =
				new org.apache.lucene.search.BooleanQuery.Builder();

		// Iterate over terms using the given tokenizer
		try (final TokenStream stream = tokenAnalyzer.tokenStream(StringUtils.EMPTY, queryString)) {
			final CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);
			stream.reset();
			while (stream.incrementToken()) {
				// For each term we build a top level boolean clause
				final List<Query> termQueries = new ArrayList<>();
				final String term = charTermAttribute.toString();
				fieldsBoosts.forEach((field, boost) -> {
					try (final TokenStream tokenStream = queryContext.queryAnalyzer.tokenStream(field, term)) {
						tokenStream.reset();
						addTermQueries(field, tokenStream, termQueries);
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				});
				final int size = termQueries.size();
				switch (size) {
					case 0:
						break;
					case 1:
						topLevelQuery.add(termQueries.get(0), topLevelOccur);
						break;
					default:
						final org.apache.lucene.search.BooleanQuery.Builder bb =
								new org.apache.lucene.search.BooleanQuery.Builder();
						termQueries.forEach(query -> bb.add(query, BooleanClause.Occur.SHOULD));
						topLevelQuery.add(bb.build(), topLevelOccur);
						break;
				}
			}
		}
		return topLevelQuery.build();
	}

	protected void addTermQuery(final String field, final String term, final Collection<Query> queries) {
		queries.add(new org.apache.lucene.search.TermQuery(new Term(field, BytesRefUtils.fromAny(term))));
	}

	private void addTermQueries(final String field, final TokenStream tokenStream, final Collection<Query> queries)
			throws IOException {
		final CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		while (tokenStream.incrementToken())
			addTermQuery(field, charTermAttribute.toString(), queries);
	}
}
