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

import com.qwazr.search.field.converters.MultiDVConverter;
import com.qwazr.search.field.converters.MultiReader;
import com.qwazr.search.field.converters.SingleDVConverter;
import com.qwazr.search.field.converters.ValueConverter;
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldConsumer;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.search.SortField;

import java.util.ArrayList;
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
        if (definition.indexOptions != null)
            ts.add(type -> type.setIndexOptions(definition.indexOptions));
        if (definition.docValuesType != null)
            ts.add(type -> type.setDocValuesType(definition.docValuesType));
        if (definition.indexDimensionCount != null && definition.dataDimensionCount != null &&
                definition.dimensionNumBytes != null)
            ts.add(type -> type.setDimensions(definition.dataDimensionCount, definition.indexDimensionCount,
                    definition.dimensionNumBytes));
        if (definition.attributes != null)
            ts.add(type -> definition.attributes.forEach(type::putAttribute));
        return ts;
    }

    private static SortFieldProvider buildSortFieldProvider(CustomFieldDefinition definition) {
        if (definition.indexOptions == null)
            return null;
        return (fieldName, sortEnum) -> {
            if (FieldDefinition.SCORE_FIELD.equals(fieldName))
                return new SortField(fieldName, SortField.Type.SCORE);
            final SortField sortField =
                    new SortField(fieldName, SortField.Type.STRING, SortUtils.sortReverse(sortEnum));
            SortUtils.sortStringMissingValue(sortEnum, sortField);
            return sortField;
        };
    }

    @Override
    final protected void newField(final String fieldName, final Object value, final FieldConsumer fieldConsumer) {
        final FieldType type = new FieldType();
        if (typeSetters != null)
            for (Consumer<FieldType> ts : typeSetters)
                ts.accept(type);
        fieldConsumer.accept(genericFieldName, fieldName, new CustomField(fieldName, type, value));
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
                return new SingleDVConverter.LongDVConverter(reader, field);
            case SORTED_NUMERIC:
                return new SingleDVConverter.LongDVConverter(reader, field);
            case SORTED:
                return new SingleDVConverter.SortedDVConverter(reader, field);
            case SORTED_SET:
                return new MultiDVConverter.SortedSetDVConverter(reader, field);
            case BINARY:
                return new SingleDVConverter.BinaryDVConverter(reader, field);
        }
        return null;
    }

    private static BytesRefUtils.Converter getConverter(final FieldDefinition definition) {
        if (!(definition instanceof CustomFieldDefinition))
            return null;
        final CustomFieldDefinition customDef = (CustomFieldDefinition) definition;
        if (customDef.docValuesType == null)
            return BytesRefUtils.Converter.STRING;
        switch (customDef.docValuesType) {
            case NONE:
                return null;
            case NUMERIC:
            case SORTED_NUMERIC:
                return BytesRefUtils.Converter.LONG;
            case BINARY:
                return BytesRefUtils.Converter.BYTESREF;
            case SORTED_SET:
            default:
                return BytesRefUtils.Converter.STRING;
        }
    }

}
