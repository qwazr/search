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

import com.qwazr.utils.FileUtils;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

class ReplicationProcessIncrementalIndex extends ReplicationProcess.Common {

	private final Path indexDirectoryPath;

	ReplicationProcessIncrementalIndex(final Path workDirectory, final Path indexDirectoryPath,
			final Directory indexDirectory, final Source source, final ReplicationSession masterFiles,
			final SourceFileProvider sourceFileProvider) throws IOException {
		super(workDirectory, source, sourceFileProvider);
		this.indexDirectoryPath = indexDirectoryPath;
		new IndexView.FromDirectory(indexDirectoryPath, indexDirectory).incremental(
				masterFiles.getSourceFiles(source).keySet(), indexFilesToObtain, indexFilesToDelete);
	}

	ReplicationProcessIncrementalIndex(final Path workDirectory, final Path indexDirectoryPath,
			final Directory indexDirectory, final ReplicationSession masterFiles,
			final SourceFileProvider sourceFileProvider) throws IOException {
		this(workDirectory, indexDirectoryPath, indexDirectory, Source.index, masterFiles, sourceFileProvider);
	}

	@Override
	public void moveInPlaceNewFiles() throws IOException {
		for (String fileToMove : indexFilesToObtain) {
			final Path source = indexWorkDirectory.resolve(fileToMove);
			final Path target = indexDirectoryPath.resolve(fileToMove);
			Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
		}
	}

	@Override
	public void deleteOldFiles() throws IOException {
		for (String fileToDelete : indexFilesToDelete)
			Files.deleteIfExists(indexDirectoryPath.resolve(fileToDelete));
	}

	@Override
	public void close() throws IOException {
		FileUtils.deleteDirectory(indexWorkDirectory);
	}

}