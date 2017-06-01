package com.qwazr.search.test.units;

import com.qwazr.search.index.QueryBuilder;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.MatchAllDocsQuery;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class ReturnedFieldsTest extends AbstractIndexTest {

	final private static String[] ID_FIELDS = { "1", "2", "3" };
	final private static String[] STORED_FIELDS = { "doc1", "doc2", "doc3" };
	final private static String[] SDV_FIELDS = { "sdv1", "sdv2", "sdv3" };
	final private static Double[] DDV_FIELDS = { 1.11d, 2.22d, 3.33d };

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexService();
		for (int i = 0; i < ID_FIELDS.length; i++)
			indexService.postDocument(new IndexRecord(ID_FIELDS[i]).storedField(STORED_FIELDS[i])
					.sortedDocValue(SDV_FIELDS[i])
					.doubleDocValue(DDV_FIELDS[i]));
	}

	private QueryBuilder builder() {
		return QueryDefinition.of(new MatchAllDocsQuery()).start(0).rows(ID_FIELDS.length);
	}

	private ResultDefinition.WithObject<IndexRecord> withRecord(QueryBuilder queryBuilder) {
		final ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(queryBuilder.build());
		Assert.assertNotNull(result.total_hits);
		Assert.assertEquals(ID_FIELDS.length, result.total_hits, 0);
		return result;
	}

	private ResultDefinition.WithMap withMap(QueryBuilder queryBuilder) {
		final ResultDefinition.WithMap result = indexService.searchQueryWithMap(queryBuilder.build());
		Assert.assertNotNull(result.total_hits);
		Assert.assertEquals(ID_FIELDS.length, result.total_hits, 0);
		return result;
	}

	@Test
	public void checkJoker() {
		withRecord(builder().returnedField("*")).forEach(doc -> {
			Assert.assertEquals(ID_FIELDS[doc.pos], doc.record.id);
			Assert.assertEquals(STORED_FIELDS[doc.pos], doc.record.storedField);
			Assert.assertEquals(SDV_FIELDS[doc.pos], doc.record.sortedDocValue);
			Assert.assertEquals(DDV_FIELDS[doc.pos], doc.record.doubleDocValue);
		});
	}

	@Test
	public void checkNoReturnedField() {
		withRecord(builder()).forEach(doc -> {
			Assert.assertNull(doc.record.id);
			Assert.assertNull(doc.record.storedField);
			Assert.assertNull(doc.record.sortedDocValue);
			Assert.assertNull(doc.record.doubleDocValue);
		});
	}

	@Test
	public void checkOnlyStoredField() {
		withRecord(builder().returnedField("storedField")).forEach(doc -> {
			Assert.assertNull(doc.record.id);
			Assert.assertEquals(STORED_FIELDS[doc.pos], doc.record.storedField);
			Assert.assertNull(doc.record.sortedDocValue);
			Assert.assertNull(doc.record.doubleDocValue);
		});
	}

	@Test
	public void checkOnlySortedDocValueField() {
		withRecord(builder().returnedField("sortedDocValue")).forEach(doc -> {
			Assert.assertNull(doc.record.id);
			Assert.assertNull(doc.record.storedField);
			Assert.assertEquals(SDV_FIELDS[doc.pos], doc.record.sortedDocValue);
			Assert.assertNull(doc.record.doubleDocValue);
		});
	}

	@Test
	public void checkOnlyDoubleDocValueField() {
		withRecord(builder().returnedField("doubleDocValue")).forEach(doc -> {
			Assert.assertNull(doc.record.id);
			Assert.assertNull(doc.record.storedField);
			Assert.assertNull(doc.record.sortedDocValue);
			Assert.assertEquals(DDV_FIELDS[doc.pos], doc.record.doubleDocValue);
		});
	}
}
