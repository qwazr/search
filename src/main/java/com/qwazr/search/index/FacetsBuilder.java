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

import com.qwazr.search.query.AbstractQuery;
import com.qwazr.utils.TimeTracker;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class FacetsBuilder {

	private final QueryContext queryContext;
	private final Query searchQuery;
	private final SortedSetDocValuesFacetCounts counts;
	private final LinkedHashMap<String, FacetDefinition> facetsDef;

	final LinkedHashMap<String, Map<String, Number>> results;

	FacetsBuilder(QueryContext queryContext, LinkedHashMap<String, FacetDefinition> facetsDef, Query searchQuery,
			FacetsCollector facetsCollector, TimeTracker timeTracker)
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException, InterruptedException {

		this.queryContext = queryContext;
		this.searchQuery = searchQuery;
		this.facetsDef = facetsDef;
		this.results = new LinkedHashMap();
		this.counts = queryContext.state == null ?
		              null :
		              new SortedSetDocValuesFacetCounts(queryContext.state, facetsCollector);

		for (Map.Entry<String, FacetDefinition> entry : facetsDef.entrySet()) {
			final String dim = entry.getKey();
			final FacetDefinition facet = entry.getValue();
			final Map<String, Number> result;
			if (facet.queries == null || facet.queries.isEmpty())
				result = buildFacetState(dim, facet);
			else
				result = buildFacetQueries(facet);
			if (result != null)
				results.put(dim, result);
		}
		if (timeTracker != null)
			timeTracker.next("facet_count");
	}

	private Map<String, Number> buildFacetState(String dim, FacetDefinition facet) throws IOException {
		if (queryContext.state == null || queryContext.state.getOrdRange(dim) == null)
			return Collections.emptyMap();
		int top = facet.top == null ? 10 : facet.top;
		final LinkedHashMap<String, Number> facetMap = new LinkedHashMap<>();
		final FacetResult facetResult = counts.getTopChildren(top, dim);
		if (facetResult == null || facetResult.labelValues == null)
			return Collections.emptyMap();
		for (LabelAndValue lv : facetResult.labelValues)
			facetMap.put(lv.label, lv.value);
		return facetMap;
	}

	private Map<String, Number> buildFacetQueries(FacetDefinition facet)
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException, InterruptedException {
		final LinkedHashMap<String, Number> facetMap = new LinkedHashMap<String, Number>();
		for (Map.Entry<String, AbstractQuery> entry : facet.queries.entrySet()) {
			BooleanQuery.Builder builder = new BooleanQuery.Builder();
			builder.add(searchQuery, BooleanClause.Occur.FILTER);
			builder.add(entry.getValue().getQuery(queryContext), BooleanClause.Occur.FILTER);
			facetMap.put(entry.getKey(), queryContext.indexSearcher.count(builder.build()));
		}
		return facetMap;
	}
}
