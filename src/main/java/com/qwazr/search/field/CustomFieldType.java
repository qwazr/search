/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

import com.qwazr.search.field.Converters.MultiDVConverter;
import com.qwazr.search.field.Converters.MultiReader;
import com.qwazr.search.field.Converters.SingleDVConverter;
import com.qwazr.search.field.Converters.ValueConverter;
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldConsumer;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.search.SortField;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

final class CustomFieldType extends CustomFieldTypeAbstract.OneField {

	private final List<Consumer<FieldType>> typeSetters;

	CustomFieldType(final String genericFieldName, final WildcardMatcher wildcardMatcher,
			final FieldDefinition definition) {
		super(of(genericFieldName, wildcardMatcher, (CustomFieldDefinition) definition).bytesRefConverter(
				getConverter(definition))
				.termProvider(FieldUtils::newStringTerm)
				.sortFieldProvider(buildSortFieldProvider((CustomFieldDefinition) definition)));
		final CustomFieldDefinition customFieldDefinition = (CustomFieldDefinition) definition;
		typeSetters = buildTypeSetters(customFieldDefinition);
	}

	private static List<Consumer<FieldType>> buildTypeSetters(CustomFieldDefinition definition) {
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
		return ts;
	}

	private static SortFieldProvider buildSortFieldProvider(CustomFieldDefinition definition) {
		if (definition.indexOptions == null)
			return null;
		if (definition.numericType == null) {
			return (fieldName, sortEnum) -> {
				if (FieldDefinition.SCORE_FIELD.equals(fieldName))
					return new SortField(fieldName, SortField.Type.SCORE);
				final SortField sortField =
						new SortField(fieldName, SortField.Type.STRING, SortUtils.sortReverse(sortEnum));
				SortUtils.sortStringMissingValue(sortEnum, sortField);
				return sortField;
			};
		}
		switch (definition.numericType) {
		case DOUBLE:
			return (fieldName, sortEnum) -> {
				if (FieldDefinition.SCORE_FIELD.equals(fieldName))
					return new SortField(fieldName, SortField.Type.SCORE);
				final SortField sortField =
						new SortField(fieldName, SortField.Type.DOUBLE, SortUtils.sortReverse(sortEnum));
				SortUtils.sortDoubleMissingValue(sortEnum, sortField);
				return sortField;
			};
		case FLOAT:
			return (fieldName, sortEnum) -> {
				if (FieldDefinition.SCORE_FIELD.equals(fieldName))
					return new SortField(fieldName, SortField.Type.SCORE);
				final SortField sortField =
						new SortField(fieldName, SortField.Type.FLOAT, SortUtils.sortReverse(sortEnum));
				SortUtils.sortFloatMissingValue(sortEnum, sortField);
				return sortField;
			};
		case INT:
			return (fieldName, sortEnum) -> {
				if (FieldDefinition.SCORE_FIELD.equals(fieldName))
					return new SortField(fieldName, SortField.Type.SCORE);
				final SortField sortField =
						new SortField(fieldName, SortField.Type.INT, SortUtils.sortReverse(sortEnum));
				SortUtils.sortIntMissingValue(sortEnum, sortField);
				return sortField;
			};
		case LONG:
			return (fieldName, sortEnum) -> {
				if (FieldDefinition.SCORE_FIELD.equals(fieldName))
					return new SortField(fieldName, SortField.Type.SCORE);
				final SortField sortField =
						new SortField(fieldName, SortField.Type.LONG, SortUtils.sortReverse(sortEnum));
				SortUtils.sortLongMissingValue(sortEnum, sortField);
				return sortField;
			};
		default:
			return null;
		}
	}

	@Override
	final protected void newField(final String fieldName, final Object value, final FieldConsumer fieldConsumer) {
		final FieldType type = new FieldType();
		if (typeSetters != null)
			for (Consumer<FieldType> ts : typeSetters)
				ts.accept(type);
		fieldConsumer.accept(genericFieldName, fieldName, new CustomField(fieldName, type, value));
	}

	private final static EnumMap<FieldType.LegacyNumericType, ValueConverter.Supplier> legacySingleTypeValueConverters;
	private final static EnumMap<FieldType.LegacyNumericType, ValueConverter.Supplier> legacyMultiTypeValueConverters;

	static {
		legacySingleTypeValueConverters = new EnumMap<>(FieldType.LegacyNumericType.class);
		legacySingleTypeValueConverters.put(FieldType.LegacyNumericType.DOUBLE,
				SingleDVConverter.DoubleDVConverter::new);
		legacySingleTypeValueConverters.put(FieldType.LegacyNumericType.FLOAT, SingleDVConverter.FloatDVConverter::new);
		legacySingleTypeValueConverters.put(FieldType.LegacyNumericType.INT, SingleDVConverter.IntegerDVConverter::new);
		legacySingleTypeValueConverters.put(FieldType.LegacyNumericType.LONG, SingleDVConverter.LongDVConverter::new);

		legacyMultiTypeValueConverters = new EnumMap<>(FieldType.LegacyNumericType.class);
		legacyMultiTypeValueConverters.put(FieldType.LegacyNumericType.DOUBLE,
				MultiDVConverter.DoubleSetDVConverter::new);
		legacyMultiTypeValueConverters.put(FieldType.LegacyNumericType.FLOAT,
				MultiDVConverter.FloatSetDVConverter::new);
		legacyMultiTypeValueConverters.put(FieldType.LegacyNumericType.INT,
				MultiDVConverter.IntegerSetDVConverter::new);
		legacyMultiTypeValueConverters.put(FieldType.LegacyNumericType.LONG, MultiDVConverter.LongSetDVConverter::new);
	}

	@Override
	public ValueConverter getConverter(final String field, final MultiReader reader) {
		if (definition == null)
			return null;
		if (definition.template != null)
			return definition.template.getConverter(reader, field);
		if (definition.docValuesType == null)
			return null;
		switch (definition.docValuesType) {
		case NONE:
			return null;
		case NUMERIC:
			if (definition.numericType == null)
				return null;
			return legacySingleTypeValueConverters.getOrDefault(definition.numericType, ValueConverter.NullSupplier)
					.getConverter(reader, field);
		case SORTED_NUMERIC:
			if (definition.numericType == null)
				return null;
			return legacyMultiTypeValueConverters.getOrDefault(definition.numericType, ValueConverter.NullSupplier)
					.getConverter(reader, field);
		case SORTED:
			return new SingleDVConverter.SortedDVConverter(reader, field);
		case SORTED_SET:
			return new MultiDVConverter.SortedSetDVConverter(reader, field);
		case BINARY:
			return new SingleDVConverter.BinaryDVConverter(reader, field);
		}
		return null;
	}

	private final static EnumMap<FieldType.LegacyNumericType, BytesRefUtils.Converter> legacyTypeConverterSet;

	static {
		legacyTypeConverterSet = new EnumMap<>(FieldType.LegacyNumericType.class);
		legacyTypeConverterSet.put(FieldType.LegacyNumericType.DOUBLE, BytesRefUtils.Converter.DOUBLE);
		legacyTypeConverterSet.put(FieldType.LegacyNumericType.FLOAT, BytesRefUtils.Converter.FLOAT);
		legacyTypeConverterSet.put(FieldType.LegacyNumericType.INT, BytesRefUtils.Converter.INT);
		legacyTypeConverterSet.put(FieldType.LegacyNumericType.LONG, BytesRefUtils.Converter.LONG);
	}

	private static BytesRefUtils.Converter getConverter(final FieldDefinition definition) {
		if (!(definition instanceof CustomFieldDefinition))
			return null;
		final CustomFieldDefinition customDef = (CustomFieldDefinition) definition;
		return legacyTypeConverterSet.getOrDefault(customDef.numericType, BytesRefUtils.Converter.STRING);
	}

}
