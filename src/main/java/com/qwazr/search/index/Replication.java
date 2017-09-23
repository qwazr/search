/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qwazr.search.index;

import com.qwazr.utils.IOUtils;
import org.apache.lucene.facet.taxonomy.SearcherTaxonomyManager;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.replicator.IndexAndTaxonomyRevision;
import org.apache.lucene.replicator.IndexRevision;
import org.apache.lucene.replicator.LocalReplicator;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.net.URISyntaxException;

interface Replication {

	interface Master {
		LocalReplicator getLocalReplicator();
	}

	final class MasterWithTaxo extends WriterAndSearcher.WithTaxo implements Master {

		final LocalReplicator localReplicator;

		MasterWithTaxo(final IndexWriter indexWriter,
				final IndexAndTaxonomyRevision.SnapshotDirectoryTaxonomyWriter taxonomyWriter,
				final SearcherFactory searcherFactory) throws IOException {
			super(indexWriter, taxonomyWriter,
					new SearcherTaxonomyManager(indexWriter, true, searcherFactory, taxonomyWriter));
			localReplicator = new LocalReplicator();
			localReplicator.publish(newRevision());
		}

		private IndexAndTaxonomyRevision newRevision() throws IOException {
			return new IndexAndTaxonomyRevision(indexWriter, taxonomyWriter);
		}

		@Override
		public void commit() throws IOException {
			super.commit();
			localReplicator.publish(newRevision());
		}

		@Override
		public void close() throws IOException {
			IOUtils.closeQuietly(localReplicator);
			super.close();
		}

		@Override
		public LocalReplicator getLocalReplicator() {
			return localReplicator;
		}
	}

	final class SlaveWithTaxo extends WriterAndSearcher.WithTaxo implements IndexReplicator.Slave {

		private final IndexReplicator indexReplicator;

		SlaveWithTaxo(final IndexReplicator indexReplicator, final Directory dataDirectory,
				final Directory taxonomyDirectory, final SearcherFactory searcherFactory)
				throws IOException, URISyntaxException {
			super(null, null, new SearcherTaxonomyManager(dataDirectory, taxonomyDirectory, searcherFactory));
			this.indexReplicator = indexReplicator;
		}

		@Override
		public void close() throws IOException {
			super.close();
			IOUtils.closeQuietly(indexReplicator);
		}

		@Override
		public IndexReplicator getIndexReplicator() {
			return indexReplicator;
		}

	}

	final class MasterNoTaxo extends WriterAndSearcher.NoTaxo implements Master {

		final LocalReplicator localReplicator;

		MasterNoTaxo(final IndexWriter indexWriter, final SearcherFactory searcherFactory) throws IOException {
			super(indexWriter, new SearcherManager(indexWriter, searcherFactory));
			localReplicator = new LocalReplicator();
			localReplicator.publish(newRevision());
		}

		private IndexRevision newRevision() throws IOException {
			return new IndexRevision(indexWriter);
		}

		@Override
		public void commit() throws IOException {
			super.commit();
			localReplicator.publish(newRevision());
		}

		@Override
		public void close() throws IOException {
			super.close();
			IOUtils.closeQuietly(localReplicator);
		}

		@Override
		public LocalReplicator getLocalReplicator() {
			return localReplicator;
		}
	}

	final class SlaveNoTaxo extends WriterAndSearcher.NoTaxo implements IndexReplicator.Slave {

		private final IndexReplicator indexReplicator;

		SlaveNoTaxo(final IndexReplicator indexReplicator, final Directory dataDirectory,
				final SearcherFactory searcherFactory) throws IOException, URISyntaxException {
			super(null, new SearcherManager(dataDirectory, searcherFactory));
			this.indexReplicator = indexReplicator;
		}

		@Override
		public void close() throws IOException {
			super.close();
			IOUtils.closeQuietly(indexReplicator);
		}

		@Override
		public IndexReplicator getIndexReplicator() {
			return indexReplicator;
		}
	}
}
