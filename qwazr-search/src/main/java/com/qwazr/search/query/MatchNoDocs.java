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
import com.qwazr.search.annotations.QuerySampleCreator;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import java.util.Map;
import org.apache.lucene.search.Query;

public class MatchNoDocs extends AbstractQuery<MatchNoDocs> {

    public final static MatchNoDocs INSTANCE = new MatchNoDocs();

    @JsonCreator
    static MatchNoDocs create() {
        return INSTANCE;
    }

    private MatchNoDocs() {
        super(MatchNoDocs.class);
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/search/MatchNoDocsQuery.html")
    public static MatchNoDocs create(final IndexSettingsDefinition settings,
                                     final Map<String, AnalyzerDefinition> analyzers,
                                     final Map<String, FieldDefinition> fields) {
        return INSTANCE;
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) {
        return new org.apache.lucene.search.MatchNoDocsQuery();
    }

    @Override
    protected boolean isEqual(final MatchNoDocs query) {
        return query != null;
    }

    @Override
    protected int computeHashCode() {
        return ownClass.hashCode();
    }
}
