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
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.StringUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PhraseQuery;

public class Phrase extends AbstractFieldQuery<Phrase> {

    final public List<String> terms;
    final public Integer slop;

    @JsonCreator
    public Phrase(@JsonProperty("generic_field") final String genericField,
                  @JsonProperty("field") final String field,
                  @JsonProperty("slop") final Integer slop,
                  @JsonProperty("terms") final List<String> terms) {
        super(Phrase.class, genericField, field);
        this.slop = slop;
        this.terms = terms;
    }

    public Phrase(final String field, final Integer slop, final List<String> terms) {
        this(null, field, slop, terms);
    }

    public Phrase(final String field, final Integer slop, final String... terms) {
        this(field, slop, Arrays.asList(terms));
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/search/PhraseQuery.html")
    public Phrase(final IndexSettingsDefinition settings,
                  final Map<String, AnalyzerDefinition> analyzers,
                  final Map<String, FieldDefinition> fields) {
        this(getFullTextField(fields, () -> getTextField(fields, () -> "text")), 2, "Hello", "World");
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(Phrase q) {
        return super.equals(q) && CollectionsUtils.equals(terms, q.terms) && Objects.equals(slop, q.slop);
    }

    @Override
    final public org.apache.lucene.search.PhraseQuery getQuery(final QueryContext queryContext) {
        Objects.requireNonNull(field, "The field property should not be null");
        final PhraseQuery.Builder builder = new PhraseQuery.Builder();
        if (slop != null)
            builder.setSlop(slop);
        if (terms != null) {
            final String resolvedField = resolveIndexTextField(queryContext.getFieldMap(), StringUtils.EMPTY);
            for (final String term : terms)
                builder.add(new Term(resolvedField, term));
        }
        return builder.build();
    }
}
