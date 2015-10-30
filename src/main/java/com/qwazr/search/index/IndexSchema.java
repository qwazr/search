/**
 * Copyright 2015 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.FileClassCompilerLoader;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.json.JsonMapper;
import com.qwazr.utils.server.ServerException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.lucene.analysis.Analyzer;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class IndexSchema implements Closeable, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(IndexSchema.class);

	private final static String SETTINGS_FILE = "settings.json";

	private final ConcurrentHashMap<String, IndexInstance> indexMap;

	private final File schemaDirectory;
	private final File settingsFile;
	private volatile SettingsDefinition settingsDefinition;

	private volatile Semaphore readSemaphore;
	private volatile Semaphore writeSemaphore;
	private volatile FileClassCompilerLoader fileClassCompilerLoader;

	private volatile SearchContext searchContext = null;

	private class SearchContext implements Closeable, AutoCloseable {

		private final MultiReader multiReader;
		private final IndexSearcher indexSearcher;
		private final Map<String, FieldDefinition> fieldMap;
		private final PerFieldAnalyzer perFieldAnalyzer;

		private SearchContext() throws IOException {
			if (indexMap.isEmpty()) {
				indexSearcher = null;
				multiReader = null;
				perFieldAnalyzer = null;
				fieldMap = null;
				return;
			}
			IndexReader[] indexReaders = new IndexReader[indexMap.size()];
			int i = 0;
			Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
			fieldMap = new HashMap<String, FieldDefinition>();
			for (IndexInstance indexInstance : indexMap.values()) {
				indexReaders[i++] = DirectoryReader.open(indexInstance.getLuceneDirectory());
				indexInstance.fillAnalyzers(analyzerMap);
				indexInstance.fillFields(fieldMap);
			}
			multiReader = new MultiReader(indexReaders);
			indexSearcher = new IndexSearcher(multiReader);
			perFieldAnalyzer = new PerFieldAnalyzer(analyzerMap);
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
						throws ServerException, IOException, QueryNodeException, InterruptedException, ParseException {
			if (indexSearcher == null)
				return null;
			return QueryUtils.search(fieldMap, indexSearcher, queryDef, perFieldAnalyzer, null);
		}
	}

	IndexSchema(File schemaDirectory) throws IOException, ServerException {
		this.schemaDirectory = schemaDirectory;
		if (!schemaDirectory.exists())
			schemaDirectory.mkdir();
		if (!schemaDirectory.exists())
			throw new IOException("The directory does not exist: " + schemaDirectory.getName());
		indexMap = new ConcurrentHashMap<String, IndexInstance>();

		settingsFile = new File(schemaDirectory, SETTINGS_FILE);
		settingsDefinition = settingsFile.exists() ?
						JsonMapper.MAPPER.readValue(settingsFile, SettingsDefinition.class) :
						null;
		checkSettings();

		File[] directories = schemaDirectory.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		if (directories == null)
			return;
		for (File indexDirectory : directories)
			indexMap.put(indexDirectory.getName(), IndexInstance.newInstance(this, indexDirectory));
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

	IndexStatus createUpdate(String indexName, Map<String, FieldDefinition> fields)
					throws ServerException, IOException, InterruptedException {
		synchronized (indexMap) {
			IndexInstance indexInstance = indexMap.get(indexName);
			if (indexInstance == null) {
				indexInstance = IndexInstance.newInstance(this, new File(schemaDirectory, indexName));
				indexMap.put(indexName, indexInstance);
			}
			if (fields != null)
				indexInstance.setFields(this, fields);
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

	synchronized void setSettings(SettingsDefinition settings) throws IOException {
		if (settings == null)
			settingsFile.delete();
		else
			JsonMapper.MAPPER.writeValue(settingsFile, settings);
		this.settingsDefinition = settings;
		checkSettings();
	}

	synchronized void mayBeRefresh() throws IOException {
		searchContext = new SearchContext();
	}

	SettingsDefinition getSettings() {
		return settingsDefinition;
	}

	private synchronized void checkSettings() throws IOException {
		if (settingsDefinition == null) {
			readSemaphore = null;
			writeSemaphore = null;
			if (fileClassCompilerLoader != null) {
				fileClassCompilerLoader.close();
				fileClassCompilerLoader = null;
			}
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

		FileClassCompilerLoader oldFccl = fileClassCompilerLoader;
		fileClassCompilerLoader = (settingsDefinition.javac != null && settingsDefinition.javac.source_root != null) ?
						new FileClassCompilerLoader(settingsDefinition.javac) :
						null;
		if (oldFccl != null)
			oldFccl.close();
	}

	private static ResultDefinition atomicSearch(SearchContext searchContext, QueryDefinition queryDef)
					throws InterruptedException, IOException, QueryNodeException, ParseException, ServerException {
		if (searchContext == null)
			return null;
		return searchContext.search(queryDef);
	}

	public ResultDefinition search(QueryDefinition queryDef)
					throws ServerException, IOException, QueryNodeException, InterruptedException, ParseException {
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

	private static void atomicCheckSize(SettingsDefinition settingsDefinition, SearchContext searchContext, int addSize)
					throws ServerException {
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

	FileClassCompilerLoader getFileClassCompilerLoader() {
		return fileClassCompilerLoader;
	}
}
