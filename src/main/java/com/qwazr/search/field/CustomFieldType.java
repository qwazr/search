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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class CustomFieldType extends CustomFieldTypeAbstract.OneField {

	private final Consumer<FieldType>[] typeSetters;

	@FunctionalInterface
	interface SortFieldProvider {

		SortField provide(final String fieldName, final QueryDefinition.SortEnum sortEnum);

	}

	private final SortFieldProvider sortFieldProvider;

	CustomFieldType(final WildcardMatcher wildcardMatcher, final FieldDefinition definition) {
		super(of(wildcardMatcher, (CustomFieldDefinition) definition).bytesRefConverter(getConverter(definition))
				.termProvider(FieldUtils::newStringTerm));
		final CustomFieldDefinition customFieldDefinition = (CustomFieldDefinition) definition;
		typeSetters = buildTypeSetters(customFieldDefinition);
		sortFieldProvider = buildSortFieldProvider(customFieldDefinition);
	}

	private static Consumer<FieldType>[] buildTypeSetters(CustomFieldDefinition definition) {
		final List<Consumer<FieldType>> ts = new ArrayList<>();
		if (definition.stored != null)
			ts.add(type -> type.setStored(definition.stored));
		if (definition.tokenized != null)
			ts.add(type -> type.setTokenized(definition.tokenized));
		if (definition.storeTermVectors != null)
			ts.add(type -> type.setStoreTermVectors(definition.storeTermVectors));
		if (definition.storeTermVectorOffsets != null)
			ts.add(type -> type.setStoreTermVectorOffsets(definition.storeTermVectorOffsets));
		if (definition.storeTermVectorPositions != null)
			ts.add(type -> type.setStoreTermVectorPositions(definition.storeTermVectorPositions));
		if (definition.storeTermVectorPayloads != null)
			ts.add(type -> type.setStoreTermVectorPayloads(definition.storeTermVectorPayloads));
		if (definition.omitNorms != null)
			ts.add(type -> type.setOmitNorms(definition.omitNorms));
		if (definition.numericType != null)
			ts.add(type -> type.setNumericType(definition.numericType));
		if (definition.indexOptions != null)
			ts.add(type -> type.setIndexOptions(definition.indexOptions));
		if (definition.docValuesType != null)
			ts.add(type -> type.setDocValuesType(definition.docValuesType));
		if (definition.dimensionCount != null && definition.dimensionNumBytes != null)
			ts.add(type -> type.setDimensions(definition.dimensionCount, definition.dimensionNumBytes));
		return ts.isEmpty() ? null : ts.toArray(new Consumer[ts.size()]);
	}

	private static SortFieldProvider buildSortFieldProvider(CustomFieldDefinition definition) {
		if (definition.indexOptions == null)
			return null;
		if (definition.numericType != null) {
			switch (definition.numericType) {
			case DOUBLE:
				return (fieldName, sortEnum) -> {
					final SortField sortField = new SortField(fieldName, SortField.Type.DOUBLE,
							SortUtils.sortReverse(sortEnum));
					SortUtils.sortDoubleMissingValue(sortEnum, sortField);
					return sortField;
				};
			case FLOAT:
				return (fieldName, sortEnum) -> {
					final SortField sortField = new SortField(fieldName, SortField.Type.FLOAT,
							SortUtils.sortReverse(sortEnum));
					SortUtils.sortFloatMissingValue(sortEnum, sortField);
					return sortField;
				};
			case INT:
				return (fieldName, sortEnum) -> {
					final SortField sortField = new SortField(fieldName, SortField.Type.INT,
							SortUtils.sortReverse(sortEnum));
					SortUtils.sortIntMissingValue(sortEnum, sortField);
					return sortField;
				};
			case LONG:
				return (fieldName, sortEnum) -> {
					final SortField sortField = new SortField(fieldName, SortField.Type.LONG,
							SortUtils.sortReverse(sortEnum));
					SortUtils.sortLongMissingValue(sortEnum, sortField);
					return sortField;
				};
			default:
				return (fieldName, sortEnum) -> {
					final SortField sortField = new SortField(fieldName, SortField.Type.STRING,
							SortUtils.sortReverse(sortEnum));
					SortUtils.sortStringMissingValue(sortEnum, sortField);
					return sortField;
				};
			}
		} else {
			return (fieldName, sortEnum) -> {
				final SortField sortField = new SortField(fieldName, SortField.Type.STRING,
						SortUtils.sortReverse(sortEnum));
				SortUtils.sortStringMissingValue(sortEnum, sortField);
				return sortField;
			};
		}
	}

	@Override
	final protected void newField(final String fieldName, final Object value, final FieldConsumer fieldConsumer) {
		final FieldType type = new FieldType();
		if (typeSetters != null)
			for (Consumer<FieldType> ts : typeSetters)
				ts.accept(type);
		fieldConsumer.accept(fieldName, new CustomField(fieldName, type, value));
	}

	@Override
	final public SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
		if (sortFieldProvider == null)
			throw new IllegalArgumentException("A not indexed field cannot be used in sorting: " + fieldName);
		if (fieldName == FieldDefinition.SCORE_FIELD)
			return new SortField(fieldName, SortField.Type.SCORE);
		return sortFieldProvider.provide(fieldName, sortEnum);
	}

	static BytesRefUtils.Converter getConverter(final FieldDefinition definition) {
		if (definition == null || !(definition instanceof CustomFieldDefinition))
			return null;
		final CustomFieldDefinition customDef = (CustomFieldDefinition) definition;
		if (customDef.numericType == null)
			return BytesRefUtils.Converter.STRING;
		switch (customDef.numericType) {
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
