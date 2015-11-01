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
 **/
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResultDocument {

	final public Float score;
	final private int doc;
	final private int shard_index;
	final public Map<String, Object> fields;
	final public Map<String, String> postings_highlights;

	public ResultDocument() {
		score = null;
		fields = null;
		postings_highlights = null;
		doc = -1;
		shard_index = -1;
	}

	ResultDocument(int pos, ScoreDoc scoreDoc, Document document, Map<String, Integer> postings_highlighter,
					Map<String, String[]> postingsHighlightsMap,
					Map<String, DocValueUtils.DVConverter> docValuesReturnedFields) throws IOException {
		this.score = scoreDoc.score;
		this.doc = scoreDoc.doc;
		this.shard_index = scoreDoc.shardIndex;
		// Build field map
		fields = ResultUtils.buildFields(document);
		ResultUtils.addDocValues(scoreDoc.doc, docValuesReturnedFields, fields);

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
		return score;
	}

	@JsonIgnore
	public int getDoc() {
		return doc;
	}

	@JsonIgnore
	public int getShard_index() {
		return shard_index;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public Map<String, String> getPostings_highlights() {
		return postings_highlights;
	}

}
