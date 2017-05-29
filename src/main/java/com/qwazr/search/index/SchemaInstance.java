/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import com.qwazr.search.analysis.AnalyzerFactory;
import com.qwazr.server.ServerException;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.LockUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.ReadWriteSemaphores;
import com.qwazr.utils.json.JsonMapper;
import com.qwazr.utils.reflection.ConstructorParametersImpl;

import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

class SchemaInstance implements IndexInstance.Provider, Closeable {

	private final static String SETTINGS_FILE = "settings.json";

	private final ConcurrentHashMap<String, IndexInstanceManager> indexMap;
	private final ConcurrentHashMap<String, AnalyzerFactory> analyzerFactoryMap;

	private final ReadWriteSemaphores readWriteSemaphores;
	private final ConstructorParametersImpl instanceFactory;
	private final IndexServiceInterface service;
	private final ExecutorService executorService;
	private final String schemaName;
	private final Path schemaDirectory;
	private final File settingsFile;
	private volatile SchemaSettingsDefinition settingsDefinition;
	private volatile Path backupRootDirectory;

	private final LockUtils.ReadWriteLock backupLock = new LockUtils.ReadWriteLock();

	SchemaInstance(final ConstructorParametersImpl instanceFactory,
			final ConcurrentHashMap<String, AnalyzerFactory> analyzerFactoryMap, final IndexServiceInterface service,
			final File schemaDirectory, final ExecutorService executorService)
			throws IOException, ReflectiveOperationException, URISyntaxException {

		this.readWriteSemaphores = new ReadWriteSemaphores(null, null);
		this.instanceFactory = instanceFactory;
		this.analyzerFactoryMap = analyzerFactoryMap;
		this.executorService = executorService;
		this.service = service;
		this.schemaName = schemaDirectory.getName();
		this.schemaDirectory = schemaDirectory.toPath();
		if (!Files.exists(this.schemaDirectory))
			Files.createDirectory(this.schemaDirectory);

		indexMap = new ConcurrentHashMap<>();

		settingsFile = new File(schemaDirectory, SETTINGS_FILE);
		settingsDefinition = settingsFile.exists() ? JsonMapper.MAPPER.readValue(settingsFile,
				SchemaSettingsDefinition.class) : SchemaSettingsDefinition.EMPTY;
		checkSettings();

		Files.list(this.schemaDirectory).filter(path -> Files.isDirectory(path)).forEach(indexPath -> {
			indexMap.put(indexPath.toFile().getName(),
					new IndexInstanceManager(this, instanceFactory, analyzerFactoryMap, readWriteSemaphores,
							executorService, service, indexPath));
		});
	}

	@Override
	public void close() throws IOException {
		indexMap.forEachValue(1, IOUtils::closeQuietly);
	}

	IndexInstance createUpdate(final String indexName, final IndexSettingsDefinition settings) throws Exception {
		Objects.requireNonNull(settings, "The settings cannot be null");
		return indexMap.computeIfAbsent(indexName,
				name -> new IndexInstanceManager(this, instanceFactory, analyzerFactoryMap, readWriteSemaphores,
						executorService, service, schemaDirectory.resolve(name))).createUpdate(settings);
	}

	private IndexInstanceManager checkIndexExists(final String indexName,
			final IndexInstanceManager indexInstanceManager) {
		if (indexInstanceManager == null)
			throw new ServerException(Response.Status.NOT_FOUND, () -> "Index not found: " + indexName);
		return indexInstanceManager;
	}

	/**
	 * Returns the indexInstance. If the index does not exists, an exception it
	 * thrown. This method never returns a null value.
	 *
	 * @param indexName        The name of the index
	 * @param ensureWriterOpen if true the index will be reopen if the writer has been closed
	 * @return the indexInstance
	 */
	public IndexInstance get(String indexName, boolean ensureWriterOpen) {
		final IndexInstanceManager indexInstanceManager = checkIndexExists(indexName, indexMap.get(indexName));
		try {
			return ensureWriterOpen ? indexInstanceManager.getIndexInstance() : indexInstanceManager.open();
		} catch (Exception e) {
			throw new ServerException(e);
		}
	}

	@Override
	public IndexInstance getIndex(String name) {
		return get(name, false);
	}

	void delete() {
		indexMap.forEachValue(1, IndexInstanceManager::delete);
		if (Files.exists(schemaDirectory))
			FileUtils.deleteDirectoryQuietly(schemaDirectory);
	}

	void delete(final String indexName) throws ServerException, IOException {
		indexMap.compute(indexName, (name, indexInstanceManager) -> {
			checkIndexExists(indexName, indexInstanceManager).delete();
			return null;
		});
	}

	Set<String> nameSet() {
		return indexMap.keySet();
	}

	final private static Pattern backupNameMatcher = Pattern.compile("[^a-zA-Z0-9-_]");

	private void checkBackupConfig() {
		if (settingsDefinition == null || StringUtils.isEmpty(settingsDefinition.backup_directory_path))
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"Backup path not defined in the schema settings - Schema: " + schemaName);
	}

	private Path getBackupDirectory(final String backupName, boolean createIfNotExists) throws IOException {
		if (backupRootDirectory == null)
			throw new IOException("The backup root directory is not set for the schema: " + schemaName);
		if (Files.notExists(backupRootDirectory) || !Files.isDirectory(backupRootDirectory))
			throw new IOException("The backup root directory does not exists: " + backupRootDirectory);
		final Path backupSchemaDirectory = backupRootDirectory.resolve(schemaName);
		if (createIfNotExists && Files.notExists(backupSchemaDirectory))
			Files.createDirectory(backupSchemaDirectory);
		if (!Files.isDirectory(backupSchemaDirectory))
			throw new IOException("The backup schema directory does not exists: " + backupSchemaDirectory);
		if (StringUtils.isEmpty(backupName))
			throw new IOException("The backup name is empty");
		if (backupNameMatcher.matcher(backupName).find())
			throw new IOException(
					"The backup name should only contains alphanumeric characters, dash, or underscore : " +
							backupName);
		final Path backupDirectory = backupSchemaDirectory.resolve(backupName);
		if (createIfNotExists && Files.notExists(backupDirectory))
			Files.createDirectory(backupDirectory);
		return backupDirectory;
	}

	private void indexIterator(final String indexName, final BiConsumer<String, IndexInstance> consumer) {
		if ("*".equals(indexName)) {
			indexMap.forEach(1, (name, indexInstanceManager) -> {
				try {
					consumer.accept(name, indexInstanceManager.open());
				} catch (Exception e) {
					throw new ServerException(e);
				}
			});
		} else
			consumer.accept(indexName, get(indexName, false));
	}

	SortedMap<String, BackupStatus> backups(final String indexName, final String backupName) throws IOException {
		return backupLock.writeEx(() -> {
			checkBackupConfig();
			final Path backupDirectory = getBackupDirectory(backupName, true);
			final SortedMap<String, BackupStatus> results = new TreeMap<>();
			indexIterator(indexName, (idxName, indexInstance) -> {
				try {
					results.put(idxName, indexInstance.backup(backupDirectory.resolve(idxName)));
				} catch (IOException e) {
					throw new ServerException(e);
				}
			});
			return results;
		});
	}

	private void backupIterator(final String backupName, final Consumer<Path> consumer) {
		final Path backupSchemaDirectory = backupRootDirectory.resolve(schemaName);
		if (Files.notExists(backupSchemaDirectory) || !Files.isDirectory(backupSchemaDirectory))
			return;
		try {
			if ("*".equals(backupName)) {
				Files.list(backupSchemaDirectory).filter(path -> Files.isDirectory(path)).forEach(consumer);
			} else
				consumer.accept(getBackupDirectory(backupName, false));
		} catch (IOException e) {
			throw new ServerException(e);
		}
	}

	SortedMap<String, SortedMap<String, BackupStatus>> getBackups(final String indexName, final String backupName,
			final boolean extractVersion) throws IOException {
		return backupLock.readEx(() -> {
			checkBackupConfig();
			final SortedMap<String, SortedMap<String, BackupStatus>> results = new TreeMap<>();

			backupIterator(backupName, backupDirectory -> {

				final SortedMap<String, BackupStatus> backupResults = new TreeMap<>();

				indexIterator(indexName, (idxName, indexInstance) -> {
					try {
						final Path backupIndexDirectory = backupDirectory.resolve(idxName);
						if (Files.exists(backupIndexDirectory) && Files.isDirectory(backupIndexDirectory))
							backupResults.put(idxName, indexInstance.getBackup(backupIndexDirectory, extractVersion));
					} catch (IOException e) {
						throw new ServerException(e);
					}
				});
				if (!backupResults.isEmpty())
					results.put(backupDirectory.toFile().getName(), backupResults);
			});
			return results;
		});
	}

	private void backupIndexDirectoryIterator(final Path backupDirectory, final String indexName,
			final Consumer<Path> consumer) {
		final Path backupSchemaDirectory = backupRootDirectory.resolve(schemaName);
		if (Files.notExists(backupSchemaDirectory) || !Files.isDirectory(backupSchemaDirectory))
			return;
		if ("*".equals(indexName)) {
			try {
				if (Files.exists(backupDirectory))
					Files.list(backupDirectory).filter(path -> Files.isDirectory(path)).forEach(consumer);
			} catch (IOException e) {
				throw new ServerException(e);
			}
		} else {
			final Path indexPath = backupDirectory.resolve(indexName);
			if (Files.exists(indexPath))
				consumer.accept(indexPath);
		}
	}

	int deleteBackups(final String indexName, final String backupName) throws IOException {
		return backupLock.writeEx(() -> {
			checkBackupConfig();
			final AtomicInteger counter = new AtomicInteger();

			backupIterator(backupName, backupDirectory -> {

				backupIndexDirectoryIterator(backupDirectory, indexName, backupIndexDirectory -> {

					try {
						FileUtils.deleteDirectory(backupIndexDirectory);
						counter.incrementAndGet();
					} catch (IOException e) {
						throw new ServerException(e);
					}

					try {
						if (Files.exists(backupDirectory))
							if (Files.list(backupDirectory).count() == 0)
								Files.deleteIfExists(backupDirectory);
					} catch (IOException e) {
						throw new ServerException(e);
					}
				});
			});
			return counter.get();
		});
	}

	synchronized void setSettings(SchemaSettingsDefinition settings) throws IOException, URISyntaxException {
		if (settings == null) {
			settings = SchemaSettingsDefinition.EMPTY;
			settingsFile.delete();
		} else
			JsonMapper.MAPPER.writeValue(settingsFile, settings);
		this.settingsDefinition = settings;
		checkSettings();
	}

	SchemaSettingsDefinition getSettings() {
		return settingsDefinition;
	}

	private synchronized void checkSettings() throws IOException, URISyntaxException {
		if (settingsDefinition == null) {
			readWriteSemaphores.setReadSize(null);
			readWriteSemaphores.setWriteSize(null);
			return;
		}
		readWriteSemaphores.setReadSize(settingsDefinition.max_simultaneous_read);
		readWriteSemaphores.setWriteSize(settingsDefinition.max_simultaneous_write);

		if (!StringUtils.isEmpty(settingsDefinition.backup_directory_path))
			backupRootDirectory = new File(settingsDefinition.backup_directory_path).toPath();
		else
			backupRootDirectory = null;
	}

	IndexCheckStatus checkIndex(final String indexName) throws IOException {
		final IndexInstanceManager indexInstanceManager = indexMap.get(indexName);
		return indexInstanceManager == null ? null : new IndexCheckStatus(indexInstanceManager.check());
	}

	IndexStatus mergeIndex(final String indexName, final String mergedIndexName,
			final Map<String, String> commitUserData) throws IOException {
		return getIndex(indexName).merge(getIndex(mergedIndexName), commitUserData);
	}
}
