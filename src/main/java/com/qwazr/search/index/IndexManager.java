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

import com.qwazr.utils.IOUtils;
import com.qwazr.utils.server.ServerException;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IndexManager {

	private static final Logger logger = LoggerFactory.getLogger(IndexManager.class);

	public static volatile IndexManager INSTANCE = null;

	public static void load(File directory) throws IOException {
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

	private final ConcurrentHashMap<String, SchemaInstance> schemaMap;

	private final File rootDirectory;

	private IndexManager(File rootDirectory) {
		this.rootDirectory = rootDirectory;
		schemaMap = new ConcurrentHashMap<String, SchemaInstance>();
		File[] directories = rootDirectory.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		if (directories == null)
			return;
		for (File schemaDirectory : directories) {
			try {
				schemaMap.put(schemaDirectory.getName(), new SchemaInstance(schemaDirectory));
			} catch (ServerException | IOException | ReflectiveOperationException | InterruptedException e) {
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

	boolean createUpdate(String schemaName)
			throws ServerException, IOException, InterruptedException, ReflectiveOperationException {
		synchronized (schemaMap) {
			SchemaInstance schemaInstance = schemaMap.get(schemaName);
			if (schemaInstance != null)
				return false;
			schemaInstance = new SchemaInstance(new File(rootDirectory, schemaName));
			schemaMap.put(schemaName, schemaInstance);
			return true;
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
