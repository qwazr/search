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
 **/
package com.qwazr.search.index;

import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.search.collector.BaseCollector;
import com.qwazr.search.field.SortUtils;
import com.qwazr.search.query.DrillDownQuery;
import com.qwazr.utils.FunctionUtils;
import com.qwazr.utils.TimeTracker;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class QueryExecution {

	final QueryContext queryContext;
	final QueryDefinition queryDef;
	final TimeTracker timeTracker;
	final int numHits;
	final Sort sort;
	final boolean bNeedScore;
	final boolean useDrillSideways;
	final Query query;
	final List<BaseCollector> userCollectors;

	private final boolean isConcurrent;

	QueryExecution(final QueryContext queryContext)
			throws QueryNodeException, ReflectiveOperationException, ParseException, IOException {

		this.timeTracker = new TimeTracker();

		this.queryContext = queryContext;
		this.queryDef = queryContext.queryDefinition;

		this.query = queryDef.query == null ? new MatchAllDocsQuery() : queryDef.query.getQuery(queryContext);

		this.sort = queryDef.sorts == null ? null : SortUtils.buildSort(queryContext.fieldMap, queryDef.sorts);

		this.numHits = queryDef.getEnd();
		this.bNeedScore = sort == null || sort.needsScores();
		this.useDrillSideways =
				queryDef.query instanceof DrillDownQuery && ((DrillDownQuery) queryDef.query).useDrillSideways
						&& queryDef.facets != null;
		if (queryDef.collectors != null && !queryDef.collectors.isEmpty()) {
			userCollectors = new ArrayList<>();
			isConcurrent =
					buildExternalCollectors(queryContext.classLoaderManager, queryDef.collectors, userCollectors);
		} else {
			userCollectors = null;
			isConcurrent = true;
		}
	}

	private static boolean buildExternalCollectors(final ClassLoaderManager classLoaderManager,
			final Map<String, QueryDefinition.CollectorDefinition> collectors,
			final Collection<BaseCollector> userCollectors) throws ReflectiveOperationException {
		if (collectors == null || collectors.isEmpty())
			return true; // By default we use concurrent
		final AtomicInteger concurrentCollectors = new AtomicInteger(0);
		final AtomicInteger classicCollectors = new AtomicInteger(0);
		FunctionUtils.forEach(collectors, (name, collector) -> {
			final Class<? extends Collector> collectorClass = classLoaderManager.findClass(collector.classname);
			Constructor<?>[] constructors = collectorClass.getConstructors();
			if (constructors.length == 0)
				throw new ReflectiveOperationException("No constructor for class: " + collectorClass);
			final BaseCollector baseCollector;
			if (collector.arguments == null || collector.arguments.length == 0)
				baseCollector = (BaseCollector) constructors[0].newInstance(name);
			else {
				final Object[] arguments = new Object[collector.arguments.length + 1];
				arguments[0] = name;
				System.arraycopy(collector.arguments, 0, arguments, 1, collector.arguments.length);
				baseCollector = (BaseCollector) constructors[0].newInstance(arguments);
			}
			userCollectors.add(baseCollector);
		});
		if (concurrentCollectors.get() > 0 && classicCollectors.get() > 0)
			throw new IllegalArgumentException("Cannot mix concurrent collectors and classic collectors");
		return concurrentCollectors.get() > 0 || classicCollectors.get() == 0;
	}

	ResultDefinition execute(final ResultDocumentBuilder.BuilderFactory documentBuilderFactory)
			throws ReflectiveOperationException, IOException, ParseException, QueryNodeException {

		final QueryCollectors queryCollectors =
				isConcurrent ? new QueryCollectorManager(this) : new QueryCollectorsClassic(this);

		final FacetsBuilder facetsBuilder = queryCollectors.execute();

		final TopDocs topDocs = queryCollectors.getTopDocs();
		final Integer totalHits = queryCollectors.getTotalHits();

		final Map<String, HighlighterImpl> highlighters;
		if (queryDef.highlighters != null && topDocs != null)

		{
			highlighters = new LinkedHashMap<>();
			queryDef.highlighters.forEach((name, highlighterDefinition) -> highlighters.put(name,
					new HighlighterImpl(highlighterDefinition,
							queryContext.indexAnalyzer.getWrappedAnalyzer(highlighterDefinition.field))));
		} else
			highlighters = null;

		timeTracker.next("search_query");

		final ResultDefinitionBuilder resultBuilder =
				new ResultDefinitionBuilder(queryDef, topDocs, queryContext.indexSearcher, query, highlighters,
						queryCollectors.getExternalResults(), queryContext.fieldMap, timeTracker,
						documentBuilderFactory, facetsBuilder, totalHits);

		return documentBuilderFactory.build(resultBuilder);
	}

}
