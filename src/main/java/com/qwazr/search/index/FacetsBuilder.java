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

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.utils.TimeTracker;
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

abstract class FacetsBuilder {

	protected final QueryContext queryContext;
	private final LinkedHashMap<String, FacetDefinition> facetsDef;
	private final Query searchQuery;
	private final TimeTracker timeTracker;

	final LinkedHashMap<String, Map<String, Number>> results = new LinkedHashMap<>();

	private FacetsBuilder(final QueryContext queryContext, final LinkedHashMap<String, FacetDefinition> facetsDef,
			final Query searchQuery, final TimeTracker timeTracker) {

		this.facetsDef = facetsDef;
		this.queryContext = queryContext;
		this.searchQuery = searchQuery;
		this.timeTracker = timeTracker;
	}

	final FacetsBuilder build() throws IOException, ReflectiveOperationException, ParseException, QueryNodeException {
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
		return this;
	}

	private Map<String, Number> buildFacetState(final String dim, final FacetDefinition facet) throws IOException {
		final int top = facet.top == null ? 10 : facet.top;
		final LinkedHashMap<String, Number> facetMap = new LinkedHashMap<>();
		final FacetResult facetResult = getFacetResult(top, dim);
		if (facetResult == null || facetResult.labelValues == null)
			return Collections.emptyMap();
		for (LabelAndValue lv : facetResult.labelValues)
			facetMap.put(lv.label, lv.value);
		return facetMap;
	}

	protected abstract FacetResult getFacetResult(final int top, final String dim) throws IOException;

	private Map<String, Number> buildFacetQueries(final FacetDefinition facet)
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {
		final LinkedHashMap<String, Number> facetMap = new LinkedHashMap<>();
		for (Map.Entry<String, AbstractQuery> entry : facet.queries.entrySet()) {
			final BooleanQuery.Builder builder = new BooleanQuery.Builder();
			builder.add(searchQuery, BooleanClause.Occur.FILTER);
			builder.add(entry.getValue().getQuery(queryContext), BooleanClause.Occur.FILTER);
			facetMap.put(entry.getKey(), queryContext.indexSearcher.count(builder.build()));
		}
		return facetMap;
	}

	static Set<String> getFields(LinkedHashMap<String, FacetDefinition> facets) {
		if (facets == null || facets.isEmpty())
			return null;
		final LinkedHashSet<String> fields = new LinkedHashSet<>();
		facets.forEach((field, facetDefinition) -> {
			if (facetDefinition.queries == null)
				fields.add(field);
		});
		return fields;
	}

	static class WithCollectors extends FacetsBuilder {

		private final SortedSetDocValuesFacetCounts sortedSetCounts;
		private final FastTaxonomyFacetCounts taxonomyCounts;
		private final FacetsConfig facetsConfig;

		WithCollectors(final QueryContext queryContext, final FacetsConfig facetsConfig,
				final LinkedHashMap<String, FacetDefinition> facetsDef, final Query searchQuery,
				final TimeTracker timeTracker, final FacetsCollector facetsCollector)
				throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {
			super(queryContext, facetsDef, searchQuery, timeTracker);
			this.facetsConfig = facetsConfig;
			this.sortedSetCounts = queryContext.docValueReaderState == null ?
					null :
					new SortedSetDocValuesFacetCounts(queryContext.docValueReaderState, facetsCollector);
			this.taxonomyCounts =
					new FastTaxonomyFacetCounts(queryContext.taxonomyReader, facetsConfig, facetsCollector);
		}

		@Override
		final protected FacetResult getFacetResult(final int top, final String dim) throws IOException {
			if (FieldDefinition.SORTEDSET_FACET_FIELD.equals(facetsConfig.getDimConfig(dim).indexFieldName)) {
				if (queryContext.docValueReaderState != null)
					if (queryContext.docValueReaderState.getOrdRange(dim) != null)
						return sortedSetCounts.getTopChildren(top, dim);
			} else
				return taxonomyCounts.getTopChildren(top, dim);
			return null;
		}
	}

	static class WithSideways extends FacetsBuilder {

		final DrillSideways.DrillSidewaysResult results;
		private final FacetsConfig facetsConfig;

		WithSideways(final QueryContext queryContext, final FacetsConfig facetsConfig,
				final LinkedHashMap<String, FacetDefinition> facetsDef, final Query searchQuery,
				final TimeTracker timeTracker, final DrillSideways.DrillSidewaysResult results)
				throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {
			super(queryContext, facetsDef, searchQuery, timeTracker);
			this.facetsConfig = facetsConfig;
			this.results = results;
		}

		@Override
		final protected FacetResult getFacetResult(final int top, final String dim) throws IOException {
			if (FieldDefinition.SORTEDSET_FACET_FIELD.equals(facetsConfig.getDimConfig(dim).indexFieldName)) {
				if (queryContext.docValueReaderState == null)
					return null;
				if (queryContext.docValueReaderState.getOrdRange(dim) == null)
					return null;
			}
			return results.facets.getTopChildren(top, dim);
		}
	}
}
