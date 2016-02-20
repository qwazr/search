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
import com.qwazr.utils.TimeTracker;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.SortedSetDocValues;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class FacetsBuilder {

	private final SortedSetDocValuesReaderState state;
	private final LinkedHashMap<String, QueryDefinition.Facet> facetsDef;
	private final Facets facets;

	final LinkedHashMap<String, Map<String, Number>> results;

	FacetsBuilder(IndexReader indexReader, LinkedHashMap<String, QueryDefinition.Facet> facetsDef,
			FacetsCollector facetsCollector, TimeTracker timeTracker) throws IOException {

		this.facetsDef = facetsDef;
		this.results = new LinkedHashMap();
		this.state = getState(indexReader);

		if (state != null) {
			facets = new SortedSetDocValuesFacetCounts(state, facetsCollector);
			buildFacets();
		} else {
			facets = null;
			facetsDef.forEach((facetName, facet) -> results.put(facetName, Collections.emptyMap()));
		}

		if (timeTracker != null)
			timeTracker.next("facet_count");
	}

	private static SortedSetDocValuesReaderState getState(IndexReader indexReader) throws IOException {
		LeafReader topReader = SlowCompositeReaderWrapper.wrap(indexReader);
		if (topReader == null)
			return null;
		SortedSetDocValues dv = topReader.getSortedSetDocValues(FieldDefinition.FACET_FIELD);
		if (dv == null)
			return null;
		return new DefaultSortedSetDocValuesReaderState(indexReader, FieldDefinition.FACET_FIELD);
	}

	private void buildFacets() throws IOException {
		for (Map.Entry<String, QueryDefinition.Facet> entry : facetsDef.entrySet()) {
			String dim = entry.getKey();
			if (state.getOrdRange(dim) == null)
				continue;
			Map<String, Number> facetMap = buildFacet(dim, entry.getValue());
			if (facetMap != null)
				results.put(dim, facetMap);
		}
	}

	private Map<String, Number> buildFacet(String dim, QueryDefinition.Facet facet) throws IOException {
		int top = facet.top == null ? 10 : facet.top;
		LinkedHashMap<String, Number> facetMap = new LinkedHashMap<String, Number>();
		FacetResult facetResult = facets.getTopChildren(top, dim);
		if (facetResult == null || facetResult.labelValues == null)
			return null;
		for (LabelAndValue lv : facetResult.labelValues)
			facetMap.put(lv.label, lv.value);
		return facetMap;
	}
}
