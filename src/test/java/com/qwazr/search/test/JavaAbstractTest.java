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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
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
			{ FieldDefinition.ID_FIELD, "title", "content", "price", "storedCategory", "serialValue", "externalValue" };

	protected abstract IndexServiceInterface getIndexService() throws URISyntaxException, IOException;

	private final IndexSettingsDefinition indexSlaveDefinition;

	public static synchronized <T> AnnotatedIndexService<T> getService(final IndexServiceInterface indexService,
			final Class<T> indexClass, final String indexName, final IndexSettingsDefinition settings)
			throws URISyntaxException {
		return new AnnotatedIndexService(indexService, indexClass, null, indexName, settings);
	}

	public static synchronized <T> AnnotatedIndexService<T> getService(final IndexServiceInterface indexService,
			final Class<T> indexClass) throws URISyntaxException {
		return getService(indexService, indexClass, null, null);
	}

	protected JavaAbstractTest(final IndexSettingsDefinition indexSlaveDefinition) {
		this.indexSlaveDefinition = indexSlaveDefinition;
	}

	private AnnotatedIndexService<AnnotatedIndex> getMaster() throws URISyntaxException, IOException {
		return getService(getIndexService(), AnnotatedIndex.class);
	}

	private AnnotatedIndexService<AnnotatedIndex> getSlave() throws URISyntaxException, IOException {
		return getService(getIndexService(), AnnotatedIndex.class, AnnotatedIndex.INDEX_NAME_SLAVE,
				indexSlaveDefinition);
	}

	@Test
	public void test000startServer() throws Exception {
		TestServer.startServer();
		Assert.assertNotNull(TestServer.service);
	}

	@Test
	public void test010CheckMasterClient() throws URISyntaxException, IOException {
		final AnnotatedIndexService master = getMaster();
		Assert.assertEquals(AnnotatedIndex.SCHEMA_NAME, master.getSchemaName());
		Assert.assertEquals(AnnotatedIndex.INDEX_NAME_MASTER, master.getIndexName());
	}

	@Test
	public void test050CreateSchema() throws URISyntaxException, IOException {
		final AnnotatedIndexService service = getMaster();
		final SchemaSettingsDefinition settings1 = service.createUpdateSchema();
		Assert.assertNotNull(settings1);
		final SchemaSettingsDefinition settings2 = service.createUpdateSchema(settings1);
		Assert.assertNotNull(settings2);
		Assert.assertEquals(settings1, settings2);
	}

	@Test
	public void test060CreateMasterIndex() throws URISyntaxException, IOException {
		final AnnotatedIndexService service = getMaster();
		final IndexStatus indexStatus1 = service.createUpdateIndex();
		Assert.assertNotNull(indexStatus1);
		Assert.assertNotNull(indexStatus1.index_uuid);
		final IndexStatus indexStatus2 = service.getIndexStatus();
		Assert.assertEquals(indexStatus1, indexStatus2);
	}

	@Test
	public void test070FieldChangesNoFields() throws URISyntaxException, IOException {
		final AnnotatedIndexService service = getMaster();
		final Map<String, AnnotatedIndexService.FieldStatus> fieldChanges = service.getFieldChanges();
		Assert.assertNotNull(fieldChanges);
		Assert.assertEquals(15, fieldChanges.size());
		fieldChanges.forEach(
				(s, fieldStatus) -> Assert.assertEquals(AnnotatedIndexService.FieldStatus.EXISTS_ONLY_IN_ANNOTATION,
						fieldStatus));
	}

	@Test
	public void test072CreateUpdateFields() throws URISyntaxException, IOException {
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
	public void test074GetFields() throws URISyntaxException, IOException {
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
	public void test076FieldNoChanges() throws URISyntaxException, IOException {
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
	public void test080GetAnalyzers() throws URISyntaxException, IOException {
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
	public void test082doAnalyzer() throws URISyntaxException, IOException {
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
			throws URISyntaxException, ReflectiveOperationException, IOException {
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
			throws URISyntaxException, IOException {
		final AnnotatedIndexService service = getMaster();
		builder.returnedFields(RETURNED_FIELDS);
		ResultDefinition.WithObject<AnnotatedIndex> result = service.searchQuery(builder.build());
		Assert.assertNotNull(result);
		if (expectedHits != null)
			Assert.assertEquals(expectedHits, result.total_hits);
		return result;
	}

	@Test
	public void test300SimpleTermQuery() throws URISyntaxException, IOException {
		QueryBuilder builder = new QueryBuilder(new TermQuery(FieldDefinition.ID_FIELD, "1"));
		checkQueryResult(builder, 1L);
	}

	@Test
	public void test301MultiTermQuery() throws URISyntaxException, IOException {
		QueryBuilder builder = new QueryBuilder(new TermsQuery(FieldDefinition.ID_FIELD, "1", "2"));
		checkQueryResult(builder, 2L);
	}

	@Test
	public void test302WildcardQuery() throws URISyntaxException, IOException {
		QueryBuilder builder = new QueryBuilder(new WildcardQuery("title", "art*"));
		checkQueryResult(builder, 2L);
		builder.query(new WildcardQuery("title", "*econ?"));
		checkQueryResult(builder, 1L);
	}

	@Test
	public void test320PointExactQuery() throws URISyntaxException, IOException {
		QueryBuilder builder = new QueryBuilder(new LongExactQuery(AnnotatedIndex.QUANTITY_FIELD, 10));
		ResultDefinition.WithObject<AnnotatedIndex> result = checkQueryResult(builder, 1L);
		Assert.assertEquals("1", result.documents.get(0).record.id);
	}

	@Test
	public void test320PointSetQuery() throws URISyntaxException, IOException {
		QueryBuilder builder = new QueryBuilder(new LongSetQuery(AnnotatedIndex.QUANTITY_FIELD, 20, 25));
		ResultDefinition.WithObject<AnnotatedIndex> result = checkQueryResult(builder, 1L);
		Assert.assertEquals("2", result.documents.get(0).record.id);
	}

	@Test
	public void test320PointRangeQuery() throws URISyntaxException, IOException {
		QueryBuilder builder = new QueryBuilder(new LongRangeQuery(AnnotatedIndex.QUANTITY_FIELD, 15L, 25L));
		ResultDefinition.WithObject<AnnotatedIndex> result = checkQueryResult(builder, 1L);
		Assert.assertEquals("2", result.documents.get(0).record.id);
	}

	@Test
	public void test320PointMultiRangeQuery() throws URISyntaxException, IOException {
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

	private final void testReturnedFieldQuery(String... returnedFields) throws URISyntaxException, IOException {
		final AnnotatedIndexService service = getMaster();
		QueryBuilder builder = new QueryBuilder(new TermQuery(FieldDefinition.ID_FIELD, record2.id.toString()));
		builder.returnedField(returnedFields);
		ResultDefinition.WithObject<AnnotatedIndex> result = service.searchQuery(builder.build());
		Assert.assertNotNull(result);
		Assert.assertEquals(new Long(1), result.total_hits);
		AnnotatedIndex returnedRecord = checkResultDocument(result, 0).record;
		checkEqualsReturnedFields(returnedRecord, record2, docValue2);
	}

	@Test
	public void test350ReturnedFieldQuery() throws URISyntaxException, IOException {
		testReturnedFieldQuery(RETURNED_FIELDS);
	}

	@Test
	public void test360ReturnedFieldQueryAll() throws URISyntaxException, IOException {
		testReturnedFieldQuery("*");
	}

	@Test
	public void test400getDocumentById() throws ReflectiveOperationException, URISyntaxException, IOException {
		final AnnotatedIndexService<AnnotatedIndex> master = getMaster();
		AnnotatedIndex record = master.getDocument(record1.id);
		checkEqualsReturnedFields(record, record1, docValue1);
	}

	@Test
	public void test420getDocuments() throws ReflectiveOperationException, URISyntaxException, IOException {
		final AnnotatedIndexService<AnnotatedIndex> master = getMaster();
		List<AnnotatedIndex> records = master.getDocuments(0, 2);
		Assert.assertNotNull(records);
		Assert.assertEquals(2L, records.size());
		checkEqualsReturnedFields(records.get(0), record1, docValue1);
		checkEqualsReturnedFields(records.get(1), record2, docValue2);
	}

	private void testSort(QueryBuilder queryBuilder, int resultCount,
			BiFunction<AnnotatedIndex, AnnotatedIndex, Boolean> checker) throws URISyntaxException, IOException {
		final AnnotatedIndexService<AnnotatedIndex> master = getMaster();
		ResultDefinition.WithObject<AnnotatedIndex> result = master.searchQuery(queryBuilder.build());
		Assert.assertNotNull(result);
		Assert.assertEquals(Long.valueOf(resultCount), result.total_hits);
		Assert.assertNotNull(result.documents);
		Assert.assertEquals(resultCount, result.documents.size());
		AnnotatedIndex current = null;
		for (ResultDocumentObject<AnnotatedIndex> resultDoc : result.documents) {
			final AnnotatedIndex next = resultDoc.getRecord();
			if (current != null)
				Assert.assertTrue(checker.apply(current, next));
			current = next;
		}
	}

	@Test
	public void test500sortByTitleDescAndScore() throws URISyntaxException, IOException {
		final QueryBuilder builder = new QueryBuilder(new MatchAllDocsQuery());
		builder.sort("titleSort", QueryDefinition.SortEnum.descending_missing_first)
				.sort(FieldDefinition.SCORE_FIELD, QueryDefinition.SortEnum.descending)
				.returnedField("title")
				.start(0)
				.rows(100);
		testSort(builder, 2, (doc1, doc2) -> doc1.title.compareTo(doc2.title) > 0);
	}

	@Test
	public void test500sortByTitleAscAndScore() throws URISyntaxException, IOException {
		final QueryBuilder builder = new QueryBuilder();
		builder.query(new MatchAllDocsQuery())
				.sort("titleSort", QueryDefinition.SortEnum.ascending_missing_last)
				.sort(FieldDefinition.SCORE_FIELD, QueryDefinition.SortEnum.descending)
				.returnedField("title")
				.start(0)
				.rows(100);
		testSort(builder, 2, (doc1, doc2) -> doc1.title.compareTo(doc2.title) < 0);
	}

	@Test
	public void test500sortByLongAsc() throws URISyntaxException, IOException {
		final QueryBuilder builder = new QueryBuilder();
		builder.query(new MatchAllDocsQuery())
				.sort("dvQty", QueryDefinition.SortEnum.ascending_missing_last)
				.sort(FieldDefinition.SCORE_FIELD, QueryDefinition.SortEnum.ascending)
				.returnedField("dvQty")
				.start(0)
				.rows(100);
		testSort(builder, 2, (doc1, doc2) -> doc1.dvQty.compareTo(doc2.dvQty) < 0);
	}

	@Test
	public void test500sortByLongDesc() throws URISyntaxException, IOException {
		final QueryBuilder builder = new QueryBuilder();
		builder.query(new MatchAllDocsQuery())
				.sort("dvQty", QueryDefinition.SortEnum.descending_missing_last)
				.sort(FieldDefinition.SCORE_FIELD, QueryDefinition.SortEnum.descending)
				.returnedField("dvQty")
				.start(0)
				.rows(100);
		testSort(builder, 2, (doc1, doc2) -> doc1.dvQty.compareTo(doc2.dvQty) > 0);
	}

	@Test
	public void test500sortByDoubleAsc() throws URISyntaxException, IOException {
		final QueryBuilder builder = new QueryBuilder();
		builder.query(new MatchAllDocsQuery())
				.sort("price", QueryDefinition.SortEnum.ascending_missing_last)
				.sort(FieldDefinition.SCORE_FIELD, QueryDefinition.SortEnum.ascending)
				.returnedField("price")
				.start(0)
				.rows(100);
		testSort(builder, 2, (doc1, doc2) -> doc1.price.compareTo(doc2.price) < 0);
	}

	@Test
	public void test500sortByDoubleDesc() throws URISyntaxException, IOException {
		final QueryBuilder builder = new QueryBuilder();
		builder.query(new MatchAllDocsQuery())
				.sort("price", QueryDefinition.SortEnum.descending_missing_last)
				.sort(FieldDefinition.SCORE_FIELD, QueryDefinition.SortEnum.descending)
				.returnedField("price")
				.start(0)
				.rows(100);
		testSort(builder, 2, (doc1, doc2) -> doc1.price.compareTo(doc2.price) > 0);
	}

	public static List<TermEnumDefinition> checkTermList(List<TermEnumDefinition> terms) {
		Assert.assertNotNull(terms);
		Assert.assertFalse(terms.isEmpty());
		return terms;
	}

	@Test
	public void test610TermsEnum() throws URISyntaxException, IOException {
		final AnnotatedIndexService<AnnotatedIndex> master = getMaster();
		int firstSize = checkTermList(master.doExtractTerms("content", null, null, 10000)).size();
		int secondSize = checkTermList(master.doExtractTerms("content", null, 2, 10000)).size();
		Assert.assertEquals(firstSize, secondSize + 2);
		checkTermList(master.doExtractTerms("content", "a", null, null));
	}

	private void checkMultiField(final MultiFieldQuery query, final String check, final int size)
			throws URISyntaxException, IOException {
		final AnnotatedIndexService<AnnotatedIndex> master = getMaster();
		final QueryBuilder builder = new QueryBuilder();
		builder.query(query).queryDebug(true);
		final ResultDefinition.WithObject<AnnotatedIndex> result = master.searchQuery(builder.build());
		Assert.assertEquals(check, result.getQuery());
		Assert.assertEquals(size, result.documents.size());
	}

	@Test
	public void test700MultiFieldWithFuzzy() throws IOException, ReflectiveOperationException, URISyntaxException {
		Map<String, Float> fields = new HashMap<>();
		fields.put("title", 10.0F);
		fields.put("titleStd", 5.0F);
		fields.put("content", 1.0F);
		Analyzer tokenizerAnalyzer = new ClassicAnalyzer();
		MultiFieldQuery query =
				new MultiFieldQuery(fields, QueryParserOperator.AND, tokenizerAnalyzer, "title sekond", null);
		checkMultiField(query,
				"+((titleStd:title)^5.0 (title:titl)^10.0 content:titl~2) +((titleStd:sekond~2)^5.0 (title:sekond~2)^10.0 content:sekond~2)",
				1);
		query = new MultiFieldQuery(fields, QueryParserOperator.OR, tokenizerAnalyzer, "title sekond", 2);
		checkMultiField(query,
				"(((titleStd:title)^5.0 (title:titl)^10.0 content:titl~2) ((titleStd:sekond~2)^5.0 (title:sekond~2)^10.0 content:sekond~2))~2",
				1);
		query = new MultiFieldQuery(fields, QueryParserOperator.OR, tokenizerAnalyzer, "title sekond", 1);
		checkMultiField(query,
				"(((titleStd:title)^5.0 (title:titl)^10.0 content:titl~2) ((titleStd:sekond~2)^5.0 (title:sekond~2)^10.0 content:sekond~2))~1",
				2);
	}

	@Test
	public void test800replicationCheck() throws URISyntaxException, IOException {
		final AnnotatedIndexService master = getMaster();
		final IndexStatus masterStatus = master.getIndexStatus();
		Assert.assertNotNull(masterStatus);
		Assert.assertNotNull(masterStatus.index_uuid);
		Assert.assertNotNull(masterStatus.version);

		final LinkedHashMap<String, FieldDefinition> masterFields = master.getFields();
		final LinkedHashMap<String, AnalyzerDefinition> masterAnalyzers = master.getAnalyzers();

		final AnnotatedIndexService slave = getSlave();
		final IndexStatus indexStatus = slave.createUpdateIndex();
		Assert.assertNotNull(indexStatus);
		Assert.assertNotNull(indexStatus.index_uuid);
		Assert.assertNull(indexStatus.master_uuid);

		// Second call to check setting comparison
		Assert.assertNotNull(slave.createUpdateIndex());

		// First replication call
		slave.replicationCheck();

		// Second replication call (nothing to do)
		slave.replicationCheck();

		final IndexStatus slaveStatus = slave.getIndexStatus();
		Assert.assertNotNull(slaveStatus);
		Assert.assertNotNull(slaveStatus.version);
		Assert.assertEquals(slaveStatus.master_uuid, masterStatus.index_uuid);

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
	public void test900join() throws URISyntaxException, IOException {
		final AnnotatedIndexService master = getMaster();
		final QueryBuilder builder = new QueryBuilder();
		builder.query(new JoinQuery(AnnotatedIndex.INDEX_NAME_SLAVE, "docValuesCategory", "storedCategory", true,
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
	public void test910collector() throws URISyntaxException, IOException {
		final AnnotatedIndexService master = getMaster();
		final QueryBuilder builder = new QueryBuilder();
		builder.collector("minPrice", MinNumericCollector.MinDouble.class, "price");
		builder.collector("maxPrice", MaxNumericCollector.MaxDouble.class, "price");
		builder.collector("minQuantity", MinNumericCollector.MinLong.class, AnnotatedIndex.DV_QUANTITY_FIELD);
		builder.collector("maxQuantity", MaxNumericCollector.MaxLong.class, AnnotatedIndex.DV_QUANTITY_FIELD);
		builder.query(new MatchAllDocsQuery());
		ResultDefinition.WithObject<AnnotatedIndex> result = master.searchQuery(builder.build());
		checkCollector(result, "minPrice", 1.11d);
		checkCollector(result, "maxPrice", 2.22d);
		checkCollector(result, "minQuantity", 10L, 10);
		checkCollector(result, "maxQuantity", 20L, 20);
	}

	@Test
	public void test950DeleteAll() throws URISyntaxException, IOException {
		getMaster().deleteAll();
	}

	@Test
	public void test980DeleteIndex() throws URISyntaxException, IOException {
		getSlave().deleteIndex();
		getMaster().deleteIndex();
	}

	@Test
	public void test990DeleteSchema() throws URISyntaxException, IOException {
		getMaster().deleteSchema();
	}

	@Test
	public void test999httpClient() {
		final PoolStats stats = HttpClients.CNX_MANAGER.getTotalStats();
		Assert.assertEquals(0, HttpClients.CNX_MANAGER.getTotalStats().getLeased());
		Assert.assertEquals(0, stats.getPending());
		Assert.assertTrue(stats.getLeased() == 0 || stats.getAvailable() > 0);
	}

}
