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
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.json.JsonMapper;
import com.qwazr.utils.server.ServerException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

public class SchemaInstance implements Closeable, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(SchemaInstance.class);

	private final static String SETTINGS_FILE = "settings.json";

	private final ConcurrentHashMap<String, IndexInstance> indexMap;

	private final ExecutorService executorService;
	private final File schemaDirectory;
	private final File settingsFile;
	private volatile SchemaSettingsDefinition settingsDefinition;

	private volatile Semaphore readSemaphore;
	private volatile Semaphore writeSemaphore;

	private volatile SearchContext searchContext = null;

	private class SearchContext implements Closeable, AutoCloseable {

		private final MultiReader multiReader;
		private final IndexSearcher indexSearcher;
		private final Map<String, AnalyzerDefinition> analyzerMap;
		private final Map<String, FieldDefinition> fieldMap;
		private final UpdatableAnalyzer queryAnalyzer;

		private SearchContext() throws IOException, ServerException {
			if (indexMap.isEmpty()) {
				indexSearcher = null;
				multiReader = null;
				queryAnalyzer = null;
				analyzerMap = null;
				fieldMap = null;
				return;
			}
			IndexReader[] indexReaders = new IndexReader[indexMap.size()];
			int i = 0;
			analyzerMap = new HashMap<String, AnalyzerDefinition>();
			fieldMap = new HashMap<String, FieldDefinition>();
			for (IndexInstance indexInstance : indexMap.values()) {
				indexReaders[i++] = DirectoryReader.open(indexInstance.getDataDirectory());
				indexInstance.fillFields(fieldMap);
				indexInstance.fillAnalyzers(analyzerMap);
			}
			multiReader = new MultiReader(indexReaders);
			indexSearcher = new IndexSearcher(multiReader);
			AnalyzerContext analyzerContext = new AnalyzerContext(analyzerMap, fieldMap);
			queryAnalyzer = new UpdatableAnalyzer(analyzerContext, analyzerContext.queryAnalyzerMap);
		}

		int numDocs() {
			if (multiReader == null)
				return 0;
			return multiReader.numDocs();
		}

		public void close() {
			if (multiReader != null)
				IOUtils.close(multiReader);
		}

		public ResultDefinition search(QueryDefinition queryDef)
				throws ServerException, IOException, QueryNodeException, InterruptedException, ParseException,
				ReflectiveOperationException {
			if (indexSearcher == null)
				return null;
			final QueryContext queryContext = new QueryContext(indexSearcher, queryAnalyzer, queryDef);
			return QueryUtils.search(queryContext);
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
			indexMap.put(indexDirectory.getName(), IndexInstance.newInstance(this, indexDirectory, null));
		mayBeRefresh();
	}

	@Override
	public void close() throws IOException {
		if (searchContext != null) {
			searchContext.close();
			searchContext = null;
		}
		synchronized (indexMap) {
			for (IndexInstance instance : indexMap.values())
				IOUtils.closeQuietly(instance);
		}
	}

	IndexStatus createUpdate(String indexName, IndexSettingsDefinition settings)
			throws ServerException, IOException, InterruptedException, ReflectiveOperationException {
		synchronized (indexMap) {
			IndexInstance indexInstance = indexMap.get(indexName);
			if (indexInstance != null && settings != null) {
				IOUtils.closeQuietly(indexInstance);
				indexMap.remove(indexName);
				indexInstance = null;
			}
			if (indexInstance == null) {
				indexInstance = IndexInstance.newInstance(this, new File(schemaDirectory, indexName), settings);
				indexMap.put(indexName, indexInstance);
			}
			mayBeRefresh();
			return indexInstance.getStatus();
		}
	}

	/**
	 * Returns the indexInstance. If the index does not exists, an exception it
	 * thrown. This method never returns a null value.
	 *
	 * @param indexName The name of the index
	 * @return the indexInstance
	 * @throws ServerException if any error occurs
	 */
	IndexInstance get(String indexName) throws ServerException {
		IndexInstance indexInstance = indexMap.get(indexName);
		if (indexInstance == null)
			throw new ServerException(Response.Status.NOT_FOUND, "Index not found: " + indexName);
		return indexInstance;
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
			IndexInstance indexInstance = get(indexName);
			indexInstance.delete();
			indexMap.remove(indexName);
			mayBeRefresh();
		}
	}

	Set<String> nameSet() {
		return indexMap.keySet();
	}

	void backups(Integer keepLastCount) throws IOException, InterruptedException {
		synchronized (indexMap) {
			for (IndexInstance instance : indexMap.values())
				instance.backup(keepLastCount);
		}
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

	synchronized void mayBeRefresh() throws IOException, ServerException {
		searchContext = new SearchContext();
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
	}

	private static ResultDefinition atomicSearch(SearchContext searchContext, QueryDefinition queryDef)
			throws InterruptedException, IOException, QueryNodeException, ParseException, ServerException,
			ReflectiveOperationException {
		if (searchContext == null)
			return null;
		return searchContext.search(queryDef);
	}

	public ResultDefinition search(QueryDefinition queryDef)
			throws ServerException, IOException, QueryNodeException, InterruptedException, ParseException,
			ReflectiveOperationException {
		final Semaphore sem = acquireReadSemaphore();
		try {
			return atomicSearch(searchContext, queryDef);
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

	void checkSize(int addSize) throws IOException, ServerException {
		atomicCheckSize(settingsDefinition, searchContext, addSize);
	}
}
