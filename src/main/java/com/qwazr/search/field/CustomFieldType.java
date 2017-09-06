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
import java.util.List;
import java.util.function.Consumer;

final class CustomFieldType extends CustomFieldTypeAbstract.OneField {

	private final Consumer<FieldType>[] typeSetters;

	CustomFieldType(final WildcardMatcher wildcardMatcher, final FieldDefinition definition) {
		super(of(wildcardMatcher, (CustomFieldDefinition) definition).bytesRefConverter(getConverter(definition))
				.termProvider(FieldUtils::newStringTerm)
				.sortFieldProvider(buildSortFieldProvider((CustomFieldDefinition) definition)));
		final CustomFieldDefinition customFieldDefinition = (CustomFieldDefinition) definition;
		typeSetters = buildTypeSetters(customFieldDefinition);
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
		fieldConsumer.accept(fieldName, new CustomField(fieldName, type, value));
	}

	@Override
	public ValueConverter getConverter(String fieldName, MultiReader reader) {
		if (definition == null)
			return null;
		if (definition.template != null) {
			switch (definition.template) {
			case IntDocValuesField:
				return new SingleDVConverter.IntegerDVConverter(reader, fieldName);
			case LongDocValuesField:
				return new SingleDVConverter.LongDVConverter(reader, fieldName);
			case FloatDocValuesField:
				return new SingleDVConverter.FloatDVConverter(reader, fieldName);
			case DoubleDocValuesField:
				return new SingleDVConverter.DoubleDVConverter(reader, fieldName);
			case SortedIntDocValuesField:
				return new MultiDVConverter.IntegerSetDVConverter(reader, fieldName);
			case SortedLongDocValuesField:
				return new MultiDVConverter.LongSetDVConverter(reader, fieldName);
			case SortedFloatDocValuesField:
				return new MultiDVConverter.FloatSetDVConverter(reader, fieldName);
			case SortedDoubleDocValuesField:
				return new MultiDVConverter.DoubleSetDVConverter(reader, fieldName);
			case SortedDocValuesField:
				return new SingleDVConverter.SortedDVConverter(reader, fieldName);
			case SortedSetDocValuesField:
				return new MultiDVConverter.SortedSetDVConverter(reader, fieldName);
			case BinaryDocValuesField:
				return new SingleDVConverter.BinaryDVConverter(reader, fieldName);
			}
		}
		if (definition.docValuesType == null)
			return null;
		switch (definition.docValuesType) {
		case NONE:
			return null;
		case NUMERIC:
			if (definition.numericType == null)
				return null;
			switch (definition.numericType) {
			case INT:
				return new SingleDVConverter.IntegerDVConverter(reader, fieldName);
			case LONG:
				return new SingleDVConverter.LongDVConverter(reader, fieldName);
			case FLOAT:
				return new SingleDVConverter.FloatDVConverter(reader, fieldName);
			case DOUBLE:
				return new SingleDVConverter.DoubleDVConverter(reader, fieldName);
			}
			return null;
		case SORTED_NUMERIC:
			if (definition.numericType == null)
				return null;
			switch (definition.numericType) {
			case INT:
				return new MultiDVConverter.IntegerSetDVConverter(reader, fieldName);
			case LONG:
				return new MultiDVConverter.LongSetDVConverter(reader, fieldName);
			case FLOAT:
				return new MultiDVConverter.FloatSetDVConverter(reader, fieldName);
			case DOUBLE:
				return new MultiDVConverter.DoubleSetDVConverter(reader, fieldName);
			}
			return null;
		case SORTED:
			return new SingleDVConverter.SortedDVConverter(reader, fieldName);
		case SORTED_SET:
			return new MultiDVConverter.SortedSetDVConverter(reader, fieldName);
		case BINARY:
			return new SingleDVConverter.BinaryDVConverter(reader, fieldName);
		}
		return null;
	}

	private static BytesRefUtils.Converter getConverter(final FieldDefinition definition) {
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

	public static FieldTypeInterface build(WildcardMatcher wildcardMatcher, CustomFieldDefinition definition) {
		final FieldDefinition.Template template =
				definition.template == null ? FieldDefinition.Template.NONE : definition.template;
		switch (template) {
		case NONE:
			return new CustomFieldType(wildcardMatcher, definition);
		case DoublePoint:
			return new DoublePointType(wildcardMatcher, definition);
		case FloatPoint:
			return new FloatPointType(wildcardMatcher, definition);
		case IntPoint:
			return new IntPointType(wildcardMatcher, definition);
		case LongPoint:
			return new LongPointType(wildcardMatcher, definition);
		case DoubleField:
			return new DoublePointType(wildcardMatcher, definition);
		case FloatField:
			return new FloatPointType(wildcardMatcher, definition);
		case IntField:
			return new IntPointType(wildcardMatcher, definition);
		case LongField:
			return new LongPointType(wildcardMatcher, definition);
		case LongDocValuesField:
			return new LongDocValuesType(wildcardMatcher, definition);
		case IntDocValuesField:
			return new IntDocValuesType(wildcardMatcher, definition);
		case FloatDocValuesField:
			return new FloatDocValuesType(wildcardMatcher, definition);
		case DoubleDocValuesField:
			return new DoubleDocValuesType(wildcardMatcher, definition);
		case LatLonPoint:
			return new LatLonPointType(wildcardMatcher, definition);
		case Geo3DPoint:
			return new Geo3DPointType(wildcardMatcher, definition);
		case SortedDocValuesField:
			return new SortedDocValuesType(wildcardMatcher, definition);
		case SortedLongDocValuesField:
			return new SortedLongDocValuesType(wildcardMatcher, definition);
		case SortedIntDocValuesField:
			return new SortedIntDocValuesType(wildcardMatcher, definition);
		case SortedDoubleDocValuesField:
			return new SortedDoubleDocValuesType(wildcardMatcher, definition);
		case SortedFloatDocValuesField:
			return new SortedFloatDocValuesType(wildcardMatcher, definition);
		case SortedSetDocValuesField:
			return new SortedSetDocValuesType(wildcardMatcher, definition);
		case BinaryDocValuesField:
			return new BinaryDocValuesType(wildcardMatcher, definition);
		case StoredField:
			return new StoredFieldType(wildcardMatcher, definition);
		case StringField:
			return new StringFieldType(wildcardMatcher, definition);
		case TextField:
			return new TextFieldType(wildcardMatcher, definition);
		case FacetField:
			return new FacetType(wildcardMatcher, definition);
		case IntAssociatedField:
			return new IntAssociationFacetType(wildcardMatcher, definition);
		case FloatAssociatedField:
			return new FloatAssociationFacetType(wildcardMatcher, definition);
		case SortedSetDocValuesFacetField:
			return new SortedSetDocValuesFacetType(wildcardMatcher, definition);
		}
		return null;
	}

}
