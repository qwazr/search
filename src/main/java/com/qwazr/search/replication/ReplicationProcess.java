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

import com.qwazr.utils.IOUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;

public interface ReplicationProcess extends Closeable {

	enum Source {
		index, taxo
	}

	@FunctionalInterface
	interface SourceFileProvider {
		InputStream obtain(Source source, String fileName) throws IOException;
	}

	void obtainNewFiles() throws IOException;

	void moveInPlaceNewFiles() throws IOException;

	void deleteOldFiles() throws IOException;

	abstract class Common implements ReplicationProcess {

		protected final Source source;
		protected final SourceFileProvider sourceFileProvider;
		protected final Path indexWorkDirectory;
		protected final Collection<String> indexFilesToObtain;
		protected final Collection<String> indexFilesToDelete;

		protected Common(final Path workDirectory, final Source source, final SourceFileProvider sourceFileProvider)
				throws IOException {
			this.source = source;
			this.sourceFileProvider = sourceFileProvider;
			this.indexWorkDirectory = workDirectory.resolve(source.name());
			this.indexFilesToObtain = new HashSet<>();
			this.indexFilesToDelete = new HashSet<>();
		}

		@Override
		public void obtainNewFiles() throws IOException {
			if (!Files.exists(indexWorkDirectory))
				Files.createDirectory(indexWorkDirectory);
			for (final String fileToObtain : indexFilesToObtain) {
				final File file = indexWorkDirectory.resolve(fileToObtain).toFile();
				try (final InputStream input = sourceFileProvider.obtain(source, fileToObtain)) {
					IOUtils.copy(input, file);
				}
			}
		}
	}
}
