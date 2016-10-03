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

import com.qwazr.search.analysis.AnalyzerContext;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.analysis.UpdatableAnalyzer;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.ExceptionUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonMapper;
import com.qwazr.utils.server.ServerException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class SchemaInstance implements Closeable {

	private final static Logger LOGGER = LoggerFactory.getLogger(SchemaInstance.class);

	private final static String SETTINGS_FILE = "settings.json";

	private final ConcurrentHashMap<String, IndexInstance> indexMap;

	private final ExecutorService executorService;
	private final File schemaDirectory;
	private final File settingsFile;
	private volatile SchemaSettingsDefinition settingsDefinition;
	private volatile File backupRootDirectory;

	private volatile Semaphore readSemaphore;
	private volatile Semaphore writeSemaphore;

	private volatile SearchContext searchContext = null;

	private class SearchContext implements Closeable, AutoCloseable {

		private final MultiReader multiReader;
		private final IndexSearcher indexSearcher;
		private final Map<String, AnalyzerDefinition> analyzerMap;
		private final FieldMap fieldMap;
		private final UpdatableAnalyzer indexAnalyzer;
		private final UpdatableAnalyzer queryAnalyzer;
		private final AtomicInteger ref = new AtomicInteger(1);

		private SearchContext(boolean failOnException) throws IOException, ServerException {
			if (indexMap.isEmpty()) {
				indexSearcher = null;
				multiReader = null;
				indexAnalyzer = null;
				queryAnalyzer = null;
				analyzerMap = null;
				fieldMap = null;
				return;
			}
			IndexReader[] indexReaders = new IndexReader[indexMap.size()];
			int i = 0;
			analyzerMap = new HashMap<>();
			FileResourceLoader resourceLoader = null;
			final LinkedHashMap<String, FieldDefinition> fieldDefinitionMap = new LinkedHashMap<>();
			for (IndexInstance indexInstance : indexMap.values()) {
				indexReaders[i++] = DirectoryReader.open(indexInstance.getDataDirectory());
				indexInstance.fillFields(fieldDefinitionMap);
				indexInstance.fillAnalyzers(analyzerMap);
				resourceLoader = indexInstance.newResourceLoader(resourceLoader);
			}
			fieldMap = new FieldMap(fieldDefinitionMap);
			multiReader = new MultiReader(indexReaders);
			indexSearcher = new IndexSearcher(multiReader);
			final AnalyzerContext analyzerContext =
					new AnalyzerContext(resourceLoader, analyzerMap, fieldDefinitionMap, failOnException);
			indexAnalyzer = new UpdatableAnalyzer(analyzerContext.indexAnalyzerMap);
			queryAnalyzer = new UpdatableAnalyzer(analyzerContext.queryAnalyzerMap);
		}

		int numDocs() {
			incRef();
			try {
				if (multiReader == null)
					return 0;
				return multiReader.numDocs();
			} finally {
				decRef();
			}
		}

		private synchronized void doClose() {
			IOUtils.close(multiReader);
		}

		@Override
		final public void close() {
			decRef();
		}

		final void incRef() {
			ref.incrementAndGet();
		}

		final void decRef() {
			if (ref.decrementAndGet() > 0)
				return;
			doClose();
		}

		ResultDefinition search(QueryDefinition queryDef, ResultDocumentBuilder.BuilderFactory documentBuilderFactory)
				throws ServerException, IOException, QueryNodeException, InterruptedException, ParseException,
				ReflectiveOperationException {
			if (indexSearcher == null)
				return null;
			incRef();
			try {
				SortedSetDocValuesReaderState state = IndexUtils.getNewFacetsState(indexSearcher.getIndexReader());
				final QueryContext queryContext =
						new QueryContext(SchemaInstance.this, null, indexSearcher, indexAnalyzer, queryAnalyzer,
								fieldMap, state, queryDef);
				return QueryUtils.search(queryContext, documentBuilderFactory);
			} finally {
				decRef();
			}
		}

	}

	SchemaInstance(ExecutorService executorService, File schemaDirectory)
			throws IOException, ServerException, InterruptedException, ReflectiveOperationException,
			URISyntaxException {
		this.executorService = executorService;
		this.schemaDirectory = schemaDirectory;
		if (!schemaDirectory.exists())
			schemaDirectory.mkdir();
		if (!schemaDirectory.exists())
			throw new IOException("The directory does not exist: " + schemaDirectory.getName());
		indexMap = new ConcurrentHashMap<String, IndexInstance>();

		settingsFile = new File(schemaDirectory, SETTINGS_FILE);
		settingsDefinition = settingsFile.exists() ?
				JsonMapper.MAPPER.readValue(settingsFile, SchemaSettingsDefinition.class) :
				SchemaSettingsDefinition.EMPTY;
		checkSettings();

		File[] directories = schemaDirectory.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		if (directories == null)
			return;
		for (File indexDirectory : directories)
			indexMap.put(indexDirectory.getName(), IndexInstanceBuilder.build(this, indexDirectory, null));
		mayBeRefresh(false);
	}

	@Override
	public void close() throws IOException {
		if (searchContext != null) {
			searchContext.close();
			searchContext = null;
		}
		synchronized (indexMap) {
			indexMap.values().forEach(IOUtils::closeQuietly);
		}
	}

	IndexInstance createUpdate(String indexName, IndexSettingsDefinition settings)
			throws ServerException, IOException, InterruptedException, ReflectiveOperationException,
			URISyntaxException {
		synchronized (indexMap) {
			IndexInstance indexInstance = indexMap.get(indexName);
			if (indexInstance != null && !IndexSettingsDefinition.equals(indexInstance.getSettings(), settings)) {
				IOUtils.closeQuietly(indexInstance);
				indexMap.remove(indexName);
				indexInstance = null;
			}
			if (indexInstance == null) {
				indexInstance = IndexInstanceBuilder.build(this, new File(schemaDirectory, indexName), settings);
				indexMap.put(indexName, indexInstance);
			}
			mayBeRefresh(true);
			return indexInstance;
		}
	}

	/**
	 * Returns the indexInstance. If the index does not exists, an exception it
	 * thrown. This method never returns a null value.
	 *
	 * @param indexName        The name of the index
	 * @param ensureWriterOpen if true the index will be reopen if the writer has been closed
	 * @return the indexInstance
	 * @throws ServerException if any error occurs
	 * @throws IOException     if any I/O error occurs
	 */
	public IndexInstance get(String indexName, boolean ensureWriterOpen) throws ServerException, IOException {
		IndexInstance indexInstance = indexMap.get(indexName);
		if (indexInstance == null)
			throw new ServerException(Response.Status.NOT_FOUND, "Index not found: " + indexName);
		if (!ensureWriterOpen)
			return indexInstance;
		try {
			return indexInstance.isIndexWriterOpen() ? indexInstance : createUpdate(indexName, null);
		} catch (InterruptedException | ReflectiveOperationException | URISyntaxException e) {
			throw new ServerException(e);
		}
	}

	void delete() {
		synchronized (indexMap) {
			for (IndexInstance instance : indexMap.values()) {
				instance.close();
				instance.delete();
			}
			if (schemaDirectory.exists())
				FileUtils.deleteQuietly(schemaDirectory);
		}
	}

	void delete(String indexName) throws ServerException, IOException {
		synchronized (indexMap) {
			IndexInstance indexInstance = get(indexName, false);
			indexInstance.delete();
			indexMap.remove(indexName);
			mayBeRefresh(false);
		}
	}

	Set<String> nameSet() {
		return indexMap.keySet();
	}

	private void checkBackupRootDirectory() throws IOException {
		if (backupRootDirectory == null)
			throw new IOException("The backup root directory is not set for the schema: " + schemaDirectory.getName());
		if (!backupRootDirectory.exists() || !backupRootDirectory.isDirectory())
			throw new IOException(
					"The backup root directory does not exists: " + backupRootDirectory.getAbsolutePath());
	}

	BackupStatus backup(final String indexName, final Integer keepLastCount) throws IOException, InterruptedException {
		checkBackupRootDirectory();
		return get(indexName, false).backup(keepLastCount, new File(backupRootDirectory, indexName));
	}

	void backups(final Integer keepLastCount)
			throws IOException, InterruptedException {
		checkBackupRootDirectory();
		final ExceptionUtils.Holder exceptionHolder = new ExceptionUtils.Holder(LOGGER);
		synchronized (indexMap) {
			indexMap.entrySet().parallelStream().forEach((entry) -> {
				try {
					final File backupIndexDirectory = new File(backupRootDirectory, entry.getKey());
					entry.getValue().backup(keepLastCount, backupIndexDirectory);
				} catch (IOException | InterruptedException e) {
					exceptionHolder.switchAndWarn(e);
				}
			});
		}
		exceptionHolder.thrownIfAny();
	}

	List<BackupStatus> getBackups(final String indexName) throws IOException, InterruptedException {
		checkBackupRootDirectory();
		return get(indexName, false).getBackups(new File(backupRootDirectory, indexName));
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

	synchronized void mayBeRefresh(final boolean failOnException) throws IOException, ServerException {
		if (searchContext != null)
			searchContext.close();
		searchContext = new SearchContext(failOnException);
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
			backupRootDirectory = new File(settingsDefinition.backup_directory_path);
		else
			backupRootDirectory = null;
	}

	private static <T extends ResultDocumentAbstract> ResultDefinition<T> atomicSearch(SearchContext searchContext,
			QueryDefinition queryDef, ResultDocumentBuilder.BuilderFactory<T> documentBuilderFactory)
			throws InterruptedException, IOException, QueryNodeException, ParseException, ServerException,
			ReflectiveOperationException {
		if (searchContext == null)
			return null;
		return searchContext.search(queryDef, documentBuilderFactory);
	}

	public <T extends ResultDocumentAbstract> ResultDefinition<T> search(QueryDefinition queryDef,
			ResultDocumentBuilder.BuilderFactory<T> documentBuilderFactory)
			throws ServerException, IOException, QueryNodeException, InterruptedException, ParseException,
			ReflectiveOperationException {
		final Semaphore sem = acquireReadSemaphore();
		try {
			return atomicSearch(searchContext, queryDef, documentBuilderFactory);
		} finally {
			if (sem != null)
				sem.release();
		}
	}

	private static Semaphore atomicAquire(Semaphore semaphore) throws InterruptedException {
		if (semaphore == null)
			return null;
		semaphore.acquire();
		return semaphore;
	}

	Semaphore acquireReadSemaphore() throws InterruptedException {
		return atomicAquire(readSemaphore);
	}

	Semaphore acquireWriteSemaphore() throws InterruptedException {
		return atomicAquire(writeSemaphore);
	}

	private static void atomicCheckSize(SchemaSettingsDefinition settingsDefinition, SearchContext searchContext,
			int addSize) throws ServerException {
		if (settingsDefinition == null)
			return;
		if (settingsDefinition.max_size == null)
			return;
		if (searchContext == null)
			return;
		if (searchContext.numDocs() + addSize > settingsDefinition.max_size)
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"This schema is limited to " + settingsDefinition.max_size + " documents");
	}

	final void checkSize(final int addSize) throws IOException, ServerException {
		atomicCheckSize(settingsDefinition, searchContext, addSize);
	}

}
