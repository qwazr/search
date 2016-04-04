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

import com.qwazr.utils.server.ServerException;
import org.apache.lucene.search.ScoreDoc;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class ResultDocumentObject<T> extends ResultDocumentAbstract {

	final public T record;

	private ResultDocumentObject(final Builder<T> builder) {
		this.record = builder.record;
	}

	public T getRecord() {
		return record;
	}

	static class Builder<T> extends ResultDocumentBuilder<ResultDocumentObject<T>> {

		private final T record;
		private final Map<String, Field> fieldMap;

		Builder(final int pos, final ScoreDoc scoreDoc, final float maxScore, final Class<T> objectClass,
						Map<String, Field> fieldMap) {
			super(pos, scoreDoc, maxScore);
			try {
				this.record = objectClass.newInstance();
			} catch (ReflectiveOperationException e) {
				throw new ServerException(e);
			}
			this.fieldMap = fieldMap;
		}

		@Override
		final ResultDocumentObject build() {
			return new ResultDocumentObject(this);
		}

		@Override
		final void setReturnedField(final String fieldName, final Object fieldValue) {
			Field field = fieldMap.get(fieldName);
			if (field == null)
				throw new ServerException("Unknown field " + fieldName + " for class " + record.getClass());
			try {
				final Class<?> type = field.getType();
				if (type.isAssignableFrom(fieldValue.getClass()))
					field.set(record, fieldValue);
				else {
					Object value = field.get(record);
					if (value != null && value instanceof Collection) {
						((Collection) value).add(fieldValue);
					} else
						System.out.println("OOCH");
				}
			} catch (IllegalAccessException e) {
				throw new ServerException(e);
			}
		}
	}
}
