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

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.SerializationUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.RunnableEx;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;

interface RecordsPoster {

    int getCount();

    abstract class CommonPoster<RECORDBUILDER extends RecordBuilder, DOCUMENTBUILDER extends DocumentBuilder>
        implements RecordsPoster {

        protected final DOCUMENTBUILDER documentBuilder;
        protected final RECORDBUILDER recordBuilder;
        protected final FieldMap fieldMap;
        protected final IndexWriter indexWriter;
        protected final TaxonomyWriter taxonomyWriter;
        protected int count;

        CommonPoster(final DOCUMENTBUILDER documentBuilder,
                     final RECORDBUILDER recordBuilder,
                     final FieldMap fieldMap,
                     final IndexWriter indexWriter,
                     final TaxonomyWriter taxonomyWriter) {
            this.documentBuilder = documentBuilder;
            this.recordBuilder = recordBuilder;
            this.fieldMap = fieldMap;
            this.indexWriter = indexWriter;
            this.taxonomyWriter = taxonomyWriter;
            this.count = 0;
        }

        @Override
        public final int getCount() {
            return count;
        }

    }

    abstract class Documents<RECORDBUILDER extends RecordBuilder>
        extends CommonPoster<RECORDBUILDER, DocumentBuilder.ForLuceneDocument> {

        protected final RunnableEx<IOException> index;

        private Documents(final DocumentBuilder.ForLuceneDocument documentBuilder,
                          final RECORDBUILDER recordBuilder,
                          final FieldMap fieldMap,
                          final IndexWriter indexWriter,
                          final TaxonomyWriter taxonomyWriter) {
            super(documentBuilder, recordBuilder, fieldMap, indexWriter, taxonomyWriter);
            if (StringUtils.isBlank(fieldMap.getPrimaryKey()))
                index = this::addDocument;
            else
                index = this::updateDocument;
        }

        private Document getFacetedDoc() throws IOException {
            final FacetsConfig facetsConfig = fieldMap.getFacetsConfig(documentBuilder.dimensions);
            return facetsConfig.build(taxonomyWriter, documentBuilder.document);
        }

        final void addDocument() throws IOException {
            indexWriter.addDocument(getFacetedDoc());
            count++;
            documentBuilder.reset();
        }

        final void updateDocument() throws IOException {
            indexWriter.updateDocument(recordBuilder.getTermId(), getFacetedDoc());
            count++;
            documentBuilder.reset();
        }

    }

    abstract class DocValues<RECORDBUILDER extends RecordBuilder>
        extends CommonPoster<RECORDBUILDER, DocumentBuilder.ForLuceneDocValues> {

        protected DocValues(final DocumentBuilder.ForLuceneDocValues documentBuilder,
                            final RECORDBUILDER recordBuilder,
                            final FieldMap fieldMap,
                            final IndexWriter indexWriter,
                            final TaxonomyWriter taxonomyWriter) {
            super(documentBuilder, recordBuilder, fieldMap, indexWriter, taxonomyWriter);
        }

        final protected void updateDocValues() throws IOException {
            indexWriter.updateDocValues(recordBuilder.getTermId(), documentBuilder.fieldList.toArray(
                new org.apache.lucene.document.Field[0]));
            count++;
            documentBuilder.reset();
        }
    }

    interface MapDocument extends RecordsPoster {

        void accept(final Map<String, ?> document) throws IOException;

        static MapDocument of(final FieldMap fieldMap,
                              final IndexWriter indexWriter,
                              final TaxonomyWriter taxonomyWriter) {
            final DocumentBuilder.ForLuceneDocument documentBuilder = new DocumentBuilder.ForLuceneDocument();
            if (!StringUtils.isEmpty(fieldMap.fieldsContext.recordField))
                return new IndexMapDocumentWithSource(documentBuilder, fieldMap, indexWriter, taxonomyWriter);
            else
                return new IndexMapDocument(documentBuilder, fieldMap, indexWriter, taxonomyWriter);
        }

        static MapDocument forDocValueUpdate(final FieldMap fieldMap,
                                             final IndexWriter indexWriter,
                                             final TaxonomyWriter taxonomyWriter) {
            final DocumentBuilder.ForLuceneDocValues documentBuilder = new DocumentBuilder.ForLuceneDocValues(fieldMap.getPrimaryKey());
            return new UpdateMapDocValues(documentBuilder, fieldMap, indexWriter, taxonomyWriter);
        }
    }

    class IndexMapDocument extends Documents<RecordBuilder.ForMap> implements MapDocument {

        private IndexMapDocument(final DocumentBuilder.ForLuceneDocument documentBuilder,
                                 final FieldMap fieldMap,
                                 final IndexWriter indexWriter,
                                 final TaxonomyWriter taxonomyWriter) {
            super(documentBuilder, new RecordBuilder.ForMap(fieldMap, documentBuilder), fieldMap, indexWriter, taxonomyWriter);
        }

        @Override
        public void accept(final Map<String, ?> document) throws IOException {
            document.forEach(recordBuilder::accept);
            index.run();
        }

    }

    final class IndexMapDocumentWithSource extends IndexMapDocument {

        private IndexMapDocumentWithSource(DocumentBuilder.ForLuceneDocument documentBuilder, FieldMap fieldMap, IndexWriter indexWriter, TaxonomyWriter taxonomyWriter) {
            super(documentBuilder, fieldMap, indexWriter, taxonomyWriter);
        }

        @Override
        final public void accept(final Map<String, ?> document) throws IOException {
            recordBuilder.addRecord(ObjectMappers.SMILE.writeValueAsBytes(document));
            super.accept(document);
        }
    }


    interface ObjectDocument extends RecordsPoster {

        void accept(Object object) throws IOException;

        static ObjectDocument of(final Map<String, Field> fields,
                                 final FieldMap fieldMap,
                                 final IndexWriter indexWriter,
                                 final TaxonomyWriter taxonomyWriter) {
            final DocumentBuilder.ForLuceneDocument documentBuilder = new DocumentBuilder.ForLuceneDocument();
            if (!StringUtils.isEmpty(fieldMap.fieldsContext.recordField))
                return new IndexObjectDocumentWithSource(documentBuilder, fieldMap, indexWriter, taxonomyWriter, fields);
            else
                return new IndexObjectDocument(documentBuilder, fieldMap, indexWriter, taxonomyWriter, fields);
        }

        static ObjectDocument forDocValueUpdate(final Map<String, Field> fields,
                                                final FieldMap fieldMap,
                                                final IndexWriter indexWriter,
                                                final TaxonomyWriter taxonomyWriter) {
            final DocumentBuilder.ForLuceneDocValues documentBuilder = new DocumentBuilder.ForLuceneDocValues(fieldMap.getPrimaryKey());
            return new UpdateObjectDocValues(documentBuilder, fieldMap, indexWriter, taxonomyWriter, fields);
        }
    }

    class IndexObjectDocument extends Documents<RecordBuilder.ForObject> implements ObjectDocument {

        private final Map<String, Field> fields;

        private IndexObjectDocument(final DocumentBuilder.ForLuceneDocument documentBuilder,
                                    final FieldMap fieldMap,
                                    final IndexWriter indexWriter,
                                    final TaxonomyWriter taxonomyWriter,
                                    final Map<String, Field> fields) {
            super(documentBuilder, new RecordBuilder.ForObject(fieldMap, documentBuilder), fieldMap, indexWriter, taxonomyWriter);
            this.fields = fields;
        }

        @Override
        public void accept(final Object object) throws IOException {
            fields.forEach((name, field) -> recordBuilder.accept(name, field, object));
            index.run();
        }
    }

    final class IndexObjectDocumentWithSource extends IndexObjectDocument {

        private IndexObjectDocumentWithSource(DocumentBuilder.ForLuceneDocument documentBuilder, FieldMap fieldMap, IndexWriter indexWriter, TaxonomyWriter taxonomyWriter, Map<String, Field> fields) {
            super(documentBuilder, fieldMap, indexWriter, taxonomyWriter, fields);
        }

        @Override
        final public void accept(final Object object) throws IOException {
            final byte[] objectBytes;
            try {
                objectBytes = SerializationUtils.toExternalizorBytes((Serializable) object);
            } catch (ReflectiveOperationException e) {
                throw new IOException("Error while serializing the objet " + object.getClass().getName(), e);
            }
            recordBuilder.addRecord(objectBytes);
            super.accept(object);
        }
    }

    interface JsonNodeDocument extends RecordsPoster {

        void accept(final ObjectNode objectNode) throws IOException;

        static JsonNodeDocument of(final FieldMap fieldMap,
                                   final SortedMap<String, SortedSet<JsonNodeType>> fieldTypes,
                                   final IndexWriter indexWriter,
                                   final TaxonomyWriter taxonomyWriter) {
            final DocumentBuilder.ForLuceneDocument documentBuilder = new DocumentBuilder.ForLuceneDocument();
            if (!StringUtils.isEmpty(fieldMap.fieldsContext.recordField))
                return new IndexJsonNodeObjectWithSource(documentBuilder, fieldMap, fieldTypes, indexWriter, taxonomyWriter);
            else
                return new IndexJsonNodeObject(documentBuilder, fieldMap, fieldTypes, indexWriter, taxonomyWriter);
        }

    }

    class IndexJsonNodeObject extends Documents<RecordBuilder.ForJson> implements JsonNodeDocument {

        private IndexJsonNodeObject(final DocumentBuilder.ForLuceneDocument documentBuilder,
                                    final FieldMap fieldMap,
                                    final SortedMap<String, SortedSet<JsonNodeType>> fieldTypes,
                                    final IndexWriter indexWriter,
                                    final TaxonomyWriter taxonomyWriter) {
            super(documentBuilder, RecordBuilder.forJsonOf(fieldMap, documentBuilder, fieldTypes), fieldMap, indexWriter, taxonomyWriter);
        }

        @Override
        public void accept(final ObjectNode jsonNode) throws IOException {
            jsonNode.fields().forEachRemaining(entry -> recordBuilder.accept(entry.getKey(), entry.getValue()));
            index.run();
        }
    }

    final class IndexJsonNodeObjectWithSource extends IndexJsonNodeObject {

        private IndexJsonNodeObjectWithSource(final DocumentBuilder.ForLuceneDocument documentBuilder,
                                              final FieldMap fieldMap,
                                              final SortedMap<String, SortedSet<JsonNodeType>> fieldTypes,
                                              final IndexWriter indexWriter,
                                              final TaxonomyWriter taxonomyWriter) {
            super(documentBuilder, fieldMap, fieldTypes, indexWriter, taxonomyWriter);
        }

        @Override
        final public void accept(final ObjectNode objectNode) throws IOException {
            recordBuilder.addRecord(ObjectMappers.SMILE.writeValueAsBytes(objectNode));
            super.accept(objectNode);
        }
    }

    final class UpdateMapDocValues extends DocValues<RecordBuilder.ForMap> implements MapDocument {

        private UpdateMapDocValues(final DocumentBuilder.ForLuceneDocValues documentBuilder,
                                   final FieldMap fieldMap,
                                   final IndexWriter indexWriter,
                                   final TaxonomyWriter taxonomyWriter) {
            super(documentBuilder, new RecordBuilder.ForMap(fieldMap, documentBuilder), fieldMap, indexWriter, taxonomyWriter);
        }

        @Override
        final public void accept(final Map<String, ?> document) throws IOException {
            document.forEach(recordBuilder::accept);
            updateDocValues();
        }
    }

    final class UpdateObjectDocValues extends DocValues<RecordBuilder.ForObject> implements ObjectDocument {

        private final Map<String, Field> fields;

        private UpdateObjectDocValues(final DocumentBuilder.ForLuceneDocValues documentBuilder,
                                      final FieldMap fieldMap,
                                      final IndexWriter indexWriter,
                                      final TaxonomyWriter taxonomyWriter,
                                      final Map<String, Field> fields) {
            super(documentBuilder, new RecordBuilder.ForObject(fieldMap, documentBuilder), fieldMap, indexWriter, taxonomyWriter);
            this.fields = fields;
        }

        @Override
        final public void accept(final Object object) throws IOException {
            fields.forEach((name, field) -> recordBuilder.accept(name, field, object));
            updateDocValues();
        }
    }
}
