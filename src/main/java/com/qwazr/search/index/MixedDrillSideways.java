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
 */
package com.qwazr.search.index;

import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.MultiFacets;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MixedDrillSideways extends DrillSideways {

	private final String stateIndexField;
	private final FieldMap fieldMap;

	MixedDrillSideways(QueryExecution queryExecution) {
		super(queryExecution.queryContext.indexSearcher, queryExecution.facetsConfig,
				queryExecution.queryContext.taxonomyReader, queryExecution.queryContext.docValueReaderState,
				queryExecution.queryContext.executorService);
		this.fieldMap = queryExecution.queryContext.fieldMap;
		this.stateIndexField = state == null ? null : state.getField();
	}

	protected Facets buildFacetsResult(final FacetsCollector drillDowns, final FacetsCollector[] drillSideways,
			final String[] drillSidewaysDims) throws IOException {

		final Map<String, Facets> drillSidewaysFacets = new HashMap<>();

		final FastTaxonomyFacetCounts fastTaxonomyFacets = taxoReader == null ? null : new FastTaxonomyFacetCounts(
				taxoReader, config, drillDowns);

		final SortedSetDocValuesFacetCounts docValuesFacets = state == null ? null : new SortedSetDocValuesFacetCounts(
				state, drillDowns);

		if (drillSideways != null) {
			final String[] resolvedDims = fieldMap.resolveFieldNames(drillSidewaysDims,
					fieldMap::resolveQueryFieldName);
			for (int i = 0; i < drillSideways.length; i++) {
				final String dim = drillSidewaysDims[i];
				final String resolvedDim = resolvedDims[i];
				final Facets facets;
				final String indexFieldName = config.getDimConfig(resolvedDim).indexFieldName;
				if (state != null && stateIndexField.equals(indexFieldName)) {
					facets = new SortedSetDocValuesFacetCounts(state, drillSideways[i]);
				} else if (taxoReader != null) {
					facets = new FastTaxonomyFacetCounts(taxoReader, config, drillSideways[i]);
				} else
					facets = null;
				if (facets != null)
					drillSidewaysFacets.put(dim, facets);
			}
		}

		final Facets facets = new MixedFacets(docValuesFacets, fastTaxonomyFacets);

		return drillSidewaysFacets.isEmpty() ? facets : new MultiFacets(drillSidewaysFacets, facets);
	}

	class MixedFacets extends Facets {

		private final SortedSetDocValuesFacetCounts docValuesFacets;
		private final FastTaxonomyFacetCounts taxonomyFacets;

		private MixedFacets(final SortedSetDocValuesFacetCounts docValuesFacets,
				final FastTaxonomyFacetCounts taxonomyFacets) {
			this.docValuesFacets = docValuesFacets;
			this.taxonomyFacets = taxonomyFacets;

		}

		private Facets getFacets(String dim) {
			if (stateIndexField != null && stateIndexField.equals(config.getDimConfig(dim).indexFieldName))
				return docValuesFacets;
			return taxonomyFacets;
		}

		@Override
		public FacetResult getTopChildren(int topN, String dim, String... path) throws IOException {
			final Facets facets = getFacets(dim);
			return facets == null ? null : facets.getTopChildren(topN, dim, path);
		}

		@Override
		public Number getSpecificValue(String dim, String... path) throws IOException {
			final Facets facets = getFacets(dim);
			return facets == null ? -1 : facets.getSpecificValue(dim, path);
		}

		@Override
		public List<FacetResult> getAllDims(int topN) throws IOException {
			if (docValuesFacets == null) {
				return taxonomyFacets == null ? null : taxonomyFacets.getAllDims(topN);
			}
			if (taxonomyFacets == null)
				return docValuesFacets.getAllDims(topN);
			final List<FacetResult> facetResultList = taxonomyFacets.getAllDims(topN);
			facetResultList.addAll(docValuesFacets.getAllDims(topN));
			return facetResultList;
		}
	}
}
