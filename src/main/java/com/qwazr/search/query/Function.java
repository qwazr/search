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
import com.qwazr.search.function.AbstractValueSource;
import com.qwazr.search.function.DoubleFieldSource;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;

public class Function extends AbstractQuery<Function> {

    final public AbstractValueSource<?> source;

    @JsonCreator
    public Function(@JsonProperty("source") final AbstractValueSource<?> source) {
        super(Function.class);
        this.source = Objects.requireNonNull(source, "The source property is missing");
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "queries/org/apache/lucene/queries/function/FunctionQuery.html")
    public Function(final IndexSettingsDefinition settings,
                    final Map<String, AnalyzerDefinition> analyzers,
                    final Map<String, FieldDefinition> fields) {
        this(new DoubleFieldSource(getDoubleField(fields, () -> "double_field")));
    }

    @Override
    final public FunctionQuery getQuery(final QueryContext queryContext)
        throws ReflectiveOperationException, IOException, ParseException, QueryNodeException {
        return new FunctionQuery(source.getValueSource(queryContext));
    }

    public static FunctionQuery[] getQueries(final Function[] scoringQueries,
                                             final QueryContext queryContext)
        throws ReflectiveOperationException, QueryNodeException, ParseException, IOException {
        if (scoringQueries == null)
            return null;
        final FunctionQuery[] functionQueries = new FunctionQuery[scoringQueries.length];
        int i = 0;
        for (Function scoringQuery : scoringQueries)
            functionQueries[i++] = scoringQuery.getQuery(queryContext);
        return functionQueries;
    }

    @Override
    protected boolean isEqual(Function q) {
        return Objects.equals(source, q.source);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hashCode(source);
    }
}
