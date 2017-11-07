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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashSet;

class ReplicationProcessImpl implements ReplicationProcess {

	private final Source source;
	private final Path indexDirectory;
	private final Path indexSourceDirectory;
	private final Collection<String> indexFilesToObtain;
	private final Collection<String> indexFilesToDelete;
	private final FileProvider fileProvider;

	ReplicationProcessImpl(final Path indexDirectory, final Path workDirectory, final Source source,
			final IndexView indexView, final ReplicationSession masterFiles, final FileProvider fileProvider) {
		this.indexDirectory = indexDirectory;
		this.source = source;
		this.indexSourceDirectory = workDirectory.resolve(source.name());
		this.indexFilesToObtain = new HashSet<>();
		this.indexFilesToDelete = new HashSet<>();
		this.fileProvider = fileProvider;
		indexView.analyse(masterFiles.files.get(source.name()), indexFilesToObtain, indexFilesToDelete);
	}

	@Override
	public void obtainNewFiles() throws IOException {
		if (!Files.exists(indexSourceDirectory))
			Files.createDirectory(indexSourceDirectory);
		for (String fileToObtain : indexFilesToObtain) {
			final File file = indexSourceDirectory.resolve(fileToObtain).toFile();
			try (final InputStream input = fileProvider.obtain(source.name(), fileToObtain)) {
				IOUtils.copy(input, file);
			}
		}
	}

	@Override
	public void moveInPlaceNewFiles() throws IOException {
		for (String fileToMove : indexFilesToObtain) {
			final Path source = indexSourceDirectory.resolve(fileToMove);
			final Path target = indexDirectory.resolve(fileToMove);
			Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
		}
	}

	@Override
	public void deleteOldFiles() throws IOException {
		for (String fileToDelete : indexFilesToDelete) {
			final Path toDelete = indexDirectory.resolve(fileToDelete);
			Files.deleteIfExists(toDelete);
		}
	}

	@Override
	public void close() throws IOException {
		FileUtils.deleteDirectory(indexSourceDirectory);
	}

}
