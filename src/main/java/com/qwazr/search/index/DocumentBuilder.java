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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public interface DocumentBuilder {

    void reset();

    void accept(final String genericFieldName, final String concreteFieldName, final Field field);

    final class ForLuceneDocument implements DocumentBuilder {

        final Map<String, String> dimensions;
        final Document document;

        public ForLuceneDocument() {
            dimensions = new HashMap<>();
            document = new Document();
        }

        @Override
        final public void reset() {
            dimensions.clear();
            document.clear();
        }

        @Override
        final public void accept(final String genericFieldName, final String concreteFieldName, final Field field) {
            document.add(field);
            dimensions.put(concreteFieldName == null ? genericFieldName : concreteFieldName,
                genericFieldName == null ? concreteFieldName : genericFieldName);
        }
    }

    final class ForLuceneDocValues implements DocumentBuilder {

        private final String primaryKey;

        final List<Field> fieldList;

        public ForLuceneDocValues(String primaryKey) {
            fieldList = new ArrayList<>();
            this.primaryKey = primaryKey;
        }

        @Override
        final public void reset() {
            fieldList.clear();
        }

        @Override
        final public void accept(final String genericFieldName, final String concreteFieldName, final Field field) {
            // We will not update the internal ID of the document
            if (primaryKey.equals(field.name()))
                return;
            fieldList.add(field);
        }

    }
}
