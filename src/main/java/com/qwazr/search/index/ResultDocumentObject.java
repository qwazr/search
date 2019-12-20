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
 **/
package com.qwazr.search.index;

import com.qwazr.binder.FieldMapWrapper;
import com.qwazr.binder.setter.FieldSetter;
import com.qwazr.search.field.Converters.ValueConverter;
import com.qwazr.server.ServerException;
import com.qwazr.utils.SerializationUtils;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
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

		private final T record;
		private final Map<String, FieldSetter> fieldMap;

		Builder(final int pos, final ScoreDoc scoreDoc, final FieldMapWrapper<T> wrapper) {
			super(pos, scoreDoc);
			try {
				this.record = wrapper.constructor.newInstance();
			} catch (ReflectiveOperationException e) {
				throw ServerException.of(e);
			}
			this.fieldMap = wrapper.fieldMap;
		}

		@Override
		final ResultDocumentObject<T> build() {
			return new ResultDocumentObject<>(this);
		}

		@Override
		void setDocValuesField(final String fieldName, final ValueConverter converter) throws IOException {
			final FieldSetter fieldSetter = fieldMap.get(fieldName);
			if (fieldSetter == null)
				throw new ServerException("Unknown field " + fieldName + " for class " + record.getClass());
			converter.fill(record, fieldSetter, scoreDoc.doc);
		}

		private FieldSetter checkFieldSetter(final String fieldName) {
			final FieldSetter fieldSetter = fieldMap.get(fieldName);
			if (fieldSetter == null)
				throw new ServerException("Unknown field " + fieldName + " for class " + record.getClass());
			return fieldSetter;
		}

		@Override
		final void setStoredFieldString(String fieldName, List<String> values) {
			checkFieldSetter(fieldName).fromCollection(String.class, values, record);
		}

		@Override
		final void setStoredFieldBytes(String fieldName, List<byte[]> values) {
			final FieldSetter fieldSetter = checkFieldSetter(fieldName);
			final Class<?> fieldType = fieldSetter.getType();
			if (Serializable.class.isAssignableFrom(fieldType)) {
                //fieldSetter.set(record, values.get(0));
				try {
                       SerializationUtils.fromExternalizorBytes(values.get(0),
							(Class<? extends Serializable>) fieldType);
				} catch (IOException | ReflectiveOperationException e) {
					throw ServerException.of("Deserialization failure " + fieldName + " for class " + record.getClass(),
							e);
				}
			} else
				fieldSetter.setValue(record, values);
		}

		@Override
		final void setStoredFieldInteger(String fieldName, int[] values) {
			checkFieldSetter(fieldName).fromInteger(values, record);
		}

		@Override
		final void setStoredFieldLong(String fieldName, long[] values) {
			checkFieldSetter(fieldName).fromLong(values, record);
		}

		@Override
		final void setStoredFieldFloat(String fieldName, float[] values) {
			checkFieldSetter(fieldName).fromFloat(values, record);
		}

		@Override
		final void setStoredFieldDouble(String fieldName, double[] values) {
			checkFieldSetter(fieldName).fromDouble(values, record);
		}

	}
}
