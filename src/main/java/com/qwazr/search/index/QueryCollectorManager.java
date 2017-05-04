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
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.FacetsCollector;
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

class QueryCollectorManager extends QueryCollectors implements CollectorManager<Collector, QueryCollectors> {

	private final Collection<QueryCollectorsClassic> queryCollectorsList;
	private FacetsCollector facetsCollector;

	QueryCollectorManager(final QueryExecution queryExecution) {
		super(queryExecution);
		this.queryCollectorsList = new ArrayList<>();
	}

	@Override
	final public FacetsBuilder execute()
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {

		final FacetsBuilder facetsBuilder;

		if (queryExecution.useDrillSideways) {

			final DrillSideways.ConcurrentDrillSidewaysResult<QueryCollectors> drillSidewaysResult =
					new MixedDrillSideways(queryExecution.queryContext.indexSearcher, queryExecution.facetsConfig,
							queryExecution.queryContext.taxonomyReader, queryExecution.queryContext.docValueReaderState,
							queryExecution.queryContext.executorService).search(
							(org.apache.lucene.facet.DrillDownQuery) queryExecution.query, this);
			facetsBuilder = new FacetsBuilder.WithSideways(queryExecution.queryContext, queryExecution.facetsConfig,
					queryExecution.queryDef.facets, queryExecution.query, queryExecution.timeTracker,
					drillSidewaysResult).build();

		} else {

			queryExecution.queryContext.indexSearcher.search(queryExecution.query, this);
			facetsCollector = getFacetsCollector();
			facetsBuilder = facetsCollector == null ?
					null :
					new FacetsBuilder.WithCollectors(queryExecution.queryContext, queryExecution.facetsConfig,
							queryExecution.queryDef.facets, queryExecution.query, queryExecution.timeTracker,
							facetsCollector).build();
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
		return TopDocs.merge(queryExecution.start, queryExecution.rows,
				topDocsList.toArray(new TopDocs[topDocsList.size()]), true);
	}

	private TopDocs getTopFieldDocs() throws IOException {
		final List<TopFieldDocs> topFieldDocsList = new ArrayList<>(queryCollectorsList.size());
		for (QueryCollectorsClassic queryCollectors : queryCollectorsList)
			if (queryCollectors.topDocsCollector != null)
				topFieldDocsList.add(((TopFieldCollector) queryCollectors.topDocsCollector).topDocs());
		return TopFieldDocs.merge(queryExecution.sort, queryExecution.start, queryExecution.rows,
				topFieldDocsList.toArray(new TopFieldDocs[topFieldDocsList.size()]), true);
	}

	private final static FacetsCollector EMPTY_FACETS_COLLECTOR = new FacetsCollector();

	@Override
	public final FacetsCollector getFacetsCollector() throws IOException {
		if (facetsCollector != null) // cache
			return facetsCollector;
		if (queryExecution.queryDef.facets == null || queryExecution.queryDef.facets.isEmpty())
			return null;
		if (queryCollectorsList == null || queryCollectorsList.isEmpty())
			return EMPTY_FACETS_COLLECTOR;
		facetsCollector = new FacetsCollector();
		final List<FacetsCollector.MatchingDocs> matchingDocs = facetsCollector.getMatchingDocs();
		for (QueryCollectorsClassic queryCollectors : queryCollectorsList)
			if (queryCollectors.facetsCollector != null)
				matchingDocs.addAll(queryCollectors.facetsCollector.getMatchingDocs());
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
