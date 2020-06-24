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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.annotations.QuerySampleCreator;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

public class FacetPath extends AbstractQuery<FacetPath> {

    @JsonProperty("dimension")
    final public String dimension;
    @JsonProperty("generic_field")
    final public String genericField;
    @JsonProperty("path")
    final public String[] path;

    @JsonCreator
    FacetPath(@JsonProperty("generic_field") final String genericField,
              @JsonProperty("dimension") final String dimension,
              @JsonProperty("path") final String... path) {
        super(FacetPath.class);
        this.genericField = genericField;
        this.dimension = dimension;
        this.path = path;
    }

    FacetPath(final Builder builder) {
        this(builder.genericField, builder.dimension, builder.path);
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "facet/org/apache/lucene/facet/FacetQuery.html")
    public FacetPath(final IndexSettingsDefinition settings,
                     final Map<String, AnalyzerDefinition> analyzers,
                     final Map<String, FieldDefinition> fields) {
        this(null, getTextField(fields, () -> "category"), "search-engine");
    }

    public static Builder of(String dimension) {
        return new Builder().dimension(dimension);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) {
        Objects.requireNonNull(dimension, "The dimension is missing");

        final FieldMap fieldMap = queryContext.getFieldMap();
        final String resolvedDimension =
            fieldMap == null ? dimension : fieldMap.getFieldType(genericField, dimension).resolveFieldName(dimension,
                FieldTypeInterface.FieldType.facetField, FieldTypeInterface.ValueType.textType);
        final String indexFieldName =
            queryContext.getFacetsConfig(genericField, dimension).getDimConfig(resolvedDimension).indexFieldName;
        final Term term = new Term(indexFieldName, FacetsConfig.pathToString(resolvedDimension, path));
        return new org.apache.lucene.search.TermQuery(term);
    }

    @Override
    protected boolean isEqual(FacetPath q) {
        return Objects.equals(dimension, q.dimension) && Objects.equals(genericField, q.genericField) &&
            Arrays.equals(path, q.path);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(dimension, genericField, path);
    }

    public static class Builder {

        String dimension;
        String genericField;
        String[] path;

        public Builder dimension(String dimension) {
            this.dimension = dimension;
            return this;
        }

        public Builder genericField(String genericField) {
            this.genericField = genericField;
            return this;
        }

        public Builder path(String... path) {
            this.path = path;
            return this;
        }

        public FacetPath build() {
            return new FacetPath(this);
        }

    }
}
