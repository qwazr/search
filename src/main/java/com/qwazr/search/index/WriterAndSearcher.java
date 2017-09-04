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
import org.apache.lucene.replicator.IndexAndTaxonomyRevision;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ReferenceManager;

import java.io.Closeable;
import java.io.IOException;

interface WriterAndSearcher extends Closeable {

	void refresh() throws IOException;

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

	class NoTaxo extends Common {

		final ReferenceManager<IndexSearcher> searcherManager;

		NoTaxo(final IndexWriter indexWriter, final ReferenceManager<IndexSearcher> searcherManager)
				throws IOException {
			super(indexWriter);
			this.searcherManager = searcherManager;
		}

		@Override
		final public void refresh() throws IOException {
			searcherManager.maybeRefresh();
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
		public void commit() throws IOException {
			indexWriter.flush();
			indexWriter.commit();
			searcherManager.maybeRefresh();
		}

		@Override
		public void close() throws IOException {
			IOUtils.closeQuietly(searcherManager);
			if (indexWriter != null && indexWriter.isOpen())
				IOUtils.closeQuietly(indexWriter);
		}
	}

	class WithTaxo extends Common {

		final IndexAndTaxonomyRevision.SnapshotDirectoryTaxonomyWriter taxonomyWriter;
		final SearcherTaxonomyManager searcherTaxonomyManager;

		WithTaxo(final IndexWriter indexWriter,
				final IndexAndTaxonomyRevision.SnapshotDirectoryTaxonomyWriter taxonomyWriter,
				final SearcherTaxonomyManager searcherTaxonomyManager) throws IOException {
			super(indexWriter);
			this.taxonomyWriter = taxonomyWriter;
			this.searcherTaxonomyManager = searcherTaxonomyManager;
		}

		@Override
		final public void refresh() throws IOException {
			searcherTaxonomyManager.maybeRefresh();
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
		public void commit() throws IOException {
			taxonomyWriter.getIndexWriter().flush();
			taxonomyWriter.commit();
			indexWriter.flush();
			indexWriter.commit();
			searcherTaxonomyManager.maybeRefresh();
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
