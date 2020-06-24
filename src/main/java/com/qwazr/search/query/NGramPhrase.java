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
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.search.NGramPhraseQuery;
import org.apache.lucene.search.Query;

public class NGramPhrase extends AbstractQuery<NGramPhrase> {

    @JsonProperty("phrase_query")
    final public Phrase phraseQuery;

    @JsonProperty("ngram_size")
    final public Integer nGramSize;

    @JsonCreator
    public NGramPhrase(@JsonProperty("phrase_query") final Phrase phraseQuery,
                       @JsonProperty("ngram_size") final Integer ngramSize) {
        super(NGramPhrase.class);
        this.phraseQuery = Objects.requireNonNull(phraseQuery, "The phrase_query should not be null");
        this.nGramSize = Objects.requireNonNull(ngramSize, "The ngram_size should not be null");
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/search/NGramPhraseQuery.html")
    public NGramPhrase(final IndexSettingsDefinition settings,
                       final Map<String, AnalyzerDefinition> analyzers,
                       final Map<String, FieldDefinition> fields) {
        this(new Phrase(settings, analyzers, fields), 2);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) {
        return new NGramPhraseQuery(nGramSize, phraseQuery.getQuery(queryContext));
    }

    @Override
    protected boolean isEqual(NGramPhrase q) {
        return Objects.equals(phraseQuery, q.phraseQuery) && Objects.equals(nGramSize, q.nGramSize);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(phraseQuery, nGramSize);
    }
}
