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

import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.*;
import com.qwazr.search.query.*;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.search.join.ScoreMode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JavaTest {

	public static final String[] RETURNED_FIELDS =
			{FieldDefinition.ID_FIELD, "title", "content", "price", "storedCategory"};

	@BeforeClass
	public static void startSearchServer() throws Exception {
		TestServer.startServer();
	}

	private AnnotatedIndexService<AnnotatedIndex.Master> getMaster() throws URISyntaxException {
		return TestServer.getService(AnnotatedIndex.Master.class);
	}

	private AnnotatedIndexService<AnnotatedIndex.Slave> getSlave() throws URISyntaxException {
		return TestServer.getService(AnnotatedIndex.Slave.class);
	}

	@Test
	public void test000CreateSchema() throws URISyntaxException {
		final AnnotatedIndexService service = getMaster();
		SchemaSettingsDefinition settings = service.createUpdateSchema();
		Assert.assertNotNull(settings);
	}

	@Test
	public void test010CreateIndex() throws URISyntaxException {
		final AnnotatedIndexService service = getMaster();
		IndexStatus indexStatus = service.createUpdateIndex();
		Assert.assertNotNull(indexStatus);
	}

	@Test
	public void test020CreateUpdateFields() throws URISyntaxException {
		final AnnotatedIndexService service = getMaster();
		LinkedHashMap<String, FieldDefinition> fields = service.createUpdateFields();
		Assert.assertNotNull(fields);
		FieldDefinition field;
		Assert.assertNotNull("The Title field is not present", field = fields.get("title"));
		Assert.assertEquals("en.EnglishAnalyzer", field.analyzer);
		Assert.assertNotNull("The Content field is not present", field = fields.get("content"));
		Assert.assertEquals(EnglishAnalyzer.class.getName(), field.analyzer);
		Assert.assertNotNull("The Category field is not present", field = fields.get("category"));
		Assert.assertEquals(FieldDefinition.Template.SortedSetMultiDocValuesFacetField, field.template);
		Assert.assertNotNull("The Price field is not present", field = fields.get("price"));
		Assert.assertEquals(FieldDefinition.Template.DoubleDocValuesField, field.template);
	}

	private final static AnnotatedIndex.Master record1 =
			new AnnotatedIndex.Master(1, "First article", "Content of the first article", 0d, 10L, "news", "economy");

	private final static AnnotatedIndex.Master record2 =
			new AnnotatedIndex.Master(2, "Second article", "Content of the second article", 0d, 20L, "news", "science");

	private AnnotatedIndex checkRecord(AnnotatedIndex refRecord)
			throws URISyntaxException, ReflectiveOperationException {
		final AnnotatedIndexService<AnnotatedIndex.Master> service = getMaster();
		AnnotatedIndex record = service.getDocument(refRecord.id);
		Assert.assertNotNull(record);
		return record;
	}

	@Test
	public void test100PostDocument()
			throws URISyntaxException, IOException, InterruptedException, ReflectiveOperationException {
		final AnnotatedIndexService<AnnotatedIndex.Master> service = getMaster();
		service.postDocument(record1);
		AnnotatedIndex newRecord1 = checkRecord(record1);
		Assert.assertEquals(record1, newRecord1);
	}

	@Test
	public void test110PostDocuments()
			throws URISyntaxException, IOException, InterruptedException, ReflectiveOperationException {
		final AnnotatedIndexService<AnnotatedIndex.Master> service = getMaster();
		service.postDocuments(Arrays.asList(record1, record2));
		AnnotatedIndex newRecord1 = checkRecord(record1);
		Assert.assertEquals(record1, newRecord1);
		AnnotatedIndex newRecord2 = checkRecord(record2);
		Assert.assertEquals(record2, newRecord2);
		Assert.assertEquals(new Long(10), service.getIndexStatus().version);
	}

	private final static AnnotatedIndex.Master docValue1 = new AnnotatedIndex.Master(1, null, null, 1.11d, null);
	private final static AnnotatedIndex.Master docValue2 = new AnnotatedIndex.Master(2, null, null, 2.22d, null);

	@Test
	public void test200UpdateDocValues() throws URISyntaxException, IOException, InterruptedException {
		final AnnotatedIndexService service = getMaster();
		service.updateDocumentValues(docValue1);
	}

	@Test
	public void test210UpdateDocsValues()
			throws URISyntaxException, IOException, InterruptedException, ReflectiveOperationException {
		final AnnotatedIndexService service = getMaster();
		service.updateDocumentsValues(Arrays.asList(docValue1, docValue2));
		checkRecord(record1);
		checkRecord(record2);
	}

	private ResultDefinition.WithObject<AnnotatedIndex.Master> checkQueryResult(QueryBuilder builder, Long expectedHits)
			throws URISyntaxException {
		final AnnotatedIndexService service = getMaster();
		builder.addReturned_field(RETURNED_FIELDS);
		ResultDefinition.WithObject<AnnotatedIndex.Master> result = service.searchQuery(builder.build());
		Assert.assertNotNull(result);
		if (expectedHits != null)
			Assert.assertEquals(expectedHits, result.total_hits);
		return result;
	}

	@Test
	public void test300SimpleTermQuery() throws URISyntaxException {
		QueryBuilder builder = new QueryBuilder();
		builder.query = new TermQuery(FieldDefinition.ID_FIELD, "1");
		checkQueryResult(builder, 1L);
	}

	@Test
	public void test301MultiTermQuery() throws URISyntaxException {
		QueryBuilder builder = new QueryBuilder();
		builder.query = new TermsQuery(FieldDefinition.ID_FIELD, "1", "2");
		ResultDefinition result = checkQueryResult(builder, 2L);
	}

	@Test
	public void test320PointExactQuery() throws URISyntaxException {
		QueryBuilder builder = new QueryBuilder();
		builder.query = new LongExactQuery("quantity", 10);
		ResultDefinition.WithObject<AnnotatedIndex.Master> result = checkQueryResult(builder, 1L);
		Assert.assertEquals("1", result.documents.get(0).record.id);
	}

	@Test
	public void test320PointSetQuery() throws URISyntaxException {
		QueryBuilder builder = new QueryBuilder();
		builder.query = new LongSetQuery("quantity", 20, 25);
		ResultDefinition.WithObject<AnnotatedIndex.Master> result = checkQueryResult(builder, 1L);
		Assert.assertEquals("2", result.documents.get(0).record.id);
	}

	@Test
	public void test320PointRangeQuery() throws URISyntaxException {
		QueryBuilder builder = new QueryBuilder();
		builder.query = new LongRangeQuery("quantity", 15, 25);
		ResultDefinition.WithObject<AnnotatedIndex.Master> result = checkQueryResult(builder, 1L);
		Assert.assertEquals("2", result.documents.get(0).record.id);
	}

	@Test
	public void test320PointMultiRangeQuery() throws URISyntaxException {
		QueryBuilder builder = new QueryBuilder();
		LongMultiRangeQuery.Builder qBuilder = new LongMultiRangeQuery.Builder("quantity");
		qBuilder.addRange(5L, 15L);
		qBuilder.addRange(15L, 25L);
		checkQueryResult(builder, 2L);
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

	private void checkEqualsReturnedFields(AnnotatedIndex record, AnnotatedIndex recordRef,
			AnnotatedIndex docValueRef) {
		Assert.assertEquals(record.title, recordRef.title);
		Assert.assertEquals(record.content, recordRef.content);
		Assert.assertEquals(record.price, docValueRef.price);
		Assert.assertArrayEquals(record.storedCategory.toArray(), recordRef.storedCategory.toArray());
	}

	private final void testReturnedFieldQuery(String... returnedFields) throws URISyntaxException {
		final AnnotatedIndexService service = getMaster();
		QueryBuilder builder = new QueryBuilder();
		builder.query = new TermQuery(FieldDefinition.ID_FIELD, record2.id.toString());
		builder.addReturned_field(returnedFields);
		ResultDefinition.WithObject<AnnotatedIndex> result = service.searchQuery(builder.build());
		Assert.assertNotNull(result);
		Assert.assertEquals(new Long(1), result.total_hits);
		AnnotatedIndex returnedRecord = checkResultDocument(result, 0).record;
		checkEqualsReturnedFields(returnedRecord, record2, docValue2);
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
	public void test400getDocumentById() throws ReflectiveOperationException, URISyntaxException {
		final AnnotatedIndexService<AnnotatedIndex.Master> master = getMaster();
		AnnotatedIndex.Master record = master.getDocument(record1.id);
		checkEqualsReturnedFields(record, record1, docValue1);
	}

	@Test
	public void test420getDocuments() throws ReflectiveOperationException, URISyntaxException {
		final AnnotatedIndexService<AnnotatedIndex.Master> master = getMaster();
		List<AnnotatedIndex.Master> records = master.getDocuments(0, 2);
		Assert.assertNotNull(records);
		Assert.assertEquals(2L, records.size());
		checkEqualsReturnedFields(records.get(0), record1, docValue1);
		checkEqualsReturnedFields(records.get(1), record2, docValue2);
	}

	@Test
	public void test800replicationCheck() throws URISyntaxException {
		final AnnotatedIndexService master = getMaster();
		final IndexStatus masterStatus = master.getIndexStatus();
		Assert.assertNotNull(masterStatus);
		Assert.assertNotNull(masterStatus.version);

		final LinkedHashMap<String, FieldDefinition> masterFields = master.getFields();
		final LinkedHashMap<String, AnalyzerDefinition> masterAnalyzers = master.getAnalyzers();


		final AnnotatedIndexService slave = getSlave();
		final IndexStatus indexStatus = slave.createUpdateIndex();
		Assert.assertNotNull(indexStatus);
		slave.replicationCheck();


		final IndexStatus slaveStatus = slave.getIndexStatus();
		Assert.assertNotNull(slaveStatus);
		Assert.assertNotNull(slaveStatus.version);

		Assert.assertEquals(masterStatus.version, slaveStatus.version);
		Assert.assertEquals(masterStatus.num_docs, slaveStatus.num_docs);

		final LinkedHashMap<String, FieldDefinition> slaveFields = slave.getFields();
		final LinkedHashMap<String, AnalyzerDefinition> slaveAnalyzers = slave.getAnalyzers();
		Assert.assertNotNull(slaveFields);
		Assert.assertNotNull(slaveAnalyzers);

		Assert.assertArrayEquals(slaveFields.keySet().toArray(), masterFields.keySet().toArray());
		Assert.assertArrayEquals(slaveAnalyzers.keySet().toArray(), masterAnalyzers.keySet().toArray());
	}

	@Test
	public void test900join() throws URISyntaxException {
		final AnnotatedIndexService master = getMaster();
		final QueryBuilder builder = new QueryBuilder();
		builder.setQuery(
				new JoinQuery(AnnotatedIndex.Slave.INDEX_NAME, "docValuesCategory", "storedCategory",
						true, ScoreMode.Max, new MatchAllDocsQuery()));
		final ResultDefinition.WithObject<AnnotatedIndex> result = master.searchQuery(builder.build());
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.total_hits);
		Assert.assertEquals(new Long(2), result.total_hits);
	}

	@Test
	public void test980DeleteIndex() throws URISyntaxException {
		getMaster().deleteIndex();
		getSlave().deleteIndex();
	}

	@Test
	public void test990DeleteSchema() throws URISyntaxException {
		getMaster().deleteSchema();
	}

}
