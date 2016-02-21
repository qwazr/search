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
package com.qwazr.search.field;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class FieldConsumer implements BiConsumer<String, Object>, Consumer<Field> {

	private final Map<String, FieldTypeInterface> fieldTypes;

	protected FieldConsumer(Map<String, FieldTypeInterface> fieldTypes) {
		this.fieldTypes = fieldTypes;
	}

	@Override
	final public void accept(String fieldName, Object fieldValue) {
		if (FieldDefinition.ID_FIELD.equals(fieldName))
			return;
		if (fieldValue == null)
			return;
		FieldTypeInterface fieldType = fieldTypes.get(fieldName);
		if (fieldType == null)
			throw new IllegalArgumentException("No field definition for the field: " + fieldName);
		fieldType.fill(fieldValue, this);
	}

	final public static class FieldsDocument extends FieldConsumer {

		final public Document document;

		public FieldsDocument(Map<String, FieldTypeInterface> fieldTypes) {
			super(fieldTypes);
			document = new Document();
		}

		@Override
		final public void accept(Field field) {
			document.add(field);
		}
	}

	final public static class FieldsCollection extends FieldConsumer {

		final private List<Field> fields;

		public FieldsCollection(Map<String, FieldTypeInterface> fieldTypes) {
			super(fieldTypes);
			fields = new ArrayList<Field>();
		}

		@Override
		final public void accept(Field field) {
			fields.add(field);
		}

		final public Field[] toArray() {
			return fields.toArray(new Field[fields.size()]);
		}
	}
}
