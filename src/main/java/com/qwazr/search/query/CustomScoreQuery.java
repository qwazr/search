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

import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.FileClassCompilerLoader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Objects;

public class CustomScoreQuery extends AbstractQuery {

	public final AbstractQuery subQuery;
	public final FunctionQuery scoringQuery;
	public final FunctionQuery[] scoringQueries;
	public final String customScoreProvider;

	public CustomScoreQuery() {
		super(null);
		subQuery = null;
		scoringQuery = null;
		scoringQueries = null;
		customScoreProvider = null;
	}

	@Override
	final protected Query getQuery(QueryContext queryContext) throws IOException, ParseException, QueryNodeException {
		Objects.requireNonNull(subQuery, "Missing subQuery property");
		final Query query = subQuery.getQuery(queryContext);
		final org.apache.lucene.queries.CustomScoreQuery customScoreQuery;

		if (scoringQueries != null) {
			customScoreQuery = new org.apache.lucene.queries.CustomScoreQuery(query,
							FunctionQuery.getQueries(scoringQueries, queryContext));
		} else if (scoringQuery != null) {
			customScoreQuery = new org.apache.lucene.queries.CustomScoreQuery(query,
							scoringQuery.getQuery(queryContext));
		} else
			customScoreQuery = new org.apache.lucene.queries.CustomScoreQuery(query);
		return customScoreQuery;
	}

	private final org.apache.lucene.queries.CustomScoreQuery buildCustomScoreQuery(Query query,
					QueryContext queryContext) throws ParseException, IOException, QueryNodeException {
		if (scoringQueries != null)
			return new org.apache.lucene.queries.CustomScoreQuery(query,
							FunctionQuery.getQueries(scoringQueries, queryContext));
		else if (scoringQuery != null)
			return new org.apache.lucene.queries.CustomScoreQuery(query, scoringQuery.getQuery(queryContext));
		else
			return new org.apache.lucene.queries.CustomScoreQuery(query);
	}

	private final org.apache.lucene.queries.CustomScoreQuery buildCustomScoreQueryWithProvider(Query query,
					QueryContext queryContext)
					throws ParseException, IOException, QueryNodeException, ClassNotFoundException,
					ReflectiveOperationException {

		Class<? extends CustomScoreProvider> customScoreProviderClass = FileClassCompilerLoader
						.findClass(queryContext.compilerLoader, customScoreProvider, null);
		Objects.requireNonNull(customScoreProviderClass, "Cannot find the class for " + customScoreProvider);
		Constructor<? extends CustomScoreProvider> customScoreProviderConstructor = customScoreProviderClass
						.getConstructor(LeafReaderContext.class);

		if (scoringQueries != null)
			return new CustomScoreQueryWithProvider(customScoreProviderConstructor, query,
							FunctionQuery.getQueries(scoringQueries, queryContext));
		else if (scoringQuery != null)
			return new CustomScoreQueryWithProvider(customScoreProviderConstructor, query,
							scoringQuery.getQuery(queryContext));
		else
			return new CustomScoreQueryWithProvider(customScoreProviderConstructor, query);
	}

	private static class CustomScoreQueryWithProvider extends org.apache.lucene.queries.CustomScoreQuery {

		private final Constructor<? extends CustomScoreProvider> customScoreProviderConstructor;

		private CustomScoreQueryWithProvider(Constructor<? extends CustomScoreProvider> customScoreProviderConstructor,
						Query subQuery) throws NoSuchMethodException {
			super(subQuery);
			this.customScoreProviderConstructor = customScoreProviderConstructor;
		}

		private CustomScoreQueryWithProvider(Constructor<? extends CustomScoreProvider> customScoreProviderConstructor,
						Query subQuery, org.apache.lucene.queries.function.FunctionQuery scoringQuery)
						throws NoSuchMethodException {
			super(subQuery, scoringQuery);
			this.customScoreProviderConstructor = customScoreProviderConstructor;
		}

		private CustomScoreQueryWithProvider(Constructor<? extends CustomScoreProvider> customScoreProviderConstructor,
						Query subQuery, org.apache.lucene.queries.function.FunctionQuery[] scoringQueries)
						throws NoSuchMethodException {
			super(subQuery, scoringQueries);
			this.customScoreProviderConstructor = customScoreProviderConstructor;
		}

		protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context) throws IOException {
			try {
				return customScoreProviderConstructor.newInstance(context);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
