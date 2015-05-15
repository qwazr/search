/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qwazr.cluster.manager.ClusterManager;
import com.qwazr.search.SearchServer;
import com.qwazr.search.index.osse.OsseException;
import com.qwazr.search.index.osse.OsseIndex;
import com.qwazr.search.memory.MemoryBuffer;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.server.ServerException;

public class IndexManager {

	private static final Logger logger = LoggerFactory
			.getLogger(IndexManager.class);

	public static volatile IndexManager INSTANCE = null;

	public static void load(File directory) throws IOException {
		OsseIndex.initOsseJNILibrary();
		if (INSTANCE != null)
			throw new IOException("Already loaded");
		INSTANCE = new IndexManager(directory);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				INSTANCE.shutdown();
			}
		});
	}

	private final MemoryBuffer memoryBuffer;

	private final ConcurrentHashMap<String, IndexInstance> indexMap;

	private final File rootDirectory;

	private IndexManager(File rootDirectory) {
		this.rootDirectory = rootDirectory;
		memoryBuffer = new MemoryBuffer();
		indexMap = new ConcurrentHashMap<String, IndexInstance>();
		File[] directories = rootDirectory
				.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		if (directories == null)
			return;
		for (File indexDirectory : directories)
			try {
				indexMap.put(indexDirectory.getName(), new IndexInstance(
						indexDirectory, memoryBuffer));
			} catch (OsseException | IOException e) {
				logger.error(e.getMessage(), e);
			}
	}

	private void shutdown() {
		synchronized (indexMap) {
			for (IndexInstance instance : indexMap.values())
				IOUtils.closeQuietly(instance);
		}
		IOUtils.closeQuietly(memoryBuffer);
	}

	public IndexStatus create(String indexName,
			Map<String, FieldDefinition> fields) throws ServerException,
			IOException, OsseException {
		synchronized (indexMap) {
			IndexInstance indexInstance = indexMap.get(indexName);
			if (indexInstance != null)
				throw new ServerException(Status.CONFLICT,
						"An index with this name already exists");
			indexInstance = new IndexInstance(
					new File(rootDirectory, indexName), memoryBuffer);
			indexMap.put(indexName, indexInstance);
			indexInstance.createFields(fields);
			return indexInstance.getStatus();
		}
	}

	/**
	 * Returns the indexInstance. If the index does not exists, an exception it
	 * thrown. This method never returns a null value.
	 * 
	 * @param indexName
	 *            The name of the index
	 * @return the indexInstance
	 * @throws ServerException
	 *             if any error occurs
	 */
	public IndexInstance get(String indexName) throws ServerException {
		IndexInstance indexInstance = indexMap.get(indexName);
		if (indexInstance == null)
			throw new ServerException(Status.NOT_FOUND, "Index not found");
		return indexInstance;
	}

	public void delete(String indexName) throws ServerException {
		synchronized (indexMap) {
			IndexInstance indexInstance = get(indexName);
			indexInstance.delete();
			indexMap.remove(indexName);
		}
	}

	public Set<String> nameSet() {
		return indexMap.keySet();
	}

	public static IndexServiceInterface getClient(int timeOut)
			throws URISyntaxException {
		return new IndexSingleClient(ClusterManager.INSTANCE.myAddress, timeOut);
	}

	static IndexMultiClient getClient() throws URISyntaxException {
		return new IndexMultiClient(ClusterManager.INSTANCE.getClusterClient()
				.getActiveNodes(SearchServer.SERVICE_NAME_INDEX), 60000);
	}

}
