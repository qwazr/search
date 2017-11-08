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

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class IndexView {

	private final Path indexDirectoryPath;
	private final Map<String, Long> files;

	IndexView(final Path indexDirectoryPath, final Iterable<String> fileNames) throws IOException {
		this.indexDirectoryPath = indexDirectoryPath;
		final Map<String, Long> f = new LinkedHashMap<>();
		for (String fileName : fileNames)
			f.put(fileName, Files.size(indexDirectoryPath.resolve(fileName)));
		files = Collections.unmodifiableMap(f);
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
			if (!files.containsKey(referenceFile))
				fileToGet.add(referenceFile);
		for (final String file : files.keySet())
			if (!referenceFiles.contains(file))
				fileToDelete.add(file);
	}

	final Map<String, Long> getFiles() {
		return files;
	}

	final InputStream getFile(String fileName) throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(indexDirectoryPath.resolve(fileName).toFile()));
	}

	static class FromCommit extends IndexView implements Closeable {

		private final SnapshotDeletionPolicy indexSnapshots;
		private final IndexCommit indexCommit;

		private FromCommit(final Path directoryPath, final SnapshotDeletionPolicy indexSnapshots,
				final IndexCommit indexCommit) throws IOException {
			super(directoryPath, indexCommit.getFileNames());
			this.indexSnapshots = indexSnapshots;
			this.indexCommit = indexCommit;
		}

		FromCommit(final Path directoryPath, final SnapshotDeletionPolicy indexSnapshots) throws IOException {
			this(directoryPath, indexSnapshots, indexSnapshots.snapshot());
		}

		@Override
		public void close() throws IOException {
			indexSnapshots.release(indexCommit);
		}
	}

	static class FromDirectory extends IndexView {

		FromDirectory(final Path directoryPath, final Directory directory) throws IOException {
			super(directoryPath, SegmentInfos.readLatestCommit(directory).files(true));
		}

	}
}
