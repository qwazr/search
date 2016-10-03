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
import com.qwazr.search.collector.MaxNumericCollector;
import com.qwazr.search.collector.MinNumericCollector;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.*;
import com.qwazr.search.query.*;
import com.qwazr.utils.http.HttpClients;
import org.apache.http.pool.PoolStats;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.search.join.ScoreMode;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.BiFunction;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class JavaAbstractTest {

	public static final String[] RETURNED_FIELDS =
			{FieldDefinition.ID_FIELD, "title", "content", "price", "storedCategory", "serialValue", "externalValue"};

	protected abstract IndexServiceInterface getIndexService() throws URISyntaxException;

	private AnnotatedIndexService<AnnotatedIndex> getMaster() throws URISyntaxException {
		return TestServer.getService(getIndexService(), AnnotatedIndex.class);
	}

	private AnnotatedIndexService<AnnotatedIndex> getSlave() throws URISyntaxException {
		IndexSettingsDefinition settings =
				new IndexSettingsDefinition(null, "http://localhost:9091/indexes/testSchema/testIndexMaster");
		return TestServer.getService(getIndexService(), AnnotatedIndex.class, AnnotatedIndex.INDEX_NAME_SLAVE,
				settings);
	}

	@Test
	public void test000startServer() throws Exception {
		TestServer.startServer();
		Assert.assertTrue(TestServer.serverStarted);
	}

	@Test
	public void test010CheckClient() throws URISyntaxException {
		final AnnotatedIndexService master = getMaster();
		Assert.assertEquals(AnnotatedIndex.SCHEMA_NAME, master.getSchemaName());
		Assert.assertEquals(AnnotatedIndex.INDEX_NAME_MASTER, master.getIndexName());
		final AnnotatedIndexService slave = getSlave();
		Assert.assertEquals(AnnotatedIndex.SCHEMA_NAME, slave.getSchemaName());
		Assert.assertEquals(AnnotatedIndex.INDEX_NAME_SLAVE, slave.getIndexName());
	}

	@Test
	public void test050CreateSchema() throws URISyntaxException {
		final AnnotatedIndexService service = getMaster();
		final SchemaSettingsDefinition settings1 = service.createUpdateSchema();
		Assert.assertNotNull(settings1);
		final SchemaSettingsDefinition settings2 = service.createUpdateSchema(settings1);
		Assert.assertNotNull(settings2);
		Assert.assertEquals(settings1, settings2);
	}

	@Test
	public void test060CreateIndex() throws URISyntaxException {
		final AnnotatedIndexService service = getMaster();
		IndexStatus indexStatus = service.createUpdateIndex();
		Assert.assertNotNull(indexStatus);
	}


	@Test
	public void test070FieldChangesNoFields() throws URISyntaxException {
		final AnnotatedIndexService service = getMaster();
		final Map<String, AnnotatedIndexService.FieldStatus> fieldChanges = service.getFieldChanges();
		Assert.assertNotNull(fieldChanges);
		Assert.assertEquals(14, fieldChanges.size());
		fieldChanges.forEach((s, fieldStatus) -> Assert.assertEquals(
				AnnotatedIndexService.FieldStatus.EXISTS_ONLY_IN_ANNOTATION, fieldStatus));
	}

	@Test
	public void test072CreateUpdateFields() throws URISyntaxException {
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

	@Test
	public void test074GetFields() throws URISyntaxException {
		final AnnotatedIndexService service = getMaster();
		LinkedHashMap<String, FieldDefinition> fields = service.getFields();
		Assert.assertNotNull(fields);
		Assert.assertFalse(fields.isEmpty());
		fields.forEach((fieldName, fieldDefinition) -> {
			FieldDefinition fieldDef = service.getField(fieldName);
			Assert.assertNotNull(fieldDef);
			Assert.assertEquals(fieldDef.template, fieldDefinition.template);
			Assert.assertEquals(fieldDef.analyzer, fieldDefinition.analyzer);
		});
	}

	@Test
	public void test076FieldNoChanges() throws URISyntaxException {
		final AnnotatedIndexService service = getMaster();
		final Map<String, AnnotatedIndexService.FieldStatus> fieldChanges = service.getFieldChanges();
		Assert.assertNotNull(fieldChanges);
		Assert.assertEquals(0, fieldChanges.size());
	}

	private void checkTermDef(List<TermDefinition> terms, int expectedSize) {
		Assert.assertNotNull(terms);
		Assert.assertEquals(expectedSize, terms.size());
	}

	@Test
	public void test080GetAnalyzers() throws URISyntaxException {
		final AnnotatedIndexService service = getMaster();
		LinkedHashMap<String, AnalyzerDefinition> analyzers = service.getAnalyzers();
		Assert.assertNotNull(analyzers);
		Assert.assertTrue(analyzers.isEmpty());
		analyzers.forEach((analyzerName, analyzerDefinition) -> {
			AnalyzerDefinition anaDef = service.getAnalyzer(analyzerName);
			Assert.assertNotNull(anaDef);
			checkTermDef(service.testAnalyzer(analyzerName, "Please analyzer this text"), 3);
		});
	}

	@Test
	public void test082doAnalyzer() throws URISyntaxException {
		final AnnotatedIndexService service = getMaster();
		checkTermDef(service.doAnalyzeIndex("content", "Please analyzer this text"), 3);
		checkTermDef(service.doAnalyzeQuery("content", "Please analyzer this text"), 3);
	}

	private final static AnnotatedIndex record1 =
			new AnnotatedIndex(1, "First article title", "Content of the first article", 0d, 10L, true, false, "news",
					"economy").multiFacet("cat", "news", "economy");

	private final static AnnotatedIndex record2 =
			new AnnotatedIndex(2, "Second article title", "Content of the second article", 0d, 20L, true, false, "news",
					"science").multiFacet("cat", "news", "science");

	private AnnotatedIndex checkRecord(AnnotatedIndex refRecord)
			throws URISyntaxException, ReflectiveOperationException {
		final AnnotatedIndexService<AnnotatedIndex> service = getMaster();
		AnnotatedIndex record = service.getDocument(refRecord.id);
		Assert.assertNotNull(record);
		return record;
	}

	@Test
	public void test100PostDocument()
			throws URISyntaxException, IOException, InterruptedException, ReflectiveOperationException {
		final AnnotatedIndexService<AnnotatedIndex> service = getMaster();
		service.postDocument(record1);
		AnnotatedIndex newRecord1 = checkRecord(record1);
		Assert.assertEquals(record1, newRecord1);
	}

	@Test
	public void test110PostDocuments()
			throws URISyntaxException, IOException, InterruptedException, ReflectiveOperationException {
		final AnnotatedIndexService<AnnotatedIndex> service = getMaster();
		service.postDocuments(Arrays.asList(record1, record2));
		AnnotatedIndex newRecord1 = checkRecord(record1);
		Assert.assertEquals(record1, newRecord1);
		AnnotatedIndex newRecord2 = checkRecord(record2);
		Assert.assertEquals(record2, newRecord2);
		Assert.assertEquals(new Long(10), service.getIndexStatus().version);
	}

	private final static AnnotatedIndex docValue1 = new AnnotatedIndex(1, null, null, 1.11d, null, false, true);
	private final static AnnotatedIndex docValue2 = new AnnotatedIndex(2, null, null, 2.22d, null, false, true);

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

	private ResultDefinition.WithObject<AnnotatedIndex> checkQueryResult(QueryBuilder builder, Long expectedHits)
			throws URISyntaxException {
		final AnnotatedIndexService service = getMaster();
		builder.addReturned_field(RETURNED_FIELDS);
		ResultDefinition.WithObject<AnnotatedIndex> result = service.searchQuery(builder.build());
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
		checkQueryResult(builder, 2L);
	}

	@Test
	public void test302WildcardQuery() throws URISyntaxException {
		QueryBuilder builder = new QueryBuilder();
		builder.query = new WildcardQuery("title", "art*");
		checkQueryResult(builder, 2L);
		builder.query = new WildcardQuery("title", "*econ?");
		checkQueryResult(builder, 1L);
	}

	@Test
	public void test320PointExactQuery() throws URISyntaxException {
		QueryBuilder builder = new QueryBuilder();
		builder.query = new LongExactQuery(AnnotatedIndex.QUANTITY_FIELD, 10);
		ResultDefinition.WithObject<AnnotatedIndex> result = checkQueryResult(builder, 1L);
		Assert.assertEquals("1", result.documents.get(0).record.id);
	}

	@Test
	public void test320PointSetQuery() throws URISyntaxException {
		QueryBuilder builder = new QueryBuilder();
		builder.query = new LongSetQuery(AnnotatedIndex.QUANTITY_FIELD, 20, 25);
		ResultDefinition.WithObject<AnnotatedIndex> result = checkQueryResult(builder, 1L);
		Assert.assertEquals("2", result.documents.get(0).record.id);
	}

	@Test
	public void test320PointRangeQuery() throws URISyntaxException {
		QueryBuilder builder = new QueryBuilder();
		builder.query = new LongRangeQuery(AnnotatedIndex.QUANTITY_FIELD, 15L, 25L);
		ResultDefinition.WithObject<AnnotatedIndex> result = checkQueryResult(builder, 1L);
		Assert.assertEquals("2", result.documents.get(0).record.id);
	}

	@Test
	public void test320PointMultiRangeQuery() throws URISyntaxException {
		QueryBuilder builder = new QueryBuilder();
		LongMultiRangeQuery.Builder qBuilder = new LongMultiRangeQuery.Builder(AnnotatedIndex.QUANTITY_FIELD);
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
		Assert.assertEquals(recordRef.title, record.title);
		Assert.assertEquals(recordRef.content, record.content);
		Assert.assertEquals(docValueRef.price, record.price);
		Assert.assertArrayEquals(recordRef.storedCategory.toArray(), record.storedCategory.toArray());
		Assert.assertEquals(recordRef.externalValue, record.externalValue);
		Assert.assertEquals(recordRef.serialValue, record.serialValue);
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
		final AnnotatedIndexService<AnnotatedIndex> master = getMaster();
		AnnotatedIndex record = master.getDocument(record1.id);
		checkEqualsReturnedFields(record, record1, docValue1);
	}

	@Test
	public void test420getDocuments() throws ReflectiveOperationException, URISyntaxException {
		final AnnotatedIndexService<AnnotatedIndex> master = getMaster();
		List<AnnotatedIndex> records = master.getDocuments(0, 2);
		Assert.assertNotNull(records);
		Assert.assertEquals(2L, records.size());
		checkEqualsReturnedFields(records.get(0), record1, docValue1);
		checkEqualsReturnedFields(records.get(1), record2, docValue2);
	}

	private void testSort(QueryBuilder queryBuilder, int resultCount,
			BiFunction<AnnotatedIndex, AnnotatedIndex, Boolean> checker)
			throws URISyntaxException {
		final AnnotatedIndexService<AnnotatedIndex> master = getMaster();
		ResultDefinition.WithObject<AnnotatedIndex> result = master.searchQuery(queryBuilder.build());
		Assert.assertNotNull(result);
		Assert.assertEquals(Long.valueOf(resultCount), result.total_hits);
		AnnotatedIndex current = null;
		for (ResultDocumentObject<AnnotatedIndex> resultDoc : result.documents) {
			final AnnotatedIndex next = resultDoc.getRecord();
			if (current != null)
				Assert.assertTrue(checker.apply(current, next));
			current = next;
		}
	}

	@Test
	public void test500sortByTitleDescAndScore() throws URISyntaxException {
		final QueryBuilder builder = new QueryBuilder();
		builder.setQuery(new MatchAllDocsQuery())
				.addSort("titleSort", QueryDefinition.SortEnum.descending_missing_first)
				.addSort(FieldDefinition.SCORE_FIELD, QueryDefinition.SortEnum.descending)
				.addReturned_field("title")
				.setStart(0).setRows(100);
		testSort(builder, 2, (doc1, doc2) -> doc1.title.compareTo(doc2.title) > 0);
	}

	@Test
	public void test500sortByTitleAscAndScore() throws URISyntaxException {
		final QueryBuilder builder = new QueryBuilder();
		builder.setQuery(new MatchAllDocsQuery())
				.addSort("titleSort", QueryDefinition.SortEnum.ascending_missing_last)
				.addSort(FieldDefinition.SCORE_FIELD, QueryDefinition.SortEnum.descending)
				.addReturned_field("title")
				.setStart(0).setRows(100);
		testSort(builder, 2, (doc1, doc2) -> doc1.title.compareTo(doc2.title) < 0);
	}

	@Test
	public void test500sortByLongAsc() throws URISyntaxException {
		final QueryBuilder builder = new QueryBuilder();
		builder.setQuery(new MatchAllDocsQuery())
				.addSort("dvQty", QueryDefinition.SortEnum.ascending_missing_last)
				.addSort(FieldDefinition.SCORE_FIELD, QueryDefinition.SortEnum.ascending)
				.addReturned_field("dvQty")
				.setStart(0).setRows(100);
		testSort(builder, 2, (doc1, doc2) -> doc1.dvQty.compareTo(doc2.dvQty) < 0);
	}

	@Test
	public void test500sortByLongDesc() throws URISyntaxException {
		final QueryBuilder builder = new QueryBuilder();
		builder.setQuery(new MatchAllDocsQuery())
				.addSort("dvQty", QueryDefinition.SortEnum.descending_missing_last)
				.addSort(FieldDefinition.SCORE_FIELD, QueryDefinition.SortEnum.descending)
				.addReturned_field("dvQty")
				.setStart(0).setRows(100);
		testSort(builder, 2, (doc1, doc2) -> doc1.dvQty.compareTo(doc2.dvQty) > 0);
	}

	@Test
	public void test500sortByDoubleAsc() throws URISyntaxException {
		final QueryBuilder builder = new QueryBuilder();
		builder.setQuery(new MatchAllDocsQuery())
				.addSort("price", QueryDefinition.SortEnum.ascending_missing_last)
				.addSort(FieldDefinition.SCORE_FIELD, QueryDefinition.SortEnum.ascending)
				.addReturned_field("price")
				.setStart(0).setRows(100);
		testSort(builder, 2, (doc1, doc2) -> doc1.price.compareTo(doc2.price) < 0);
	}

	@Test
	public void test500sortByDoubleDesc() throws URISyntaxException {
		final QueryBuilder builder = new QueryBuilder();
		builder.setQuery(new MatchAllDocsQuery())
				.addSort("price", QueryDefinition.SortEnum.descending_missing_last)
				.addSort(FieldDefinition.SCORE_FIELD, QueryDefinition.SortEnum.descending)
				.addReturned_field("price")
				.setStart(0).setRows(100);
		testSort(builder, 2, (doc1, doc2) -> doc1.price.compareTo(doc2.price) > 0);
	}


	public static List<TermEnumDefinition> checkTermList(List<TermEnumDefinition> terms) {
		Assert.assertNotNull(terms);
		Assert.assertFalse(terms.isEmpty());
		return terms;
	}

	@Test
	public void test610TermsEnum() throws URISyntaxException {
		final AnnotatedIndexService<AnnotatedIndex> master = getMaster();
		int firstSize = checkTermList(master.doExtractTerms("content", null, null, 10000)).size();
		int secondSize = checkTermList(master.doExtractTerms("content", null, 2, 10000)).size();
		Assert.assertEquals(firstSize, secondSize + 2);
		checkTermList(master.doExtractTerms("content", "a", null, null));
	}

	@Test
	public void test700MultiFieldWithFuzzy() throws IOException, ReflectiveOperationException, URISyntaxException {
		final AnnotatedIndexService<AnnotatedIndex> master = getMaster();
		Map<String, Float> fields = new HashMap<>();
		fields.put("title", 10.0F);
		fields.put("content", 1.0F);
		final QueryBuilder builder = new QueryBuilder();
		final MultiFieldQuery query =
				new MultiFieldQuery(fields, QueryParserOperator.AND, null, "title sekond");
		builder.setQuery(query).setQuery_debug(true);
		final ResultDefinition.WithObject<AnnotatedIndex> result = master.searchQuery(builder.build());
		Assert.assertEquals("+((title:titl)^10.0 content:titl) +((title:sekond~2)^10.0 content:sekond~2)",
				result.getQuery());
		Assert.assertEquals(1, result.documents.size());
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
		// Second call to check setting comparison
		Assert.assertNotNull(slave.createUpdateIndex());

		// First replication call
		slave.replicationCheck();

		// Second replication call (nothing to do)
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
		builder.setQuery(new JoinQuery(AnnotatedIndex.INDEX_NAME_SLAVE, "docValuesCategory", "storedCategory", true,
				ScoreMode.Max, new MatchAllDocsQuery()));
		final ResultDefinition.WithObject<AnnotatedIndex> result = master.searchQuery(builder.build());
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.total_hits);
		Assert.assertEquals(new Long(2), result.total_hits);
	}

	static void checkCollector(ResultDefinition result, String name, Object... possibleValues) {
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.collectors);
		Object collectorResult = result.getCollector(name);
		if (possibleValues == null || possibleValues.length == 0) {
			Assert.assertNull(collectorResult);
		} else {
			Assert.assertNotNull(collectorResult);
			for (Object value : possibleValues) {
				if (value.equals(collectorResult)) {
					Assert.assertEquals(value, collectorResult);
					return;
				}
			}
			Assert.fail("Right value not found. Got: " + collectorResult);
		}
	}

	@Test
	public void test910collector() throws URISyntaxException {
		final AnnotatedIndexService master = getMaster();
		final QueryBuilder builder = new QueryBuilder();
		builder.addCollector("minPrice", MinNumericCollector.MinDouble.class, "price");
		builder.addCollector("maxPrice", MaxNumericCollector.MaxDouble.class, "price");
		builder.addCollector("minQuantity", MinNumericCollector.MinLong.class, AnnotatedIndex.DV_QUANTITY_FIELD);
		builder.addCollector("maxQuantity", MaxNumericCollector.MaxLong.class, AnnotatedIndex.DV_QUANTITY_FIELD);
		builder.setQuery(new MatchAllDocsQuery());
		ResultDefinition.WithObject<AnnotatedIndex> result = master.searchQuery(builder.build());
		checkCollector(result, "minPrice", 1.11d);
		checkCollector(result, "maxPrice", 2.22d);
		checkCollector(result, "minQuantity", 10L, 10);
		checkCollector(result, "maxQuantity", 20L, 20);
	}

	@Test
	public void test950DeleteAll() throws URISyntaxException {
		getMaster().deleteAll();
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

	@Test
	public void test999httpClient() {
		final PoolStats stats = HttpClients.CNX_MANAGER.getTotalStats();
		Assert.assertEquals(0, HttpClients.CNX_MANAGER.getTotalStats().getLeased());
		Assert.assertEquals(0, stats.getPending());
		Assert.assertTrue(stats.getAvailable() > 0);
	}


}
