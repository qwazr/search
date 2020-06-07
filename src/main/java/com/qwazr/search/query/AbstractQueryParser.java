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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.QueryBuilder;

import java.util.Objects;

public abstract class AbstractQueryParser<T extends AbstractQueryParser<T>> extends AbstractQuery<T> {

	@JsonProperty("enable_position_increments")
	final public Boolean enablePositionIncrements;

	@JsonProperty("auto_generate_multi_term_synonyms_phrase_query")
	final public Boolean autoGenerateMultiTermSynonymsPhraseQuery;

	@JsonProperty("enable_graph_queries")
	final public Boolean enableGraphQueries;

	@JsonProperty("query_string")
	final public String queryString;

	@JsonIgnore
	final protected Analyzer analyzer;

	protected AbstractQueryParser(Class<T> queryClass) {
		super(queryClass);
		analyzer = null;
		enablePositionIncrements = null;
		autoGenerateMultiTermSynonymsPhraseQuery = null;
		enableGraphQueries = null;
		queryString = null;
	}

	protected AbstractQueryParser(Class<T> queryClass, AbstractBuilder builder) {
		super(queryClass);
		this.analyzer = builder.analyzer;
		this.enablePositionIncrements = builder.enablePositionIncrements;
		this.autoGenerateMultiTermSynonymsPhraseQuery = builder.autoGenerateMultiTermSynonymsPhraseQuery;
		this.enableGraphQueries = builder.enableGraphQueries;
		this.queryString = builder.queryString;
	}

	protected void setQueryBuilderParameters(final QueryBuilder queryBuilder) {
		if (analyzer != null)
			queryBuilder.setAnalyzer(analyzer);
		if (enablePositionIncrements != null)
			queryBuilder.setEnablePositionIncrements(enablePositionIncrements);
		if (autoGenerateMultiTermSynonymsPhraseQuery != null)
			queryBuilder.setAutoGenerateMultiTermSynonymsPhraseQuery(autoGenerateMultiTermSynonymsPhraseQuery);
		if (enableGraphQueries != null)
			queryBuilder.setEnableGraphQueries(enableGraphQueries);
	}

	@Override
	@JsonIgnore
	protected boolean isEqual(T q) {
		return Objects.equals(enablePositionIncrements, q.enablePositionIncrements) &&
				Objects.equals(autoGenerateMultiTermSynonymsPhraseQuery, q.autoGenerateMultiTermSynonymsPhraseQuery) &&
				Objects.equals(enableGraphQueries, q.enableGraphQueries) &&
				Objects.equals(queryString, q.queryString) && Objects.equals(analyzer, q.analyzer);
	}

	public static abstract class AbstractBuilder<B extends AbstractBuilder, T extends AbstractQueryParser> {

		private final Class<B> builderClass;

		private Analyzer analyzer;
		private Boolean enablePositionIncrements;
		private Boolean autoGenerateMultiTermSynonymsPhraseQuery;
		private Boolean enableGraphQueries;
		private String queryString;

		protected AbstractBuilder(Class<B> builderClass) {
			this.builderClass = builderClass;
		}

		protected B me() {
			return builderClass.cast(this);
		}

		public abstract T build();

		final public B setAnalyzer(Analyzer analyzer) {
			this.analyzer = analyzer;
			return me();
		}

		final public B setEnablePositionIncrements(Boolean enablePositionIncrements) {
			this.enablePositionIncrements = enablePositionIncrements;
			return me();
		}

		final public B setAutoGenerateMultiTermSynonymsPhraseQuery(Boolean autoGenerateMultiTermSynonymsPhraseQuery) {
			this.autoGenerateMultiTermSynonymsPhraseQuery = autoGenerateMultiTermSynonymsPhraseQuery;
			return me();
		}

		final public B setEnableGraphQueries(Boolean enableGraphQueries) {
			this.enableGraphQueries = enableGraphQueries;
			return me();
		}

		final public B setQueryString(String queryString) {
			this.queryString = queryString;
			return me();
		}
	}
}
