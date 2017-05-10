/**
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
 **/

package com.qwazr.search.index;

import com.qwazr.utils.IOUtils;
import org.apache.lucene.facet.taxonomy.SearcherTaxonomyManager;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.replicator.IndexAndTaxonomyRevision;
import org.apache.lucene.replicator.IndexRevision;
import org.apache.lucene.replicator.Revision;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

interface WriterAndSearcher extends Closeable {

	void refresh() throws IOException;

	Revision newRevision() throws IOException;

	<T> T search(final SearchAction<T> action) throws IOException;

	<T> T write(final WriteAction<T> action) throws IOException;

	void commit() throws IOException;

	IndexWriter getIndexWriter();

	static WriterAndSearcher of(final Directory dataDirectory, final Directory taxonomyDirectory,
			final SearcherFactory searcherFactory) throws IOException {
		if (taxonomyDirectory == null)
			return new WithoutTaxo(dataDirectory, searcherFactory);
		else
			return new WithTaxo(dataDirectory, taxonomyDirectory, searcherFactory);
	}

	static WriterAndSearcher of(final IndexWriter indexWriter,
			final IndexAndTaxonomyRevision.SnapshotDirectoryTaxonomyWriter taxonomyWriter,
			final SearcherFactory searcherFactory) throws IOException {
		if (taxonomyWriter == null)
			return new WithoutTaxo(indexWriter, searcherFactory);
		else
			return new WithTaxo(indexWriter, taxonomyWriter, searcherFactory);
	}

	abstract class Common implements WriterAndSearcher {

		protected final ReentrantLock commitLock;

		final IndexWriter indexWriter;

		protected Common(final IndexWriter indexWriter) {
			commitLock = new ReentrantLock(true);
			this.indexWriter = indexWriter;
		}

		@Override
		final public IndexWriter getIndexWriter() {
			return indexWriter;
		}
	}

	final class WithoutTaxo extends Common {

		final SearcherManager searcherManager;

		WithoutTaxo(final IndexWriter indexWriter, final SearcherFactory searcherFactory) throws IOException {
			super(indexWriter);
			this.searcherManager = new SearcherManager(indexWriter, searcherFactory);
		}

		WithoutTaxo(final Directory dataDirectory, final SearcherFactory searcherFactory) throws IOException {
			super(null);
			searcherManager = new SearcherManager(dataDirectory, searcherFactory);
		}

		@Override
		final public void refresh() throws IOException {
			searcherManager.maybeRefresh();
		}

		@Override
		final public IndexRevision newRevision() throws IOException {
			return new IndexRevision(indexWriter);
		}

		@Override
		final public <T> T search(final SearchAction<T> action) throws IOException {
			final IndexSearcher searcher = searcherManager.acquire();
			try {
				return action.apply(searcher, null);
			} finally {
				searcherManager.release(searcher);
			}
		}

		@Override
		final public <T> T write(final WriteAction<T> action) throws IOException {
			return action.apply(indexWriter, null);
		}

		@Override
		final public void commit() throws IOException {
			commitLock.lock();
			try {
				indexWriter.flush();
				indexWriter.commit();
				searcherManager.maybeRefresh();
			} finally {
				commitLock.unlock();
			}
		}

		@Override
		public void close() throws IOException {
			IOUtils.closeQuietly(searcherManager);
			if (indexWriter != null && indexWriter.isOpen())
				IOUtils.closeQuietly(indexWriter);
		}
	}

	final class WithTaxo extends Common {

		final IndexAndTaxonomyRevision.SnapshotDirectoryTaxonomyWriter taxonomyWriter;
		final SearcherTaxonomyManager searcherTaxonomyManager;

		WithTaxo(final IndexWriter indexWriter,
				final IndexAndTaxonomyRevision.SnapshotDirectoryTaxonomyWriter taxonomyWriter,
				final SearcherFactory searcherFactory) throws IOException {
			super(indexWriter);
			this.taxonomyWriter = taxonomyWriter;
			this.searcherTaxonomyManager =
					new SearcherTaxonomyManager(indexWriter, true, searcherFactory, taxonomyWriter);
		}

		WithTaxo(final Directory dataDirectory, final Directory taxonomyDirectory,
				final SearcherFactory searcherFactory) throws IOException {
			super(null);
			taxonomyWriter = null;
			searcherTaxonomyManager = new SearcherTaxonomyManager(dataDirectory, taxonomyDirectory, searcherFactory);
		}

		@Override
		final public void refresh() throws IOException {
			searcherTaxonomyManager.maybeRefresh();
		}

		@Override
		public IndexAndTaxonomyRevision newRevision() throws IOException {
			return new IndexAndTaxonomyRevision(indexWriter, taxonomyWriter);
		}

		@Override
		final public <T> T search(final SearchAction<T> action) throws IOException {
			final SearcherTaxonomyManager.SearcherAndTaxonomy reference = searcherTaxonomyManager.acquire();
			try {
				return action.apply(reference.searcher, reference.taxonomyReader);
			} finally {
				searcherTaxonomyManager.release(reference);
			}
		}

		@Override
		final public <T> T write(final WriteAction<T> action) throws IOException {
			return action.apply(indexWriter, taxonomyWriter);
		}

		@Override
		final public void commit() throws IOException {
			commitLock.lock();
			try {
				taxonomyWriter.getIndexWriter().flush();
				taxonomyWriter.commit();
				indexWriter.flush();
				indexWriter.commit();
				searcherTaxonomyManager.maybeRefresh();
			} finally {
				commitLock.unlock();
			}
		}

		@Override
		public void close() throws IOException {

			IOUtils.closeQuietly(searcherTaxonomyManager);

			if (taxonomyWriter != null)
				IOUtils.closeQuietly(taxonomyWriter);

			if (indexWriter != null && indexWriter.isOpen())
				IOUtils.closeQuietly(indexWriter);
		}
	}

	interface SearchAction<T> {

		T apply(final IndexSearcher indexSearcher, final TaxonomyReader taxonomyReader) throws IOException;
	}

	interface WriteAction<T> {

		T apply(final IndexWriter indexWriter,
				final IndexAndTaxonomyRevision.SnapshotDirectoryTaxonomyWriter taxonomyWriter) throws IOException;
	}

}
