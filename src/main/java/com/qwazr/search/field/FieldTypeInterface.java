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

import com.qwazr.search.index.QueryDefinition;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.SortField;

interface FieldTypeInterface {

	void fillDocument(final String fieldName, final Object value, Document doc);

	SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum);

	static FieldTypeInterface getInstance(FieldDefinition fieldDefinition) {
		if (fieldDefinition.template == null)
			return new CustomFieldType(fieldDefinition);
		switch (fieldDefinition.template) {
		case BinaryDocValuesField:
			return new BinaryDocValuesType();
		case DoubleDocValuesField:
			return new DoubleDocValuesType();
		case DoubleField:
			return new DoubleFieldType(fieldDefinition);
		case FloatDocValuesField:
			return new FloatDocValuesType();
		case FloatField:
			return new FloatFieldType(fieldDefinition);
		case IntDocValuesField:
			return new IntDocValuesType();
		case IntField:
			return new IntFieldType(fieldDefinition);
		case LongDocValuesField:
			return new LongDocValuesType();
		case LongField:
			return new LongFieldType(fieldDefinition);
		case SortedDocValuesField:
			return new SortedDocValuesType();
		case SortedDoubleDocValuesField:
			return new SortedDoubleDocValuesType();
		case SortedFloatDocValuesField:
			return new SortedFloatDocValuesType();
		case SortedIntDocValuesField:
			return new SortedIntDocValuesType();
		case SortedLongDocValuesField:
			return new SortedLongDocValuesType();
		case SortedSetDocValuesField:
			return new SortedSetDocValuesType();
		case FacetField:
		case SortedSetDocValuesFacetField:
		case MultiFacetField:
		case SortedSetMultiDocValuesFacetField:
			return new SortedSetDocValuesFacetType();
		case StoredField:
			return new StoredFieldType();
		case StringField:
			return new StringFieldType(fieldDefinition);
		case TextField:
			return new TextFieldType(fieldDefinition);
		}
		throw new IllegalArgumentException("Unsupported field type");
	}

}
