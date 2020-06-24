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
import com.qwazr.utils.StringUtils;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiTermQuery;

public class Fuzzy extends AbstractMultiTermQuery<Fuzzy> {

    final public String text;
    final public Integer max_edits;
    final public Integer max_expansions;
    final public Boolean transpositions;
    final public Integer prefix_length;

    @JsonCreator
    private Fuzzy(@JsonProperty("generic_field") final String genericField,
                  @JsonProperty("field") final String field,
                  @JsonProperty("text") final String text,
                  @JsonProperty("max_edits") final Integer maxEdits,
                  @JsonProperty("max_expansions") final Integer maxExpansions,
                  @JsonProperty("transpositions") final Boolean transpositions,
                  @JsonProperty("prefix_length") final Integer prefixLength) {
        super(Fuzzy.class, genericField, field, null);
        this.text = text;
        this.max_edits = maxEdits;
        this.max_expansions = maxExpansions;
        this.transpositions = transpositions;
        this.prefix_length = prefixLength;
    }

    public Fuzzy(final String field,
                 final String text,
                 final Integer maxEdits,
                 final Integer maxExpansions,
                 final Boolean transpositions,
                 final Integer prefixLength) {
        this(null, field, text, maxEdits, maxExpansions, transpositions, prefixLength);
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/search/FuzzyQuery.html")
    public Fuzzy(final IndexSettingsDefinition settings,
                 final Map<String, AnalyzerDefinition> analyzers,
                 final Map<String, FieldDefinition> fields) {
        this(getFullTextField(fields, () -> getTextField(fields, () -> "text")),
            "Helo",
            FuzzyQuery.defaultMaxEdits,
            FuzzyQuery.defaultMaxExpansions,
            FuzzyQuery.defaultTranspositions,
            FuzzyQuery.defaultPrefixLength
        );
    }

    public Fuzzy(final String field, final String text, final Integer maxEdits, final Integer maxExpansions,
                 final Boolean transpositions, final Integer prefixLength,
                 final MultiTermQuery.RewriteMethod rewriteMethod) {
        super(Fuzzy.class, null, field, rewriteMethod);
        this.text = text;
        this.max_edits = maxEdits;
        this.max_expansions = maxExpansions;
        this.transpositions = transpositions;
        this.prefix_length = prefixLength;
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(Fuzzy q) {
        return super.isEqual(q) && Objects.equals(text, q.text) && Objects.equals(max_edits, q.max_edits) &&
            Objects.equals(max_expansions, q.max_expansions) && Objects.equals(transpositions, q.transpositions) &&
            Objects.equals(prefix_length, q.prefix_length);
    }

    @Override
    final public MultiTermQuery getQuery(final QueryContext queryContext) {
        return applyRewriteMethod(
            new org.apache.lucene.search.FuzzyQuery(
                new Term(resolveFullTextField(queryContext.getFieldMap(), StringUtils.EMPTY), text),
                max_edits == null ? FuzzyQuery.defaultMaxEdits : max_edits,
                prefix_length == null ? FuzzyQuery.defaultPrefixLength : prefix_length,
                max_expansions == null ? FuzzyQuery.defaultMaxExpansions : max_expansions,
                transpositions == null ? FuzzyQuery.defaultTranspositions : transpositions)
        );
    }
}
