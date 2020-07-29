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
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.qwazr.search.field.FieldTypeInterface;
import java.lang.reflect.Field;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.ws.rs.BadRequestException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.Term;

abstract class RecordBuilder<DOC> {

    private final FieldMap fieldMap;
    private final DocumentBuilder<DOC> documentBuilder;
    private Term termId;

    RecordBuilder(final FieldMap fieldMap, final DocumentBuilder<DOC> documentBuilder) {
        this.fieldMap = fieldMap;
        this.documentBuilder = documentBuilder;
    }

    final Term getTermId() {
        if (termId == null)
            throw new BadRequestException("The primary key \"" + fieldMap.fieldsContext.primaryKey + "\" is missing.");
        return termId;
    }

    final void addRecord(final byte[] sourceBytes) {
        documentBuilder.acceptField(new StoredField(fieldMap.fieldsContext.recordField, sourceBytes));
    }

    // TODO type aware ?
    final void addFieldValue(final String fieldName, final Object fieldValue) {
        if (fieldValue == null)
            return;

        final FieldTypeInterface fieldType = fieldMap.getFieldType(null, fieldName, fieldValue);
        fieldType.dispatch(fieldName, fieldValue, documentBuilder);

        if (fieldName.equals(fieldMap.fieldsContext.primaryKey))
            termId = fieldType.newPrimaryTerm(fieldName, fieldValue);
    }

    final static class ForMap<DOC> extends RecordBuilder<DOC> {

        ForMap(final FieldMap fieldMap, final DocumentBuilder<DOC> documentBuilder) {
            super(fieldMap, documentBuilder);
        }

        final void accept(final String fieldName, final Object fieldValue) {
            addFieldValue(fieldName, fieldValue);
        }

    }

    final static class ForObject<DOC> extends RecordBuilder<DOC> {

        ForObject(final FieldMap fieldMap,
                  final DocumentBuilder<DOC> documentBuilder) {
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

    static class ForJson extends RecordBuilder<Document> {

        private ForJson(final FieldMap fieldMap,
                        final DocumentBuilder<Document> documentBuilder) {
            super(fieldMap, documentBuilder);
        }

        boolean accept(final String fieldName, final JsonNode jsonValue) {
            switch (jsonValue.getNodeType()) {
                case STRING:
                    addFieldValue(fieldName, jsonValue.textValue());
                    return true;
                case NUMBER:
                    addFieldValue(fieldName, jsonValue.numberValue());
                    return true;
                case BOOLEAN:
                    addFieldValue(fieldName, jsonValue.booleanValue());
                    return true;
                case ARRAY:
                    for (final JsonNode element : jsonValue)
                        accept(fieldName, element);
                    return false;
                case OBJECT:
                    jsonValue.fields().forEachRemaining(
                        entry -> accept(fieldName + '.' + entry.getKey(), entry.getValue()));
                    return false;
                default:
                    // Ignored
                    return false;
            }
        }

    }

    static ForJson forJsonOf(final FieldMap fieldMap,
                             final DocumentBuilder<Document> documentBuilder,
                             final SortedMap<String, SortedSet<JsonNodeType>> fieldTypes) {
        if (fieldTypes == null)
            return new ForJson(fieldMap, documentBuilder);
        else
            return new ForJsonWithFieldTypes(fieldMap, documentBuilder, fieldTypes);
    }

    final static class ForJsonWithFieldTypes extends ForJson {

        private final SortedMap<String, SortedSet<JsonNodeType>> fieldTypes;

        private ForJsonWithFieldTypes(final FieldMap fieldMap,
                                      final DocumentBuilder<Document> documentBuilder,
                                      final SortedMap<String, SortedSet<JsonNodeType>> fieldTypes) {
            super(fieldMap, documentBuilder);
            this.fieldTypes = fieldTypes;
        }


        final boolean accept(final String fieldName, final JsonNode jsonValue) {
            if (!super.accept(fieldName, jsonValue))
                return false;
            fieldTypes.computeIfAbsent(fieldName, f -> new TreeSet<>())
                .add(jsonValue.getNodeType());
            return true;
        }
    }

}
