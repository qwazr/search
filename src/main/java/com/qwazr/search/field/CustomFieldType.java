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
import org.apache.lucene.document.FieldType;
import org.apache.lucene.search.SortField;

import java.util.Collection;

class CustomFieldType implements FieldTypeInterface {

	private final FieldDefinition fieldDef;

	CustomFieldType(FieldDefinition fieldDefinition) {
		this.fieldDef = fieldDefinition;
	}

	@Override
	public void fillDocument(final String fieldName, final Object value, Document doc) {
		final FieldType type = new FieldType();
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
			if (value instanceof Collection) {
				addCollection(fieldName, type, (Collection) value, doc);
			} else
				doc.add(new CustomField(fieldName, type, value));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Error on field: " + fieldName + " - " + e.getMessage(), e);
		}
	}

	private final void addCollection(String fieldName, FieldType type, Collection<Object> values, Document doc) {
		for (Object value : values)
			fillDocument(fieldName, value, doc);
	}

	@Override
	public final SortField getSortField(String fieldName, QueryDefinition.SortEnum sortEnum) {
		if (fieldDef.index_options == null)
			throw new IllegalArgumentException("A not indexed field cannot be used in sorting: " + fieldName);
		if (fieldName == FieldDefinition.SCORE_FIELD)
			return new SortField(fieldName, SortField.Type.SCORE);
		final boolean reverse = FieldUtils.sortReverse(sortEnum);
		final SortField sortField;
		if (fieldDef.numeric_type != null) {
			switch (fieldDef.numeric_type) {
			case DOUBLE:
				sortField = new SortField(fieldName, SortField.Type.DOUBLE, reverse);
				FieldUtils.sortDoubleMissingValue(sortEnum, sortField);
				break;
			case FLOAT:
				sortField = new SortField(fieldName, SortField.Type.FLOAT, reverse);
				FieldUtils.sortFloatMissingValue(sortEnum, sortField);
				break;
			case INT:
				sortField = new SortField(fieldName, SortField.Type.INT, reverse);
				FieldUtils.sortIntMissingValue(sortEnum, sortField);
				break;
			case LONG:
				sortField = new SortField(fieldName, SortField.Type.LONG, reverse);
				FieldUtils.sortLongMissingValue(sortEnum, sortField);
				break;
			default:
				sortField = new SortField(fieldName, SortField.Type.STRING, reverse);
				FieldUtils.sortStringMissingValue(sortEnum, sortField);
				break;
			}
		} else {
			sortField = new SortField(fieldName, SortField.Type.STRING, reverse);
			FieldUtils.sortStringMissingValue(sortEnum, sortField);
		}
		return sortField;
	}

}
