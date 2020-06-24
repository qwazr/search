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
import com.qwazr.search.annotations.QuerySampleCreator;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.WildcardQuery;

public class Wildcard extends AbstractMultiTermQuery<Wildcard> {

    final public String term;

    @JsonCreator
    private Wildcard(@JsonProperty("generic_field") final String genericField,
                     @JsonProperty("field") final String field,
                     @JsonProperty("term") final String term) {
        super(Wildcard.class, genericField, field, null);
        this.term = term;
    }

    public Wildcard(final String field, final String term, final MultiTermQuery.RewriteMethod rewriteMethod) {
        super(null, field, term, rewriteMethod);
        this.term = term;
    }

    public Wildcard(final String field, final String term) {
        this(null, field, term);
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/search/WildcardQuery.html")
    public Wildcard(final IndexSettingsDefinition settings,
                    final Map<String, AnalyzerDefinition> analyzers,
                    final Map<String, FieldDefinition> fields) {
        this(getFullTextField(fields, () -> getTextField(fields, () -> "text")), "H*l?");
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(Wildcard q) {
        return super.isEqual(q) && Objects.equals(term, q.term);
    }

    @Override
    final public MultiTermQuery getQuery(final QueryContext queryContext) {
        return applyRewriteMethod(new WildcardQuery(resolveIndexTextTerm(queryContext.getFieldMap(), term)));
    }

}
