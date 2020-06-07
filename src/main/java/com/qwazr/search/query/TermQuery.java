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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.search.Query;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class TermQuery extends AbstractFieldQuery<TermQuery> {

    final public Object term;

    @JsonCreator
    public TermQuery(@JsonProperty("generic_field") final String genericField,
                     @JsonProperty("field") final String field,
                     @JsonProperty("term") final Object term) {
        super(TermQuery.class, genericField, field);
        this.term = term;
    }

    public TermQuery(final String field, final Object term) {
        this(null, field, term);
    }

    private final static URI DOC = URI.create("core/org/apache/lucene/search/TermQuery.html");

    public TermQuery(final IndexSettingsDefinition settings,
                     final Map<String, AnalyzerDefinition> analyzers,
                     final Map<String, FieldDefinition> fields) {
        super(TermQuery.class, DOC,
            null, getFullTextField(fields, () -> getTextField(fields, () -> "text")));
        this.term = "Hello";
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(final TermQuery q) {
        return super.isEqual(q) && Objects.equals(term, q.term);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) {
        return new org.apache.lucene.search.TermQuery(getResolvedTerm(queryContext.getFieldMap(), term));
    }

}
