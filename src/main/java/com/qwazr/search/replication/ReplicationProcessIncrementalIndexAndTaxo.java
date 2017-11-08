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

import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.nio.file.Path;

class ReplicationProcessIncrementalIndexAndTaxo extends ReplicationProcessIncrementalIndex {

	private final ReplicationProcessIncrementalIndex taxoReplicationProcess;

	ReplicationProcessIncrementalIndexAndTaxo(final Path workDirectory, final Path indexDirectoryPath,
			final Directory indexDirectory, final Path taxoDirectoryPath, final Directory taxoDirectory,
			final ReplicationSession masterFiles, final SourceFileProvider sourceFileProvider) throws IOException {
		super(workDirectory, indexDirectoryPath, indexDirectory, Source.index, masterFiles, sourceFileProvider);
		taxoReplicationProcess =
				new ReplicationProcessIncrementalIndex(workDirectory, taxoDirectoryPath, taxoDirectory, Source.taxo,
						masterFiles, sourceFileProvider);
	}

	@Override
	public void obtainNewFiles() throws IOException {
		super.obtainNewFiles();
		taxoReplicationProcess.obtainNewFiles();
	}

	@Override
	public void moveInPlaceNewFiles() throws IOException {
		super.moveInPlaceNewFiles();
		taxoReplicationProcess.moveInPlaceNewFiles();
	}

	@Override
	public void deleteOldFiles() throws IOException {
		super.deleteOldFiles();
		taxoReplicationProcess.deleteOldFiles();
	}

	@Override
	public void close() throws IOException {
		super.close();
		taxoReplicationProcess.close();
	}
}
