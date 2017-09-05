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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.SegmentCommitInfo;
import org.apache.lucene.replicator.nrt.CopyJob;
import org.apache.lucene.replicator.nrt.FileMetaData;
import org.apache.lucene.replicator.nrt.PrimaryNode;
import org.apache.lucene.replicator.nrt.ReplicaNode;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.Map;

public class ReplicationNrt {

	private static class Primary extends PrimaryNode {

		Primary(IndexWriter writer, long primaryGen, SearcherFactory searcherFactory) throws IOException {
			super(writer, 1, primaryGen, -1, searcherFactory, /* TODO: Remove */
					System.out);
		}

		@Override
		protected void preCopyMergedSegmentFiles(SegmentCommitInfo info, Map<String, FileMetaData> files)
				throws IOException {
		}
	}

	private static class Replica extends ReplicaNode {

		public Replica(int id, Directory dir, long currentGen, SearcherFactory searcherFactory) throws IOException {
			super(id, dir, searcherFactory, /* TODO: Remove */System.out);
			start(currentGen);
		}

		@Override
		protected CopyJob newCopyJob(String reason, Map<String, FileMetaData> files,
				Map<String, FileMetaData> prevFiles, boolean highPriority, CopyJob.OnceDone onceDone)
				throws IOException {
			return null;
		}

		@Override
		protected void launch(CopyJob job) {

		}

		@Override
		protected void sendNewReplica() throws IOException {

		}
	}

	final static class Master extends WriterAndSearcher.NoTaxo {

		private final Primary primary;

		Master(final IndexWriter indexWriter, final Primary primary) throws IOException {
			super(indexWriter, primary.getSearcherManager());
			this.primary = primary;
		}

		public void close() throws IOException {
			if (!primary.isClosed())
				IOUtils.closeQuietly(primary);
			super.close();
		}
	}

	static WriterAndSearcher master(IndexWriter indexWriter, long primaryGen, SearcherFactory searcherFactory)
			throws IOException {
		final Primary primary = new Primary(indexWriter, primaryGen, searcherFactory);
		return new Master(indexWriter, primary);
	}

	private static class Slave extends WriterAndSearcher.NoTaxo {

		private final Replica replica;

		Slave(Replica replica) throws IOException {
			super(null, replica.getSearcherManager());
			this.replica = replica;
		}

		public void close() throws IOException {
			super.close();
			IOUtils.closeQuietly(replica);
		}
	}

	static WriterAndSearcher slave(Directory dataDirectory, long currentGen, SearcherFactory searcherFactory)
			throws IOException {
		final Replica replica = new Replica(2, dataDirectory, currentGen, searcherFactory);
		return new Slave(replica);
	}
}
