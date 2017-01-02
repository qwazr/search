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

import com.qwazr.search.collector.BaseCollector;
import com.qwazr.search.collector.ConcurrentCollector;
import com.qwazr.search.query.DrillDownQuery;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsCollectorManager;
import org.apache.lucene.facet.ParallelDrillSideways;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopFieldDocs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class QueryCollectorManager extends QueryCollectors implements CollectorManager<Collector, QueryCollectors> {

	private final Collection<QueryCollectorsClassic> queryCollectorsList;
	private volatile FacetsCollector facetsCollector;

	QueryCollectorManager(final QueryExecution queryExecution) {
		super(queryExecution);
		this.queryCollectorsList = new ArrayList<>();
	}

	@Override
	final public FacetsBuilder execute()
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {

		final FacetsBuilder facetsBuilder;

		if (queryExecution.useDrillSideways) {

			final Set<String> facetKeys = queryExecution.queryDef.facets.keySet();
			final ParallelDrillSideways.Result<QueryCollectors> drillSidewaysResult =
					new ParallelDrillSideways(queryExecution.queryContext.executorService,
							queryExecution.queryContext.indexSearcher,
							queryExecution.queryContext.fieldMap.getNewFacetsConfig(facetKeys),
							queryExecution.queryContext.state).search(
							(org.apache.lucene.facet.DrillDownQuery) queryExecution.query, facetKeys,
							getDimPathPairs((DrillDownQuery) queryExecution.queryDef.query), this);
			facetsBuilder = new FacetsBuilder.WithSideways(queryExecution.queryContext, queryExecution.queryDef.facets,
					queryExecution.query, queryExecution.timeTracker, drillSidewaysResult).build();

		} else {

			queryExecution.queryContext.indexSearcher.search(queryExecution.query, this);
			facetsCollector = getFacetsCollector();
			facetsBuilder = facetsCollector == null ?
					null :
					new FacetsBuilder.WithCollectors(queryExecution.queryContext, queryExecution.queryDef.facets,
							queryExecution.query, queryExecution.timeTracker, facetsCollector).build();
		}

		return facetsBuilder;
	}

	@Override
	final public Collector newCollector() throws IOException {
		final QueryCollectorsClassic queryCollectors;
		try {
			queryCollectors = new QueryCollectorsClassic(queryExecution);
		} catch (ReflectiveOperationException e) {
			throw new IOException(e);
		}
		queryCollectorsList.add(queryCollectors);
		return queryCollectors.finalCollector;
	}

	@Override
	final public QueryCollectors reduce(final Collection collectors) throws IOException {
		return this;
	}

	@Override
	public final Integer getTotalHits() {
		if (queryCollectorsList == null || queryCollectorsList.isEmpty())
			return 0;
		int totalHits = 0;
		for (QueryCollectorsClassic queryCollectors : queryCollectorsList) {
			if (queryCollectors.totalHitCountCollector != null)
				totalHits += queryCollectors.totalHitCountCollector.getTotalHits();
			else if (queryCollectors.topDocsCollector != null)
				totalHits += queryCollectors.topDocsCollector.getTotalHits();
		}
		return totalHits;
	}

	@Override
	public final TopDocs getTopDocs() throws IOException {
		if (queryCollectorsList == null || queryCollectorsList.isEmpty())
			return null;
		if (queryExecution.sort != null)
			return getTopFieldDocs();
		final List<TopDocs> topDocsList = new ArrayList<>(queryCollectorsList.size());
		for (QueryCollectorsClassic queryCollectors : queryCollectorsList)
			if (queryCollectors.topDocsCollector != null)
				topDocsList.add(queryCollectors.topDocsCollector.topDocs());
		return TopDocs.merge(queryExecution.numHits, topDocsList.toArray(new TopDocs[topDocsList.size()]));
	}

	private TopDocs getTopFieldDocs() throws IOException {
		final List<TopFieldDocs> topFieldDocsList = new ArrayList<>(queryCollectorsList.size());
		for (QueryCollectorsClassic queryCollectors : queryCollectorsList)
			if (queryCollectors.topDocsCollector != null)
				topFieldDocsList.add(((TopFieldCollector) queryCollectors.topDocsCollector).topDocs());
		return TopFieldDocs.merge(queryExecution.sort, queryExecution.numHits,
				topFieldDocsList.toArray(new TopFieldDocs[topFieldDocsList.size()]));
	}

	@Override
	public final FacetsCollector getFacetsCollector() throws IOException {
		if (facetsCollector != null) // cache
			return facetsCollector;
		if (queryExecution.queryDef.facets == null || queryExecution.queryDef.facets.isEmpty())
			return null;
		if (queryCollectorsList == null || queryCollectorsList.isEmpty())
			return FacetsCollectorManager.EMPTY;
		final FacetsCollectorManager manager = new FacetsCollectorManager();
		final List<FacetsCollector> facetCollectors = new ArrayList<>();
		for (QueryCollectorsClassic queryCollectors : queryCollectorsList)
			if (queryCollectors.facetsCollector != null)
				facetCollectors.add(queryCollectors.facetsCollector);
		facetsCollector = manager.reduce(facetCollectors);
		return facetsCollector;
	}

	@Override
	public final Map<String, Object> getExternalResults() {
		if (queryCollectorsList == null || queryCollectorsList.isEmpty())
			return null;
		if (queryExecution.queryDef.collectors == null)
			return null;
		final Map<String, Object> results = new HashMap<>();
		int i = 0;
		for (String name : queryExecution.queryDef.collectors.keySet()) {
			final List<BaseCollector> externalCollectors = new ArrayList<>();
			for (QueryCollectorsClassic queryCollectors : queryCollectorsList)
				if (queryCollectors.userCollectors != null)
					externalCollectors.add(queryCollectors.userCollectors.get(i));
			if (!externalCollectors.isEmpty()) {
				results.put(name,
						((ConcurrentCollector) externalCollectors.get(0)).getReducedResult(externalCollectors));
			}
			i++;
		}
		return results;
	}
}
