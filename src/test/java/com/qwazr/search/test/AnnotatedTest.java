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
package com.qwazr.search.test;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.search.index.SchemaSettingsDefinition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashMap;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AnnotatedTest {

	public static final String SCHEMA_NAME = "schema-test-full";
	public static final String INDEX_NAME = "index-test-full";

	@BeforeClass
	public static void startSearchServer() throws Exception {
		TestServer.startServer();
	}

	private AnnotatedIndexService getService() throws URISyntaxException {
		return TestServer.getService(AnnotatedIndex.class);
	}

	@Test
	public void test000CreateSchema() throws URISyntaxException {
		final AnnotatedIndexService service = getService();
		SchemaSettingsDefinition settings = service.createUpdateSchema();
		Assert.assertNotNull(settings);
	}

	@Test
	public void test010CreateIndex() throws URISyntaxException {
		final AnnotatedIndexService service = getService();
		IndexStatus indexStatus = service.createUpdateIndex();
		Assert.assertNotNull(indexStatus);
	}

	@Test
	public void test020CreateUpdateFields() throws URISyntaxException {
		final AnnotatedIndexService service = getService();
		LinkedHashMap<String, FieldDefinition> fields = service.createUpdateFields();
		Assert.assertNotNull(fields);
		FieldDefinition field;
		Assert.assertNotNull("The Title field is not present", field = fields.get("title"));
		Assert.assertEquals("en.EnglishAnalyzer", field.analyzer);
		Assert.assertNotNull("The Content field is not present", field = fields.get("content"));
		Assert.assertEquals("en.EnglishAnalyzer", field.analyzer);
		Assert.assertNotNull("The Category field is not present", field = fields.get("category"));
		Assert.assertEquals(FieldDefinition.Template.SortedSetMultiDocValuesFacetField, field.template);
		Assert.assertNotNull("The Price field is not present", field = fields.get("price"));
		Assert.assertEquals(FieldDefinition.Template.DoubleDocValuesField, field.template);
	}

	private final static AnnotatedIndex record1 = new AnnotatedIndex(1, "First article", "Content of the first article",
			new String[] { "news", "economy" }, 0d);
	private final static AnnotatedIndex record2 = new AnnotatedIndex(2, "Second article",
			"Content of the second article", new String[] { "news", "science" }, 0d);

	@Test
	public void test100PostDocument() throws URISyntaxException, IOException, InterruptedException {
		final AnnotatedIndexService service = getService();
		service.postDocument(record1);
	}

	@Test
	public void test110PostDocuments() throws URISyntaxException, IOException, InterruptedException {
		final AnnotatedIndexService service = getService();
		service.postDocuments(Arrays.asList(record1, record2));
	}

	private final static AnnotatedIndex docValue1 = new AnnotatedIndex(1, null, null, null, 1.11d);
	private final static AnnotatedIndex docValue2 = new AnnotatedIndex(2, null, null, null, 2.22d);

	@Test
	public void test200UpdateDocValues() throws URISyntaxException, IOException, InterruptedException {
		final AnnotatedIndexService service = getService();
		service.updateDocumentValues(docValue1);
	}

	@Test
	public void test210UpdateDocsValues() throws URISyntaxException, IOException, InterruptedException {
		final AnnotatedIndexService service = getService();
		service.updateDocumentsValues(Arrays.asList(docValue1, docValue2));
	}
}
