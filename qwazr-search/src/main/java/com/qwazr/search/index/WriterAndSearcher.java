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

import com.qwazr.utils.IOUtils;
import org.apache.lucene.facet.taxonomy.SearcherTaxonomyManager;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ReferenceManager;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

interface WriterAndSearcher extends Closeable {

	void refresh() throws IOException;

	void reload() throws IOException;

	<T> T search(final SearchAction<T> action) throws IOException;

	<T> T write(final WriteAction<T> action) throws IOException;

	void commit() throws IOException;

	IndexWriter getIndexWriter();

	abstract class Common implements WriterAndSearcher {

		final IndexWriter indexWriter;

		protected Common(final IndexWriter indexWriter) {
			this.indexWriter = indexWriter;
		}

		@Override
		final public IndexWriter getIndexWriter() {
			return indexWriter;
		}

	}

	@FunctionalInterface
	interface SearcherManagerFactory {
		ReferenceManager<IndexSearcher> supply() throws IOException;
	}

	class WithIndex extends Common {

		private final SearcherManagerFactory searcherManagerFactory;
		private volatile ReferenceManager<IndexSearcher> searcherManager;

		WithIndex(final IndexWriter indexWriter, final SearcherManagerFactory searcherManagerFactory)
				throws IOException {
			super(indexWriter);
			this.searcherManagerFactory = searcherManagerFactory;
			this.searcherManager = searcherManagerFactory.supply();
			refresh();
		}

		@Override
		final public void refresh() throws IOException {
			searcherManager.maybeRefresh();
		}

		@Override
		final synchronized public void reload() throws IOException {
			final ReferenceManager<IndexSearcher> oldSearcherManager = searcherManager;
			searcherManager = searcherManagerFactory.supply();
			oldSearcherManager.close();
		}

		@Override
		final public <T> T search(final SearchAction<T> action) throws IOException {
			final ReferenceManager<IndexSearcher> sm =
					Objects.requireNonNull(searcherManager, "No SearchManager available");
			final IndexSearcher searcher = sm.acquire();
			try {
				return action.apply(searcher, null);
			} finally {
				sm.release(searcher);
			}
		}

		@Override
		final public <T> T write(final WriteAction<T> action) throws IOException {
			return action.apply(indexWriter, null);
		}

		@Override
		public void commit() throws IOException {
			indexWriter.flush();
			indexWriter.commit();
			refresh();
		}

		@Override
		public synchronized void close() {
			if (searcherManager != null) {
				IOUtils.closeQuietly(searcherManager);
				searcherManager = null;
			}
			if (indexWriter != null && indexWriter.isOpen())
				IOUtils.closeQuietly(indexWriter);
		}

	}

	@FunctionalInterface
	interface SearcherTaxonomyManagerFactory {
		SearcherTaxonomyManager supply() throws IOException;
	}

	class WithIndexAndTaxo extends Common {

		private final SearcherTaxonomyManagerFactory searcherTaxonomyManagerFactory;
		private final SnapshotDirectoryTaxonomyWriter taxonomyWriter;
		private volatile SearcherTaxonomyManager searcherTaxonomyManager;

		WithIndexAndTaxo(final IndexWriter indexWriter, final SnapshotDirectoryTaxonomyWriter taxonomyWriter,
				final SearcherTaxonomyManagerFactory searcherTaxonomyManagerFactory) throws IOException {
			super(indexWriter);
			this.taxonomyWriter = taxonomyWriter;
			this.searcherTaxonomyManagerFactory = searcherTaxonomyManagerFactory;
			this.searcherTaxonomyManager = searcherTaxonomyManagerFactory.supply();
			refresh();
		}

		@Override
		final public void refresh() throws IOException {
			searcherTaxonomyManager.maybeRefresh();
		}

		@Override
		final synchronized public void reload() throws IOException {
			final SearcherTaxonomyManager oldSearcherManager = searcherTaxonomyManager;
			searcherTaxonomyManager = searcherTaxonomyManagerFactory.supply();
			oldSearcherManager.close();
		}

		@Override
		final public <T> T search(final SearchAction<T> action) throws IOException {
			final SearcherTaxonomyManager sm =
					Objects.requireNonNull(searcherTaxonomyManager, "No SearcherTaxonomyManager available");
			final SearcherTaxonomyManager.SearcherAndTaxonomy reference = sm.acquire();
			try {
				return action.apply(reference.searcher, reference.taxonomyReader);
			} finally {
				sm.release(reference);
			}
		}

		@Override
		final public <T> T write(final WriteAction<T> action) throws IOException {
			return action.apply(indexWriter, taxonomyWriter);
		}

		@Override
		public void commit() throws IOException {
			taxonomyWriter.getIndexWriter().flush();
			taxonomyWriter.commit();
			indexWriter.flush();
			indexWriter.commit();
			refresh();
		}

		@Override
		public synchronized void close() {
			if (searcherTaxonomyManager != null) {
				IOUtils.closeQuietly(searcherTaxonomyManager);
				searcherTaxonomyManager = null;
			}

			if (taxonomyWriter != null)
				IOUtils.closeQuietly(taxonomyWriter);

			if (indexWriter != null && indexWriter.isOpen())
				IOUtils.closeQuietly(indexWriter);
		}
	}

	@FunctionalInterface
	interface SearchAction<T> {
		T apply(final IndexSearcher indexSearcher, final TaxonomyReader taxonomyReader) throws IOException;
	}

	@FunctionalInterface
	interface WriteAction<T> {
		T apply(final IndexWriter indexWriter, final SnapshotDirectoryTaxonomyWriter taxonomyWriter) throws IOException;
	}

}
