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

import com.qwazr.search.field.FieldTypeInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.NotAcceptableException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;

public interface DocumentBuilder<RESULT> {

    void reset();

    void acceptField(final Field field);

    void acceptFacetField(final Field field,
                          final String dimensionName,
                          final FieldTypeInterface.FacetsConfigSupplier facetsConfigSupplier);

    RESULT build() throws IOException;

    static DocumentBuilder.ForLuceneDocument of(final FieldMap fieldMap, final TaxonomyWriter taxonomyWriter) {
        return taxonomyWriter == null
            ? new DocumentBuilder.ForLuceneDocument(fieldMap.fieldsContext)
            : new DocumentBuilder.ForLuceneDocumentWithTaxonomy(fieldMap.fieldsContext, taxonomyWriter);
    }

    class ForLuceneDocument implements DocumentBuilder<Document> {

        private final FieldsContext fieldsContext;
        protected final FacetsConfig facetsConfig;
        private final Map<String, FacetsConfig.DimConfig> dimConfigs;
        protected final Document document;

        private ForLuceneDocument(final FieldsContext fieldsContext) {
            this.fieldsContext = fieldsContext;
            this.facetsConfig = new FacetsConfig();
            this.dimConfigs = facetsConfig.getDimConfigs();
            this.document = new Document();
        }

        @Override
        final public void reset() {
            document.clear();
        }

        @Override
        final public void acceptField(final Field field) {
            document.add(field);
        }

        @Override
        final public void acceptFacetField(final Field field,
                                           final String dimensionName,
                                           final FieldTypeInterface.FacetsConfigSupplier facetsConfigSupplier) {
            document.add(field);
            if (!dimConfigs.containsKey(dimensionName))
                facetsConfigSupplier.setConfig(dimensionName, fieldsContext, facetsConfig);
        }

        public Document build() throws IOException {
            return facetsConfig.build(document);
        }
    }

    final class ForLuceneDocumentWithTaxonomy extends ForLuceneDocument {

        private final TaxonomyWriter taxonomyWriter;

        private ForLuceneDocumentWithTaxonomy(final FieldsContext fieldsContext,
                                              final TaxonomyWriter taxonomyWriter) {
            super(fieldsContext);
            this.taxonomyWriter = taxonomyWriter;
        }

        final public Document build() throws IOException {
            return facetsConfig.build(taxonomyWriter, document);
        }
    }

    final class ForLuceneDocValues implements DocumentBuilder<Field[]> {

        private final String primaryKey;

        private final List<Field> fieldList;

        public ForLuceneDocValues(String primaryKey) {
            fieldList = new ArrayList<>();
            this.primaryKey = primaryKey;
        }

        @Override
        final public void reset() {
            fieldList.clear();
        }

        @Override
        final public void acceptField(final Field field) {
            // We will not update the internal ID of the document
            if (primaryKey.equals(field.name()))
                return;
            fieldList.add(field);
        }

        @Override
        final public void acceptFacetField(final Field field,
                                           final String dimensionName,
                                           final FieldTypeInterface.FacetsConfigSupplier facetsConfigSupplier) {
            throw new NotAcceptableException("Facet field can't be used for a DocValues update: " + field.name());
        }

        private final static Field[] EMPTY_FIELDS = new Field[0];

        @Override
        public Field[] build() {
            return fieldList.toArray(EMPTY_FIELDS);
        }
    }
}
