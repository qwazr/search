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
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

public class ConstantScore extends AbstractQuery<ConstantScore> {

    public final AbstractQuery<?> query;

    @JsonCreator
    public ConstantScore(@JsonProperty("query") final AbstractQuery<?> query) {
        super(ConstantScore.class);
        this.query = Objects.requireNonNull(query, "The embedded query is missing");
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/search/ConstantScoreQuery.html")
    public ConstantScore(final IndexSettingsDefinition settings,
                         final Map<String, AnalyzerDefinition> analyzers,
                         final Map<String, FieldDefinition> fields) {
        this(new HasTerm(settings, analyzers, fields));
    }

    @Override
    final public Query getQuery(final QueryContext queryContext)
        throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
        return new org.apache.lucene.search.ConstantScoreQuery(query.getQuery(queryContext));
    }

    @Override
    protected boolean isEqual(final ConstantScore q) {
        return Objects.equals(query, q.query);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hashCode(query);
    }
}
