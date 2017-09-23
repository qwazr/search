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
package com.qwazr.search.index;

import com.qwazr.binder.FieldMapWrapper;
import com.qwazr.search.analysis.UpdatableAnalyzers;
import com.qwazr.server.ServerException;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.IndexSearcher;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;

final class QueryContextImpl extends IndexContextImpl implements QueryContext, Closeable {

	final IndexSearcher indexSearcher;
	final IndexReader indexReader;
	final TaxonomyReader taxonomyReader;
	final SortedSetDocValuesReaderState docValueReaderState;
	final FieldMapWrapper.Cache fieldMapWrappers;
	final FieldMap fieldMap;

	QueryContextImpl(final IndexInstance.Provider indexProvider, final ResourceLoader resourceLoader,
			final ExecutorService executorService, final UpdatableAnalyzers indexAnalyzers,
			final UpdatableAnalyzers queryAnalyzers, final FieldMap fieldMap,
			final FieldMapWrapper.Cache fieldMapWrappers, final IndexSearcher indexSearcher,
			final TaxonomyReader taxonomyReader) {
		super(indexProvider, resourceLoader, executorService, indexAnalyzers, queryAnalyzers, fieldMap);
		this.docValueReaderState = ((MultiThreadSearcherFactory.StateIndexSearcher) indexSearcher).state;
		this.fieldMap = fieldMap;
		this.fieldMapWrappers = fieldMapWrappers;
		this.indexSearcher = indexSearcher;
		this.indexReader = indexSearcher.getIndexReader();
		this.taxonomyReader = taxonomyReader;
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
			final ResultDocuments<T> resultDocuments) throws IOException {
		try {
			return new QueryExecution<T>(this, queryDefinition).execute(resultDocuments);
		} catch (ReflectiveOperationException | ParseException | QueryNodeException e) {
			throw ServerException.of(e);
		}
	}

	@Override
	public ResultDefinition.WithMap searchMap(QueryDefinition queryDefinition) throws IOException {
		final Set<String> returnedFields =
				queryDefinition.returned_fields != null && queryDefinition.returned_fields.contains("*") ?
						fieldMap.getStaticFieldSet() :
						queryDefinition.returned_fields;
		final ResultDocumentsMap resultDocumentsMap = new ResultDocumentsMap(this, queryDefinition, returnedFields);
		return (ResultDefinition.WithMap) search(queryDefinition, resultDocumentsMap);
	}

	@Override
	public <T> ResultDefinition.WithObject<T> searchObject(QueryDefinition queryDefinition, FieldMapWrapper<T> wrapper)
			throws IOException {
		final Set<String> returnedFields =
				queryDefinition.returned_fields != null && queryDefinition.returned_fields.contains("*") ?
						wrapper.fieldMap.keySet() :
						queryDefinition.returned_fields;
		final ResultDocumentsObject<T> resultDocumentsObject =
				new ResultDocumentsObject<>(this, queryDefinition, returnedFields, wrapper);
		return (ResultDefinition.WithObject<T>) search(queryDefinition, resultDocumentsObject);
	}

	@Override
	public <T> ResultDefinition.WithObject<T> searchObject(QueryDefinition queryDefinition, Class<T> objectClass)
			throws IOException {
		return searchObject(queryDefinition, fieldMapWrappers.get(objectClass));
	}

	@Override
	public ResultDefinition.Empty searchInterface(final QueryDefinition queryDefinition,
			final ResultDocumentsInterface resultDocuments) throws IOException {
		final ResultDocumentsEmpty resultDocumentEmpty = new ResultDocumentsEmpty(resultDocuments);
		return (ResultDefinition.Empty) search(queryDefinition, resultDocumentEmpty);
	}

}
