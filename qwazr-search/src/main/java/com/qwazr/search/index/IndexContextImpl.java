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
package com.qwazr.search.index;

import com.qwazr.search.analysis.AnalyzerContext;
import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.facet.FacetsConfig;

class IndexContextImpl implements IndexContext, Closeable {

    final ExecutorService executorService;
    final AnalyzerContext analyzerContext;
    final FieldMap fieldMap;
    final ResourceLoader resourceLoader;
    final IndexInstance.Provider indexProvider;

    IndexContextImpl(final IndexInstance.Provider indexProvider,
                     final ResourceLoader resourceLoader,
                     final ExecutorService executorService,
                     final AnalyzerContext analyzerContext,
                     final FieldMap fieldMap) {
        this.indexProvider = indexProvider == null ? i -> null : indexProvider;
        this.resourceLoader = resourceLoader;
        this.executorService = executorService;
        this.analyzerContext = analyzerContext.acquire();
        this.fieldMap = fieldMap;
    }

    @Override
    final public IndexInstance getIndex(final String indexName) {
        return indexProvider.get(indexName);
    }

    @Override
    final public FacetsConfig getFacetsConfig(final String genericFieldName, final String concreteFieldName) {
        return fieldMap.getFacetsConfig(genericFieldName, concreteFieldName);
    }

    @Override
    final public FacetsConfig getFacetsConfig(final Map<String, String> dimensions) {
        return fieldMap.getFacetsConfig(dimensions);
    }

    @Override
    public void close() {
        analyzerContext.close();
    }
}
