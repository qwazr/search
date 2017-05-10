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

import com.qwazr.search.analysis.UpdatableAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.facet.FacetsConfig;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

class IndexContextImpl implements IndexContext {

	final ExecutorService executorService;
	final UpdatableAnalyzer queryAnalyzer;
	final UpdatableAnalyzer indexAnalyzer;
	final FieldMap fieldMap;
	final ResourceLoader resourceLoader;
	final IndexInstance.Provider indexProvider;

	IndexContextImpl(final IndexInstance.Provider indexProvider, final ResourceLoader resourceLoader,
			final ExecutorService executorService, final UpdatableAnalyzer indexAnalyzer,
			final UpdatableAnalyzer queryAnalyzer, final FieldMap fieldMap) {
		this.indexProvider = indexProvider;
		this.resourceLoader = resourceLoader;
		this.executorService = executorService;
		this.queryAnalyzer = queryAnalyzer;
		this.indexAnalyzer = indexAnalyzer;
		this.fieldMap = fieldMap;
	}

	@Override
	final public IndexInstance getIndex(final String indexName) {
		return indexProvider == null ? null : indexProvider.getIndex(indexName);
	}

	@Override
	final public Analyzer getQueryAnalyzer() {
		return queryAnalyzer;
	}

	@Override
	final public Analyzer getIndexAnalyzer() {
		return indexAnalyzer;
	}

	@Override
	final public FacetsConfig getFacetsConfig(final String dimension) {
		return fieldMap.getFacetsConfig(dimension);
	}

	@Override
	final public FacetsConfig getFacetsConfig(Collection<String> fieldSet) {
		return fieldMap.getFacetsConfig(fieldSet);
	}

}
