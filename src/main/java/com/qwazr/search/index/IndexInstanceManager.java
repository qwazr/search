/**
 * Copyright 2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import com.qwazr.server.ServerException;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.LockUtils;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.store.Directory;

import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.UUID;

class IndexInstanceManager implements Closeable {

	private final LockUtils.ReadWriteLock rwl;

	private final SchemaInstance schema;
	private final IndexFileSet fileSet;

	private UUID indexUuid;
	private IndexSettingsDefinition settings;
	private IndexInstance indexInstance;

	IndexInstanceManager(final SchemaInstance schema, final File indexDirectory) throws IOException {

		rwl = new LockUtils.ReadWriteLock();
		this.schema = schema;
		this.fileSet = new IndexFileSet(indexDirectory);

		checkDirectoryAndUuid();
		settings = fileSet.loadSettings();
	}

	private void checkDirectoryAndUuid() throws IOException {
		fileSet.checkIndexDirectory();
		indexUuid = fileSet.checkUuid();
	}

	private IndexInstance ensureOpen() throws ReflectiveOperationException, IOException, URISyntaxException {
		if (indexInstance == null)
			indexInstance = new IndexInstanceBuilder(schema, fileSet, settings, indexUuid).build();
		return indexInstance;
	}

	IndexInstance open() throws Exception {
		return rwl.writeEx(this::ensureOpen);
	}

	private boolean isNewMaster(final IndexSettingsDefinition newSettings) {
		if (newSettings == null || newSettings.master == null)
			return false;
		return settings == null || !Objects.equals(settings.master, newSettings.master);
	}

	IndexInstance createUpdate(final IndexSettingsDefinition newSettings) throws Exception {
		return rwl.writeEx(() -> {
			final boolean same = Objects.equals(newSettings, settings);
			if (same && indexInstance != null)
				return indexInstance;
			if (indexInstance != null) {
				if (isNewMaster(newSettings)) {
					if (indexInstance.getStatus().num_docs > 0)
						throw new ServerException(Response.Status.NOT_ACCEPTABLE,
								"This index already contains document.");
					indexInstance.close();
					indexInstance = null;
					FileUtils.deleteQuietly(fileSet.mainDirectory);
					checkDirectoryAndUuid();
				}
			}
			closeIndex();
			if (settings != null && !same) {
				fileSet.writeSettings(newSettings);
				settings = newSettings;
			}
			return ensureOpen();
		});
	}

	CheckIndex.Status check() throws IOException {
		return rwl.writeEx(() -> {
			closeIndex();
			try (final Directory directory = IndexInstanceBuilder.getDirectory(settings, fileSet.dataDirectory)) {
				try (final CheckIndex checkIndex = new CheckIndex(directory)) {
					return checkIndex.checkIndex();
				}
			}
		});
	}

	/**
	 * Return the loaded instance or null if the index cannot be loaded
	 *
	 * @return the loaded instance
	 */
	IndexInstance getIndexInstance() {
		return rwl.read(() -> indexInstance);
	}

	private void closeIndex() {
		if (indexInstance == null)
			return;
		IOUtils.closeQuietly(indexInstance);
		indexInstance = null;
	}

	@Override
	public void close() throws IOException {
		rwl.writeEx(this::closeIndex);
	}

	public void delete() {
		rwl.writeEx(() -> {
			closeIndex();
			if (fileSet.mainDirectory.exists())
				FileUtils.deleteQuietly(fileSet.mainDirectory);
		});
	}

}
