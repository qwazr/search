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
package com.qwazr.search.test.units;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.index.ExplainDefinition;
import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.utils.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractIndexTest {

	private static Path rootDirectory;
	static IndexManager indexManager;
	static AnnotatedIndexService<IndexRecord> indexService;

	static final Logger LOGGER = LoggerFactory.getLogger(AbstractIndexTest.class);

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

	static ResultDefinition.WithObject<IndexRecord> checkQuery(QueryDefinition queryDef) {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(queryDef);
		Assert.assertNotNull(result);
		if (result.query != null)
			LOGGER.info(result.query);
		Assert.assertEquals(Long.valueOf(1), result.total_hits);
		ExplainDefinition explain = indexService.explainQuery(queryDef, result.documents.get(0).getDoc());
		Assert.assertNotNull(explain);
		return result;
	}

	@AfterClass
	public static void afterClass() throws IOException {
		if (indexManager != null) {
			indexManager.close();
			indexManager = null;
		}
		if (rootDirectory != null)
			FileUtils.deleteDirectoryQuietly(rootDirectory);
	}

}
