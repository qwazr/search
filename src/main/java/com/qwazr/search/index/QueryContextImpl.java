/**
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

import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.search.analysis.UpdatableAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

final class QueryContextImpl implements QueryContext {

	final IndexSearcher indexSearcher;
	final TaxonomyReader taxonomyReader;
	final ExecutorService executorService;
	final SortedSetDocValuesReaderState docValueReaderState;
	final UpdatableAnalyzer indexAnalyzer;
	final UpdatableAnalyzer queryAnalyzer;
	final FieldMap fieldMap;
	final ResourceLoader resourceLoader;
	final ClassLoaderManager classLoaderManager;
	final SchemaInstance schemaInstance;

	QueryContextImpl(final SchemaInstance schemaInstance, final ResourceLoader resourceLoader,
			final IndexSearcher indexSearcher, final TaxonomyReader taxonomyReader,
			final ExecutorService executorService, final UpdatableAnalyzer indexAnalyzer,
			final UpdatableAnalyzer queryAnalyzer, final FieldMap fieldMap,
			final SortedSetDocValuesReaderState docValueReaderState) {
		this.schemaInstance = schemaInstance;
		this.classLoaderManager = schemaInstance == null ? null : schemaInstance.getClassLoaderManager();
		this.resourceLoader = resourceLoader;
		this.indexSearcher = indexSearcher;
		this.taxonomyReader = taxonomyReader;
		this.executorService = executorService;
		this.docValueReaderState = docValueReaderState;
		this.indexAnalyzer = indexAnalyzer;
		this.queryAnalyzer = queryAnalyzer;
		this.fieldMap = fieldMap;
	}

	@Override
	public IndexInstance getIndex(final String indexName) {
		return schemaInstance.get(indexName, false);
	}

	@Override
	public ClassLoaderManager getClassLoaderManager() {
		return classLoaderManager;
	}

	@Override
	public Analyzer getQueryAnalyzer() {
		return queryAnalyzer;
	}

	@Override
	public FacetsConfig getFacetsConfig(final String dimension) {
		return fieldMap.getFacetsConfig(dimension);
	}

	@Override
	public FacetsConfig getFacetsConfig(Collection<String> fieldSet) {
		return fieldMap.getFacetsConfig(fieldSet);
	}

	@Override
	public IndexReader getIndexReader() {
		return indexSearcher.getIndexReader();
	}

}
