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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qwazr.search.field.Converters.ValueConverter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResultDocumentMap extends ResultDocumentAbstract {

	final public LinkedHashMap<String, Object> fields;

	public ResultDocumentMap() {
		fields = null;
	}

	private ResultDocumentMap(Builder builder) {
		super(builder);
		fields = builder.fields;
	}

	public LinkedHashMap<String, Object> getFields() {
		return fields;
	}

	final static class Builder extends ResultDocumentBuilder<ResultDocumentMap> {

		private final LinkedHashMap<String, Object> fields;

		Builder(final int pos, final ScoreDoc scoreDoc, final float maxScore) {
			super(pos, scoreDoc, maxScore);
			this.fields = new LinkedHashMap<>();
		}

		@Override
		final ResultDocumentMap build() {
			return new ResultDocumentMap(this);
		}

		@Override
		final void setDocValuesField(final String fieldName, final ValueConverter converter, final int docId) {
			// TODO optimize for map
			setStoredField(fieldName, converter.convert(docId));
		}

		@Override
		final void setStoredField(final String fieldName, Object fieldValue) {
			if (fieldValue instanceof BytesRef)
				fieldValue = ((BytesRef) fieldValue).bytes;
			final Object oldValue = fields.get(fieldName);
			if (oldValue == null) {
				fields.put(fieldName, fieldValue);
				return;
			}
			if (oldValue instanceof Collection<?>) {
				((Collection<Object>) oldValue).add(fieldValue);
				return;
			}
			final List<Object> list = new ArrayList<>(2);
			list.add(oldValue);
			list.add(fieldValue);
			fields.put(fieldName, list);
		}
	}
}
