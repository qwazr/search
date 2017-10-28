/*
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
 **/

package com.qwazr.search.index;

import com.qwazr.search.collector.BaseCollector;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class QueryCollectorsClassic extends QueryCollectors {

	private final List<Collector> collectors;

	final FacetsCollector facetsCollector;

	final List<BaseCollector> userCollectors;

	final TotalHitCountCollector totalHitCountCollector;

	final TopDocsCollector topDocsCollector;

	final Collector finalCollector;

	QueryCollectorsClassic(final QueryExecution<?> queryExecution) throws IOException, ReflectiveOperationException {
		super(queryExecution);
		collectors = new ArrayList<>();
		facetsCollector = queryExecution.useDrillSideways ? null : buildFacetsCollector(queryExecution.queryDef.facets);
		totalHitCountCollector = buildTotalHitsCollector(queryExecution.end);
		topDocsCollector = buildTopDocCollector(queryExecution.sort, queryExecution.end, queryExecution.bNeedScore);
		if (queryExecution.collectorConstructors != null) {
			userCollectors = new ArrayList<>();
			for (Pair<Constructor, Object[]> item : queryExecution.collectorConstructors)
				userCollectors.add(add((BaseCollector) item.getLeft().newInstance(item.getRight())));
		} else
			userCollectors = null;
		finalCollector = getFinalCollector();
	}

	private <T extends Collector> T add(final T collector) {
		collectors.add(collector);
		return collector;
	}

	private Collector getFinalCollector() {
		switch (collectors.size()) {
		case 0:
			return null;
		case 1:
			return collectors.get(0);
		default:
			return MultiCollector.wrap(collectors);
		}
	}

	private FacetsCollector buildFacetsCollector(final LinkedHashMap<String, FacetDefinition> facets) {
		if (facets == null || facets.isEmpty())
			return null;
		for (FacetDefinition facet : facets.values())
			if (facet.queries == null || facet.queries.isEmpty())
				return add(new FacetsCollector());
		return null;
	}

	private TopDocsCollector buildTopDocCollector(final Sort sort, final int numHits, final boolean bNeedScore)
			throws IOException {
		if (numHits == 0)
			return null;
		final TopDocsCollector topDocsCollector;
		if (sort != null)
			topDocsCollector = TopFieldCollector.create(sort, numHits, true, bNeedScore, bNeedScore);
		else
			topDocsCollector = TopScoreDocCollector.create(numHits);
		return add(topDocsCollector);
	}

	private TotalHitCountCollector buildTotalHitsCollector(final int numHits) {
		if (numHits > 0)
			return null;
		return add(new TotalHitCountCollector());
	}

	@Override
	public final FacetsBuilder execute() throws Exception {

		final FacetsBuilder facetsBuilder;

		if (queryExecution.useDrillSideways) {

			final DrillSideways.DrillSidewaysResult drillSidewaysResult =
					new DrillSideways(queryExecution.queryContext.indexSearcher, queryExecution.facetsConfig,
							queryExecution.queryContext.taxonomyReader, queryExecution.queryContext.docValueReaderState)
							.search((org.apache.lucene.facet.DrillDownQuery) queryExecution.query, finalCollector);
			facetsBuilder = new FacetsBuilder.WithSideways(queryExecution.queryContext, queryExecution.facetsConfig,
					queryExecution.queryDef.facets, queryExecution.query, queryExecution.timeTracker,
					drillSidewaysResult).build();

		} else {

			queryExecution.queryContext.indexSearcher.search(queryExecution.query, finalCollector);
			facetsBuilder = facetsCollector == null ?
					null :
					new FacetsBuilder.WithCollectors(queryExecution.queryContext, queryExecution.facetsConfig,
							queryExecution.queryDef.facets, queryExecution.query, queryExecution.timeTracker,
							facetsCollector).build();

		}

		return facetsBuilder;
	}

	@Override
	public final Integer getTotalHits() {
		if (totalHitCountCollector != null)
			return totalHitCountCollector.getTotalHits();
		if (topDocsCollector != null)
			return topDocsCollector.getTotalHits();
		return 0;
	}

	@Override
	public final TopDocs getTopDocs() {
		return topDocsCollector == null ? null : topDocsCollector.topDocs(queryExecution.start, queryExecution.rows);
	}

	@Override
	public final FacetsCollector getFacetsCollector() {
		return facetsCollector;
	}

	@Override
	public final Map<String, Object> getExternalResults() {
		if (userCollectors == null)
			return null;
		final Map<String, Object> results = new HashMap<>();
		int i = 0;
		for (String name : queryExecution.queryDef.collectors.keySet())
			results.put(name, userCollectors.get(i++).getResult());
		return results;
	}
}
