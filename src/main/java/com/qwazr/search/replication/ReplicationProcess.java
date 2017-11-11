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
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.IOUtils;
import org.apache.lucene.store.Directory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.rmi.ServerException;
import java.util.Collection;
import java.util.HashSet;

public interface ReplicationProcess extends Closeable {

	enum Source {
		data, taxonomy, resources, metadata
	}

	@FunctionalInterface
	interface SourceFileProvider {
		InputStream obtain(Source source, String fileName) throws IOException;
	}

	void obtainNewFiles() throws IOException;

	void moveInPlaceNewFiles() throws IOException;

	void deleteOldFiles() throws IOException;

	class Builder {

		private final Path workDirectory;
		private final SourceFileProvider sourceFileProvider;
		private final ReplicationSession session;
		private final ReplicationStatus.Strategy strategy;

		public Builder(final Path workDirectory, final SourceFileProvider sourceFileProvider,
				final ReplicationStatus.Strategy strategy, final ReplicationSession session) {
			this.workDirectory = workDirectory;
			this.sourceFileProvider = sourceFileProvider;
			this.strategy = strategy;
			this.session = session;
		}

		private ReplicationProcess full(final Path targetDirectoryPath, final Source source,
				final SourceView sourceView) throws IOException {
			return new Full(workDirectory, targetDirectoryPath, source, sourceFileProvider, sourceView, session);
		}

		private ReplicationProcess incremental(final Path targetDirectoryPath, final Source source,
				final SourceView sourceView) throws IOException {
			return new Differential(workDirectory, targetDirectoryPath, source, sourceFileProvider, sourceView,
					session);
		}

		public ReplicationProcess resources(final Path resourcesPath) throws IOException {
			final SourceView sourceView = new SourceView.FromPath(resourcesPath);
			switch (strategy) {
			case full:
				return full(resourcesPath, Source.resources, sourceView);
			case incremental:
				return incremental(resourcesPath, Source.resources, sourceView);
			}
			throw new ServerException("Unknown replication strategy: " + strategy);
		}

		private ReplicationProcess index(final Source source, final Path indexDirectoryPath,
				final Directory indexDirectory) throws IOException {
			final SourceView sourceView = new SourceView.FromDirectory(indexDirectoryPath, indexDirectory);
			switch (strategy) {
			case full:
				return full(indexDirectoryPath, source, sourceView);
			case incremental:
				return incremental(indexDirectoryPath, source, sourceView);
			}
			throw new ServerException("Unknown replication strategy: " + strategy);
		}

		public ReplicationProcess dataIndex(final Path indexDirectoryPath, final Directory indexDirectory)
				throws IOException {
			return index(Source.data, indexDirectoryPath, indexDirectory);
		}

		public ReplicationProcess taxoIndex(final Path taxoDirectoryPath, final Directory taxoDirectory)
				throws IOException {
			return index(Source.taxonomy, taxoDirectoryPath, taxoDirectory);
		}

		public ReplicationProcess build(final ReplicationProcess... processes) {
			if (processes == null || processes.length == 0)
				return null;
			if (processes.length == 1)
				return processes[0];
			return new Chain(processes);
		}

	}

	abstract class Common implements ReplicationProcess {

		protected final Source source;
		protected final SourceFileProvider sourceFileProvider;
		protected final Path sourceWorkDirectory;
		protected final Path targetDirectoryPath;
		protected final Collection<String> filesToObtain;
		protected final Collection<String> filesToDelete;

		protected Common(final Path workDirectory, final Path targetDirectoryPath, final Source source,
				final SourceFileProvider sourceFileProvider) throws IOException {
			this.source = source;
			this.sourceFileProvider = sourceFileProvider;
			this.sourceWorkDirectory = workDirectory.resolve(source.name());
			this.targetDirectoryPath = targetDirectoryPath;
			this.filesToObtain = new HashSet<>();
			this.filesToDelete = new HashSet<>();
		}

		@Override
		final public void obtainNewFiles() throws IOException {
			if (!Files.exists(sourceWorkDirectory))
				Files.createDirectory(sourceWorkDirectory);
			for (final String fileToObtain : filesToObtain) {
				final File file = sourceWorkDirectory.resolve(fileToObtain).toFile();
				try (final InputStream input = sourceFileProvider.obtain(source, fileToObtain)) {
					IOUtils.copy(input, file);
				}
			}
		}

		@Override
		public void close() throws IOException {
			if (!Files.exists(sourceWorkDirectory))
				FileUtils.deleteDirectory(sourceWorkDirectory);
		}
	}

	/**
	 * Implements an differential replication process.
	 */
	final class Differential extends Common {

		Differential(Path workDirectory, Path targetDirectoryPath, Source source, SourceFileProvider sourceFileProvider,
				final SourceView sourceView, final ReplicationSession session) throws IOException {
			super(workDirectory, targetDirectoryPath, source, sourceFileProvider);
			sourceView.differential(session.getSourceFiles(source).keySet(), filesToObtain, filesToDelete);
		}

		@Override
		public void moveInPlaceNewFiles() throws IOException {
			for (String fileToMove : filesToObtain) {
				final Path source = sourceWorkDirectory.resolve(fileToMove);
				final Path target = targetDirectoryPath.resolve(fileToMove);
				Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
			}
		}

		@Override
		public void deleteOldFiles() throws IOException {
			for (String fileToDelete : filesToDelete)
				Files.deleteIfExists(targetDirectoryPath.resolve(fileToDelete));
		}

	}

	/**
	 * Implements a full replication. Everything will be copied.
	 */
	final class Full extends Common {

		private final Path sourceTrashPath;

		protected Full(Path workDirectory, Path targetDirectoryPath, Source source,
				SourceFileProvider sourceFileProvider, final SourceView sourceView, final ReplicationSession session)
				throws IOException {
			super(workDirectory, targetDirectoryPath, source, sourceFileProvider);
			this.sourceTrashPath = workDirectory.resolve("trash-" + source.name());
			if (!Files.exists(sourceTrashPath))
				Files.createDirectory(sourceTrashPath);
			sourceView.full(session.getSourceFiles(source).keySet(), filesToObtain, filesToDelete);
		}

		@Override
		final public void moveInPlaceNewFiles() throws IOException {
			for (String fileToMove : filesToDelete) {
				final Path source = targetDirectoryPath.resolve(fileToMove);
				final Path target = sourceTrashPath.resolve(fileToMove);
				Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
			}
			for (String fileToMove : filesToObtain) {
				final Path source = sourceWorkDirectory.resolve(fileToMove);
				final Path target = targetDirectoryPath.resolve(fileToMove);
				Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
			}
		}

		@Override
		final public void deleteOldFiles() throws IOException {
			// Nothing to do, we already moved the file to the trash directory
		}

		@Override
		final public void close() throws IOException {
			super.close();
			if (Files.exists(sourceTrashPath))
				FileUtils.deleteDirectory(sourceTrashPath);
		}
	}

	/**
	 * Implements a chain of replicationProcess
	 */
	final class Chain implements ReplicationProcess {

		private final ReplicationProcess[] processes;

		Chain(final ReplicationProcess... processes) {
			this.processes = processes;
		}

		@Override
		final public void obtainNewFiles() throws IOException {
			for (ReplicationProcess process : processes)
				process.obtainNewFiles();
		}

		@Override
		final public void moveInPlaceNewFiles() throws IOException {
			for (ReplicationProcess process : processes)
				process.moveInPlaceNewFiles();
		}

		@Override
		final public void deleteOldFiles() throws IOException {
			for (ReplicationProcess process : processes)
				process.deleteOldFiles();
		}

		@Override
		final public void close() throws IOException {
			for (ReplicationProcess process : processes)
				process.close();
		}
	}
}
