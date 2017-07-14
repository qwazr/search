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

package com.qwazr.search.field;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.SmartField;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.query.MatchAllDocsQuery;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class SmartFieldSortedTest extends AbstractIndexTest {

	private static AnnotatedIndexService<Record> indexService;

	@BeforeClass
	public static void setup() throws IOException, URISyntaxException, InterruptedException {
		indexService = initIndexService(Record.class);
		List<Record> records = new ArrayList<>();
		for (int i = 0; i < RandomUtils.nextInt(10, 50); i++)
			records.add(new Record(true));
		indexService.addDocuments(records);
		Assert.assertEquals(records.size(), indexService.getIndexStatus().num_docs, 0);
	}

	private void checkSort(Iterator<Record> iterator, BiConsumer<Record, Record> checker) {
		final AtomicReference<Record> atomicRecord = new AtomicReference<>();
		iterator.forEachRemaining(nextRecord -> {
			final Record previousRecord = atomicRecord.getAndSet(nextRecord);
			if (previousRecord != null)
				checker.accept(previousRecord, nextRecord);
		});
	}

	@Test
	public void testString() {
		checkSort(indexService.searchIterator(QueryDefinition.of(new MatchAllDocsQuery()).sort("stringSort",
				QueryDefinition.SortEnum.ascending).returnedField("*").build(), Record.class),
				(r1, r2) -> Assert.assertTrue(r1.stringSort.compareTo(r2.stringSort) <= 0));
	}

	@Test
	public void testLong() {
		checkSort(indexService.searchIterator(QueryDefinition.of(new MatchAllDocsQuery()).sort("longSort",
				QueryDefinition.SortEnum.ascending).returnedField("*").build(), Record.class),
				(r1, r2) -> Assert.assertTrue(r1.longSort.compareTo(r2.longSort) <= 0));
	}

	@Test
	public void testInteger() {
		checkSort(indexService.searchIterator(QueryDefinition.of(new MatchAllDocsQuery()).sort("intSort",
				QueryDefinition.SortEnum.ascending).returnedField("*").build(), Record.class),
				(r1, r2) -> Assert.assertTrue(r1.intSort.compareTo(r2.intSort) <= 0));
	}

	@Test
	public void testFloat() {
		checkSort(indexService.searchIterator(QueryDefinition.of(new MatchAllDocsQuery()).sort("floatSort",
				QueryDefinition.SortEnum.ascending).returnedField("*").build(), Record.class),
				(r1, r2) -> Assert.assertTrue(r1.floatSort.compareTo(r2.floatSort) <= 0));
	}

	@Test
	public void testDouble() {
		checkSort(indexService.searchIterator(QueryDefinition.of(new MatchAllDocsQuery()).sort("doubleSort",
				QueryDefinition.SortEnum.ascending).returnedField("*").build(), Record.class),
				(r1, r2) -> Assert.assertTrue(r1.doubleSort.compareTo(r2.doubleSort) <= 0));
	}

	@Test(expected = WebApplicationException.class)
	public void testNonSortable() {
		indexService.searchIterator(QueryDefinition.of(new MatchAllDocsQuery()).sort("nonSortable",
				QueryDefinition.SortEnum.ascending).returnedField("*").build(), Record.class);
	}

	@Index(name = "SmartFieldSorted", schema = "TestQueries")
	static public class Record {

		@SmartField(type = SmartFieldDefinition.Type.LONG, sort = true, stored = true)
		final public Long longSort;

		@SmartField(type = SmartFieldDefinition.Type.INTEGER, sort = true, stored = true)
		final public Integer intSort;

		@SmartField(type = SmartFieldDefinition.Type.FLOAT, sort = true, stored = true)
		final public Float floatSort;

		@SmartField(type = SmartFieldDefinition.Type.DOUBLE, sort = true, stored = true)
		final public Double doubleSort;

		@SmartField(type = SmartFieldDefinition.Type.TEXT, sort = true, stored = true)
		final public String stringSort;

		@SmartField(type = SmartFieldDefinition.Type.TEXT, index = true)
		final public String nonSortable;

		Record(boolean random) {
			longSort = random ? RandomUtils.nextLong() : null;
			intSort = random ? RandomUtils.nextInt() : null;
			floatSort = random ? RandomUtils.nextFloat() : null;
			doubleSort = random ? RandomUtils.nextDouble() : null;
			stringSort = nonSortable = random ? RandomUtils.alphanumeric(5) : null;
		}

		public Record() {
			this(false);
		}
	}
}
