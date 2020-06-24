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
import java.net.URI;
import java.util.Map;
import org.apache.lucene.search.Query;

public class EqualsString extends AbstractExactQuery<String, EqualsString> {

    @JsonCreator
    public EqualsString(@JsonProperty("field") final String field,
                        @JsonProperty("value") final String value) {
        super(EqualsString.class, field, value);
    }

    private final static URI DOC = URI.create("core/org/apache/lucene/search/TermQuery.html");

    public EqualsString(final IndexSettingsDefinition settings,
                        final Map<String, AnalyzerDefinition> analyzers,
                        final Map<String, FieldDefinition> fields) {
        super(EqualsString.class, DOC, getFullTextField(fields, () -> getTextField(fields, () -> "text")), "Hello");
    }

    @Override
    @JsonIgnore
    final public Query getQuery(final QueryContext queryContext) {
        return new org.apache.lucene.search.TermQuery(resolveIndexTextTerm(queryContext.getFieldMap(), value));
    }

}
