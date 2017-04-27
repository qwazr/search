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
package com.qwazr.search.test.units;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.index.IndexManager;
import com.qwazr.utils.FileUtils;
import org.junit.AfterClass;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractIndexTest {

	private static Path rootDirectory;
	static IndexManager indexManager;
	static AnnotatedIndexService<IndexRecord> indexService;

	static void initIndexManager() throws IOException, URISyntaxException {
		rootDirectory = Files.createTempDirectory("qwazr_index_test");
		indexManager = new IndexManager(rootDirectory, null);
	}

	static void initIndexService() throws IOException, URISyntaxException {
		indexService = createIndexService(IndexRecord.class);
	}

	static <T> AnnotatedIndexService<T> createIndexService(Class<T> recordClass)
			throws URISyntaxException, IOException {
		if (indexManager == null)
			initIndexManager();
		final AnnotatedIndexService<T> indexService = indexManager.getService(recordClass);
		indexService.createUpdateSchema();
		indexService.createUpdateIndex();
		indexService.createUpdateFields();
		return indexService;
	}

	@AfterClass
	public static void afterClass() throws IOException {
		indexManager.close();
		FileUtils.deleteQuietly(rootDirectory.toFile());
	}

}
