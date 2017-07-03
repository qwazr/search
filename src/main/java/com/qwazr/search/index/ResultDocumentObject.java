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

import com.qwazr.binder.FieldMapWrapper;
import com.qwazr.binder.setter.FieldSetter;
import com.qwazr.search.field.Converters.ValueConverter;
import com.qwazr.server.ServerException;
import com.qwazr.utils.SerializationUtils;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class ResultDocumentObject<T> extends ResultDocumentAbstract {

	final public T record;

	private ResultDocumentObject(final Builder<T> builder) {
		super(builder);
		this.record = builder.record;
	}

	public ResultDocumentObject(final ResultDocumentAbstract resultDocument, final T record) {
		super(resultDocument);
		this.record = record;
	}

	public T getRecord() {
		return record;
	}

	static class Builder<T> extends ResultDocumentBuilder<ResultDocumentObject<T>> {

		private final FieldMapWrapper<T> wrapper;
		private final T record;
		private final Map<String, FieldSetter> fieldMap;

		Builder(final int pos, final ScoreDoc scoreDoc, final FieldMapWrapper<T> wrapper) {
			super(pos, scoreDoc);
			try {
				this.record = wrapper.constructor.newInstance();
				this.wrapper = wrapper;
			} catch (ReflectiveOperationException e) {
				throw new ServerException(e);
			}
			this.fieldMap = wrapper.fieldMap;
		}

		@Override
		final ResultDocumentObject<T> build() {
			return new ResultDocumentObject<>(this);
		}

		@Override
		void setDocValuesField(final String fieldName, final ValueConverter converter) {
			final FieldSetter fieldSetter = fieldMap.get(fieldName);
			if (fieldSetter == null)
				throw new ServerException(() -> "Unknown field " + fieldName + " for class " + record.getClass());
			converter.fill(record, fieldSetter, scoreDoc.doc);
		}

		@Override
		final void setStoredField(final String fieldName, final Object fieldValue) {
			final FieldSetter fieldSetter = fieldMap.get(fieldName);
			if (fieldSetter == null)
				throw new ServerException(() -> "Unknown field " + fieldName + " for class " + record.getClass());
			final Class<?> fieldType = fieldSetter.getType();
			try {

				if (fieldValue instanceof BytesRef) {
					final BytesRef br = (BytesRef) fieldValue;
					if (Serializable.class.isAssignableFrom(fieldType)) {
						fieldSetter.set(record, SerializationUtils.fromExternalizorBytes(br.bytes,
								(Class<? extends Serializable>) fieldType));
						return;
					}
				}
				fieldSetter.setValue(record, fieldValue);
			} catch (ReflectiveOperationException | IOException e) {
				throw new ServerException(e);
			}
		}
	}

}
