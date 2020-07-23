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

import com.qwazr.binder.FieldMapWrapper;
import com.qwazr.search.analysis.AnalyzerContext;
import com.qwazr.server.ServerException;
import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

final class QueryContextImpl extends IndexContextImpl implements QueryContext, Closeable {

    final IndexSearcher indexSearcher;
    final IndexReader indexReader;
    final TaxonomyReader taxonomyReader;
    final AnalyzerContext analyzerContext;
    final SortedSetDocValuesReaderState docValueReaderState;
    final FieldMap fieldMap;

    QueryContextImpl(final IndexInstance.Provider indexProvider,
                     final ResourceLoader resourceLoader,
                     final ExecutorService executorService,
                     final AnalyzerContext analyzerContext,
                     final FieldMap fieldMap,
                     final IndexSearcher indexSearcher,
                     final TaxonomyReader taxonomyReader) {
        super(indexProvider, resourceLoader, executorService, analyzerContext, fieldMap);
        this.docValueReaderState = ((MultiThreadSearcherFactory.StateIndexSearcher) indexSearcher).state;
        this.fieldMap = fieldMap;
        this.indexSearcher = indexSearcher;
        this.indexReader = indexSearcher.getIndexReader();
        this.taxonomyReader = taxonomyReader;
        this.analyzerContext = analyzerContext;
    }

    @Override
    public IndexReader getIndexReader() {
        return indexReader;
    }

    @Override
    public IndexSearcher getIndexSearcher() {
        return indexSearcher;
    }

    @Override
    public FieldMap getFieldMap() {
        return fieldMap;
    }

    private <T extends ResultDocumentAbstract> ResultDefinition<T> search(final QueryDefinition queryDefinition,
                                                                          final ResultDocuments<T> resultDocuments) {
        try {
            return new QueryExecution<T>(this, queryDefinition).execute(resultDocuments);
        } catch (Exception e) {
            throw ServerException.of(e);
        }
    }

    @Override
    public ResultDefinition.WithMap searchMap(QueryDefinition queryDefinition) {
        final ReturnedFieldStrategy returnedFieldStrategy = ReturnedFieldStrategy.of(this, queryDefinition, fieldMap::getStaticFieldSet);
        final ResultDocumentsMap resultDocumentsMap = ResultDocumentsMap.of(queryDefinition, returnedFieldStrategy);
        return (ResultDefinition.WithMap) search(queryDefinition, resultDocumentsMap);
    }

    @Override
    public <T> ResultDefinition.WithObject<T> searchObject(final QueryDefinition queryDefinition,
                                                           final FieldMapWrapper<T> wrapper) {
        final ReturnedFieldStrategy returnedFieldStrategy = ReturnedFieldStrategy.of(this, queryDefinition, wrapper.fieldMap::keySet);
        final ResultDocumentsObject<T> resultDocumentsObject = ResultDocumentsObject.of(queryDefinition, returnedFieldStrategy, wrapper);
        return (ResultDefinition.WithObject<T>) search(queryDefinition, resultDocumentsObject);
    }

    @Override
    public ResultDefinition.Empty searchInterface(final QueryDefinition queryDefinition,
                                                  final ResultDocumentsInterface resultDocuments) {
        final ResultDocumentsEmpty resultDocumentEmpty = new ResultDocumentsEmpty(resultDocuments);
        return (ResultDefinition.Empty) search(queryDefinition, resultDocumentEmpty);
    }

    public Analyzer resolveQueryAnalyzer(final String analyzer) {
        return analyzerContext.resolveQueryAnalyzer(analyzer);
    }

}
