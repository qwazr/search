/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

package com.qwazr.search.query;

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.field.SmartFieldDefinition;
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldMap;
import com.qwazr.utils.Equalizer;
import com.qwazr.utils.StringUtils;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.ws.rs.NotAcceptableException;
import org.apache.lucene.index.Term;

public abstract class AbstractQuery<T extends AbstractQuery<T>> extends Equalizer.Immutable<T> implements QueryInterface {


    protected AbstractQuery(final Class<T> queryClass) {
        super(queryClass);
    }

    static private boolean fieldCheck(final FieldDefinition field,
                                      final Function<FieldDefinition, Boolean> fieldConsumer,
                                      final Function<SmartFieldDefinition, Boolean> smartFieldConsumer) {

        if (field instanceof SmartFieldDefinition)
            if (smartFieldConsumer.apply((SmartFieldDefinition) field))
                return true;
        return fieldConsumer.apply(field);
    }

    static private String forEachField(final Map<String, FieldDefinition> fields,
                                       final Function<FieldDefinition, Boolean> fieldConsumer,
                                       final Function<SmartFieldDefinition, Boolean> smartFieldConsumer,
                                       final Supplier<String> defaultField) {
        if (fields != null)
            for (final Map.Entry<String, FieldDefinition> entry : fields.entrySet())
                if (fieldCheck(entry.getValue(), fieldConsumer, smartFieldConsumer))
                    return entry.getKey();
        return defaultField.get();
    }

    static protected String getTextField(Map<String, FieldDefinition> fields,
                                         final Supplier<String> defaultField) {
        return forEachField(fields,
            f -> Boolean.FALSE,
            s -> s.getType() == SmartFieldDefinition.Type.TEXT,
            defaultField);
    }

    static protected String getFullTextField(final Map<String, FieldDefinition> fields,
                                             final Supplier<String> defaultField) {
        return forEachField(fields,
            FieldDefinition::hasFullTextAnalyzer,
            s -> Boolean.FALSE,
            defaultField);
    }

    static protected String getDoubleField(final Map<String, FieldDefinition> fields,
                                           final Supplier<String> defaultField) {
        return forEachField(fields,
            f -> Boolean.FALSE,
            s -> s.getType() == SmartFieldDefinition.Type.DOUBLE,
            defaultField);
    }

    static protected String getLongField(final Map<String, FieldDefinition> fields,
                                         final Supplier<String> defaultField) {
        return forEachField(fields,
            f -> Boolean.FALSE,
            s -> s.getType() == SmartFieldDefinition.Type.LONG,
            defaultField);
    }

    static protected String getFloatField(final Map<String, FieldDefinition> fields,
                                          final Supplier<String> defaultField) {
        return forEachField(fields,
            f -> Boolean.FALSE,
            s -> s.getType() == SmartFieldDefinition.Type.FLOAT,
            defaultField);
    }

    static protected String getIntField(final Map<String, FieldDefinition> fields,
                                        final Supplier<String> defaultField) {
        return forEachField(fields,
            f -> Boolean.FALSE,
            s -> s.getType() == SmartFieldDefinition.Type.INTEGER,
            defaultField);
    }

    static FieldTypeInterface getFieldType(final FieldMap fieldMap,
                                           final String genericFieldName,
                                           final String concreteFieldName,
                                           final Object value,
                                           final FieldTypeInterface.ValueType expectedValue) {
        final FieldTypeInterface fieldTypeInterface = fieldMap.getFieldType(genericFieldName, concreteFieldName, value);
        if (expectedValue != null && fieldTypeInterface.getValueType() != expectedValue)
            throw new NotAcceptableException("The field " + (genericFieldName == null ? concreteFieldName : genericFieldName)
                + " has a wrong value type: " + fieldTypeInterface.getValueType() + " -  The expected value type is " + expectedValue + ".");
        return fieldTypeInterface;
    }

    static String resolveFieldName(final FieldMap fieldMap,
                                   final String genericFieldName,
                                   final String concreteFieldName,
                                   final Object value,
                                   final FieldTypeInterface.ValueType expectedValueType,
                                   final FieldTypeInterface.FieldType... fieldTypes) {
        final FieldTypeInterface fieldTypeInterface = getFieldType(fieldMap, genericFieldName, concreteFieldName, value, expectedValueType);
        final FieldTypeInterface.FieldType fieldType = fieldTypeInterface.findFirstOf(fieldTypes);
        if (fieldType == null)
            throw new NotAcceptableException("The field \""
                + (genericFieldName == null ? concreteFieldName : genericFieldName)
                + "\" has not the expected types: " + Arrays.toString(fieldTypes));
        return fieldTypeInterface.resolveFieldName(concreteFieldName, fieldType, expectedValueType);
    }

    static String resolveFacetField(final FieldMap fieldMap,
                                    final String genericFieldName,
                                    final String concreteFieldName) {
        return resolveFieldName(fieldMap, genericFieldName, concreteFieldName,
            StringUtils.EMPTY, FieldTypeInterface.ValueType.textType, FieldTypeInterface.FieldType.facetField);
    }

    static String resolveDocValueField(final FieldMap fieldMap,
                                       final String genericFieldName,
                                       final String concreteFieldName,
                                       final Object value,
                                       final FieldTypeInterface.ValueType valueType) {
        return resolveFieldName(fieldMap, genericFieldName, concreteFieldName, value,
            valueType, FieldTypeInterface.FieldType.docValues);
    }

    static String resolvePointField(final FieldMap fieldMap,
                                    final String genericFieldName,
                                    final String concreteFieldName,
                                    final Object value,
                                    final FieldTypeInterface.ValueType valueType) {
        return resolveFieldName(fieldMap, genericFieldName, concreteFieldName, value,
            valueType, FieldTypeInterface.FieldType.pointField);
    }

    static String resolveIndexTextField(final FieldMap fieldMap,
                                        final String genericFieldName,
                                        final String concreteFieldName,
                                        final Object value) {
        return resolveFieldName(fieldMap, genericFieldName, concreteFieldName, value, null,
            FieldTypeInterface.FieldType.stringField, FieldTypeInterface.FieldType.textField);
    }

    static String resolveFullTextField(final FieldMap fieldMap,
                                       final String genericFieldName,
                                       final String concreteFieldName,
                                       final Object value) {
        return resolveFieldName(fieldMap, genericFieldName, concreteFieldName, value, null,
            FieldTypeInterface.FieldType.textField, FieldTypeInterface.FieldType.stringField);
    }

    static Term resolveIndexTextTerm(final FieldMap fieldMap,
                                     final String genericFieldName,
                                     final String concreteFieldName,
                                     final Object value) {
        final String fieldName = resolveIndexTextField(fieldMap, genericFieldName, concreteFieldName, value);
        return new Term(fieldName, BytesRefUtils.fromAny(value));
    }

    static Term resolveFullTextTerm(final FieldMap fieldMap,
                                    final String genericFieldName,
                                    final String concreteFieldName,
                                    final Object value) {
        final String fieldName = resolveFullTextField(fieldMap, genericFieldName, concreteFieldName, value);
        return new Term(fieldName, BytesRefUtils.fromAny(value));
    }

}

