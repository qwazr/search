/**
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
 **/
package com.qwazr.search.index;

import com.qwazr.utils.StringUtils;
import com.qwazr.utils.TimeTracker;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

class ResultDocumentsBuilder {

	final Map<String, Object> collectors;
	final LinkedHashMap<String, Map<String, Number>> facets;
	final String queryDebug;
	final TimeTracker.Status timeTrackerStatus;
	final float maxScore;
	final long totalHits;

	ResultDocumentsBuilder(final QueryDefinition queryDefinition, final TopDocs topDocs,
			final IndexSearcher indexSearcher, final Query luceneQuery, final Map<String, HighlighterImpl> highlighters,
			final Map<String, Object> externalCollectorsResults, final TimeTracker timeTracker,
			final FacetsBuilder facetsBuilder, long totalHits, @NotNull final ResultDocumentsInterface resultDocuments)
			throws ReflectiveOperationException, IOException {

		this.collectors = externalCollectorsResults;

		if (topDocs != null && topDocs.scoreDocs != null) {

			this.maxScore = topDocs.getMaxScore();
			int pos = 0;
			for (ScoreDoc scoreDoc : topDocs.scoreDocs)
				resultDocuments.doc(indexSearcher, pos++, scoreDoc);

			if (timeTracker != null)
				timeTracker.next("documents");

			if (highlighters != null && topDocs.scoreDocs.length > 0) {

				highlighters.forEach((name, highlighter) -> {
					try {
						final String[] snippetsByDoc = highlighter.highlights(luceneQuery, indexSearcher, topDocs);
						int pos2 = 0;
						for (String snippet : snippetsByDoc)
							resultDocuments.highlight(pos2++, name, snippet);
					} catch (IOException e) {
						throw new RuntimeException("Highlighting failure: " + name, e);
					}
				});
				if (timeTracker != null)
					timeTracker.next("highlighting");
			}
		} else
			this.maxScore = 0;

		this.totalHits = totalHits;

		this.facets = facetsBuilder == null ? null : facetsBuilder.results;
		this.queryDebug = queryDefinition.query_debug != null && queryDefinition.query_debug && luceneQuery != null ?
				luceneQuery.toString(StringUtils.EMPTY) :
				null;

		this.timeTrackerStatus = timeTracker == null ? null : timeTracker.getStatus();
	}

}
