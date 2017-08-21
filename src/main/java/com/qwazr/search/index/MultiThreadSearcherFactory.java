/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SimpleMergedSegmentWarmer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.InfoStream;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

class MultiThreadSearcherFactory extends SearcherFactory {

	final private static IndexWriter.IndexReaderWarmer INDEX_READER_WARMER =
			new SimpleMergedSegmentWarmer(InfoStream.getDefault());

	static MultiThreadSearcherFactory of(ExecutorService executorService, boolean useWarmer, Similarity similarity) {
		return similarity == null ?
				new MultiThreadSearcherFactory(executorService, useWarmer) :
				new WithSimilarity(executorService, useWarmer, similarity);
	}

	protected final ExecutorService executorService;
	private final boolean useWarmer;

	private MultiThreadSearcherFactory(final ExecutorService executorService, boolean useWarmer) {
		this.executorService = executorService;
		this.useWarmer = useWarmer;
	}

	public IndexSearcher newSearcher(final IndexReader reader, final IndexReader previousReader) throws IOException {
		if (useWarmer)
			for (LeafReaderContext context : reader.leaves())
				INDEX_READER_WARMER.warm(context.reader());
		return new IndexSearcher(reader, executorService);
	}

	static class WithSimilarity extends MultiThreadSearcherFactory {

		private final Similarity similarity;

		private WithSimilarity(final ExecutorService executorService, final boolean useWarmer,
				final Similarity similarity) {
			super(executorService, useWarmer);
			this.similarity = similarity;
		}

		final public IndexSearcher newSearcher(final IndexReader reader, final IndexReader previousReader)
				throws IOException {
			final IndexSearcher searcher = super.newSearcher(reader, previousReader);
			searcher.setSimilarity(similarity);
			return searcher;
		}
	}
}
