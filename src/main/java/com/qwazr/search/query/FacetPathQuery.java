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
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

import java.util.Arrays;
import java.util.Objects;

public class FacetPathQuery extends AbstractQuery<FacetPathQuery> {

    final public String dimension;
    @JsonProperty("generic_field")
    final public String genericField;
    final public String[] path;

    @JsonCreator
    FacetPathQuery(@JsonProperty("generic_field") final String genericField,
                   @JsonProperty("dimension") final String dimension, @JsonProperty("path") final String... path) {
        super(FacetPathQuery.class);
        this.genericField = genericField;
        this.dimension = dimension;
        this.path = path;
    }

    FacetPathQuery(Builder builder) {
        this(builder.genericField, builder.dimension, builder.path);
    }

    public static Builder of(String dimension) {
        return new Builder().dimension(dimension);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) {
        Objects.requireNonNull(dimension, "The dimension is missing");

        final FieldMap fieldMap = queryContext.getFieldMap();
        final String resolvedDimension =
            fieldMap == null ? dimension : fieldMap.resolveQueryFieldName(FieldTypeInterface.LuceneFieldType.facet, genericField, dimension);
        final String indexFieldName =
            queryContext.getFacetsConfig(genericField, dimension).getDimConfig(resolvedDimension).indexFieldName;
        final Term term = new Term(indexFieldName, FacetsConfig.pathToString(resolvedDimension, path));
        return new org.apache.lucene.search.TermQuery(term);
    }

    @Override
    protected boolean isEqual(FacetPathQuery q) {
        return Objects.equals(dimension, q.dimension) && Objects.equals(genericField, q.genericField) &&
            Arrays.equals(path, q.path);
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

        public FacetPathQuery build() {
            return new FacetPathQuery(this);
        }

    }
}
