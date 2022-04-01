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
import com.qwazr.search.field.SmartFieldDefinition;
import com.qwazr.utils.Equalizer;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

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

}

