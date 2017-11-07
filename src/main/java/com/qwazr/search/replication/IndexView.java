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

import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.store.Directory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class IndexView {

	private final Collection<String> files;

	IndexView(final Iterable<String> fileNames) throws IOException {
		final Set<String> f = new HashSet<>();
		for (String fileName : fileNames)
			f.add(fileName);
		files = Collections.unmodifiableSet(f);
	}

	/**
	 * Compute which files are missing, and which files should be deleted
	 *
	 * @param referenceFiles the files of the reference (master)
	 * @param fileToGet      the files to get
	 * @param fileToDelete   the files to delete
	 */
	void analyse(final Collection<String> referenceFiles, final Collection<String> fileToGet,
			final Collection<String> fileToDelete) {
		for (final String referenceFile : referenceFiles)
			if (!files.contains(referenceFile))
				fileToGet.add(referenceFile);
		for (final String file : files)
			if (!referenceFiles.contains(file))
				fileToDelete.add(file);
	}

	Collection<String> getFiles() {
		return files;
	}

	static class FromCommit extends IndexView implements Closeable {

		private final SnapshotDeletionPolicy indexSnapshots;
		private final IndexCommit indexCommit;

		private FromCommit(final SnapshotDeletionPolicy indexSnapshots, final IndexCommit indexCommit)
				throws IOException {
			super(indexCommit.getFileNames());
			this.indexSnapshots = indexSnapshots;
			this.indexCommit = indexCommit;
		}

		FromCommit(final SnapshotDeletionPolicy indexSnapshots) throws IOException {
			this(indexSnapshots, indexSnapshots.snapshot());
		}

		@Override
		public void close() throws IOException {
			indexSnapshots.release(indexCommit);
		}
	}

	static class FromDirectory extends IndexView {

		FromDirectory(final Directory directory) throws IOException {
			super(SegmentInfos.readLatestCommit(directory).files(true));
		}

	}
}
