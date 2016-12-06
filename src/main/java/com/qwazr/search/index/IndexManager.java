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

import com.qwazr.utils.FunctionUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.server.ServerBuilder;
import com.qwazr.utils.server.ServerConfiguration;
import com.qwazr.utils.server.ServerException;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexManager implements Closeable {

	public final static String SERVICE_NAME_SEARCH = "search";
	public final static String INDEXES_DIRECTORY = "index";

	private static final Logger logger = LoggerFactory.getLogger(IndexManager.class);

	static volatile IndexManager INSTANCE = null;

	public synchronized static void load(final ServerBuilder builder, final ServerConfiguration configuration,
			final ExecutorService executorService)
			throws IOException {
		if (INSTANCE != null)
			throw new IOException("Already loaded");
		INSTANCE = new IndexManager(builder, configuration, executorService);
	}

	private final ConcurrentHashMap<String, SchemaInstance> schemaMap;

	private final File rootDirectory;

	private final IndexServiceInterface service;

	private final ExecutorService executorService;

	private IndexManager(final ServerBuilder builder, final ServerConfiguration configuration,
			final ExecutorService executorService) throws IOException {
		this(new File(configuration.dataDirectory, INDEXES_DIRECTORY), executorService);
		builder.registerWebService(IndexServiceImpl.class);
		builder.registerShutdownListener(server -> close());
	}

	public IndexManager(final Path workDirectory, final ExecutorService executorService) throws IOException {
		this(workDirectory.toFile(), executorService);
	}

	public IndexManager(final File workDirectory, final ExecutorService executorService) throws IOException {
		this.rootDirectory = workDirectory;
		this.executorService = executorService;
		if (!rootDirectory.exists())
			rootDirectory.mkdir();
		if (!rootDirectory.isDirectory())
			throw new IOException(
					"This name is not valid. No directory exists for this location: " + rootDirectory.getName());
		service = new IndexServiceImpl(this);
		schemaMap = new ConcurrentHashMap<>();
		File[] directories = rootDirectory.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		if (directories == null)
			return;
		for (File schemaDirectory : directories) {
			try {
				schemaMap.put(schemaDirectory.getName(), new SchemaInstance(service, schemaDirectory, executorService));
			} catch (ServerException | IOException | ReflectiveOperationException | URISyntaxException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	final public IndexServiceInterface getService() {
		return service;
	}

	@Override
	public void close() {
		synchronized (schemaMap) {
			schemaMap.values().forEach(IOUtils::closeQuietly);
		}
	}

	SchemaSettingsDefinition createUpdate(String schemaName, SchemaSettingsDefinition settings)
			throws IOException, ReflectiveOperationException, URISyntaxException {
		synchronized (schemaMap) {
			SchemaInstance schemaInstance = schemaMap.get(schemaName);
			if (schemaInstance == null) {
				schemaInstance = new SchemaInstance(service, new File(rootDirectory, schemaName), executorService);
				schemaMap.put(schemaName, schemaInstance);
			}
			if (settings != null)
				schemaInstance.setSettings(settings);
			return schemaInstance.getSettings();
		}
	}

	/**
	 * Returns the indexSchema. If the schema does not exist, an exception it
	 * thrown. This method never returns a null value.
	 *
	 * @param schemaName The name of the index
	 * @return the indexSchema
	 * @throws ServerException if any error occurs
	 */
	SchemaInstance get(final String schemaName) {
		final SchemaInstance schemaInstance = schemaMap.get(schemaName);
		if (schemaInstance == null)
			throw new ServerException(Status.NOT_FOUND, "Schema not found: " + schemaName);
		return schemaInstance;
	}

	void delete(final String schemaName) {
		synchronized (schemaMap) {
			final SchemaInstance schemaInstance = get(schemaName);
			schemaInstance.delete();
			schemaMap.remove(schemaName);
		}
	}

	Set<String> nameSet() {
		synchronized (schemaMap) {
			return new TreeSet<>(schemaMap.keySet());
		}
	}

	private void schemaIterator(final String schemaName,
			final FunctionUtils.BiConsumerEx<String, SchemaInstance, IOException> consumer) throws IOException {
		synchronized (schemaMap) {
			if ("*".equals(schemaName)) {
				for (Map.Entry<String, SchemaInstance> entry : schemaMap.entrySet())
					consumer.accept(entry.getKey(), entry.getValue());
			} else
				consumer.accept(schemaName, get(schemaName));
		}
	}

	SortedMap<String, SortedMap<String, BackupStatus>> backups(final String schemaName, final String indexName,
			final String backupName) throws IOException {
		final SortedMap<String, SortedMap<String, BackupStatus>> results = new TreeMap<>();
		schemaIterator(schemaName, (schName, schemaInstance) -> {
			synchronized (results) {
				final SortedMap<String, BackupStatus> schemaResults = schemaInstance.backups(indexName, backupName);
				if (schemaResults != null && !schemaResults.isEmpty())
					results.put(schName, schemaResults);
			}
		});
		return results;
	}

	SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>> getBackups(final String schemaName,
			final String indexName, final String backupName) throws IOException {
		final SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>> results = new TreeMap<>();
		schemaIterator(schemaName, (schName, schemaInstance) -> {
			synchronized (results) {
				final SortedMap<String, SortedMap<String, BackupStatus>> schemaResults =
						schemaInstance.getBackups(indexName, backupName);
				if (schemaResults != null && !schemaResults.isEmpty())
					results.put(schName, schemaResults);
			}
		});
		return results;
	}

	int deleteBackups(final String schemaName, final String indexName, final String backupName) throws IOException {
		final AtomicInteger counter = new AtomicInteger();
		schemaIterator(schemaName,
				(schName, schemaInstance) -> counter.addAndGet(schemaInstance.deleteBackups(indexName, backupName)));
		return counter.get();
	}

}
