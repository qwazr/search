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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.SortedSetSortField;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

import javax.ws.rs.core.Response;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FieldUtils {

	private static Number checkNumberType(String fieldName, Object value) {
		if (!(value instanceof Number))
			throw new IllegalArgumentException(
					"Wrong value type for the field: " + fieldName + " - " + value.getClass().getSimpleName());
		return (Number) value;
	}

	private static BytesRef checkStringBytesRef(Object value) {
		return new BytesRef(value.toString());
	}

	public static final Field newLuceneField(FieldDefinition fieldDef, final String fieldName, final Object value) {
		if (value == null)
			return null;
		String stringValue = null;
		Field field = null;
		Field.Store store = (fieldDef.stored != null && fieldDef.stored) ? Field.Store.YES : Field.Store.NO;
		if (fieldDef.template != null) {
			switch (fieldDef.template) {
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
			case LongDocValuesField:
				field = new NumericDocValuesField(fieldName, checkNumberType(fieldName, value).longValue());
				break;
			case IntDocValuesField:
				field = new NumericDocValuesField(fieldName, checkNumberType(fieldName, value).intValue());
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
			case SortedLongDocValuesField:
				field = new SortedNumericDocValuesField(fieldName, checkNumberType(fieldName, value).longValue());
				break;
			case SortedIntDocValuesField:
				field = new SortedNumericDocValuesField(fieldName, checkNumberType(fieldName, value).intValue());
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
			case BinaryDocValuesField:
				field = new BinaryDocValuesField(fieldName, checkStringBytesRef(value));
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
				else
					field = new StoredField(fieldName, value.toString());
				break;
			case StringField:
				if (StringUtils.isEmpty(stringValue = value.toString()))
					return null;
				field = new StringField(fieldName, stringValue, store);
				break;
			case TextField:
				if (StringUtils.isEmpty(stringValue = value.toString()))
					return null;
				field = new TextField(fieldName, stringValue, store);
				break;
			case FacetField:
				if (StringUtils.isEmpty(stringValue = value.toString()))
					return null;
				field = new FacetField(fieldName, stringValue);
				break;
			case SortedSetDocValuesFacetField:
			case SortedSetMultiDocValuesFacetField:
				if (StringUtils.isEmpty(stringValue = value.toString()))
					return null;
				field = new SortedSetDocValuesFacetField(fieldName, stringValue);
				break;
			}
		}
		if (field == null) {
			FieldType type = new FieldType();
			if (fieldDef.stored != null)
				type.setStored(fieldDef.stored);
			if (fieldDef.tokenized != null)
				type.setTokenized(fieldDef.tokenized);
			if (fieldDef.store_termvectors != null)
				type.setStoreTermVectors(fieldDef.store_termvectors);
			if (fieldDef.store_termvector_offsets != null)
				type.setStoreTermVectorOffsets(fieldDef.store_termvector_offsets);
			if (fieldDef.store_termvector_positions != null)
				type.setStoreTermVectorPositions(fieldDef.store_termvector_positions);
			if (fieldDef.store_termvector_payloads != null)
				type.setStoreTermVectorPayloads(fieldDef.store_termvector_payloads);
			if (fieldDef.omit_norms != null)
				type.setOmitNorms(fieldDef.omit_norms);
			if (fieldDef.numeric_type != null)
				type.setNumericType(fieldDef.numeric_type);
			if (fieldDef.index_options != null)
				type.setIndexOptions(fieldDef.index_options);
			if (fieldDef.docvalues_type != null)
				type.setDocValuesType(fieldDef.docvalues_type);

			try {
				field = new Field(fieldName, value.toString(), type);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Error on field: " + fieldName + " - " + e.getMessage(), e);
			}
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

	public final static SortField getSortField(FieldDefinition fieldDef, String field, boolean reverse)
			throws ServerException {
		if (fieldDef.template == null) {
			if (fieldDef.index_options == null)
				throw new ServerException(Response.Status.BAD_REQUEST,
						"A not indexed field cannot be used in sorting: " + field);
		} else {
			switch (fieldDef.template) {
			case DoubleField:
			case DoubleDocValuesField:
				return new SortField(field, SortField.Type.DOUBLE, reverse);
			case FloatField:
			case FloatDocValuesField:
				return new SortField(field, SortField.Type.FLOAT, reverse);
			case IntField:
			case IntDocValuesField:
				return new SortField(field, SortField.Type.INT, reverse);
			case LongField:
			case LongDocValuesField:
				return new SortField(field, SortField.Type.LONG, reverse);
			case SortedDocValuesField:
				return new SortedSetSortField(field, reverse);
			case SortedDoubleDocValuesField:
				return new SortedNumericSortField(field, SortField.Type.DOUBLE, reverse);
			case SortedFloatDocValuesField:
				return new SortedNumericSortField(field, SortField.Type.FLOAT, reverse);
			case SortedSetDocValuesField:
				return new SortedSetSortField(field, reverse);
			case StringField:
				return new SortField(field, SortField.Type.STRING, reverse);
			case StoredField:
			case TextField:
			case FacetField:
			case SortedSetDocValuesFacetField:
			case SortedSetMultiDocValuesFacetField:
				break;
			}
		}
		throw new ServerException(Response.Status.BAD_REQUEST, "The field cannot be used for sorting: " + field);
	}
}
