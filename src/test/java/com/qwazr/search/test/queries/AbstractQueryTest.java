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
package com.qwazr.search.test.queries;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexManager;
import com.qwazr.utils.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractQueryTest {

	private static Path rootDirectory;
	private static IndexManager indexManager;
	protected static AnnotatedIndexService<IndexRecord> indexService;

	@BeforeClass
	public static void beforeClass() throws IOException, URISyntaxException {
		rootDirectory = Files.createTempDirectory("qwazr_index_test");
		indexManager = new IndexManager(null, rootDirectory, null);
		indexService = indexManager.getService(IndexRecord.class);
		indexService.createUpdateSchema();
		indexService.createUpdateIndex();
		indexService.createUpdateFields();
	}

	@AfterClass
	public static void afterClass() throws IOException {
		indexManager.close();
		FileUtils.deleteQuietly(rootDirectory.toFile());
	}

	@Index(name = "QueryTest", schema = "QueriesTest")
	public static class IndexRecord {

		@IndexField(name = FieldDefinition.ID_FIELD, template = FieldDefinition.Template.StringField)
		final public String id;

		@IndexField(name = "label",
				template = FieldDefinition.Template.TextField,
				analyzerClass = StandardAnalyzer.class)
		public String label;

		@IndexField(name = "intDocValue", template = FieldDefinition.Template.IntDocValuesField)
		public Integer intDocValue;

		public IndexRecord() {
			id = null;
		}

		protected IndexRecord(String id) {
			this.id = id;
		}

		protected IndexRecord label(String label) {
			this.label = label;
			return this;
		}

		protected IndexRecord intDocValue(Integer intDocValue) {
			this.intDocValue = intDocValue;
			return this;
		}
	}
}
