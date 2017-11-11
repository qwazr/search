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
import com.qwazr.search.replication.ReplicationProcess;
import com.qwazr.search.replication.ReplicationSession;
import org.apache.lucene.index.IndexWriter;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

interface ReplicationMaster extends Closeable {

	ReplicationSession newReplicationSession() throws IOException;

	InputStream getItem(final String sessionId, final ReplicationProcess.Source source, final String itemName)
			throws FileNotFoundException;

	void releaseSession(String sessionId) throws IOException;

	abstract class Base implements ReplicationMaster {

		private final MasterNode masterNode;

		private final ConcurrentHashMap<String, ReplicationSession> sessions;

		private Base(final MasterNode masterNode) {
			this.masterNode = masterNode;
			sessions = new ConcurrentHashMap<>();
		}

		@Override
		final public ReplicationSession newReplicationSession() throws IOException {
			final ReplicationSession newSession = masterNode.newSession();
			sessions.put(newSession.sessionUuid, newSession);
			return newSession;
		}

		@Override
		final public InputStream getItem(final String sessionId, final ReplicationProcess.Source source,
				final String fileName) throws FileNotFoundException {
			return masterNode.getItem(sessionId, source, fileName);
		}

		@Override
		final public void releaseSession(final String id) throws IOException {
			masterNode.releaseSession(id);
			sessions.remove(id);
		}

		@Override
		final public void close() throws IOException {
			for (final ReplicationSession session : sessions.values())
				releaseSession(session.sessionUuid);
			masterNode.close();
		}

	}

	final class WithIndex extends Base {

		WithIndex(final String masterUuid, final IndexFileSet indexFileSet, final IndexWriter indexWriter)
				throws IOException {
			super(new MasterNode.WithIndex(masterUuid, indexFileSet.resourcesDirectoryPath, indexFileSet.dataDirectory,
					indexWriter, indexFileSet.mainDirectory, IndexFileSet.ANALYZERS_FILE, IndexFileSet.FIELDS_FILE));
		}
	}

	final class WithIndexAndTaxo extends Base {

		WithIndexAndTaxo(final String masterUuid, final IndexFileSet indexFileSet, final IndexWriter indexWriter,
				final SnapshotDirectoryTaxonomyWriter taxonomyWriter) throws IOException {
			super(new MasterNode.WithIndexAndTaxo(masterUuid, indexFileSet.resourcesDirectoryPath,
					indexFileSet.dataDirectory, indexWriter, indexFileSet.taxonomyDirectory, taxonomyWriter,
					indexFileSet.mainDirectory, IndexFileSet.ANALYZERS_FILE, IndexFileSet.FIELDS_FILE));
		}
	}

}
