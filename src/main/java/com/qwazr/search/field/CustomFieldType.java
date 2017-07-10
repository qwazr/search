/*
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

import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldConsumer;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.search.SortField;

class CustomFieldType extends FieldTypeAbstract<CustomFieldDefinition> {

	CustomFieldType(final WildcardMatcher wildcardMatcher, final FieldDefinition definition) {
		super(wildcardMatcher, (CustomFieldDefinition) definition, getConverter(definition));
	}

	@Override
	final public void fillValue(final String fieldName, final Object value, final Float boost,
			final FieldConsumer consumer) {
		final FieldType type = new FieldType();
		if (definition.stored != null)
			type.setStored(definition.stored);
		if (definition.tokenized != null)
			type.setTokenized(definition.tokenized);
		if (definition.store_termvectors != null)
			type.setStoreTermVectors(definition.store_termvectors);
		if (definition.store_termvector_offsets != null)
			type.setStoreTermVectorOffsets(definition.store_termvector_offsets);
		if (definition.store_termvector_positions != null)
			type.setStoreTermVectorPositions(definition.store_termvector_positions);
		if (definition.store_termvector_payloads != null)
			type.setStoreTermVectorPayloads(definition.store_termvector_payloads);
		if (definition.omit_norms != null)
			type.setOmitNorms(definition.omit_norms);
		if (definition.numeric_type != null)
			type.setNumericType(definition.numeric_type);
		if (definition.index_options != null)
			type.setIndexOptions(definition.index_options);
		if (definition.docvalues_type != null)
			type.setDocValuesType(definition.docvalues_type);
		consumer.accept(fieldName, new CustomField(fieldName, type, value), boost);
	}

	@Override
	final public SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
		if (definition.index_options == null)
			throw new IllegalArgumentException("A not indexed field cannot be used in sorting: " + fieldName);
		if (fieldName == FieldDefinition.SCORE_FIELD)
			return new SortField(fieldName, SortField.Type.SCORE);
		final boolean reverse = SortUtils.sortReverse(sortEnum);
		final SortField sortField;
		if (definition.numeric_type != null) {
			switch (definition.numeric_type) {
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

	static BytesRefUtils.Converter getConverter(final FieldDefinition definition) {
		if (definition == null || !(definition instanceof CustomFieldDefinition))
			return null;
		final CustomFieldDefinition customDef = (CustomFieldDefinition) definition;
		if (customDef.numeric_type == null)
			return BytesRefUtils.Converter.STRING;
		switch (customDef.numeric_type) {
		case DOUBLE:
			return BytesRefUtils.Converter.DOUBLE;
		case FLOAT:
			return BytesRefUtils.Converter.FLOAT;
		case INT:
			return BytesRefUtils.Converter.INT;
		case LONG:
			return BytesRefUtils.Converter.LONG;
		default:
			return BytesRefUtils.Converter.STRING;
		}

	}
}
