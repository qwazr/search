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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.ClassLoaderUtils;
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
	@JsonProperty("customScoreProvider")
	public final String customScoreProviderClassName;
	@JsonIgnore
	public final Class<? extends CustomScoreProvider> customScoreProviderClass;

	public CustomScoreQuery() {
		subQuery = null;
		scoringQuery = null;
		scoringQueries = null;
		customScoreProviderClassName = null;
		customScoreProviderClass = null;
	}

	public CustomScoreQuery(AbstractQuery subQuery, FunctionQuery... scoringQueries) {
		this(subQuery, null, null, scoringQueries);
	}

	private CustomScoreQuery(AbstractQuery subQuery, Class<? extends CustomScoreProvider> customScoreProviderClass,
			String customScoreProviderClassName, FunctionQuery... scoringQueries) {
		this.subQuery = subQuery;
		if (scoringQueries == null || scoringQueries.length == 0) {
			this.scoringQuery = null;
			this.scoringQueries = null;
		} else {
			if (scoringQueries.length == 1) {
				this.scoringQuery = scoringQueries[0];
				this.scoringQueries = null;
			} else {
				this.scoringQuery = null;
				this.scoringQueries = scoringQueries;
			}
		}
		this.customScoreProviderClassName = customScoreProviderClassName;
		this.customScoreProviderClass = customScoreProviderClass;
	}

	public CustomScoreQuery(AbstractQuery subQuery, String customScoreProviderClass, FunctionQuery... scoringQueries) {
		this(subQuery, null, customScoreProviderClass, scoringQueries);
	}

	public CustomScoreQuery(final AbstractQuery subQuery,
			final Class<? extends CustomScoreProvider> customScoreProviderClass,
			final FunctionQuery... scoringQueries) {
		this(subQuery, customScoreProviderClass, null, scoringQueries);
	}

	@Override
	final public Query getQuery(final QueryContext queryContext)
			throws IOException, ParseException, QueryNodeException, ReflectiveOperationException, InterruptedException {
		Objects.requireNonNull(subQuery, "Missing subQuery property");
		final Query query = subQuery.getQuery(queryContext);
		final org.apache.lucene.queries.CustomScoreQuery customScoreQuery;

		if (customScoreProviderClass != null)
			customScoreQuery = buildCustomScoreQueryProvider(query, queryContext, customScoreProviderClass);
		else if (customScoreProviderClassName != null)
			customScoreQuery = buildCustomScoreQueryProvider(query, queryContext, getProviderClass());
		else
			customScoreQuery = buildCustomScoreQuery(query, queryContext);
		return customScoreQuery;
	}

	private final org.apache.lucene.queries.CustomScoreQuery buildCustomScoreQuery(Query query,
			QueryContext queryContext)
			throws ParseException, IOException, QueryNodeException, ReflectiveOperationException, InterruptedException {
		if (scoringQueries != null)
			return new org.apache.lucene.queries.CustomScoreQuery(query,
					FunctionQuery.getQueries(scoringQueries, queryContext));
		else if (scoringQuery != null)
			return new org.apache.lucene.queries.CustomScoreQuery(query, scoringQuery.getQuery(queryContext));
		else
			return new org.apache.lucene.queries.CustomScoreQuery(query);
	}

	private final Class<? extends CustomScoreProvider> getProviderClass()
			throws ParseException, IOException, QueryNodeException, ReflectiveOperationException {
		Class<? extends CustomScoreProvider> customScoreProviderClass = ClassLoaderUtils
				.findClass(ClassLoaderManager.classLoader, customScoreProviderClassName, null);
		Objects.requireNonNull(customScoreProviderClass, "Cannot find the class for " + customScoreProviderClassName);
		return customScoreProviderClass;
	}

	private final org.apache.lucene.queries.CustomScoreQuery buildCustomScoreQueryProvider(final Query query,
			final QueryContext queryContext, final Class<? extends CustomScoreProvider> customScoreProviderClass)
			throws ParseException, IOException, QueryNodeException, ReflectiveOperationException, InterruptedException {

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

	private final org.apache.lucene.queries.CustomScoreQuery buildCustomScoreQueryWithProviderInstance(
			final Query query, final QueryContext queryContext)
			throws ParseException, IOException, QueryNodeException, ReflectiveOperationException, InterruptedException {

		if (scoringQueries != null)
			return new org.apache.lucene.queries.CustomScoreQuery(query,
					FunctionQuery.getQueries(scoringQueries, queryContext));
		else if (scoringQuery != null)
			return new org.apache.lucene.queries.CustomScoreQuery(query, scoringQuery.getQuery(queryContext));
		else
			return new org.apache.lucene.queries.CustomScoreQuery(query);
	}

	private static class CustomScoreQueryWithProvider extends org.apache.lucene.queries.CustomScoreQuery {

		private final Constructor<? extends CustomScoreProvider> customScoreProviderConstructor;

		private CustomScoreQueryWithProvider(
				final  Constructor<? extends CustomScoreProvider> customScoreProviderConstructor,
				final  Query subQuery) throws NoSuchMethodException {
			super(subQuery);
			this.customScoreProviderConstructor = customScoreProviderConstructor;
		}

		private CustomScoreQueryWithProvider(
				final Constructor<? extends CustomScoreProvider> customScoreProviderConstructor,
				final Query subQuery, final org.apache.lucene.queries.function.FunctionQuery scoringQuery)
				throws NoSuchMethodException {
			super(subQuery, scoringQuery);
			this.customScoreProviderConstructor = customScoreProviderConstructor;
		}

		private CustomScoreQueryWithProvider(
				final Constructor<? extends CustomScoreProvider> customScoreProviderConstructor,
				final Query subQuery, final org.apache.lucene.queries.function.FunctionQuery[] scoringQueries)
				throws NoSuchMethodException {
			super(subQuery, scoringQueries);
			this.customScoreProviderConstructor = customScoreProviderConstructor;
		}

		protected CustomScoreProvider getCustomScoreProvider(final LeafReaderContext context) throws IOException {
			try {
				return customScoreProviderConstructor.newInstance(context);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
