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

import com.qwazr.search.analysis.AnalyzerContext;
import com.qwazr.search.field.SortUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.TimeTracker;
import com.qwazr.utils.server.ServerException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.postingshighlight.PostingsHighlighter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class QueryUtils {

	final static String getFinalQueryString(QueryDefinition queryDef) {
		// Deal wih query string
		final String qs;
		// Check if we have to escape some characters
		if (queryDef.escape_query != null && queryDef.escape_query) {
			if (queryDef.escaped_chars != null && queryDef.escaped_chars.length > 0)
				qs = StringUtils.escape_chars(queryDef.query_string, queryDef.escaped_chars);
			else
				qs = QueryParser.escape(queryDef.query_string);
		} else
			qs = queryDef.query_string;
		return qs;
	}

	final static Query getLuceneQuery(QueryContext queryContext)
			throws QueryNodeException, ParseException, IOException, ReflectiveOperationException {

		Query query = queryContext.queryDefinition.query == null ?
				new MatchAllDocsQuery() :
				queryContext.queryDefinition.query.getBoostedQuery(queryContext);

		return query;
	}

	static private Pair<TotalHitCountCollector, TopDocsCollector> getCollectorPair(List<Collector> collectors,
			boolean bNeedScore, int numHits, Sort sort) throws IOException {
		final TotalHitCountCollector totalHitCollector;
		final TopDocsCollector topDocsCollector;
		if (numHits == 0) {
			totalHitCollector = new TotalHitCountCollector();
			topDocsCollector = null;
			collectors.add(0, totalHitCollector);
		} else {
			if (sort != null)
				topDocsCollector = TopFieldCollector.create(sort, numHits, true, bNeedScore, bNeedScore);
			else
				topDocsCollector = TopScoreDocCollector.create(numHits);
			totalHitCollector = null;
			collectors.add(0, topDocsCollector);
		}
		return Pair.of(totalHitCollector, topDocsCollector);
	}

	final static ResultDefinition search(final QueryContext queryContext)
			throws ServerException, IOException, QueryNodeException, InterruptedException, ParseException,
			ReflectiveOperationException {

		final QueryDefinition queryDef = queryContext.queryDefinition;

		Query query = getLuceneQuery(queryContext);

		final IndexSearcher indexSearcher = queryContext.indexSearcher;
		final IndexReader indexReader = indexSearcher.getIndexReader();
		final TimeTracker timeTracker = new TimeTracker();

		final AnalyzerContext analyzerContext = queryContext.analyzer.getContext();
		final Sort sort =
				queryDef.sorts == null ? null : SortUtils.buildSort(analyzerContext.fieldTypes, queryDef.sorts);
		final FacetsBuilder facetsBuilder;

		final int numHits = queryDef.getEnd();
		final boolean bNeedScore = sort != null ? sort.needsScores() : true;

		final QueryCollectors queryCollectors = new QueryCollectors(bNeedScore, sort, numHits, queryDef.facets,
				queryDef.functions, analyzerContext.fieldTypes);

		indexSearcher.search(query, queryCollectors.finalCollector);
		final TopDocs topDocs = queryCollectors.getTopDocs();
		final Integer totalHits = queryCollectors.getTotalHits();

		timeTracker.next("search_query");

		facetsBuilder = queryCollectors.facetsCollector == null ?
				null :
				new FacetsBuilder(indexReader, queryDef.facets, queryCollectors.facetsCollector, timeTracker);

		Map<String, String[]> postingsHighlightersMap = null;
		if (queryDef.postings_highlighter != null && topDocs != null) {
			postingsHighlightersMap = new LinkedHashMap<>();
			for (Map.Entry<String, Integer> entry : queryDef.postings_highlighter.entrySet()) {
				String field = entry.getKey();
				PostingsHighlighter highlighter = new PostingsHighlighter(entry.getValue());
				String highlights[] = highlighter.highlight(field, query, indexSearcher, topDocs);
				if (highlights != null) {
					postingsHighlightersMap.put(field, highlights);
				}
			}
			timeTracker.next("postings_highlighters");
		}

		return new ResultDefinition(analyzerContext.fieldTypes, timeTracker, indexSearcher, totalHits, topDocs,
				queryDef, facetsBuilder, postingsHighlightersMap, queryCollectors.functionsCollectors, query);
	}

}
