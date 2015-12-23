/**
 * Copyright 2015 Emmanuel Keller / QWAZR
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

import com.qwazr.search.analysis.UpdatableAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Objects;

public class CustomScoreQuery extends AbstractQuery {

	public final AbstractQuery subQuery;
	public final FunctionQuery scoringQuery;
	public final FunctionQuery[] scoringQueries;

	public CustomScoreQuery() {
		super(null);
		subQuery = null;
		scoringQuery = null;
		scoringQueries = null;
	}

	@Override
	final protected Query getQuery(UpdatableAnalyzer analyzer, String queryString)
			throws IOException, ParseException, QueryNodeException {
		Objects.requireNonNull(subQuery, "Missing subQuery property");
		final Query query = subQuery.getQuery(analyzer, queryString);
		final org.apache.lucene.queries.CustomScoreQuery customScoreQuery;
		if (scoringQueries != null) {
			customScoreQuery = new org.apache.lucene.queries.CustomScoreQuery(query,
					FunctionQuery.getQueries(scoringQueries, analyzer, queryString));
		} else if (scoringQuery != null) {
			customScoreQuery = new org.apache.lucene.queries.CustomScoreQuery(query,
					scoringQuery.getQuery(analyzer, queryString));
		} else
			customScoreQuery = new org.apache.lucene.queries.CustomScoreQuery(query);
		return customScoreQuery;
	}
}
