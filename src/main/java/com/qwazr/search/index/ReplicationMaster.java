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

import com.qwazr.search.replication.MasterNode;
import com.qwazr.search.replication.ReplicationSession;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.replicator.IndexAndTaxonomyRevision;

import java.io.Closeable;
import java.io.IOException;

interface ReplicationMaster extends Closeable {

	ReplicationSession newReplicationSession() throws IOException;

	abstract class Base implements ReplicationMaster {

		private final MasterNode masterNode;

		private Base(final MasterNode masterNode) {
			this.masterNode = masterNode;
		}

		@Override
		final public void close() throws IOException {
			masterNode.close();
		}

		public ReplicationSession newReplicationSession() throws IOException {
			return masterNode.newSession();
		}

	}

	final class WithIndexAndTaxo extends Base {

		WithIndexAndTaxo(final IndexWriter indexWriter,
				final IndexAndTaxonomyRevision.SnapshotDirectoryTaxonomyWriter taxonomyWriter) throws IOException {
			super(new MasterNode.WithIndexAndTaxo(indexWriter, taxonomyWriter));
		}
	}

	final class WithIndex extends Base {

		WithIndex(final IndexWriter indexWriter) throws IOException {
			super(new MasterNode.WithIndex(indexWriter));
		}
	}

}
