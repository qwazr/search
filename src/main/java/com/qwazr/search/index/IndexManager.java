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

import com.qwazr.utils.IOUtils;
import com.qwazr.utils.server.ServerBuilder;
import com.qwazr.utils.server.ServerException;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class IndexManager {

	public final static String SERVICE_NAME_SEARCH = "search";
	public final static String INDEXES_DIRECTORY = "index";

	private static final Logger logger = LoggerFactory.getLogger(IndexManager.class);

	static IndexManager INSTANCE = null;

	public synchronized static void load(final ServerBuilder builder) throws IOException {
		if (INSTANCE != null)
			throw new IOException("Already loaded");
		INSTANCE = new IndexManager(builder);
	}

	private final ExecutorService executorService;

	private final ConcurrentHashMap<String, SchemaInstance> schemaMap;

	private final File rootDirectory;

	private IndexManager(final ServerBuilder builder) throws IOException {
		this.executorService = builder.getExecutorService();
		rootDirectory = new File(builder.getServerConfiguration().dataDirectory, INDEXES_DIRECTORY);
		if (!rootDirectory.exists())
			rootDirectory.mkdir();
		if (!rootDirectory.isDirectory())
			throw new IOException(
					"This name is not valid. No directory exists for this location: " + rootDirectory.getName());
		builder.registerWebService(IndexServiceImpl.class);
		builder.registerShutdownListener(server -> shutdown());
		schemaMap = new ConcurrentHashMap<>();
		File[] directories = rootDirectory.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		if (directories == null)
			return;
		for (File schemaDirectory : directories) {
			try {
				schemaMap.put(schemaDirectory.getName(), new SchemaInstance(executorService, schemaDirectory));
			} catch (ServerException | IOException | ReflectiveOperationException | InterruptedException | URISyntaxException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void shutdown() {
		synchronized (schemaMap) {
			for (SchemaInstance instance : schemaMap.values())
				IOUtils.closeQuietly(instance);
		}
	}

	SchemaSettingsDefinition createUpdate(String schemaName, SchemaSettingsDefinition settings)
			throws ServerException, IOException, InterruptedException, ReflectiveOperationException,
			URISyntaxException {
		synchronized (schemaMap) {
			SchemaInstance schemaInstance = schemaMap.get(schemaName);
			if (schemaInstance == null) {
				schemaInstance = new SchemaInstance(executorService, new File(rootDirectory, schemaName));
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
	SchemaInstance get(String schemaName) throws ServerException {
		SchemaInstance schemaInstance = schemaMap.get(schemaName);
		if (schemaInstance == null)
			throw new ServerException(Status.NOT_FOUND, "Schema not found: " + schemaName);
		return schemaInstance;
	}

	void delete(String schemaName) throws ServerException {
		synchronized (schemaMap) {
			SchemaInstance schemaInstance = get(schemaName);
			schemaInstance.delete();
			schemaMap.remove(schemaName);
		}
	}

	Set<String> nameSet() {
		return schemaMap.keySet();
	}

	void backups(Integer keepLastCount) throws IOException, InterruptedException {
		synchronized (schemaMap) {
			for (SchemaInstance instance : schemaMap.values())
				instance.backups(keepLastCount);
		}
	}
}
