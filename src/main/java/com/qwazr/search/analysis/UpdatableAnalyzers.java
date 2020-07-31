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
package com.qwazr.search.analysis;

import com.qwazr.server.ServerException;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.AnalyzerWrapper;

final public class UpdatableAnalyzers extends AnalyzerWrapper {

    private volatile Map<String, Analyzer> perFieldAnalyzers;

    public UpdatableAnalyzers() throws ServerException {
        super(PER_FIELD_REUSE_STRATEGY);
    }

    final public synchronized void update(final Map<String, Analyzer> perFieldAnalyzers) throws ServerException {
        this.perFieldAnalyzers = perFieldAnalyzers;
    }

    @Override
    final protected Analyzer getWrappedAnalyzer(final String fieldName) {
        return perFieldAnalyzers.getOrDefault(fieldName, AnalyzerContext.defaultKeywordAnalyzer);
    }

}
