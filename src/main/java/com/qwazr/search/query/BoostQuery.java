/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class BoostQuery extends AbstractQuery<BoostQuery> {

    final public AbstractQuery<?> query;
    final public Float boost;

    @JsonCreator
    public BoostQuery(@JsonProperty("query") final AbstractQuery<?> query, @JsonProperty("boost") final Float boost) {
        super(BoostQuery.class);
        this.query = Objects.requireNonNull(query, "The query property is missing");
        this.boost = Objects.requireNonNull(boost, "The boost property is missing");
    }

    private final static URI DOC = URI.create("core/org/apache/lucene/search/BoostQuery.html");

    public BoostQuery(final IndexSettingsDefinition settings,
                      final Map<String, AnalyzerDefinition> analyzers,
                      final Map<String, FieldDefinition> fields) {
        super(BoostQuery.class, DOC);
        final String field = getFullTextField(fields,
            () -> getTextField(fields,
                () -> "text"));
        this.query = new TermQuery(field, "hot");
        this.boost = 5.0f;
    }

    @Override
    final public Query getQuery(final QueryContext queryContext)
        throws IOException, ParseException, QueryNodeException, ReflectiveOperationException {
        return new org.apache.lucene.search.BoostQuery(query.getQuery(queryContext), boost);
    }

    @Override
    protected boolean isEqual(final BoostQuery q) {
        return Objects.equals(query, q.query) && Objects.equals(boost, q.boost);
    }
}
