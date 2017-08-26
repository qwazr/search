/*
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
import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.server.ApplicationBuilder;
import com.qwazr.server.GenericServer;
import com.qwazr.server.ServerException;
import com.qwazr.utils.FunctionUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.reflection.ConstructorParameters;
import com.qwazr.utils.reflection.ConstructorParametersImpl;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

import javax.ws.rs.core.Response.Status;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IndexManager extends ConstructorParametersImpl implements Closeable {

	public final static String INDEXES_DIRECTORY = "index";

	private static final Logger LOGGER = LoggerUtils.getLogger(IndexManager.class);

	private final ConcurrentHashMap<String, SchemaInstance> schemaMap;

	private final File rootDirectory;

	private final IndexServiceInterface service;

	private final ConcurrentHashMap<String, AnalyzerFactory> analyzerFactoryMap;

	private final ExecutorService executorService;

	public IndexManager(final Path indexesDirectory, final ExecutorService executorService,
			final ConstructorParameters constructorParameters) throws IOException {
		super(constructorParameters == null ? new ConcurrentHashMap<>() : constructorParameters.getMap());
		this.rootDirectory = indexesDirectory.toFile();
		this.executorService = executorService;

		service = new IndexServiceImpl(this);
		schemaMap = new ConcurrentHashMap<>();
		analyzerFactoryMap = new ConcurrentHashMap<>();

		final File[] directories = rootDirectory.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		if (directories == null)
			return;
		for (File schemaDirectory : directories) {
			try {
				schemaMap.put(schemaDirectory.getName(),
						new SchemaInstance(this, analyzerFactoryMap, service, schemaDirectory, executorService));
			} catch (ServerException | IOException | ReflectiveOperationException | URISyntaxException e) {
				LOGGER.log(Level.SEVERE, e, e::getMessage);
			}
		}
	}

	public IndexManager(final Path indexesDirectory, final ExecutorService executorService) throws IOException {
		this(indexesDirectory, executorService, null);
	}

	public static Path checkIndexesDirectory(final Path dataDirectory) throws IOException {
		final Path indexesDirectory = dataDirectory.resolve(INDEXES_DIRECTORY);
		if (!Files.exists(indexesDirectory))
			Files.createDirectory(indexesDirectory);
		if (!Files.isDirectory(indexesDirectory))
			throw new IOException("This name is not valid. No directory exists for this location: " + indexesDirectory);
		return indexesDirectory;
	}

	public IndexManager registerContextAttribute(final GenericServer.Builder builder) {
		builder.contextAttribute(this);
		return this;
	}

	public IndexManager registerWebService(final ApplicationBuilder builder) {
		builder.singletons(service);
		return this;
	}

	public IndexManager registerShutdownListener(final GenericServer.Builder builder) {
		builder.shutdownListener(server -> close());
		return this;
	}

	public IndexManager registerAnalyzerFactory(final String name, final AnalyzerFactory factory) {
		analyzerFactoryMap.put(name, factory);
		return this;
	}

	final public IndexServiceInterface getService() {
		return service;
	}

	final public <T> AnnotatedIndexService<T> getService(Class<T> indexClass) throws URISyntaxException {
		return new AnnotatedIndexService<>(service, indexClass);
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
				schemaInstance =
						new SchemaInstance(this, analyzerFactoryMap, service, new File(rootDirectory, schemaName),
								executorService);
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
				if ("*".equals(schemaName) && StringUtils.isEmpty(schemaInstance.getSettings().backupDirectoryPath))
					return;
				final SortedMap<String, BackupStatus> schemaResults = schemaInstance.backups(indexName, backupName);
				if (schemaResults != null && !schemaResults.isEmpty())
					results.put(schName, schemaResults);
			}
		});
		return results;
	}

	SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>> getBackups(final String schemaName,
			final String indexName, final String backupName, final boolean extractVersion) throws IOException {
		final SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>> results = new TreeMap<>();
		schemaIterator(schemaName, (schName, schemaInstance) -> {
			synchronized (results) {
				final SortedMap<String, SortedMap<String, BackupStatus>> schemaResults =
						schemaInstance.getBackups(indexName, backupName, extractVersion);
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
