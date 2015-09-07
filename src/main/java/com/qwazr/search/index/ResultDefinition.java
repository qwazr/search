/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
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

	final public long total_hits;
	final public List<Map<String, Object>> documents;
	final public Map<String, Map<String, Object>> facets;

	ResultDefinition(long total_hits,
					 List<Map<String, Object>> documents,
					 Map<String, Map<String, Object>> facets) {
		this.total_hits = total_hits;
		this.documents = documents;
		this.facets = facets;
	}

	public ResultDefinition() {
		this(0, null, null);
	}

	public ResultDefinition(IndexSearcher searcher, TopDocs topDocs, QueryDefinition queryDef,
							FacetsCollector facetsCollector) throws IOException {
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
			for (IndexableField field : document)
				doc.put(field.name(), field.fieldType().toString());
			documents.add(doc);
			pos++;
		}
		facets = null;
	}
}
