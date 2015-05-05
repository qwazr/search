/**
 * Copyright 2015 OpenSearchServer Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import java.util.LinkedHashMap;
import java.util.Map;

public class DocumentBuilder {

	private Map<String, FieldContentBuilder> fields = null;

	private FieldContentBuilder getField(String name) {
		if (fields == null)
			fields = new LinkedHashMap<String, FieldContentBuilder>();
		FieldContentBuilder fieldContentBuilder = fields.get(name);
		if (fieldContentBuilder == null) {
			fieldContentBuilder = new FieldContentBuilder();
			fields.put(name, fieldContentBuilder);
		}
		return fieldContentBuilder;
	}

	public DocumentBuilder addTerm(String field, String term) {
		getField(field).addTerm(term);
		return this;
	}

	public DocumentBuilder addTerm(String field, String term, int increment) {
		getField(field).addTerm(term, increment);
		return this;
	}

	public DocumentBuilder addTerm(String field, String term, int increment,
			int offset_start, int offset_end) {
		getField(field).addTerm(term, increment, offset_start, offset_end);
		return this;
	}

	public DocumentBuilder addTerm(String field, String term, int offset_start,
			int offset_end) {
		getField(field).addTerm(term, offset_start, offset_end);
		return this;
	}

	/**
	 * @return a map of fieldContent
	 */
	public Map<String, FieldContent> build() {
		if (fields == null)
			return null;
		Map<String, FieldContent> map = new LinkedHashMap<String, FieldContent>();
		for (Map.Entry<String, FieldContentBuilder> entry : fields.entrySet())
			map.put(entry.getKey(), entry.getValue().build());
		return map;
	}
}
