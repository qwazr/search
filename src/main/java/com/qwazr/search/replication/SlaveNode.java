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

import com.qwazr.search.index.ReplicationStatus;
import com.qwazr.server.ServerException;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface SlaveNode {

	ReplicationProcess newReplicationProcess(final ReplicationStatus.Strategy strategy,
			final ReplicationSession masterFiles, final ReplicationProcess.SourceFileProvider sourceFileProvider)
			throws IOException;

	class WithIndex implements SlaveNode {

		protected final Directory indexDirectory;
		protected final Path indexDirectoryPath;
		protected final Path workDirectory;

		public WithIndex(final Directory indexDirectory, final Path indexDirectoryPath, final Path workDirectory)
				throws IOException {
			this.indexDirectory = indexDirectory;
			this.indexDirectoryPath = indexDirectoryPath;
			this.workDirectory = workDirectory;
			if (!Files.exists(workDirectory))
				Files.createDirectory(workDirectory);
		}

		@Override
		public ReplicationProcess newReplicationProcess(final ReplicationStatus.Strategy strategy,
				final ReplicationSession masterFiles, final ReplicationProcess.SourceFileProvider fileProvider)
				throws IOException {
			switch (strategy) {
			case incremental:
				return new ReplicationProcessIncrementalIndex(workDirectory, indexDirectoryPath, indexDirectory,
						masterFiles, fileProvider);
			case full:
				return new ReplicationProcessFullIndex(workDirectory, indexDirectoryPath, indexDirectory, masterFiles,
						fileProvider);
			}
			throw new ServerException("Unsupported replication strategy: " + strategy);
		}
	}

	class WithIndexAndTaxo extends WithIndex {

		private final Directory taxoDirectory;
		private final Path taxoDirectoryPath;

		public WithIndexAndTaxo(final Directory indexDirectory, final Path indexDirectoryPath,
				final Directory taxoDirectory, final Path taxoDirectoryPath, final Path workDirectory)
				throws IOException {
			super(indexDirectory, indexDirectoryPath, workDirectory);
			this.taxoDirectory = taxoDirectory;
			this.taxoDirectoryPath = taxoDirectoryPath;
		}

		@Override
		public ReplicationProcess newReplicationProcess(final ReplicationStatus.Strategy strategy,
				final ReplicationSession masterFiles, final ReplicationProcess.SourceFileProvider fileProvider)
				throws IOException {
			switch (strategy) {
			case incremental:
				return new ReplicationProcessIncrementalIndexAndTaxo(workDirectory, indexDirectoryPath, indexDirectory,
						taxoDirectoryPath, taxoDirectory, masterFiles, fileProvider);
			case full:
				return new ReplicationProcessFullIndexAndTaxo(workDirectory, indexDirectoryPath, indexDirectory,
						taxoDirectoryPath, taxoDirectory, masterFiles, fileProvider);
			}
			throw new ServerException("Unsupported replication strategy: " + strategy);
		}
	}
}
