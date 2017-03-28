/**
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
package com.qwazr.search.field;

import com.qwazr.search.field.Converters.ValueConverter;
import com.qwazr.search.index.FieldConsumer;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public interface FieldTypeInterface {

	void dispatch(final String fieldName, final Object value, final Float boost, final FieldConsumer fieldConsumer);

	default SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
		return null;
	}

	ValueConverter getConverter(final String fieldName, final IndexReader reader) throws IOException;

	Object toTerm(final BytesRef bytesRef);

	FieldDefinition getDefinition();

	void copyTo(final String fieldName, final FieldTypeInterface fieldType);

	static FieldTypeInterface getInstance(final WildcardMatcher wildcardMatcher, final FieldDefinition definition) {
		if (definition.template == null)
			return new CustomFieldType(wildcardMatcher, definition);
		switch (definition.template) {
		case BinaryDocValuesField:
			return new BinaryDocValuesType(wildcardMatcher, definition);
		case DoubleDocValuesField:
			return new DoubleDocValuesType(wildcardMatcher, definition);
		case DoubleField:
		case DoublePoint:
			return new DoublePointType(wildcardMatcher, definition);
		case FloatDocValuesField:
			return new FloatDocValuesType(wildcardMatcher, definition);
		case FloatField:
		case FloatPoint:
			return new FloatPointType(wildcardMatcher, definition);
		case Geo3DPoint:
			return new Geo3DPointType(wildcardMatcher, definition);
		case IntDocValuesField:
			return new IntDocValuesType(wildcardMatcher, definition);
		case IntField:
		case IntPoint:
			return new IntPointType(wildcardMatcher, definition);
		case LatLonPoint:
			return new LatLonPointType(wildcardMatcher, definition);
		case LongDocValuesField:
			return new LongDocValuesType(wildcardMatcher, definition);
		case LongField:
		case LongPoint:
			return new LongPointType(wildcardMatcher, definition);
		case SortedDocValuesField:
			return new SortedDocValuesType(wildcardMatcher, definition);
		case SortedDoubleDocValuesField:
			return new SortedDoubleDocValuesType(wildcardMatcher, definition);
		case SortedFloatDocValuesField:
			return new SortedFloatDocValuesType(wildcardMatcher, definition);
		case SortedIntDocValuesField:
			return new SortedIntDocValuesType(wildcardMatcher, definition);
		case SortedLongDocValuesField:
			return new SortedLongDocValuesType(wildcardMatcher, definition);
		case SortedSetDocValuesField:
			return new SortedSetDocValuesType(wildcardMatcher, definition);
		case FacetField:
			return new FacetType(wildcardMatcher, definition);
		case IntAssociatedField:
			return new IntAssociationFacetType(wildcardMatcher, definition);
		case FloatAssociatedField:
			return new FloatAssociationFacetType(wildcardMatcher, definition);
		case SortedSetDocValuesFacetField:
			return new SortedSetDocValuesFacetType(wildcardMatcher, definition);
		case StoredField:
			return new StoredFieldType(wildcardMatcher, definition);
		case StringField:
			return new StringFieldType(wildcardMatcher, definition);
		case TextField:
			return new TextFieldType(wildcardMatcher, definition);
		case NONE:
			return new CustomFieldType(wildcardMatcher, definition);
		}
		throw new IllegalArgumentException("Unsupported field type");
	}

}
