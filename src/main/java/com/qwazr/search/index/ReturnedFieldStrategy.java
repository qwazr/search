/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.search.index;

import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.field.converters.MultiReader;
import com.qwazr.search.field.converters.ValueConverter;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.ConcurrentUtils;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public interface ReturnedFieldStrategy {

    enum Type {
        RECORD, FIELDS, NONE;
    }

    // No parameters =  const and singleton
    None NONE = new None();

    static ReturnedFieldStrategy of(final QueryContextImpl context,
                                    final QueryDefinition queryDefinition,
                                    final Supplier<Set<String>> wildcardSupplier) {
        if (queryDefinition.returnedFields != null) {
            if (queryDefinition.returnedFields.contains("*")) {
                if (!StringUtils.isEmpty(context.fieldMap.recordField))
                    return new Record(context.fieldMap.recordField);
                else
                    return new Fields(context, wildcardSupplier.get());
            }
            if (!queryDefinition.returnedFields.isEmpty())
                return new Fields(context, queryDefinition.returnedFields);
        }
        return NONE;
    }

    void extract(final IndexSearcher searcher, final ResultDocumentBuilder<?> builder) throws IOException;

    Type type();

    /**
     * Strategy used when we use the byte records
     */
    final class Record implements ReturnedFieldStrategy {

        private final String recordField;

        private Record(final String recordField) {
            this.recordField = recordField;
        }


        @Override
        public void extract(final IndexSearcher searcher, final ResultDocumentBuilder<?> builder) throws IOException {
            searcher.doc(builder.scoreDoc().doc, new RecordVisitor(recordField, builder));
        }

        @Override
        public Type type() {
            return Type.RECORD;
        }

        private static class RecordVisitor extends StoredFieldVisitor {

            private final String recordField;
            private final ResultDocumentBuilder<?> builder;

            private RecordVisitor(final String recordField, final ResultDocumentBuilder<?> builder) {
                this.recordField = recordField;
                this.builder = builder;
            }

            @Override
            public Status needsField(final FieldInfo fieldInfo) {
                return recordField.equals(fieldInfo.name) ? Status.YES : Status.NO;
            }

            @Override
            public void binaryField(final FieldInfo fieldInfo, final byte[] value) throws IOException {
                builder.setStoredFieldBytes(recordField, value);
            }
        }
    }

    final class None implements ReturnedFieldStrategy {

        private None() {
        }

        @Override
        public void extract(final IndexSearcher searcher, final ResultDocumentBuilder<?> builder) throws IOException {
        }

        @Override
        public Type type() {
            return Type.NONE;
        }
    }

    /**
     * Strategy used when we have a list of fields to return
     */
    class Fields implements ReturnedFieldStrategy {

        private final Map<String, String> storedFields;
        private final Map<String, ValueConverter<?>> returnedFieldsConverter;

        private Fields(final QueryContextImpl context, final Set<String> returnedFields) {
            this.storedFields = new HashMap<>();
            this.returnedFieldsConverter = new LinkedHashMap<>();
            final MultiReader multiReader = new MultiReader(context.indexReader);
            for (final String fieldName : returnedFields) {
                final FieldTypeInterface fieldType = context.fieldMap.getFieldType(null, fieldName);
                if (fieldType == null)
                    continue;
                final String storedFieldName = fieldType.getStoredFieldName(fieldName);
                if (storedFieldName != null)
                    storedFields.put(storedFieldName, fieldName);
                final ValueConverter<?> converter = fieldType.getConverter(fieldName, multiReader);
                if (converter != null)
                    returnedFieldsConverter.put(fieldName, converter);
            }
        }

        @Override
        public Type type() {
            return Type.FIELDS;
        }

        @Override
        public void extract(final IndexSearcher searcher, final ResultDocumentBuilder<?> builder) throws IOException {
            if (!storedFields.isEmpty()) {
                final FieldVisitor fieldVisitor = new FieldVisitor(storedFields);
                searcher.doc(builder.scoreDoc().doc, fieldVisitor);
                fieldVisitor.apply(builder);
            }
            if (!returnedFieldsConverter.isEmpty())
                ConcurrentUtils.forEachEx(returnedFieldsConverter, builder::setDocValuesField);
        }

        private static class FieldVisitor extends StoredFieldVisitor {

            private final Map<String, String> storedFields;

            private Map<String, StringHolder> stringHolderMap;
            private Map<String, BytesHolder> bytesHolderMap;
            private Map<String, IntHolder> intHolderMap;
            private Map<String, LongHolder> longHolderMap;
            private Map<String, FloatHolder> floatHolderMap;
            private Map<String, DoubleHolder> doubleHolderMap;

            private FieldVisitor(final Map<String, String> storedFields) {
                this.storedFields = storedFields;
            }

            @Override
            public Status needsField(final FieldInfo fieldInfo) {
                return storedFields.containsKey(fieldInfo.name) ? Status.YES : Status.NO;
            }

            private String getReturnedField(String fieldInfoName) {
                return storedFields.get(fieldInfoName);
            }

            @Override
            public void binaryField(final FieldInfo fieldInfo, final byte[] value) {
                if (bytesHolderMap == null)
                    bytesHolderMap = new HashMap<>();
                bytesHolderMap.computeIfAbsent(fieldInfo.name, f -> new BytesHolder()).add(value);
            }

            @Override
            public void stringField(final FieldInfo fieldInfo, final byte[] value) {
                if (stringHolderMap == null)
                    stringHolderMap = new HashMap<>();
                stringHolderMap.computeIfAbsent(fieldInfo.name, f -> new StringHolder())
                    .add(new String(value, StandardCharsets.UTF_8));
            }

            @Override
            public void intField(final FieldInfo fieldInfo, final int value) {
                if (intHolderMap == null)
                    intHolderMap = new HashMap<>();
                intHolderMap.computeIfAbsent(fieldInfo.name, f -> new IntHolder()).add(value);
            }

            @Override
            public void longField(final FieldInfo fieldInfo, final long value) {
                if (longHolderMap == null)
                    longHolderMap = new HashMap<>();
                longHolderMap.computeIfAbsent(fieldInfo.name, f -> new LongHolder()).add(value);
            }

            @Override
            public void floatField(final FieldInfo fieldInfo, final float value) {
                if (floatHolderMap == null)
                    floatHolderMap = new HashMap<>();
                floatHolderMap.computeIfAbsent(fieldInfo.name, f -> new FloatHolder()).add(value);
            }

            @Override
            public void doubleField(final FieldInfo fieldInfo, final double value) {
                if (doubleHolderMap == null)
                    doubleHolderMap = new HashMap<>();
                doubleHolderMap.computeIfAbsent(fieldInfo.name, f -> new DoubleHolder()).add(value);
            }

            public void apply(final ResultDocumentBuilder<?> builder) {
                if (stringHolderMap != null)
                    stringHolderMap.forEach((field, holder) -> holder.apply(getReturnedField(field), builder));
                if (bytesHolderMap != null)
                    bytesHolderMap.forEach((field, holder) -> holder.apply(getReturnedField(field), builder));
                if (intHolderMap != null)
                    intHolderMap.forEach((field, holder) -> holder.apply(getReturnedField(field), builder));
                if (longHolderMap != null)
                    longHolderMap.forEach((field, holder) -> holder.apply(getReturnedField(field), builder));
                if (floatHolderMap != null)
                    floatHolderMap.forEach((field, holder) -> holder.apply(getReturnedField(field), builder));
                if (doubleHolderMap != null)
                    doubleHolderMap.forEach((field, holder) -> holder.apply(getReturnedField(field), builder));
            }
        }


        private static abstract class Holder {

            protected enum State {
                nothing, single, multi;
            }

            protected State state = State.nothing;

            abstract void applySingle(final String field, final ResultDocumentBuilder<?> builder);

            abstract void applyMulti(final String field, final ResultDocumentBuilder<?> builder);

            final void apply(final String field, final ResultDocumentBuilder<?> builder) {
                switch (state) {
                    case single:
                        applySingle(field, builder);
                        break;
                    case multi:
                        applyMulti(field, builder);
                    case nothing:
                    default:
                        break;
                }
            }
        }

        private static abstract class ObjectHolder<T> extends Holder {

            protected T value = null;
            protected List<T> values = null;

            final void add(final T newValue) {
                if (state == State.nothing) {
                    value = newValue;
                    state = State.single;
                } else {
                    if (values == null) {
                        values = new ArrayList<>();
                        values.add(value);
                        state = State.multi;
                    }
                    values.add(newValue);
                }
            }


        }

        final private static class StringHolder extends ObjectHolder<String> {

            @Override
            final void applySingle(final String field, final ResultDocumentBuilder<?> builder) {
                builder.setStoredFieldString(field, value);
            }

            @Override
            final void applyMulti(final String field, final ResultDocumentBuilder<?> builder) {
                builder.setStoredFieldString(field, values);
            }
        }

        final private static class BytesHolder extends ObjectHolder<byte[]> {

            @Override
            final void applySingle(final String field, final ResultDocumentBuilder<?> builder) {
                builder.setStoredFieldBytes(field, value);
            }

            @Override
            final void applyMulti(final String field, final ResultDocumentBuilder<?> builder) {
                builder.setStoredFieldBytes(field, values);
            }
        }

        final private static class IntHolder extends Holder {

            protected int value;
            protected IntList values;

            final void add(final int newValue) {
                if (state == State.nothing) {
                    value = newValue;
                    state = State.single;
                } else {
                    if (values == null) {
                        values = new IntArrayList();
                        values.add(value);
                        state = State.multi;
                    }
                    values.add(newValue);
                }
            }

            @Override
            final void applySingle(final String field, final ResultDocumentBuilder<?> builder) {
                builder.setStoredFieldInteger(field, value);
            }

            @Override
            final void applyMulti(final String field, final ResultDocumentBuilder<?> builder) {
                builder.setStoredFieldInteger(field, values.toIntArray());
            }
        }

        final private static class LongHolder extends Holder {

            protected long value;
            protected LongList values;

            final void add(final long newValue) {
                if (state == State.nothing) {
                    value = newValue;
                    state = State.single;
                } else {
                    if (values == null) {
                        values = new LongArrayList();
                        values.add(value);
                        state = State.multi;
                    }
                    values.add(newValue);
                }
            }

            @Override
            final void applySingle(final String field, final ResultDocumentBuilder<?> builder) {
                builder.setStoredFieldLong(field, value);

            }

            @Override
            final void applyMulti(final String field, final ResultDocumentBuilder<?> builder) {
                builder.setStoredFieldLong(field, values.toLongArray());
            }
        }

        final private static class FloatHolder extends Holder {

            protected float value;
            protected FloatList values;

            final void add(final float newValue) {
                if (state == State.nothing) {
                    value = newValue;
                    state = State.single;
                } else {
                    if (values == null) {
                        values = new FloatArrayList();
                        values.add(value);
                        state = State.multi;
                    }
                    values.add(newValue);
                }
            }

            @Override
            final void applySingle(final String field, final ResultDocumentBuilder<?> builder) {
                builder.setStoredFieldFloat(field, value);
            }

            @Override
            final void applyMulti(final String field, final ResultDocumentBuilder<?> builder) {
                builder.setStoredFieldFloat(field, values.toFloatArray());
            }

        }

        final private static class DoubleHolder extends Holder {

            protected double value;
            protected DoubleList values;

            final void add(final double newValue) {
                if (state == State.nothing) {
                    value = newValue;
                    state = State.single;
                } else {
                    if (values == null) {
                        values = new DoubleArrayList();
                        values.add(value);
                        state = State.multi;
                    }
                    values.add(newValue);
                }
            }

            @Override
            final void applySingle(final String field, final ResultDocumentBuilder<?> builder) {
                builder.setStoredFieldDouble(field, value);
            }

            @Override
            final void applyMulti(final String field, final ResultDocumentBuilder<?> builder) {
                builder.setStoredFieldDouble(field, values.toDoubleArray());
            }

        }
    }


}
