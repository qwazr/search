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

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SimpleMergedSegmentWarmer;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.InfoStream;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

class MultiThreadSearcherFactory extends SearcherFactory {

	static MultiThreadSearcherFactory of(ExecutorService executorService, boolean useWarmer, Similarity similarity) {
		return similarity == null ?
				new MultiThreadSearcherFactory(executorService, useWarmer) :
				new WithSimilarity(executorService, useWarmer, similarity);
	}

	private static final SimpleMergedSegmentWarmer WARMER = new SimpleMergedSegmentWarmer(InfoStream.getDefault());

	protected final ExecutorService executorService;
	private final boolean useWarmer;

	private MultiThreadSearcherFactory(final ExecutorService executorService, boolean useWarmer) {
		this.executorService = executorService;
		this.useWarmer = useWarmer;
	}

	protected IndexSearcher warm(final IndexReader indexReader, final IndexSearcher indexSearcher) throws IOException {
		if (!useWarmer)
			return indexSearcher;

		for (final LeafReaderContext context : indexReader.leaves())
			WARMER.warm(context.reader());

		final StoredFieldVisitor allFields = new StoredFieldVisitor() {
			@Override
			public StoredFieldVisitor.Status needsField(FieldInfo fieldInfo) throws IOException {
				return StoredFieldVisitor.Status.YES;
			}
		};

		final TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), 10);
		if (topDocs != null && topDocs.scoreDocs != null)
			for (final ScoreDoc scoreDoc : topDocs.scoreDocs)
				indexSearcher.doc(scoreDoc.doc, allFields);

		return indexSearcher;
	}

	public IndexSearcher newSearcher(final IndexReader reader, final IndexReader previousReader) throws IOException {
		return warm(reader, new IndexSearcher(reader, executorService));
	}

	static class WithSimilarity extends MultiThreadSearcherFactory {

		private final Similarity similarity;

		private WithSimilarity(final ExecutorService executorService, final boolean useWarmer,
				final Similarity similarity) {
			super(executorService, useWarmer);
			this.similarity = similarity;
		}

		public IndexSearcher newSearcher(final IndexReader reader, final IndexReader previousReader)
				throws IOException {
			final IndexSearcher searcher = new IndexSearcher(reader, executorService);
			searcher.setSimilarity(similarity);
			return warm(reader, searcher);
		}
	}
}
