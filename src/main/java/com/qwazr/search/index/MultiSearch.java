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

import com.qwazr.server.ServerException;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.concurrent.ReferenceCounter;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.IndexSearcher;

import java.io.Closeable;
import java.io.IOException;

class MultiSearch implements Closeable, AutoCloseable {

	private final MultiSearchContext context;
	private final MultiReader multiReader;
	private final IndexSearcher indexSearcher;
	private final ReferenceCounter refCounter;

	MultiSearch(final MultiSearchContext context) throws IOException, ServerException {
		this.refCounter = new ReferenceCounter.Impl().acquire();
		this.context = context;
		if (context.indexReaders == null) {
			multiReader = null;
			indexSearcher = null;
			return;
		}
		multiReader = new MultiReader(context.indexReaders);
		indexSearcher = new IndexSearcher(multiReader);
	}

	int numDocs() {
		incRef();
		try {
			return multiReader == null ? 0 : multiReader.numDocs();
		} finally {
			decRef();
		}
	}

	private synchronized void doClose() {
		IOUtils.close(multiReader);
	}

	@Override
	final public void close() {
		decRef();
	}

	final void incRef() {
		refCounter.acquire();
	}

	final void decRef() {
		if (refCounter.release() > 0)
			return;
		doClose();
	}

	<T extends ResultDocumentAbstract> ResultDefinition<T> search(final QueryDefinition queryDef,
			final ResultDocuments<T> resultDocuments)
			throws IOException, QueryNodeException, ParseException, ReflectiveOperationException {
		if (indexSearcher == null)
			return null;
		incRef();
		try {
			final SortedSetDocValuesReaderState state =
					IndexUtils.getNewFacetsState(indexSearcher.getIndexReader(), null);
			try (final QueryContextImpl queryContext = new QueryContextImpl(context.indexProvider, null,
					context.executorService, context.indexAnalyzers, context.queryAnalyzers, context.fieldMap, null,
					state, indexSearcher, null)) {
				return new QueryExecution<T>(queryContext, queryDef).execute(resultDocuments);
			}
		} finally {
			decRef();
		}
	}
}
