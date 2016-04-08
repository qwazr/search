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
import org.apache.lucene.document.FieldType;
import org.apache.lucene.search.SortField;

class CustomFieldType extends FieldTypeAbstract {

	CustomFieldType(final String fieldName, final FieldDefinition fieldDefinition) {
		super(fieldName, fieldDefinition);
	}

	@Override
	final public void fillValue(final Object value, final FieldConsumer consumer) {
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
		consumer.accept(new CustomField(fieldName, type, value));
	}

	@Override
	final public SortField getSortField(final QueryDefinition.SortEnum sortEnum) {
		if (fieldDef.index_options == null)
			throw new IllegalArgumentException("A not indexed field cannot be used in sorting: " + fieldName);
		if (fieldName == FieldDefinition.SCORE_FIELD)
			return new SortField(fieldName, SortField.Type.SCORE);
		final boolean reverse = SortUtils.sortReverse(sortEnum);
		final SortField sortField;
		if (fieldDef.numeric_type != null) {
			switch (fieldDef.numeric_type) {
			case DOUBLE:
				sortField = new SortField(fieldName, SortField.Type.DOUBLE, reverse);
				SortUtils.sortDoubleMissingValue(sortEnum, sortField);
				break;
			case FLOAT:
				sortField = new SortField(fieldName, SortField.Type.FLOAT, reverse);
				SortUtils.sortFloatMissingValue(sortEnum, sortField);
				break;
			case INT:
				sortField = new SortField(fieldName, SortField.Type.INT, reverse);
				SortUtils.sortIntMissingValue(sortEnum, sortField);
				break;
			case LONG:
				sortField = new SortField(fieldName, SortField.Type.LONG, reverse);
				SortUtils.sortLongMissingValue(sortEnum, sortField);
				break;
			default:
				sortField = new SortField(fieldName, SortField.Type.STRING, reverse);
				SortUtils.sortStringMissingValue(sortEnum, sortField);
				break;
			}
		} else {
			sortField = new SortField(fieldName, SortField.Type.STRING, reverse);
			SortUtils.sortStringMissingValue(sortEnum, sortField);
		}
		return sortField;
	}

}
