/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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
import com.qwazr.utils.StringUtils;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;

import java.util.concurrent.ExecutorService;

final public class QueryContext {

	final public IndexSearcher indexSearcher;
	final public TaxonomyReader taxonomyReader;
	final public ExecutorService executorService;
	final public SortedSetDocValuesReaderState docValueReaderState;
	final public UpdatableAnalyzer indexAnalyzer;
	final public UpdatableAnalyzer queryAnalyzer;
	final public FieldMap fieldMap;
	final public QueryDefinition queryDefinition;
	final public String queryString;
	final public ResourceLoader resourceLoader;
	final public ClassLoaderManager classLoaderManager;
	final public SchemaInstance schemaInstance;

	public QueryContext(final SchemaInstance schemaInstance, final ResourceLoader resourceLoader,
			final IndexSearcher indexSearcher, final TaxonomyReader taxonomyReader,
			final ExecutorService executorService, final UpdatableAnalyzer indexAnalyzer,
			final UpdatableAnalyzer queryAnalyzer, final FieldMap fieldMap,
			final SortedSetDocValuesReaderState docValueReaderState, final QueryDefinition queryDefinition) {
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
		this.queryDefinition = queryDefinition;
		this.queryString = queryDefinition == null ? null : getFinalQueryString(queryDefinition);
	}

	private static String getFinalQueryString(final QueryDefinition queryDef) {
		// Deal wih query string
		final String qs;
		// Check if we have to escape some characters
		if (queryDef.escape_query != null && queryDef.escape_query) {
			if (queryDef.escaped_chars != null && queryDef.escaped_chars.length > 0)
				qs = StringUtils.escape_chars(queryDef.query_string, queryDef.escaped_chars);
			else
				qs = QueryParser.escape(queryDef.query_string);
		} else
			qs = queryDef.query_string;
		return qs;
	}

}
