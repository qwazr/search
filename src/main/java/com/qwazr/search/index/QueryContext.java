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

import com.qwazr.search.analysis.UpdatableAnalyzer;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.search.IndexSearcher;

final public class QueryContext {

	final public IndexSearcher indexSearcher;
	final public SortedSetDocValuesReaderState state;
	final public UpdatableAnalyzer indexAnalyzer;
	final public UpdatableAnalyzer queryAnalyzer;
	final public QueryDefinition queryDefinition;
	final public String queryString;
	final public SchemaInstance schemaInstance;

	QueryContext(final SchemaInstance schemaInstance, final IndexSearcher indexSearcher,
			final UpdatableAnalyzer indexAnalyzer, final UpdatableAnalyzer queryAnalyzer,
			final SortedSetDocValuesReaderState state, final QueryDefinition queryDefinition) {
		this.schemaInstance = schemaInstance;
		this.indexSearcher = indexSearcher;
		this.state = state;
		this.indexAnalyzer = indexAnalyzer;
		this.queryAnalyzer = queryAnalyzer;
		this.queryDefinition = queryDefinition;
		this.queryString = queryDefinition == null ? null : QueryUtils.getFinalQueryString(queryDefinition);
	}

}
