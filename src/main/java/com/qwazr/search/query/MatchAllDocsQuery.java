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
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.search.Query;

import java.net.URI;
import java.util.Map;

public class MatchAllDocsQuery extends AbstractQuery<MatchAllDocsQuery> {

    @JsonCreator
    public MatchAllDocsQuery() {
        super(MatchAllDocsQuery.class);
    }

    private final static URI DOC = URI.create("core/org/apache/lucene/search/MatchAllDocsQuery.html");

    public MatchAllDocsQuery(final IndexSettingsDefinition settings,
                             final Map<String, AnalyzerDefinition> analyzers,
                             final Map<String, FieldDefinition> fields) {
        super(MatchAllDocsQuery.class, DOC);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) {
        return new org.apache.lucene.search.MatchAllDocsQuery();
    }

    @Override
    protected boolean isEqual(MatchAllDocsQuery query) {
        return query != null;
    }

    @Override
    public int hashCode() {
        return MatchAllDocsQuery.class.hashCode();
    }
}
