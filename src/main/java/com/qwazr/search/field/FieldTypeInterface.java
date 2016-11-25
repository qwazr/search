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

import com.qwazr.search.field.Converters.ValueConverter;
import com.qwazr.search.index.FieldConsumer;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.QueryDefinition;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public interface FieldTypeInterface {

	void dispatch(final String fieldName, final Object value, final FieldConsumer fieldConsumer);

	SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum);

	ValueConverter getConverter(final String fieldName, final IndexReader reader) throws IOException;

	Object toTerm(final BytesRef bytesRef);

	static FieldTypeInterface getInstance(final FieldMap.Item fieldMapItem) {
		if (fieldMapItem.definition.template == null)
			return new CustomFieldType(fieldMapItem);
		switch (fieldMapItem.definition.template) {
		case BinaryDocValuesField:
			return new BinaryDocValuesType(fieldMapItem);
		case DoubleDocValuesField:
			return new DoubleDocValuesType(fieldMapItem);
		case DoubleField:
		case DoublePoint:
			return new DoublePointType(fieldMapItem);
		case FloatDocValuesField:
			return new FloatDocValuesType(fieldMapItem);
		case FloatField:
		case FloatPoint:
			return new FloatPointType(fieldMapItem);
		case GeoPoint:
			return new GeoPointType(fieldMapItem);
		case Geo3DPoint:
			return new Geo3DPointType(fieldMapItem);
		case IntDocValuesField:
			return new IntDocValuesType(fieldMapItem);
		case IntField:
		case IntPoint:
			return new IntPointType(fieldMapItem);
		case LongDocValuesField:
			return new LongDocValuesType(fieldMapItem);
		case LongField:
		case LongPoint:
			return new LongPointType(fieldMapItem);
		case SortedDocValuesField:
			return new SortedDocValuesType(fieldMapItem);
		case SortedDoubleDocValuesField:
			return new SortedDoubleDocValuesType(fieldMapItem);
		case SortedFloatDocValuesField:
			return new SortedFloatDocValuesType(fieldMapItem);
		case SortedIntDocValuesField:
			return new SortedIntDocValuesType(fieldMapItem);
		case SortedLongDocValuesField:
			return new SortedLongDocValuesType(fieldMapItem);
		case SortedSetDocValuesField:
			return new SortedSetDocValuesType(fieldMapItem);
		case FacetField:
		case SortedSetDocValuesFacetField:
		case MultiFacetField:
		case SortedSetMultiDocValuesFacetField:
			return new SortedSetDocValuesFacetType(fieldMapItem);
		case StoredField:
			return new StoredFieldType(fieldMapItem);
		case StringField:
			return new StringFieldType(fieldMapItem);
		case TextField:
			return new TextFieldType(fieldMapItem);
		case NONE:
			return new CustomFieldType(fieldMapItem);
		}
		throw new IllegalArgumentException("Unsupported field type");
	}

}
