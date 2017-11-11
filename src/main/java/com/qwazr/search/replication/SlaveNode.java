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
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface SlaveNode {

	ReplicationProcess newReplicationProcess(final ReplicationStatus.Strategy strategy,
			final ReplicationSession masterFiles, final ReplicationProcess.SourceFileProvider sourceFileProvider)
			throws IOException;

	class WithIndex implements SlaveNode {

		protected final Path resourcesPath;
		protected final Directory indexDirectory;
		protected final Path indexDirectoryPath;
		protected final Path workDirectory;

		public WithIndex(final Path resourcesPath, final Directory indexDirectory, final Path indexDirectoryPath,
				final Path workDirectory) throws IOException {
			this.resourcesPath = resourcesPath;
			this.indexDirectory = indexDirectory;
			this.indexDirectoryPath = indexDirectoryPath;
			this.workDirectory = workDirectory;
			if (!Files.exists(workDirectory))
				Files.createDirectory(workDirectory);
		}

		@Override
		public ReplicationProcess newReplicationProcess(final ReplicationStatus.Strategy strategy,
				final ReplicationSession session, final ReplicationProcess.SourceFileProvider fileProvider)
				throws IOException {
			final ReplicationProcess.Builder builder =
					new ReplicationProcess.Builder(workDirectory, fileProvider, strategy, session);
			return builder.build(builder.resources(resourcesPath),
					builder.dataIndex(indexDirectoryPath, indexDirectory));
		}
	}

	class WithIndexAndTaxo extends WithIndex {

		private final Directory taxoDirectory;
		private final Path taxoDirectoryPath;

		public WithIndexAndTaxo(final Path resourcesPath, final Directory indexDirectory, final Path indexDirectoryPath,
				final Directory taxoDirectory, final Path taxoDirectoryPath, final Path workDirectory)
				throws IOException {
			super(resourcesPath, indexDirectory, indexDirectoryPath, workDirectory);
			this.taxoDirectory = taxoDirectory;
			this.taxoDirectoryPath = taxoDirectoryPath;
		}

		@Override
		public ReplicationProcess newReplicationProcess(final ReplicationStatus.Strategy strategy,
				final ReplicationSession session, final ReplicationProcess.SourceFileProvider fileProvider)
				throws IOException {
			final ReplicationProcess.Builder builder =
					new ReplicationProcess.Builder(workDirectory, fileProvider, strategy, session);
			return builder.build(builder.resources(resourcesPath),
					builder.dataIndex(indexDirectoryPath, indexDirectory),
					builder.taxoIndex(taxoDirectoryPath, taxoDirectory));
		}
	}
}
