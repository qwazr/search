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

package com.qwazr.search.replication;

import com.qwazr.utils.HashUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.replicator.IndexAndTaxonomyRevision;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public interface MasterNode extends Closeable {

	ReplicationSession newSession() throws IOException;

	InputStream getFile(String sessionId, ReplicationProcess.Source source, String fileName)
			throws FileNotFoundException;

	void releaseSession(String sessionId) throws IOException;

	abstract class Base implements MasterNode {

		private final String masterUuid;

		protected Base(final String masterUuid) {
			this.masterUuid = masterUuid;
		}

		protected abstract void fillSession(final String sessionUuid, final Map<String, Map<String, Long>> sessionMap)
				throws IOException;

		final public ReplicationSession newSession() throws IOException {
			final Map<String, Map<String, Long>> sessionMap = new HashMap<>();
			final String sessionUuid = HashUtils.newTimeBasedUUID().toString();
			fillSession(sessionUuid, sessionMap);
			return new ReplicationSession(masterUuid, sessionUuid, sessionMap);
		}

	}

	class WithIndex extends Base {

		private final Path indexDirectoryPath;
		private final SnapshotDeletionPolicy indexSnapshot;
		private final HashMap<String, IndexView.FromCommit> indexSessions;

		public WithIndex(final String masterUuid, final Path indexDirectoryPath, final IndexWriter indexWriter)
				throws IOException {
			super(masterUuid);
			this.indexDirectoryPath = indexDirectoryPath;
			this.indexSnapshot = (SnapshotDeletionPolicy) indexWriter.getConfig().getIndexDeletionPolicy();
			this.indexSessions = new HashMap<>();
		}

		@Override
		protected void fillSession(String sessionId, Map<String, Map<String, Long>> sessionMap) throws IOException {
			synchronized (indexSessions) {
				final IndexView.FromCommit indexView = new IndexView.FromCommit(indexDirectoryPath, indexSnapshot);
				indexSessions.put(sessionId, indexView);
				sessionMap.put(ReplicationProcess.Source.index.name(), indexView.getFiles());
			}
		}

		@Override
		public InputStream getFile(String sessionId, ReplicationProcess.Source source, String fileName)
				throws FileNotFoundException {
			if (source != null && source != ReplicationProcess.Source.index)
				return null;
			final IndexView.FromCommit indexView = indexSessions.get(sessionId);
			return indexView == null ? null : indexView.getFile(fileName);
		}

		@Override
		public void releaseSession(String sessionId) throws IOException {
			synchronized (indexSessions) {
				final IndexView.FromCommit indexView = indexSessions.remove(sessionId);
				if (indexView != null)
					indexView.close();
			}
		}

		@Override
		public void close() throws IOException {
			synchronized (indexSessions) {
				for (IndexView.FromCommit indexView : indexSessions.values())
					indexView.close();
			}
		}

	}

	class WithIndexAndTaxo extends WithIndex {

		private final Path taxoDirectoryPath;
		private final SnapshotDeletionPolicy taxoSnapshots;
		private final HashMap<String, IndexView.FromCommit> taxoSessions;

		public WithIndexAndTaxo(final String masterUuid, final Path indexDirectoryPath, final IndexWriter indexWriter,
				final Path taxoDirectoryPath, IndexAndTaxonomyRevision.SnapshotDirectoryTaxonomyWriter taxonomyWriter)
				throws IOException {
			super(masterUuid, indexDirectoryPath, indexWriter);
			this.taxoDirectoryPath = taxoDirectoryPath;
			this.taxoSnapshots = taxonomyWriter.getDeletionPolicy();
			this.taxoSessions = new HashMap<>();
		}

		@Override
		protected void fillSession(String sessionId, Map<String, Map<String, Long>> sessionMap) throws IOException {
			super.fillSession(sessionId, sessionMap);
			synchronized (taxoSessions) {
				final IndexView.FromCommit indexView = new IndexView.FromCommit(taxoDirectoryPath, taxoSnapshots);
				taxoSessions.put(sessionId, indexView);
				sessionMap.put(ReplicationProcess.Source.taxo.name(), indexView.getFiles());
			}
		}

		public InputStream getFile(String sessionId, ReplicationProcess.Source source, String fileName)
				throws FileNotFoundException {
			if (source != null && source != ReplicationProcess.Source.taxo)
				return super.getFile(sessionId, source, fileName);
			final IndexView.FromCommit indexView = taxoSessions.get(sessionId);
			return indexView == null ? null : indexView.getFile(fileName);
		}

		@Override
		public void releaseSession(String sessionId) throws IOException {
			super.releaseSession(sessionId);
			synchronized (taxoSessions) {
				final IndexView.FromCommit indexView = taxoSessions.remove(sessionId);
				if (indexView != null)
					indexView.close();
			}
		}

		@Override
		public void close() throws IOException {
			super.close();
			synchronized (taxoSessions) {
				for (IndexView.FromCommit indexView : taxoSessions.values())
					indexView.close();
			}
		}
	}
}