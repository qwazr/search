/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import com.qwazr.utils.StringUtils;
import com.qwazr.utils.TimeTracker;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.postingshighlight.PostingsHighlighter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class QueryUtils {

	final static Query getLuceneQuery(QueryDefinition queryDef, Analyzer analyzer)
					throws QueryNodeException, ParseException {
		final StandardQueryParser parser = new StandardQueryParser(analyzer);
		if (queryDef.multi_field != null && !queryDef.multi_field.isEmpty()) {
			Set<String> fieldSet = queryDef.multi_field.keySet();
			String[] fieldArray = fieldSet.toArray(new String[fieldSet.size()]);
			parser.setMultiFields(fieldArray);
			parser.setFieldsBoost(queryDef.multi_field);
		}
		if (queryDef.default_operator != null)
			parser.setDefaultOperator(queryDef.default_operator);
		if (queryDef.allow_leading_wildcard != null)
			parser.setAllowLeadingWildcard(queryDef.allow_leading_wildcard);
		if (queryDef.phrase_slop != null)
			parser.setPhraseSlop(queryDef.phrase_slop);
		if (queryDef.enable_position_increments != null)
			parser.setEnablePositionIncrements(queryDef.enable_position_increments);
		final String qs;
		// Check if we have to escape some characters
		if (queryDef.escape_query != null && queryDef.escape_query) {
			if (queryDef.escaped_chars != null && queryDef.escaped_chars.length > 0)
				qs = StringUtils.escape_chars(queryDef.query_string, queryDef.escaped_chars);
			else
				qs = QueryParser.escape(queryDef.query_string);
		} else
			qs = queryDef.query_string;

		Query query = parser.parse(qs, queryDef.default_field);

		if (queryDef.auto_generate_phrase_query != null && queryDef.auto_generate_phrase_query && qs != null
						&& qs.length() > 0 && qs.indexOf('"') == -1) {
			Query phraseQuery = parser.parse('"' + qs + '"', queryDef.default_field);
			query = new BooleanQuery.Builder().add(query, BooleanClause.Occur.SHOULD)
							.add(phraseQuery, BooleanClause.Occur.SHOULD).build();
		}
		return query;
	}

	final static ResultDefinition search(Map<String, FieldDefinition> fieldMap, IndexSearcher indexSearcher,
					QueryDefinition queryDef, Analyzer analyzer, FacetsConfig facetsConfig)
					throws ServerException, IOException, QueryNodeException, InterruptedException, ParseException {

		Query query = getLuceneQuery(queryDef, analyzer);

		final IndexReader indexReader = indexSearcher.getIndexReader();
		final TimeTracker timeTracker = new TimeTracker();
		final TopDocs topDocs;
		final Facets facets;

		// Overload query with filters
		if (queryDef.filters != null && !queryDef.filters.isEmpty()) {
			DrillDownQuery drillDownQuery = new DrillDownQuery(facetsConfig, query);
			for (Map.Entry<String, Set<String>> entry : queryDef.filters.entrySet()) {
				Set<String> filter_terms = entry.getValue();
				if (filter_terms == null)
					continue;
				String filter_field = entry.getKey();
				for (String filter_term : filter_terms)
					drillDownQuery.add(filter_field, filter_term);
			}
			query = drillDownQuery;
		}
		if (queryDef.facets != null && queryDef.facets.size() > 0) {
			FacetsCollector facetsCollector = new FacetsCollector();
			SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(indexReader);
			topDocs = FacetsCollector.search(indexSearcher, query, queryDef.getEnd(), facetsCollector);
			timeTracker.next("search_query");
			facets = new SortedSetDocValuesFacetCounts(state, facetsCollector);
			timeTracker.next("facet_count");
		} else {
			topDocs = indexSearcher.search(query, queryDef.getEnd());
			timeTracker.next("search_query");
			facets = null;
		}
		Map<String, String[]> postingsHighlightersMap = null;
		if (queryDef.postings_highlighter != null) {
			postingsHighlightersMap = new LinkedHashMap<String, String[]>();
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

		return new ResultDefinition(fieldMap, timeTracker, indexSearcher, topDocs, queryDef, facets,
						postingsHighlightersMap, query);

	}
}
