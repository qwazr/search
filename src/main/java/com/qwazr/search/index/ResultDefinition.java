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
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetsCollector;
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

	public class Timer {
		final public long search_time;
		final public long fields_time;

		private Timer(long search_time, long fields_time) {
			this.search_time = search_time;
			this.fields_time = fields_time;
		}

		public long getSearch_time() {
			return search_time;
		}

		public long getFields_time() {
			return fields_time;
		}
	}

	final public Timer timer;
	final public long total_hits;
	final public List<Map<String, Object>> documents;
	final public Map<String, Map<String, Object>> facets;

	public ResultDefinition() {
		this.timer = null;
		this.total_hits = 0;
		this.documents = null;
		this.facets = null;
	}

	public final static String FIELD_SCORE = "_score";
	public final static String FIELD_ID = "_id";

	public ResultDefinition(long search_time, IndexSearcher searcher, TopDocs topDocs, QueryDefinition queryDef,
							FacetsCollector facetsCollector) throws IOException {
		long start_time = System.currentTimeMillis();
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
		long fields_time = System.currentTimeMillis() - start_time;
		facets = null;
		timer = new Timer(search_time, fields_time);
	}

	public long getTotal_hits() {
		return total_hits;
	}

	public List<Map<String, Object>> getDocuments() {
		return documents;
	}

	public Map<String, Map<String, Object>> getFacets() {
		return facets;
	}

	public Timer getTimer() {
		return timer;
	}
}
