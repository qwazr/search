/**
 * Copyright 2015 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FieldDefinition {

	public final String analyzer;
	public final Boolean tokenized;
	public final Boolean stored;
	public final Boolean store_termvectors;
	public final Boolean store_termvector_offsets;
	public final Boolean store_termvector_positions;
	public final Boolean store_termvector_payloads;
	public final Boolean omit_norms;
	public final FieldType.NumericType numeric_type;
	public final IndexOptions index_options;
	public final DocValuesType docvalues_type;

	public enum Template {
		DoubleField, FloatField, IntField, LongField, NumericDocValuesField, FloatDocValuesField, DoubleDocValuesField,
		SortedDocValuesField, SortedNumericDocValuesField, SortedDoubleDocValuesField, SortedFloatDocValuesField,
		SortedSetDocValuesField, StoredField, StringField, TextField, FacetField, SortedSetDocValuesFacetField,
		SortedSetMultiDocValuesFacetField;
	}

	public final Template template;

	public FieldDefinition() {
		analyzer = null;
		tokenized = null;
		stored = null;
		store_termvectors = null;
		store_termvector_offsets = null;
		store_termvector_positions = null;
		store_termvector_payloads = null;
		omit_norms = null;
		numeric_type = null;
		index_options = null;
		docvalues_type = null;
		template = null;
	}

	private Number checkNumberType(String fieldName, Object value) {
		if (!(value instanceof Number))
			throw new IllegalArgumentException("Wrong value type for the field: " + fieldName + " - " +
					value.getClass().getSimpleName());
		return (Number) value;
	}

	private BytesRef checkStringBytesRef(Object value) {
		return new BytesRef(value.toString());
	}

	Field getNewField(String fieldName, Object value) {
		if (value == null)
			return null;
		Field field = null;
		Field.Store store = (stored != null && stored) ? Field.Store.YES : Field.Store.NO;
		if (template != null) {
			switch (template) {
				case DoubleField:
					field = new DoubleField(fieldName, checkNumberType(fieldName, value).doubleValue(), store);
					break;
				case FloatField:
					field = new FloatField(fieldName, checkNumberType(fieldName, value).floatValue(), store);
					break;
				case IntField:
					field = new IntField(fieldName, checkNumberType(fieldName, value).intValue(), store);
					break;
				case LongField:
					field = new LongField(fieldName, checkNumberType(fieldName, value).longValue(), store);
					break;
				case NumericDocValuesField:
					field = new NumericDocValuesField(fieldName, checkNumberType(fieldName, value).longValue());
					break;
				case FloatDocValuesField:
					field = new FloatDocValuesField(fieldName, checkNumberType(fieldName, value).floatValue());
					break;
				case DoubleDocValuesField:
					field = new DoubleDocValuesField(fieldName, checkNumberType(fieldName, value).doubleValue());
					break;
				case SortedDocValuesField:
					field = new SortedDocValuesField(fieldName, checkStringBytesRef(value));
					break;
				case SortedNumericDocValuesField:
					field = new SortedNumericDocValuesField(fieldName, checkNumberType(fieldName, value).longValue());
					break;
				case SortedDoubleDocValuesField:
					field = new SortedNumericDocValuesField(fieldName,
							NumericUtils.doubleToSortableLong(checkNumberType(fieldName, value).doubleValue()));
					break;
				case SortedFloatDocValuesField:
					field = new SortedNumericDocValuesField(fieldName,
							NumericUtils.floatToSortableInt(checkNumberType(fieldName, value).floatValue()));
					break;
				case SortedSetDocValuesField:
					field = new SortedSetDocValuesField(fieldName, checkStringBytesRef(value));
					break;
				case StoredField:
					if (value instanceof String)
						field = new StoredField(fieldName, (String) value);
					else if (value instanceof Integer)
						field = new StoredField(fieldName, (int) value);
					else if (value instanceof Long)
						field = new StoredField(fieldName, (long) value);
					else if (value instanceof Float)
						field = new StoredField(fieldName, (float) value);
					else if (value instanceof Double)
						field = new StoredField(fieldName, (double) value);
					break;
				case StringField:
					field = new StringField(fieldName, value.toString(), store);
					break;
				case TextField:
					field = new TextField(fieldName, value.toString(), store);
					break;
				case FacetField:
					field = new FacetField(fieldName, value.toString());
					break;
				case SortedSetDocValuesFacetField:
				case SortedSetMultiDocValuesFacetField:
					field = new SortedSetDocValuesFacetField(fieldName, value.toString());
					break;
			}
		}
		if (field == null) {
			FieldType type = new FieldType();
			if (stored != null)
				type.setStored(stored);
			if (tokenized != null)
				type.setTokenized(tokenized);
			if (store_termvectors != null)
				type.setStoreTermVectors(store_termvectors);
			if (store_termvector_offsets != null)
				type.setStoreTermVectorOffsets(store_termvector_offsets);
			if (store_termvector_positions != null)
				type.setStoreTermVectorPositions(store_termvector_positions);
			if (store_termvector_payloads != null)
				type.setStoreTermVectorPayloads(store_termvector_payloads);
			if (omit_norms != null)
				type.setOmitNorms(omit_norms);
			if (numeric_type != null)
				type.setNumericType(numeric_type);
			if (index_options != null)
				type.setIndexOptions(index_options);
			if (docvalues_type != null)
				type.setDocValuesType(docvalues_type);

			field = new Field(fieldName, value.toString(), type);
		}
		return field;
	}

	public final static Object getValue(IndexableField field) {
		if (field == null)
			return null;
		String s = field.stringValue();
		if (s != null)
			return s;
		Number n = field.numericValue();
		if (n != null)
			return n;
		return null;
	}

	public final static TypeReference<Map<String, FieldDefinition>> MapStringFieldTypeRef =
			new TypeReference<Map<String, FieldDefinition>>() {
			};

	public static Map<String, FieldDefinition> newFieldMap(String jsonString) throws IOException {
		if (StringUtils.isEmpty(jsonString))
			return null;
		return JsonMapper.MAPPER.readValue(jsonString, MapStringFieldTypeRef);
	}
}
