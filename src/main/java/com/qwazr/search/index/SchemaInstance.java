/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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

import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.server.ServerException;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.LockUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.lucene.search.SearcherFactory;

import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class SchemaInstance implements Closeable {

	private final static String SETTINGS_FILE = "settings.json";

	private final SearcherFactory searcherFactory;

	private final ConcurrentHashMap<String, IndexInstanceManager> indexMap;

	private final ClassLoaderManager classLoaderManager;
	private final IndexServiceInterface service;
	private final ExecutorService executorService;
	private final File schemaDirectory;
	private final File settingsFile;
	private volatile SchemaSettingsDefinition settingsDefinition;
	private volatile Path backupRootDirectory;

	private final LockUtils.ReadWriteLock backupLock = new LockUtils.ReadWriteLock();

	private volatile Semaphore readSemaphore;
	private volatile Semaphore writeSemaphore;

	SchemaInstance(final ClassLoaderManager classLoaderManager, final IndexServiceInterface service,
			final File schemaDirectory, final ExecutorService executorService)
			throws IOException, ReflectiveOperationException, URISyntaxException {

		this.searcherFactory = new MultiThreadSearcherFactory(executorService);
		this.executorService = executorService;
		this.classLoaderManager = classLoaderManager;
		this.service = service;
		this.schemaDirectory = schemaDirectory;
		if (!schemaDirectory.exists())
			schemaDirectory.mkdir();
		if (!schemaDirectory.exists())
			throw new IOException("The directory does not exist: " + schemaDirectory.getName());

		indexMap = new ConcurrentHashMap<>();

		settingsFile = new File(schemaDirectory, SETTINGS_FILE);
		settingsDefinition = settingsFile.exists() ?
				JsonMapper.MAPPER.readValue(settingsFile, SchemaSettingsDefinition.class) :
				SchemaSettingsDefinition.EMPTY;
		checkSettings();

		File[] directories = schemaDirectory.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		if (directories == null)
			return;
		for (File indexDirectory : directories)
			indexMap.put(indexDirectory.getName(), new IndexInstanceManager(this, indexDirectory));
	}

	final IndexServiceInterface getService() {
		return service;
	}

	final SearcherFactory getSearcherFactory() {
		return searcherFactory;
	}

	final ClassLoaderManager getClassLoaderManager() {
		return classLoaderManager;
	}

	final ExecutorService getExecutorService() {
		return executorService;
	}

	@Override
	public void close() throws IOException {
		indexMap.forEachValue(1, IOUtils::closeQuietly);
	}

	IndexInstance createUpdate(final String indexName, final IndexSettingsDefinition settings) throws Exception {
		Objects.requireNonNull(settings, "The settings cannot be null");
		return indexMap.computeIfAbsent(indexName, name -> {
			try {
				return new IndexInstanceManager(this, new File(schemaDirectory, name));
			} catch (IOException e) {
				throw new ServerException(e);
			}
		}).createUpdate(settings);
	}

	private IndexInstanceManager checkIndexExists(final String indexName,
			final IndexInstanceManager indexInstanceManager) {
		if (indexInstanceManager == null)
			throw new ServerException(Response.Status.NOT_FOUND, "Index not found: " + indexName);
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

	void delete() {
		indexMap.forEachValue(1, IndexInstanceManager::delete);
		if (schemaDirectory.exists())
			FileUtils.deleteQuietly(schemaDirectory);
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

	private Path getBackupDirectory(final String backupName, boolean createIfNotExists) throws IOException {
		if (backupRootDirectory == null)
			throw new IOException("The backup root directory is not set for the schema: " + schemaDirectory.getName());
		if (Files.notExists(backupRootDirectory) || !Files.isDirectory(backupRootDirectory))
			throw new IOException("The backup root directory does not exists: " + backupRootDirectory);
		final Path backupSchemaDirectory = backupRootDirectory.resolve(schemaDirectory.getName());
		if (createIfNotExists && Files.notExists(backupSchemaDirectory))
			Files.createDirectory(backupSchemaDirectory);
		if (!Files.isDirectory(backupSchemaDirectory))
			throw new IOException("The backup schema directory does not exists: " + backupSchemaDirectory);
		if (StringUtils.isEmpty(backupName))
			throw new IOException("The backup name is empty");
		if (backupNameMatcher.matcher(backupName).find())
			throw new IOException("The backup name should only contains alphanumeric characters, dash, or underscore : "
					+ backupName);
		final Path backupDirectory = backupSchemaDirectory.resolve(backupName);
		if (createIfNotExists && Files.notExists(backupDirectory))
			Files.createDirectory(backupDirectory);
		if (!Files.isDirectory(backupDirectory))
			throw new IOException("The backup directory does not exists: " + backupName);
		return backupDirectory;
	}

	BackupStatus backup(final String indexName, final String backupName) throws IOException {
		return backupLock.writeEx(() -> {
			final Path backupIndexDirectory = getBackupDirectory(backupName, true).resolve(indexName);
			return get(indexName, false).backup(backupIndexDirectory);
		});
	}

	private void indexIterator(final String indexName, final BiConsumer<String, IndexInstance> consumer) {
		if ("*".equals(indexName)) {
			indexMap.forEach(1,
					(name, indexInstanceManager) -> consumer.accept(name, indexInstanceManager.getIndexInstance()));
		} else
			consumer.accept(indexName, get(indexName, false));
	}

	SortedMap<String, BackupStatus> backups(final String indexName, final String backupName) throws IOException {
		return backupLock.writeEx(() -> {
			if (settingsDefinition == null || StringUtils.isEmpty(settingsDefinition.backup_directory_path))
				return null;
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

	private void backupIterator(final String backupName, final Consumer<Path> consumer) throws IOException {
		final Path backupSchemaDirectory = backupRootDirectory.resolve(schemaDirectory.getName());
		if (Files.notExists(backupSchemaDirectory) || !Files.isDirectory(backupSchemaDirectory))
			return;
		if ("*".equals(backupName)) {
			Files.list(backupSchemaDirectory).filter(path -> Files.isDirectory(path)).forEach(consumer);
		} else
			consumer.accept(getBackupDirectory(backupName, false));
	}

	SortedMap<String, SortedMap<String, BackupStatus>> getBackups(final String indexName, final String backupName)
			throws IOException {
		return backupLock.readEx(() -> {
			if (settingsDefinition == null || StringUtils.isEmpty(settingsDefinition.backup_directory_path))
				return null;
			final SortedMap<String, SortedMap<String, BackupStatus>> results = new TreeMap<>();

			backupIterator(backupName, backupDirectory -> {

				final SortedMap<String, BackupStatus> backupResults = new TreeMap<>();

				indexIterator(indexName, (idxName, indexInstance) -> {
					try {
						final Path backupIndexDirectory = backupDirectory.resolve(idxName);
						if (Files.exists(backupIndexDirectory) && Files.isDirectory(backupIndexDirectory))
							backupResults.put(idxName, indexInstance.getBackup(backupIndexDirectory));
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

	int deleteBackups(final String indexName, final String backupName) throws IOException {
		return backupLock.writeEx(() -> {
			if (settingsDefinition == null || StringUtils.isEmpty(settingsDefinition.backup_directory_path))
				return 0;
			final AtomicInteger counter = new AtomicInteger();

			backupIterator(backupName, backupDirectory -> {

				indexIterator(indexName, (idxName, indexInstance) -> {

					final Path backupIndexDirectory = backupDirectory.resolve(idxName);
					try {
						FileUtils.deleteDirectory(backupIndexDirectory.toFile());
						counter.incrementAndGet();
					} catch (IOException e) {
						throw new ServerException(e);
					}
				});

				try {
					if (Files.exists(backupDirectory))
						if (Files.list(backupDirectory).count() == 0)
							Files.deleteIfExists(backupDirectory);
				} catch (IOException e) {
					throw new ServerException(e);
				}
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
			readSemaphore = null;
			writeSemaphore = null;
			return;
		}
		if (settingsDefinition.max_simultaneous_read != null)
			readSemaphore = new Semaphore(settingsDefinition.max_simultaneous_read);
		else
			readSemaphore = null;
		if (settingsDefinition.max_simultaneous_write != null)
			writeSemaphore = new Semaphore(settingsDefinition.max_simultaneous_write);
		else
			writeSemaphore = null;
		if (!StringUtils.isEmpty(settingsDefinition.backup_directory_path))
			backupRootDirectory = new File(settingsDefinition.backup_directory_path).toPath();
		else
			backupRootDirectory = null;
	}

	private static Semaphore atomicAquire(final Semaphore semaphore) {
		if (semaphore == null)
			return null;
		try {
			semaphore.acquire();
			return semaphore;
		} catch (InterruptedException e) {
			throw new ServerException(e);
		}
	}

	Semaphore acquireReadSemaphore() {
		return atomicAquire(readSemaphore);
	}

	Semaphore acquireWriteSemaphore() {
		return atomicAquire(writeSemaphore);
	}

	final void checkSize(final int addSize) throws IOException {
		if (settingsDefinition == null)
			return;
		if (settingsDefinition.max_size == null)
			return;
		final AtomicLong totalSize = new AtomicLong();
		indexIterator("*", (name, indexInstance) -> {
			final long indexSize;
			try {
				indexSize = indexInstance.getStatus().num_docs;
			} catch (IOException | InterruptedException e) {
				throw new ServerException(e);
			}
			if (totalSize.addAndGet(indexSize) + addSize > settingsDefinition.max_size)
				throw new ServerException(Response.Status.NOT_ACCEPTABLE,
						"This schema is limited to " + settingsDefinition.max_size + " documents");
		});
	}

	IndexCheckStatus checkIndex(String indexName) throws IOException {
		final IndexInstanceManager indexInstanceManager = indexMap.get(indexName);
		return indexInstanceManager == null ? null : new IndexCheckStatus(indexInstanceManager.check());
	}
}
