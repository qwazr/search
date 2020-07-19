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
 **/
package com.qwazr.search.index;

import com.qwazr.binder.FieldMapWrapper;
import com.qwazr.binder.setter.FieldSetter;
import com.qwazr.search.field.converters.ValueConverter;
import com.qwazr.server.ServerException;
import com.qwazr.utils.SerializationUtils;
import org.apache.lucene.search.ScoreDoc;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ResultDocumentObject<T> extends ResultDocumentAbstract {

    final public T record;

    private ResultDocumentObject(final ResultDocumentBuilder.Base<?> builder,
                                 final T record) {
        super(builder);
        this.record = record;
    }

    public ResultDocumentObject(final ResultDocumentAbstract resultDocument, final T record) {
        super(resultDocument);
        this.record = record;
    }

    public T getRecord() {
        return record;
    }

    final static class ForFields<T> extends ResultDocumentBuilder.Base<ResultDocumentObject<T>> {

        private final T record;
        private final Map<String, FieldSetter> fieldMap;


        ForFields(final int pos, final ScoreDoc scoreDoc, final FieldMapWrapper<T> wrapper) {
            super(pos, scoreDoc);
            try {
                this.record = wrapper.constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw ServerException.of(e);
            }
            this.fieldMap = wrapper.fieldMap;
        }

        @Override
        public final ResultDocumentObject<T> build() {
            return new ResultDocumentObject<>(this, record);
        }

        @Override
        public final void setDocValuesField(final String fieldName, final ValueConverter<?> converter) {
            final FieldSetter fieldSetter = fieldMap.get(fieldName);
            if (fieldSetter == null)
                throw new ServerException("Unknown field " + fieldName + " for class " + record.getClass());
            try {
                converter.fill(record, fieldSetter, scoreDoc.doc);
            } catch (IOException e) {
                throw new ServerException(Response.Status.INTERNAL_SERVER_ERROR,
                    "I/O error on field " + fieldName + " for class " + record.getClass() + ": " + e.getMessage(), e);
            }
        }

        private FieldSetter checkFieldSetter(final String fieldName) {
            final FieldSetter fieldSetter = fieldMap.get(fieldName);
            if (fieldSetter == null)
                throw new ServerException("Unknown field " + fieldName + " for class " + record.getClass());
            return fieldSetter;
        }

        @Override
        public final void setStoredFieldString(final String fieldName, final String value) {
            checkFieldSetter(fieldName).fromString(value, record);
        }

        @Override
        public final void setStoredFieldString(final String fieldName, final List<String> values) {
            checkFieldSetter(fieldName).fromCollection(String.class, values, record);
        }

        @Override
        public final void setStoredFieldBytes(final String fieldName, final byte[] value) {
            setStoredFieldBytes(fieldName, Collections.singletonList(value));
        }

        @Override
        public final void setStoredFieldBytes(final String fieldName, final List<byte[]> values) {
            final FieldSetter fieldSetter = checkFieldSetter(fieldName);
            final Class<?> fieldType = fieldSetter.getType();
            if (fieldType.isArray()) {
                final Class<?> fieldComponentType = fieldType.getComponentType();
                final byte[] data = values.get(0);
                if (fieldComponentType == byte.class) {
                    fieldSetter.setValue(record, data);
                } else if (fieldComponentType == Byte.class) {
                    final Byte[] boxedData = new Byte[data.length];
                    for (int i = 0; i != data.length; ++i) {
                        boxedData[i] = data[i];
                    }
                    fieldSetter.setValue(record, boxedData);
                }
            } else if (Serializable.class.isAssignableFrom(fieldType)) {
                try {
                    fieldSetter.set(record, SerializationUtils.fromExternalizorBytes(values.get(0),
                        (Class<? extends Serializable>) fieldType));
                } catch (IOException | ReflectiveOperationException e) {
                    throw ServerException.of("Deserialization failure " + fieldName + " for class " + record.getClass(),
                        e);
                }
            } else
                fieldSetter.setValue(record, values);
        }

        @Override
        public final void setStoredFieldInteger(final String fieldName, final int value) {
            checkFieldSetter(fieldName).fromInteger(value, record);
        }

        @Override
        public final void setStoredFieldInteger(final String fieldName, final int[] values) {
            checkFieldSetter(fieldName).fromInteger(values, record);
        }

        @Override
        public final void setStoredFieldLong(final String fieldName, final long value) {
            checkFieldSetter(fieldName).fromLong(value, record);
        }

        @Override
        public final void setStoredFieldLong(final String fieldName, final long[] values) {
            checkFieldSetter(fieldName).fromLong(values, record);
        }

        @Override
        public final void setStoredFieldFloat(final String fieldName, final float value) {
            checkFieldSetter(fieldName).fromFloat(value, record);
        }

        @Override
        public final void setStoredFieldFloat(final String fieldName, final float[] values) {
            checkFieldSetter(fieldName).fromFloat(values, record);
        }

        @Override
        public final void setStoredFieldDouble(final String fieldName, final double value) {
            checkFieldSetter(fieldName).fromDouble(value, record);
        }

        @Override
        public final void setStoredFieldDouble(final String fieldName, final double[] values) {
            checkFieldSetter(fieldName).fromDouble(values, record);
        }

    }

    final static class ForRecord<T> extends ResultDocumentBuilder.Base<ResultDocumentObject<T>> {

        private final Class<T> recordClass;
        private T record;

        ForRecord(final int pos, final ScoreDoc scoreDoc, final Class<T> recordClass) {
            super(pos, scoreDoc);
            this.recordClass = recordClass;
        }

        @Override
        final public void setStoredFieldBytes(final String fieldName, final byte[] value) {
            try {
                record = SerializationUtils.fromExternalizorBytes(value, (Class<? extends Serializable>) recordClass);
            } catch (ReflectiveOperationException | IOException e) {
                throw ServerException.of("Deserialization failure " + fieldName + " for class " + recordClass.getName(), e);
            }
        }

        @Override
        final public ResultDocumentObject<T> build() {
            return new ResultDocumentObject<>(this, record);
        }
    }

    final static class ForNone<T> extends ResultDocumentBuilder.Base<ResultDocumentObject<T>> {

        ForNone(final int pos, final ScoreDoc scoreDoc) {
            super(pos, scoreDoc);
        }

        @Override
        final public ResultDocumentObject<T> build() {
            return new ResultDocumentObject<>(this, null);
        }
    }
}
