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
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.server.ServerException;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

interface RecordsPoster {

    int getCount();

    abstract class CommonPoster
        <RECORDBUILDER extends RecordBuilder,
            DOCUMENTBUILDER extends DocumentBuilder>
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

        private Documents(final DocumentBuilder.ForLuceneDocument documentBuilder,
                          final RECORDBUILDER recordBuilder,
                          final FieldMap fieldMap,
                          final IndexWriter indexWriter,
                          final TaxonomyWriter taxonomyWriter) {
            super(documentBuilder, recordBuilder, fieldMap, indexWriter, taxonomyWriter);
        }

        private Document getFacetedDoc() throws IOException {
            final FacetsConfig facetsConfig = fieldMap.getFacetsConfig(documentBuilder.dimensions);
            return facetsConfig.build(taxonomyWriter, documentBuilder.document);
        }

        private void updateDocument(final Term termId) throws IOException {
            indexWriter.updateDocument(termId, getFacetedDoc());
            count++;
            documentBuilder.reset();
        }

        private void addDocument() throws IOException {
            indexWriter.addDocument(getFacetedDoc());
            count++;
            documentBuilder.reset();
        }

        void index() throws IOException {
            if (recordBuilder.termId != null)
                updateDocument(recordBuilder.termId);
            else
                addDocument();
            recordBuilder.reset();
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

        final void updateDocValues() throws IOException {
            if (recordBuilder.termId == null)
                throw new ServerException(Response.Status.BAD_REQUEST,
                    "The primary key " + FieldDefinition.ID_FIELD + " is missing - Index: " + indexWriter);
            indexWriter.updateDocValues(recordBuilder.termId, documentBuilder.fieldList.toArray(
                new org.apache.lucene.document.Field[0]));
            count++;
            documentBuilder.reset();
            recordBuilder.reset();
        }

    }

    interface MapDocument extends RecordsPoster {

        void accept(final Map<String, ?> document) throws IOException;

        static MapDocument of(final FieldMap fieldMap,
                              final IndexWriter indexWriter,
                              final TaxonomyWriter taxonomyWriter) {
            final DocumentBuilder.ForLuceneDocument documentBuilder = new DocumentBuilder.ForLuceneDocument();
            return new IndexMapDocument(documentBuilder, fieldMap, indexWriter, taxonomyWriter);
        }

        static MapDocument forDocValueUpdate(final FieldMap fieldMap,
                                             final IndexWriter indexWriter,
                                             final TaxonomyWriter taxonomyWriter) {
            final DocumentBuilder.ForLuceneDocValues documentBuilder = new DocumentBuilder.ForLuceneDocValues();
            return new UpdateMapDocValues(documentBuilder, fieldMap, indexWriter, taxonomyWriter);
        }
    }

    final class IndexMapDocument extends Documents<RecordBuilder.ForMap> implements MapDocument {

        private IndexMapDocument(final DocumentBuilder.ForLuceneDocument documentBuilder,
                                 final FieldMap fieldMap,
                                 final IndexWriter indexWriter,
                                 final TaxonomyWriter taxonomyWriter) {
            super(documentBuilder, new RecordBuilder.ForMap(fieldMap, documentBuilder), fieldMap, indexWriter, taxonomyWriter);
        }

        @Override
        final public void accept(final Map<String, ?> document) throws IOException {
            document.forEach(recordBuilder::accept);
            index();
        }

    }


    interface ObjectDocument extends RecordsPoster {

        void accept(Object object) throws IOException;

        static ObjectDocument of(final Map<String, Field> fields,
                                 final FieldMap fieldMap,
                                 final IndexWriter indexWriter,
                                 final TaxonomyWriter taxonomyWriter) {
            final DocumentBuilder.ForLuceneDocument documentBuilder = new DocumentBuilder.ForLuceneDocument();
            return new IndexObjectDocument(documentBuilder, fieldMap, indexWriter, taxonomyWriter, fields);
        }

        static ObjectDocument forDocValueUpdate(final Map<String, Field> fields,
                                                final FieldMap fieldMap,
                                                final IndexWriter indexWriter,
                                                final TaxonomyWriter taxonomyWriter) {
            final DocumentBuilder.ForLuceneDocValues documentBuilder = new DocumentBuilder.ForLuceneDocValues();
            return new UpdateObjectDocValues(documentBuilder, fieldMap, indexWriter, taxonomyWriter, fields);
        }
    }

    final class IndexObjectDocument extends Documents<RecordBuilder.ForObject> implements ObjectDocument {

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
        final public void accept(final Object object) throws IOException {
            fields.forEach((name, field) -> recordBuilder.accept(name, field, object));
            index();
        }
    }

    interface JsonNodeDocument extends RecordsPoster {

        void accept(final JsonNode jsonNode) throws IOException;

        static JsonNodeDocument of(final FieldMap fieldMap,
                                   final IndexWriter indexWriter,
                                   final TaxonomyWriter taxonomyWriter) {
            final DocumentBuilder.ForLuceneDocument documentBuilder = new DocumentBuilder.ForLuceneDocument();
            return new IndexJsonNodeObject(documentBuilder, fieldMap, indexWriter, taxonomyWriter);
        }

    }

    final class IndexJsonNodeObject extends Documents<RecordBuilder.ForJson> implements JsonNodeDocument {

        private IndexJsonNodeObject(final DocumentBuilder.ForLuceneDocument documentBuilder,
                                    final FieldMap fieldMap,
                                    final IndexWriter indexWriter,
                                    final TaxonomyWriter taxonomyWriter) {
            super(documentBuilder, new RecordBuilder.ForJson(fieldMap, documentBuilder), fieldMap, indexWriter, taxonomyWriter);
        }

        @Override
        final public void accept(final JsonNode jsonNode) throws IOException {
            jsonNode.fields().forEachRemaining(entry -> recordBuilder.accept(entry.getKey(), entry.getValue()));
            index();
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
