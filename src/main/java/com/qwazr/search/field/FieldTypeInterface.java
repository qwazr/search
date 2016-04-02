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

import com.qwazr.search.index.FieldConsumer;
import com.qwazr.search.index.QueryDefinition;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.search.SortField;

import java.io.IOException;

public interface FieldTypeInterface {

	void fill(final Object value, final FieldConsumer fieldConsumer);

	SortField getSortField(final QueryDefinition.SortEnum sortEnum);

	ValueConverter getConverter(final LeafReader reader) throws IOException;

	static FieldTypeInterface getInstance(final String fieldName, final FieldDefinition fieldDefinition) {
		if (fieldDefinition.template == null)
			return new CustomFieldType(fieldName, fieldDefinition);
		switch (fieldDefinition.template) {
		case BinaryDocValuesField:
			return new BinaryDocValuesType(fieldName, fieldDefinition);
		case DoubleDocValuesField:
			return new DoubleDocValuesType(fieldName, fieldDefinition);
		case DoubleField:
			return new DoubleFieldType(fieldName, fieldDefinition);
		case FloatDocValuesField:
			return new FloatDocValuesType(fieldName, fieldDefinition);
		case FloatField:
			return new FloatFieldType(fieldName, fieldDefinition);
		case IntDocValuesField:
			return new IntDocValuesType(fieldName, fieldDefinition);
		case IntField:
			return new IntFieldType(fieldName, fieldDefinition);
		case LongDocValuesField:
			return new LongDocValuesType(fieldName, fieldDefinition);
		case LongField:
			return new LongFieldType(fieldName, fieldDefinition);
		case SortedDocValuesField:
			return new SortedDocValuesType(fieldName, fieldDefinition);
		case SortedDoubleDocValuesField:
			return new SortedDoubleDocValuesType(fieldName, fieldDefinition);
		case SortedFloatDocValuesField:
			return new SortedFloatDocValuesType(fieldName, fieldDefinition);
		case SortedIntDocValuesField:
			return new SortedIntDocValuesType(fieldName, fieldDefinition);
		case SortedLongDocValuesField:
			return new SortedLongDocValuesType(fieldName, fieldDefinition);
		case SortedSetDocValuesField:
			return new SortedSetDocValuesType(fieldName, fieldDefinition);
		case FacetField:
		case SortedSetDocValuesFacetField:
		case MultiFacetField:
		case SortedSetMultiDocValuesFacetField:
			return new SortedSetDocValuesFacetType(fieldName, fieldDefinition);
		case StoredField:
			return new StoredFieldType(fieldName, fieldDefinition);
		case StringField:
			return new StringFieldType(fieldName, fieldDefinition);
		case TextField:
			return new TextFieldType(fieldName, fieldDefinition);
		case NONE:
			return new CustomFieldType(fieldName, fieldDefinition);
		}
		throw new IllegalArgumentException("Unsupported field type");
	}

}
