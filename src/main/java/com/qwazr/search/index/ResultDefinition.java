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
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.qwazr.utils.TimeTracker;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
public class ResultDefinition {

	final public Map<String, Long> timer;
	final public long total_hits;
	final public List<Map<String, Object>> documents;
	final public Map<String, Map<String, Number>> facets;

	public ResultDefinition() {
		this.timer = null;
		this.total_hits = 0;
		this.documents = null;
		this.facets = null;
	}

	public final static String FIELD_SCORE = "$score";
	public final static String FIELD_ID = "$id";

	public ResultDefinition(TimeTracker timeTracker, IndexSearcher searcher, TopDocs topDocs, QueryDefinition queryDef,
							Facets facets) throws IOException {
		total_hits = topDocs.totalHits;
		int pos = queryDef.start == null ? 0 : queryDef.start;
		int end = queryDef.getEnd();
		documents = new ArrayList<Map<String, Object>>();
		ScoreDoc[] docs = topDocs.scoreDocs;
		IndexReader reader = searcher.getIndexReader();
		while (pos < total_hits && pos < end) {
			ScoreDoc scoreDoc = docs[pos];
			Document document = searcher.doc(scoreDoc.doc, queryDef.returned_fields);
			Map<String, Object> doc = new LinkedHashMap<String, Object>();
			for (IndexableField field : document) {
				Object value = FieldDefinition.getValue(field);
				if (value != null)
					doc.put(field.name(), value);
			}
			doc.put(FIELD_SCORE, scoreDoc.score);
			documents.add(doc);
			pos++;
		}
		long facet_start_time = System.currentTimeMillis();
		timeTracker.next("returned_field");
		this.facets = facets != null && queryDef != null ? buildFacets(timeTracker, queryDef.facets, facets) : null;
		this.timer = timeTracker == null ? null : timeTracker.getMap();
	}

	private Map<String, Map<String, Number>> buildFacets(TimeTracker timeTracker,
														 Map<String, QueryDefinition.Facet> facetsDef,
														 Facets facets) throws IOException {
		Map<String, Map<String, Number>> facetResults = new LinkedHashMap<String, Map<String, Number>>();
		for (Map.Entry<String, QueryDefinition.Facet> entry : facetsDef.entrySet()) {
			String dim = entry.getKey();
			Map<String, Number> facetMap = buildFacet(dim, entry.getValue(), facets);
			if (facetMap != null)
				facetResults.put(dim, facetMap);
		}
		timeTracker.next("facet_fields");
		return facetResults;
	}

	private static Map<String, Number> buildFacet(String dim, QueryDefinition.Facet facet, Facets facets)
			throws IOException {
		int top = facet.top == null ? 10 : facet.top;
		LinkedHashMap<String, Number> facetMap = new LinkedHashMap<String, Number>();
		FacetResult facetResult = facets.getTopChildren(top, dim);
		if (facetResult == null || facetResult.labelValues == null)
			return null;
		for (LabelAndValue lv : facetResult.labelValues)
			facetMap.put(lv.label, lv.value);
		return facetMap;
	}

	public long getTotal_hits() {
		return total_hits;
	}

	public List<Map<String, Object>> getDocuments() {
		return documents;
	}

	public Map<String, Map<String, Number>> getFacets() {
		return facets;
	}

	public Map<String, Long> getTimer() {
		return timer;
	}
}
