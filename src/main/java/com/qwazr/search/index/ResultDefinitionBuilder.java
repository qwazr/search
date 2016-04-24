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

import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.field.ValueConverter;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.TimeTracker;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.*;

class ResultDefinitionBuilder<T extends ResultDocumentAbstract> {

	private final QueryDefinition queryDefinition;
	private final IndexSearcher indexSearcher;
	private final Query luceneQuery;
	private final Map<String, HighlighterImpl> highlighters;
	private final Collection<FunctionCollector> functionsCollector;
	private final Map<String, FieldTypeInterface> fieldMap;
	private final TimeTracker timeTracker;
	private final ResultDocumentBuilder.BuilderFactory documentBuilderFactory;
	private final TopDocs topDocs;

	final ResultDocumentBuilder<T>[] resultDocumentBuilders;
	final List<T> documents;
	final List<ResultDefinition.Function> functions;
	final String queryDebug;
	final TimeTracker.Status timeTrackerStatus;
	final Long totalHits;
	final Float maxScore;
	final LinkedHashMap<String, Map<String, Number>> facets;

	ResultDefinitionBuilder(final QueryDefinition queryDefinition, final TopDocs topDocs,
			final IndexSearcher indexSearcher, final Query luceneQuery, final Map<String, HighlighterImpl> highlighters,
			final Collection<FunctionCollector> functionsCollector, final Map<String, FieldTypeInterface> fieldMap,
			final TimeTracker timeTracker, final ResultDocumentBuilder.BuilderFactory documentBuilderFactory,
			final FacetsBuilder facetsBuilder, Integer totalHits) throws IOException {

		this.queryDefinition = queryDefinition;
		this.topDocs = topDocs;
		this.indexSearcher = indexSearcher;
		this.luceneQuery = luceneQuery;
		this.highlighters = highlighters;
		this.functionsCollector = functionsCollector;
		this.fieldMap = fieldMap;
		this.timeTracker = timeTracker;
		this.documentBuilderFactory = documentBuilderFactory;

		this.maxScore = topDocs == null ? null : topDocs.getMaxScore();
		this.totalHits = totalHits == null ? null : (long) totalHits;

		this.resultDocumentBuilders = buildResultDocuments();
		if (resultDocumentBuilders != null) {
			this.documents = new ArrayList<>(resultDocumentBuilders.length);
			if (resultDocumentBuilders.length > 0) {
				final Set<String> returnedFields =
						queryDefinition.returned_fields != null && queryDefinition.returned_fields.contains("*") ?
								fieldMap.keySet() :
								queryDefinition.returned_fields;

				if (returnedFields != null && !returnedFields.isEmpty()) {
					buildStoredFields(returnedFields);
					buildDocValueReturnedFields(returnedFields);
				}
				buildHighlights();
				for (ResultDocumentBuilder<T> rdb : resultDocumentBuilders)
					this.documents.add(rdb.build());
			}
		} else
			this.documents = null;

		this.facets = facetsBuilder == null ? null : facetsBuilder.results;
		this.functions = buildFunctions();
		this.queryDebug = buildQueryDebug();

		this.timeTrackerStatus = timeTracker == null ? null : timeTracker.getStatus();
	}

	final private ResultDocumentBuilder<T>[] buildResultDocuments() throws IOException {
		if (topDocs == null)
			return null;
		if (topDocs.scoreDocs == null)
			return null;
		int pos = queryDefinition.start == null ? 0 : queryDefinition.start;
		int end = queryDefinition.getEnd();
		if (end > topDocs.totalHits)
			end = topDocs.totalHits;
		final int size = end - pos;
		if (size <= 0)
			return new ResultDocumentBuilder[0];
		final float maxScore = topDocs.getMaxScore();
		ResultDocumentBuilder<T>[] resultDocuments = documentBuilderFactory.createArray(size);
		for (int i = 0; i < size; i++)
			resultDocuments[i] = documentBuilderFactory.createBuilder(pos, topDocs.scoreDocs[pos++], maxScore);
		return resultDocuments;
	}

	final private void buildHighlights() {

		if (highlighters == null)
			return;

		int[] docIDs = new int[resultDocumentBuilders.length];
		int pos = 0;
		for (ResultDocumentBuilder resultDocumentBuilder : resultDocumentBuilders)
			docIDs[pos++] = resultDocumentBuilder.scoreDoc.doc;
		highlighters.forEach((name, highlighter) -> {
			try {
				String[] snippetsByDoc = highlighter.highlights(luceneQuery, indexSearcher, docIDs);
				int i = 0;
				for (String snippet : snippetsByDoc)
					resultDocumentBuilders[i++].setHighlight(name, snippet);
			} catch (IOException e) {
				throw new RuntimeException("Highlighter failure: " + name, e);
			}
		});

		if (timeTracker != null)
			timeTracker.next("highlighting");
	}

	final private void buildStoredFields(final Set<String> returnedFields) throws IOException {
		for (ResultDocumentBuilder resultDocumentBuider : resultDocumentBuilders) {
			resultDocumentBuider.setStoredFields(indexSearcher.doc(resultDocumentBuider.scoreDoc.doc, returnedFields));
		}
		if (timeTracker != null)
			timeTracker.next("storedFields");
	}

	final private void buildDocValueReturnedFields(final Set<String> returnedFields) throws IOException {

		final IndexReader indexReader = indexSearcher.getIndexReader();

		returnedFields.forEach(fieldName -> {
			final FieldTypeInterface fieldType = fieldMap.get(fieldName);
			if (fieldType == null)
				return;
			final ValueConverter converter;
			try {
				converter = fieldType.getConverter(indexReader);
			} catch (IOException e) {
				throw new ServerException(e);
			}
			if (converter == null)
				return;
			for (ResultDocumentBuilder resultDocumentBuilder : resultDocumentBuilders)
				resultDocumentBuilder
						.setReturnedField(fieldName, converter.convert(resultDocumentBuilder.scoreDoc.doc));
		});
		if (timeTracker != null)
			timeTracker.next("docValuesFields");
	}

	final private String buildQueryDebug() {
		if (queryDefinition.query_debug == null || luceneQuery == null)
			return null;
		if (!queryDefinition.query_debug)
			return null;
		return luceneQuery.toString(StringUtils.EMPTY);
	}

	final private List<ResultDefinition.Function> buildFunctions() {
		if (functionsCollector == null || functionsCollector.isEmpty())
			return null;
		List<ResultDefinition.Function> functions = new ArrayList<>(functionsCollector.size());
		functionsCollector
				.forEach(functionCollector -> functions.add(new ResultDefinition.Function(functionCollector)));
		return functions;
	}

}
