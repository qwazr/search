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
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
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
	final public List<ResultDocument> documents;
	final public Map<String, Map<String, Number>> facets;

	public ResultDefinition() {
		this.timer = null;
		this.total_hits = 0;
		this.documents = null;
		this.facets = null;
	}

	public class ResultDocument {

		final private ScoreDoc scoreDoc;
		final public Map<String, Object> fields;
		final public Map<String, String> postings_highlights;

		public ResultDocument() {
			scoreDoc = null;
			fields = null;
			postings_highlights = null;
		}

		private ResultDocument(int pos, ScoreDoc[] scoreDocs, IndexSearcher searcher, QueryDefinition queryDef,
							   Map<String, String[]> postingsHighlightsMap)
				throws IOException {

			scoreDoc = scoreDocs[pos];
			Document document = searcher.doc(scoreDoc.doc, queryDef.returned_fields);

			// Build field map
			fields = new LinkedHashMap<String, Object>();
			for (IndexableField field : document) {
				Object newValue = FieldDefinition.getValue(field);
				if (newValue == null)
					continue;
				Object oldValue = fields.get(field.name());
				if (oldValue == null) {
					fields.put(field.name(), newValue);
					continue;
				}
				if (oldValue instanceof List<?>) {
					((List<Object>) oldValue).add(newValue);
					continue;
				}
				List<Object> list = new ArrayList<Object>(2);
				list.add(oldValue);
				list.add(newValue);
				fields.put(field.name(), list);
			}

			// Build postings hightlights
			if (queryDef.postings_highlighter != null && postingsHighlightsMap != null) {
				postings_highlights = new LinkedHashMap<String, String>();
				for (String field : queryDef.postings_highlighter.keySet()) {
					String[] highlights = postingsHighlightsMap.get(field);
					if (highlights == null)
						continue;
					String highlight = highlights[pos];
					if (highlight == null)
						continue;
					postings_highlights.put(field, highlight);
				}
			} else
				postings_highlights = null;
		}

		public Float getScore() {
			return scoreDoc != null ? scoreDoc.score : null;
		}

		public Integer getDoc() {
			return scoreDoc != null ? scoreDoc.doc : null;
		}

		public Integer getShard_index() {
			return scoreDoc != null ? scoreDoc.shardIndex : null;
		}

		public Map<String, Object> getFields() {
			return fields;
		}

		public Map<String, String> getPostings_highlights() {
			return postings_highlights;
		}
	}

	public ResultDefinition(TimeTracker timeTracker, IndexSearcher searcher, TopDocs topDocs, QueryDefinition queryDef,
							Facets facets, Map<String, String[]> postingsHighlightsMap)
			throws IOException {
		total_hits = topDocs.totalHits;
		int pos = queryDef.start == null ? 0 : queryDef.start;
		int end = queryDef.getEnd();
		documents = new ArrayList<ResultDocument>();
		ScoreDoc[] docs = topDocs.scoreDocs;
		IndexReader reader = searcher.getIndexReader();
		while (pos < total_hits && pos < end)
			documents.add(new ResultDocument(pos++, docs, searcher, queryDef, postingsHighlightsMap));
		timeTracker.next("returned_fields");
		this.facets = facets != null && queryDef != null ? buildFacets(reader, queryDef.facets, facets) : null;
		timeTracker.next("facet_fields");
		this.timer = timeTracker == null ? null : timeTracker.getMap();
	}

	private Map<String, Map<String, Number>> buildFacets(IndexReader reader,
														 Map<String, QueryDefinition.Facet> facetsDef,
														 Facets facets) throws IOException {
		Map<String, Map<String, Number>> facetResults = new LinkedHashMap<String, Map<String, Number>>();
		for (Map.Entry<String, QueryDefinition.Facet> entry : facetsDef.entrySet()) {
			String dim = entry.getKey();
			if (reader.getDocCount(dim) == -1)
				continue;
			Map<String, Number> facetMap = buildFacet(dim, entry.getValue(), facets);
			if (facetMap != null)
				facetResults.put(dim, facetMap);
		}
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

	public List<ResultDocument> getDocuments() {
		return documents;
	}

	public Map<String, Map<String, Number>> getFacets() {
		return facets;
	}

	public Map<String, Long> getTimer() {
		return timer;
	}
}
