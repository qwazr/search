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
 */
package com.qwazr.search.index;

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import org.apache.lucene.util.BytesRef;

import java.util.Map;
import java.util.function.BiConsumer;

abstract class RecordBuilder {

	private final FieldConsumer fieldConsumer;
	private final Map<String, FieldTypeInterface> fieldTypes;

	volatile BytesRef id;

	RecordBuilder(final Map<String, FieldTypeInterface> fieldTypes, final FieldConsumer fieldConsumer) {
		this.fieldTypes = fieldTypes;
		this.fieldConsumer = fieldConsumer;
		this.id = null;
	}

	final protected void addFieldValue(final String fieldName, final Object fieldValue) {
		if (fieldValue == null)
			return;

		FieldTypeInterface fieldType = fieldTypes.get(fieldName);
		if (fieldType == null)
			throw new IllegalArgumentException("No field definition for the field: " + fieldName);
		fieldType.fill(fieldValue, fieldConsumer);

		if (FieldDefinition.ID_FIELD.equals(fieldName))
			id = BytesRefUtils.fromAny(fieldValue);
	}

	final static class ForMap extends RecordBuilder implements BiConsumer<String, Object> {

		ForMap(final Map<String, FieldTypeInterface> fieldTypes, final FieldConsumer fieldConsumer) {
			super(fieldTypes, fieldConsumer);
		}

		@Override
		final public void accept(final String fieldName, final Object fieldValue) {
			addFieldValue(fieldName, fieldValue);
		}

	}

	final static class ForObject extends RecordBuilder implements BiConsumer<String, java.lang.reflect.Field> {

		private final Object record;

		ForObject(final Map<String, FieldTypeInterface> fieldTypes, final FieldConsumer fieldConsumer,
		          final Object record) {
			super(fieldTypes, fieldConsumer);
			this.record = record;
		}

		@Override
		final public void accept(final String fieldName, final java.lang.reflect.Field field) {
			try {
				addFieldValue(fieldName, field.get(record));
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

	}

}
