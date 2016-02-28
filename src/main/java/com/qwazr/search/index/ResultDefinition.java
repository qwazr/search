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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.TimeTracker;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

@JsonInclude(Include.NON_NULL)
public class ResultDefinition {

	final public TimeTracker.Status timer;
	final public Long total_hits;
	final public Float max_score;
	final public List<ResultDocument> documents;
	final public Map<String, Map<String, Number>> facets;
	final public String query;
	final public List<Function> functions;

	public static class Function extends QueryDefinition.Function {

		final public Object value;

		public Function() {
			value = null;
		}

		Function(FunctionCollector functionCollector) {
			super(functionCollector.function);
			this.value = functionCollector.getValue();
		}
	}

	public ResultDefinition() {
		this.timer = null;
		this.total_hits = null;
		this.documents = null;
		this.facets = null;
		this.functions = null;
		this.max_score = null;
		this.query = null;
	}

	private static String getQuery(Boolean queryDebug, Query query) {
		if (queryDebug == null || query == null)
			return null;
		if (!queryDebug && query != null)
			return null;
		return query.toString(StringUtils.EMPTY);
	}

	ResultDefinition(Map<String, FieldTypeInterface> fieldMap, TimeTracker timeTracker, IndexSearcher searcher,
			Integer totalHits, TopDocs topDocs, QueryDefinition queryDef, FacetsBuilder facetsBuilder,
			Map<String, HighlighterImpl> highlighters, Collection<FunctionCollector> functionsCollector, Query query)
			throws IOException {
		this.query = getQuery(queryDef.query_debug, query);
		this.total_hits = totalHits == null ? null : (long) totalHits;
		max_score = topDocs != null ? topDocs.getMaxScore() : null;
		int pos = queryDef.start == null ? 0 : queryDef.start;
		int end = queryDef.getEnd();
		documents = new ArrayList<ResultDocument>();
		ScoreDoc[] docs = topDocs != null ? topDocs.scoreDocs : null;

		LeafReader leafReader = SlowCompositeReaderWrapper.wrap(searcher.getIndexReader());

		if (docs != null) {

			List<ScoreDoc> scoreDocs = new ArrayList<>();
			while (pos < total_hits && pos < end) {
				scoreDocs.add(docs[pos]);
				pos++;
			}

			int[] docIDs = new int[scoreDocs.size()];
			int i = 0;
			for (ScoreDoc scoreDoc : scoreDocs)
				docIDs[i++] = scoreDoc.doc;

			// Highlights
			final Map<String, String>[] highlightsArray;
			if (highlighters != null) {

				//Compute highlights
				highlightsArray = new Map[docIDs.length];
				final LinkedHashMap<String, String[]> highlightsByName = new LinkedHashMap<>();
				highlighters.forEach(new BiConsumer<String, HighlighterImpl>() {
					@Override
					public void accept(String name, HighlighterImpl highlighter) {
						try {
							highlightsByName.put(name, highlighter.highlights(query, searcher, docIDs));
						} catch (IOException e) {
							throw new RuntimeException("Highlighter failure: " + name, e);
						}
					}
				});

				// Copy results
				AtomicInteger ai = new AtomicInteger(0);
				for (ScoreDoc scoreDoc : scoreDocs) {
					final LinkedHashMap<String, String> highlightsMap = new LinkedHashMap<>();
					highlightsByName.forEach(new BiConsumer<String, String[]>() {
						@Override
						public void accept(String name, String[] highlights) {
							highlightsMap.put(name, highlights[ai.get()]);
						}
					});
					highlightsArray[ai.getAndIncrement()] = highlightsMap;
				}
				if (timeTracker != null)
					timeTracker.next("highlighting");
			} else
				highlightsArray = null;

			// Returned doc values
			ReturnedFields.DocValuesReturnedFields docValuesReturnedFields = new ReturnedFields.DocValuesReturnedFields(
					fieldMap, leafReader, queryDef.returned_fields);

			i = 0;
			for (ScoreDoc scoreDoc : scoreDocs) {
				final Document document = searcher.doc(scoreDoc.doc, queryDef.returned_fields);
				Map<String, String> highlights = highlightsArray == null ? null : highlightsArray[i++];
				documents.add(new ResultDocument(pos, scoreDoc, max_score, document, highlights,
						docValuesReturnedFields));
			}

			if (timeTracker != null)
				timeTracker.next("returned_fields");
		}

		this.facets = facetsBuilder == null ? null : facetsBuilder.results;
		this.functions = functionsCollector != null ? ResultUtils.buildFunctions(functionsCollector) : null;
		if (timeTracker != null)
			timeTracker.next("facet_fields");

		this.timer = timeTracker != null ? timeTracker.getStatus() : null;
	}

	ResultDefinition(TimeTracker timeTracker) {
		query = null;
		total_hits = 0L;
		documents = Collections.emptyList();
		facets = null;
		functions = null;
		max_score = null;
		this.timer = timeTracker != null ? timeTracker.getStatus() : null;
	}

	ResultDefinition(long total_hits) {
		query = null;
		this.total_hits = total_hits;
		documents = Collections.emptyList();
		facets = null;
		functions = null;
		max_score = null;
		this.timer = null;
	}

	public Long getTotal_hits() {
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

	public TimeTracker.Status getTimer() {
		return timer;
	}

	public String getQuery() {
		return query;
	}
}
