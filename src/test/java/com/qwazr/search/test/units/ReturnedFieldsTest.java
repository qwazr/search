/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	final private static String[][] MULTI_STRING_STORED_FIELDS =
			{ { "s01", "s02", "s03" }, { "s11", "s12", "s13" }, { "s21", "s22", "s23" } };
	final private static Integer[][] MULTI_INTEGER_STORED_FIELDS = { { 11, 12, 13 }, { 21, 22, 23 }, { 31, 32, 33 } };

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexService();
		for (int i = 0; i < ID_FIELDS.length; i++)
			indexService.postDocument(new IndexRecord(ID_FIELDS[i]).storedField(STORED_FIELDS[i])
					.sortedDocValue(SDV_FIELDS[i])
					.doubleDocValue(DDV_FIELDS[i])
					.multivaluedStringStoredField(MULTI_STRING_STORED_FIELDS[i])
					.multivaluedIntegerStoredField(MULTI_INTEGER_STORED_FIELDS[i]));
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
		QueryBuilder builder = builder().returnedField("*");

		withRecord(builder).forEach(doc -> {
			Assert.assertEquals(ID_FIELDS[doc.pos], doc.record.id);
			Assert.assertEquals(STORED_FIELDS[doc.pos], doc.record.storedField);
			Assert.assertEquals(SDV_FIELDS[doc.pos], doc.record.sortedDocValue);
			Assert.assertEquals(DDV_FIELDS[doc.pos], doc.record.doubleDocValue);
		});

		withMap(builder).forEach(doc -> {
			Assert.assertEquals(ID_FIELDS[doc.pos], doc.fields.get("$id$"));
			Assert.assertEquals(STORED_FIELDS[doc.pos], doc.fields.get("storedField"));
			Assert.assertEquals(SDV_FIELDS[doc.pos], doc.fields.get("sortedDocValue"));
			Assert.assertEquals(DDV_FIELDS[doc.pos], doc.fields.get("doubleDocValue"));
		});
	}

	@Test
	public void checkNoReturnedField() {
		QueryBuilder builder = builder();

		withRecord(builder()).forEach(doc -> {
			Assert.assertNull(doc.record.id);
			Assert.assertNull(doc.record.storedField);
			Assert.assertNull(doc.record.sortedDocValue);
			Assert.assertNull(doc.record.doubleDocValue);
		});

		withMap(builder).forEach(doc -> {
			Assert.assertNull(doc.fields.get("$id$"));
			Assert.assertNull(doc.fields.get("storedField"));
			Assert.assertNull(doc.fields.get("sortedDocValue"));
			Assert.assertNull(doc.fields.get("doubleDocValue"));
		});
	}

	@Test
	public void checkOnlyStoredField() {
		QueryBuilder builder = builder().returnedField("storedField");

		withRecord(builder).forEach(doc -> {
			Assert.assertNull(doc.record.id);
			Assert.assertEquals(STORED_FIELDS[doc.pos], doc.record.storedField);
			Assert.assertNull(doc.record.sortedDocValue);
			Assert.assertNull(doc.record.doubleDocValue);
		});

		withMap(builder).forEach(doc -> {
			Assert.assertNull(doc.fields.get("$id$"));
			Assert.assertEquals(STORED_FIELDS[doc.pos], doc.fields.get("storedField"));
			Assert.assertNull(doc.fields.get("sortedDocValue"));
			Assert.assertNull(doc.fields.get("doubleDocValue"));
		});
	}

	@Test
	public void checkOnlySortedDocValueField() {
		QueryBuilder builder = builder().returnedField("sortedDocValue");

		withRecord(builder).forEach(doc -> {
			Assert.assertNull(doc.record.id);
			Assert.assertNull(doc.record.storedField);
			Assert.assertEquals(SDV_FIELDS[doc.pos], doc.record.sortedDocValue);
			Assert.assertNull(doc.record.doubleDocValue);
		});

		withMap(builder).forEach(doc -> {
			Assert.assertNull(doc.fields.get("$id$"));
			Assert.assertNull(doc.fields.get("storedField"));
			Assert.assertEquals(SDV_FIELDS[doc.pos], doc.fields.get("sortedDocValue"));
			Assert.assertNull(doc.fields.get("doubleDocValue"));
		});
	}

	@Test
	public void checkMultiStringStoredField() {
		QueryBuilder builder = builder().returnedField("multivaluedStringStoredField");
		withRecord(builder).forEach(doc -> Assert.assertArrayEquals(MULTI_STRING_STORED_FIELDS[doc.pos],
				doc.record.multivaluedStringStoredField.toArray()));
	}

	@Test
	public void checkMultiIntegerStoredField() {
		QueryBuilder builder = builder().returnedField("multivaluedIntegerStoredField");
		withRecord(builder).forEach(doc -> Assert.assertArrayEquals(MULTI_INTEGER_STORED_FIELDS[doc.pos],
				doc.record.multivaluedIntegerStoredField.toArray()));
	}

	@Test
	public void checkOnlyDoubleDocValueField() {
		QueryBuilder builder = builder().returnedField("doubleDocValue");

		withRecord(builder).forEach(doc -> {
			Assert.assertNull(doc.record.id);
			Assert.assertNull(doc.record.storedField);
			Assert.assertNull(doc.record.sortedDocValue);
			Assert.assertEquals(DDV_FIELDS[doc.pos], doc.record.doubleDocValue);
		});

		withMap(builder).forEach(doc -> {
			Assert.assertNull(doc.fields.get("$id$"));
			Assert.assertNull(doc.fields.get("storedField"));
			Assert.assertNull(doc.fields.get("sortedDocValue"));
			Assert.assertEquals(DDV_FIELDS[doc.pos], doc.fields.get("doubleDocValue"));
		});
	}
}
