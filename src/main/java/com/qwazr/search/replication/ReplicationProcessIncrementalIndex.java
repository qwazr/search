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
import com.qwazr.utils.IOUtils;
import org.apache.lucene.store.Directory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashSet;

class ReplicationProcessIncrementalIndex implements ReplicationProcess {

	private final Path indexDirectoryPath;
	private final Source source;
	private final SourceFileProvider sourceFileProvider;
	private final Path indexWorkDirectory;
	private final Collection<String> indexFilesToObtain;
	private final Collection<String> indexFilesToDelete;

	ReplicationProcessIncrementalIndex(final Path workDirectory, final Path indexDirectoryPath,
			final Directory indexDirectory, final Source source, final ReplicationSession masterFiles,
			final SourceFileProvider sourceFileProvider) throws IOException {
		this.indexDirectoryPath = indexDirectoryPath;
		this.source = source;
		this.sourceFileProvider = sourceFileProvider;
		this.indexWorkDirectory = workDirectory.resolve(source.name());
		this.indexFilesToObtain = new HashSet<>();
		this.indexFilesToDelete = new HashSet<>();
		new IndexView.FromDirectory(indexDirectoryPath, indexDirectory).analyse(
				masterFiles.getSourceFiles(source).keySet(), indexFilesToObtain, indexFilesToDelete);
	}

	ReplicationProcessIncrementalIndex(final Path workDirectory, final Path indexDirectoryPath,
			final Directory indexDirectory, final ReplicationSession masterFiles,
			final SourceFileProvider sourceFileProvider) throws IOException {
		this(workDirectory, indexDirectoryPath, indexDirectory, Source.index, masterFiles, sourceFileProvider);
	}

	@Override
	public void obtainNewFiles() throws IOException {
		if (!Files.exists(indexWorkDirectory))
			Files.createDirectory(indexWorkDirectory);
		for (String fileToObtain : indexFilesToObtain) {
			final File file = indexWorkDirectory.resolve(fileToObtain).toFile();
			try (final InputStream input = sourceFileProvider.obtain(source, fileToObtain)) {
				IOUtils.copy(input, file);
			}
		}
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
