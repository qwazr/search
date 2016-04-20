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
import com.qwazr.search.index.*;
import com.qwazr.search.query.TermQuery;
import com.qwazr.search.query.TermsQuery;
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
public class JavaTest {

	public static final String[] RETURNED_FIELDS = { "title", "content", "price", "storedCategory" };

	@BeforeClass
	public static void startSearchServer() throws Exception {
		TestServer.startServer();
	}

	private AnnotatedIndexService<AnnotatedIndex> getService() throws URISyntaxException {
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
			0d, "news", "economy");
	private final static AnnotatedIndex record2 = new AnnotatedIndex(2, "Second article",
			"Content of the second article", 0d, "news", "science");

	private AnnotatedIndex checkRecord(AnnotatedIndex refRecord)
			throws URISyntaxException, ReflectiveOperationException {
		final AnnotatedIndexService<AnnotatedIndex> service = getService();
		AnnotatedIndex record = service.getDocument(refRecord.id.toString());
		Assert.assertNotNull(record);
		return record;
	}

	@Test
	public void test100PostDocument()
			throws URISyntaxException, IOException, InterruptedException, ReflectiveOperationException {
		final AnnotatedIndexService service = getService();
		service.postDocument(record1);
		AnnotatedIndex newRecord1 = checkRecord(record1);
		Assert.assertEquals(record1, newRecord1);
	}

	@Test
	public void test110PostDocuments()
			throws URISyntaxException, IOException, InterruptedException, ReflectiveOperationException {
		final AnnotatedIndexService service = getService();
		service.postDocuments(Arrays.asList(record1, record2));
		AnnotatedIndex newRecord1 = checkRecord(record1);
		Assert.assertEquals(record1, newRecord1);
		AnnotatedIndex newRecord2 = checkRecord(record2);
		Assert.assertEquals(record2, newRecord2);
	}

	private final static AnnotatedIndex docValue1 = new AnnotatedIndex(1, null, null, 1.11d);
	private final static AnnotatedIndex docValue2 = new AnnotatedIndex(2, null, null, 2.22d);

	@Test
	public void test200UpdateDocValues() throws URISyntaxException, IOException, InterruptedException {
		final AnnotatedIndexService service = getService();
		service.updateDocumentValues(docValue1);
	}

	@Test
	public void test210UpdateDocsValues()
			throws URISyntaxException, IOException, InterruptedException, ReflectiveOperationException {
		final AnnotatedIndexService service = getService();
		service.updateDocumentsValues(Arrays.asList(docValue1, docValue2));
		checkRecord(record1);
		checkRecord(record2);
	}

	@Test
	public void test300SimpleTermQuery() throws URISyntaxException {
		final AnnotatedIndexService service = getService();
		QueryBuilder builder = new QueryBuilder();
		builder.query = new TermQuery(FieldDefinition.ID_FIELD, "1");
		ResultDefinition result = service.searchQuery(builder.build());
		Assert.assertNotNull(result);
		Assert.assertEquals(new Long(1), result.total_hits);
	}

	@Test
	public void test301MultiTermQuery() throws URISyntaxException {
		final AnnotatedIndexService service = getService();
		QueryBuilder builder = new QueryBuilder();
		builder.query = new TermsQuery(FieldDefinition.ID_FIELD, "1", "2");
		ResultDefinition result = service.searchQuery(builder.build());
		Assert.assertNotNull(result);
		Assert.assertEquals(new Long(2), result.total_hits);
	}

	private ResultDocumentObject<AnnotatedIndex> checkResultDocument(
			ResultDefinition<ResultDocumentObject<AnnotatedIndex>> result, int pos) {
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.documents);
		Assert.assertTrue(result.documents.size() > pos);
		ResultDocumentObject<AnnotatedIndex> resultDocument = result.documents.get(pos);
		Assert.assertNotNull(resultDocument);
		return resultDocument;
	}

	private final void testReturnedFieldQuery(String... returnedFields) throws URISyntaxException {
		final AnnotatedIndexService service = getService();
		QueryBuilder builder = new QueryBuilder();
		builder.query = new TermQuery(FieldDefinition.ID_FIELD, record2.id.toString());
		builder.addReturned_field(returnedFields);
		ResultDefinition.WithObject<AnnotatedIndex> result = service.searchQuery(builder.build());
		Assert.assertNotNull(result);
		Assert.assertEquals(new Long(1), result.total_hits);
		AnnotatedIndex returnedRecord = checkResultDocument(result, 0).record;
		Assert.assertEquals(record2.title, returnedRecord.title);
		Assert.assertEquals(record2.content, returnedRecord.content);
		Assert.assertEquals(docValue2.price, returnedRecord.price);
		Assert.assertArrayEquals(record2.storedCategory.toArray(), returnedRecord.storedCategory.toArray());
	}

	@Test
	public void test350ReturnedFieldQuery() throws URISyntaxException {
		testReturnedFieldQuery(RETURNED_FIELDS);
	}

	@Test
	public void test360ReturnedFieldQueryAll() throws URISyntaxException {
		testReturnedFieldQuery("*");
	}

	@Test
	public void test980DeleteIndex() throws URISyntaxException {
		final AnnotatedIndexService service = getService();
		service.deleteIndex();
	}

	@Test
	public void test990DeleteSchema() throws URISyntaxException {
		final AnnotatedIndexService service = getService();
		service.deleteSchema();
	}

}
