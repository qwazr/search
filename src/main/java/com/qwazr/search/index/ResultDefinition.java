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
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.TimeTracker;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.*;

@JsonInclude(Include.NON_EMPTY) public class ResultDefinition {

	final public Map<String, Long> timer;
	final public long total_hits;
	final public Float max_score;
	final public List<ResultDocument> documents;
	final public Map<String, Map<String, Number>> facets;
	final public String query;

	public ResultDefinition() {
		this.timer = null;
		this.total_hits = 0;
		this.documents = null;
		this.facets = null;
		this.max_score = null;
		this.query = null;
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

		private ResultDocument(int pos, ScoreDoc scoreDoc, Document document, Map<String, Integer> postings_highlighter,
						Map<String, String[]> postingsHighlightsMap, Map<String, Object> docValuesReturnedFields)
						throws IOException {
			this.scoreDoc = scoreDoc;
			// Build field map
			fields = buildFields(document);
			addDocValues(scoreDoc.doc, docValuesReturnedFields, fields);

			// Build postings hightlights
			if (postings_highlighter != null && postingsHighlightsMap != null) {
				postings_highlights = new LinkedHashMap<String, String>();
				for (String field : postings_highlighter.keySet()) {
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

	private static String getQuery(Boolean queryDebug, String defaultField, Query query) {
		if (queryDebug == null || query == null)
			return null;
		if (!queryDebug && query != null)
			return null;
		return query.toString(defaultField == null ? StringUtils.EMPTY : defaultField);
	}

	ResultDefinition(TimeTracker timeTracker, IndexSearcher searcher, TopDocs topDocs, QueryDefinition queryDef,
					Facets facets, Map<String, String[]> postingsHighlightsMap, Query query) throws IOException {
		this.query = getQuery(queryDef.query_debug, queryDef.default_field, query);
		total_hits = topDocs.totalHits;
		max_score = topDocs.getMaxScore();
		int pos = queryDef.start == null ? 0 : queryDef.start;
		int end = queryDef.getEnd();
		documents = new ArrayList<ResultDocument>();
		ScoreDoc[] docs = topDocs.scoreDocs;
		Map<String, Object> docValuesSources = extractDocValuesFields(searcher.getIndexReader(),
						queryDef.returned_fields);
		while (pos < total_hits && pos < end) {
			final ScoreDoc scoreDoc = docs[pos];
			final Document document = searcher.doc(scoreDoc.doc, queryDef.returned_fields);
			documents.add(new ResultDocument(pos, scoreDoc, document, queryDef.postings_highlighter,
							postingsHighlightsMap, docValuesSources));
			pos++;
		}
		timeTracker.next("returned_fields");
		this.facets = facets != null && queryDef != null ? buildFacets(queryDef.facets, facets) : null;
		timeTracker.next("facet_fields");
		this.timer = timeTracker == null ? null : timeTracker.getMap();
	}

	ResultDefinition(TimeTracker timeTracker, IndexSearcher searcher, TopDocs topDocs, MltQueryDefinition mltQueryDef,
					Query query) throws IOException {
		this.query = getQuery(mltQueryDef.query_debug, null, query);
		total_hits = topDocs.totalHits;
		max_score = topDocs.getMaxScore();
		int pos = mltQueryDef.start == null ? 0 : mltQueryDef.start;
		int end = mltQueryDef.getEnd();
		documents = new ArrayList<ResultDocument>();
		ScoreDoc[] docs = topDocs.scoreDocs;
		Map<String, Object> docValuesSources = extractDocValuesFields(searcher.getIndexReader(),
						mltQueryDef.returned_fields);
		while (pos < total_hits && pos < end) {
			final ScoreDoc scoreDoc = docs[pos];
			final Document document = searcher.doc(scoreDoc.doc, mltQueryDef.returned_fields);
			documents.add(new ResultDocument(pos, scoreDoc, document, null, null, docValuesSources));
			pos++;
		}
		timeTracker.next("returned_fields");
		this.facets = null;
		this.timer = timeTracker == null ? null : timeTracker.getMap();
	}

	ResultDefinition(TimeTracker timeTracker) {
		query = null;
		total_hits = 0;
		documents = Collections.emptyList();
		facets = null;
		max_score = null;
		this.timer = timeTracker == null ? null : timeTracker.getMap();
	}

	private Map<String, Map<String, Number>> buildFacets(Map<String, QueryDefinition.Facet> facetsDef, Facets facets)
					throws IOException {
		Map<String, Map<String, Number>> facetResults = new LinkedHashMap<String, Map<String, Number>>();
		for (Map.Entry<String, QueryDefinition.Facet> entry : facetsDef.entrySet()) {
			String dim = entry.getKey();
			Map<String, Number> facetMap = buildFacet(dim, entry.getValue(), facets);
			if (facetMap != null)
				facetResults.put(dim, facetMap);
		}
		return facetResults;
	}

	private static Map<String, Object> buildFields(Document document) {
		Map<String, Object> fields = new LinkedHashMap<String, Object>();
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
		return fields;
	}

	private static void addDocValues(int docId, Map<String, Object> sources, Map<String, Object> dest) {
		if (sources == null)
			return;
		for (Map.Entry<String, Object> entry : sources.entrySet()) {
			Object source = entry.getValue();
			if (source instanceof SortedDocValues) {
				SortedDocValues dv = (SortedDocValues) source;
				BytesRef bytesRef = dv.get(docId);
				if (bytesRef == null)
					continue;
				dest.put(entry.getKey(),bytesRef.utf8ToString());
			}
		}
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

	private Map<String, Object> extractDocValuesFields(IndexReader indexReader, Set<String> returned_fields)
					throws IOException {
		if (returned_fields == null)
			return null;
		FieldInfos fieldInfos = MultiFields.getMergedFieldInfos(indexReader);
		LeafReader dvReader = SlowCompositeReaderWrapper.wrap(indexReader);
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (String field : returned_fields) {
			FieldInfo fieldInfo = dvReader.getFieldInfos().fieldInfo(field);
			if (fieldInfo == null)
				continue;
			DocValuesType type = fieldInfo.getDocValuesType();
			if (type == null)
				continue;
			switch (type) {
			case BINARY:
				map.put(field, dvReader.getBinaryDocValues(field));
				break;
			case NONE:
				break;
			case NUMERIC:
				map.put(field, dvReader.getNumericDocValues(field));
				break;
			case SORTED:
				map.put(field, dvReader.getSortedDocValues(field));
				break;
			case SORTED_NUMERIC:
				map.put(field, dvReader.getSortedNumericDocValues(field));
				break;
			case SORTED_SET:
				map.put(field, dvReader.getSortedSetDocValues(field));
				break;
			default:
				throw new IOException("Unsupported doc value type: " + type + " for field: " + field);
			}
		}
		return map;
	}

	public long getTotal_hits() {
		return total_hits;
	}

	public Float getMax_score() {
		return max_score;
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

	public String getQuery() {
		return query;
	}
}
