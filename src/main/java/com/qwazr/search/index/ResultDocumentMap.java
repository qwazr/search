/*
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
 */
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.field.Converters.ValueConverter;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResultDocumentMap extends ResultDocumentAbstract {

	final public LinkedHashMap<String, Object> fields;

	@JsonCreator
	public ResultDocumentMap(@JsonProperty("score") Float score, @JsonProperty("pos") Integer pos,
			@JsonProperty("doc") Integer doc, @JsonProperty("shard_index") Integer shardIndex,
			@JsonProperty("highlights") Map<String, String> highlights,
			@JsonProperty("fields") LinkedHashMap<String, Object> fields) {
		super(score, pos, doc, shardIndex, highlights);
		this.fields = fields;
	}

	ResultDocumentMap(Builder builder) {
		super(builder);
		fields = builder.fields;
	}

	public LinkedHashMap<String, Object> getFields() {
		return fields;
	}

	final static class Builder extends ResultDocumentBuilder<ResultDocumentMap> {

		private final LinkedHashMap<String, Object> fields;

		Builder(final int pos, final ScoreDoc scoreDoc) {
			super(pos, scoreDoc);
			this.fields = new LinkedHashMap<>();
		}

		@Override
		final ResultDocumentMap build() {
			return new ResultDocumentMap(this);
		}

		@Override
		final void setDocValuesField(final String fieldName, final ValueConverter converter) throws IOException {
			fields.put(fieldName, converter.convert(scoreDoc.doc));
		}

		@Override
		void setStoredFieldString(String fieldName, List<String> values) {
			if (values.size() == 1)
				fields.put(fieldName, values.get(0));
			else
				fields.put(fieldName, values);
		}

		@Override
		void setStoredFieldBytes(String fieldName, List<byte[]> values) {
			if (values.size() == 1)
				fields.put(fieldName, values.get(0));
			else
				fields.put(fieldName, values);
		}

		@Override
		void setStoredFieldInteger(String fieldName, int[] values) {
			if (values.length == 1)
				fields.put(fieldName, values[0]);
			else
				fields.put(fieldName, values);
		}

		@Override
		void setStoredFieldLong(String fieldName, long[] values) {
			if (values.length == 1)
				fields.put(fieldName, values[0]);
			else
				fields.put(fieldName, values);
		}

		@Override
		void setStoredFieldFloat(String fieldName, float[] values) {
			if (values.length == 1)
				fields.put(fieldName, values[0]);
			else
				fields.put(fieldName, values);
		}

		@Override
		void setStoredFieldDouble(String fieldName, double[] values) {
			if (values.length == 1)
				fields.put(fieldName, values[0]);
			else
				fields.put(fieldName, values);
		}

	}
}
