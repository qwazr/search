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
package com.qwazr.search.index;

import com.fasterxml.jackson.databind.JsonNode;
import com.qwazr.search.field.FieldTypeInterface;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.Term;

import java.lang.reflect.Field;

abstract class RecordBuilder {

    private final String primaryKey;
    private final DocumentBuilder documentBuilder;
    private final FieldMap fieldMap;

    volatile Term termId;

    RecordBuilder(final FieldMap fieldMap, final DocumentBuilder documentBuilder) {
        this.primaryKey = fieldMap.getPrimaryKey();
        this.fieldMap = fieldMap;
        this.documentBuilder = documentBuilder;
        this.termId = null;
    }

    final void reset() {
        termId = null;
    }

    final void addRecord(final byte[] sourceBytes) {
        documentBuilder.accept(
            null, fieldMap.fieldsContext.recordField,
            new StoredField(fieldMap.fieldsContext.recordField, sourceBytes));
    }

    // TODO type aware !
    final void addFieldValue(final String fieldName, final Object fieldValue) {
        if (fieldValue == null)
            return;

        final FieldTypeInterface fieldType = fieldMap.getFieldType(null, fieldName, fieldValue);
        fieldType.dispatch(fieldName, fieldValue, documentBuilder);

        if (fieldName.equals(primaryKey))
            termId = fieldType.newPrimaryTerm(fieldName, fieldValue);
    }

    final static class ForMap extends RecordBuilder {

        ForMap(final FieldMap fieldMap, final DocumentBuilder documentBuilder) {
            super(fieldMap, documentBuilder);
        }

        final public void accept(final String fieldName, final Object fieldValue) {
            addFieldValue(fieldName, fieldValue);
        }

    }

    final static class ForObject extends RecordBuilder {

        ForObject(final FieldMap fieldMap,
                  final DocumentBuilder documentBuilder) {
            super(fieldMap, documentBuilder);
        }

        final public void accept(final String fieldName,
                                 final Field field,
                                 final Object record) {
            try {
                addFieldValue(fieldName, field.get(record));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }

    final static class ForJson extends RecordBuilder {

        ForJson(final FieldMap fieldMap,
                final DocumentBuilder documentBuilder) {
            super(fieldMap, documentBuilder);
        }

        final public void accept(final String fieldName, final JsonNode jsonValue) {
            switch (jsonValue.getNodeType()) {
                case STRING:
                    addFieldValue(fieldName, jsonValue.textValue());
                    break;
                case NUMBER:
                    addFieldValue(fieldName, jsonValue.numberValue());
                    break;
                case BOOLEAN:
                    addFieldValue(fieldName, jsonValue.booleanValue());
                    break;
                case ARRAY:
                    for (final JsonNode element : jsonValue)
                        accept(fieldName, element);
                case OBJECT:
                    jsonValue.fields().forEachRemaining(
                        entry -> accept(fieldName + '.' + entry.getKey(), entry.getValue()));
                    break;
                default:
                    // Ignored
                    break;
            }

        }

    }

}
