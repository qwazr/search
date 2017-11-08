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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface ReplicationProcess extends Closeable {

	enum Source {
		index, taxo;
	}

	@FunctionalInterface
	interface FileProvider {
		InputStream obtain(Source source, String fileName) throws IOException;
	}

	void obtainNewFiles() throws IOException;

	void moveInPlaceNewFiles() throws IOException;

	void deleteOldFiles() throws IOException;

	class WithIndex implements ReplicationProcess {

		private final ReplicationProcess indexReplicationProcess;

		WithIndex(final Path indexDirectory, final Path workDirectory, final IndexView indexView,
				final ReplicationSession masterFiles, final FileProvider fileProvider) {
			indexReplicationProcess =
					new ReplicationProcessImpl(indexDirectory, workDirectory, Source.index, indexView, masterFiles,
							fileProvider);
		}

		@Override
		public void obtainNewFiles() throws IOException {
			indexReplicationProcess.obtainNewFiles();
		}

		@Override
		public void moveInPlaceNewFiles() throws IOException {
			indexReplicationProcess.moveInPlaceNewFiles();
		}

		@Override
		public void deleteOldFiles() throws IOException {
			indexReplicationProcess.deleteOldFiles();
		}

		@Override
		public void close() throws IOException {
			indexReplicationProcess.close();
		}

	}

	class WithIndexAndTaxo extends WithIndex {

		private final ReplicationProcess taxoReplicationProcess;

		WithIndexAndTaxo(final Path indexDirectory, final Path taxoDirectory, final Path workDirectory,
				final IndexView indexView, final IndexView taxoView, final ReplicationSession masterFiles,
				final FileProvider fileProvider) {
			super(indexDirectory, workDirectory, indexView, masterFiles, fileProvider);
			taxoReplicationProcess =
					new ReplicationProcessImpl(taxoDirectory, workDirectory, Source.taxo, taxoView, masterFiles,
							fileProvider);
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
}
