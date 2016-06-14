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

import com.qwazr.search.field.SortUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.TimeTracker;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.LinkedHashMap;
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

	final static ResultDefinition search(final QueryContext queryContext,
			final ResultDocumentBuilder.BuilderFactory documentBuilderFactory)
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException, InterruptedException {

		final QueryDefinition queryDef = queryContext.queryDefinition;

		final Query query = queryContext.queryDefinition.query == null ?
				new MatchAllDocsQuery() :
				queryContext.queryDefinition.query.getQuery(queryContext);

		final TimeTracker timeTracker = new TimeTracker();

		final Sort sort = queryDef.sorts == null ? null : SortUtils.buildSort(queryContext.fieldMap, queryDef.sorts);

		final int numHits = queryDef.getEnd();
		final boolean bNeedScore = sort != null ? sort.needsScores() : true;

		final QueryCollectors queryCollectors =
				new QueryCollectors(bNeedScore, sort, numHits, queryDef.facets, queryDef.functions,
						queryContext.fieldMap);

		queryContext.indexSearcher.search(query, queryCollectors.finalCollector);
		final TopDocs topDocs = queryCollectors.getTopDocs();
		final Integer totalHits = queryCollectors.getTotalHits();

		timeTracker.next("search_query");

		final FacetsBuilder facetsBuilder = queryCollectors.facetsCollector == null ?
				null :
				new FacetsBuilder(queryContext, queryDef.facets, query, queryCollectors.facetsCollector, timeTracker);

		final Map<String, HighlighterImpl> highlighters;
		if (queryDef.highlighters != null && topDocs != null) {
			highlighters = new LinkedHashMap<>();
			queryDef.highlighters.forEach((name, highlighterDefinition) -> highlighters.put(name,
					new HighlighterImpl(highlighterDefinition,
							queryContext.indexAnalyzer.getWrappedAnalyzer(highlighterDefinition.field))));
		} else
			highlighters = null;

		ResultDefinitionBuilder resultBuilder =
				new ResultDefinitionBuilder(queryDef, topDocs, queryContext.indexSearcher, query, highlighters,
						queryCollectors.functionsCollectors, queryContext.fieldMap, timeTracker, documentBuilderFactory,
						facetsBuilder, totalHits);
		return documentBuilderFactory.build(resultBuilder);
	}

}
