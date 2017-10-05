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
package com.qwazr.search.query;

import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.function.Consumer;

/**
 * This class check the set of queries for DocValues and SortedDocValues
 */
public class DocValuesQueryTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexService();
		indexService.postDocument(new IndexRecord.NoTaxonomy("i1").intDocValue(1));
		indexService.postDocument(new IndexRecord.NoTaxonomy("si1").sortedIntDocValue(1));
		indexService.postDocument(new IndexRecord.NoTaxonomy("l2").longDocValue(2));
		indexService.postDocument(new IndexRecord.NoTaxonomy("sl2").sortedLongDocValue(2));
		indexService.postDocument(new IndexRecord.NoTaxonomy("f3").floatDocValue(3f));
		indexService.postDocument(new IndexRecord.NoTaxonomy("sf3").sortedFloatDocValue(3f));
		indexService.postDocument(new IndexRecord.NoTaxonomy("d4").doubleDocValue(4d));
		indexService.postDocument(new IndexRecord.NoTaxonomy("sd4").sortedDoubleDocValue(4d));
		indexService.postDocument(new IndexRecord.NoTaxonomy("sdv").sortedDocValue("b"));
		indexService.postDocument(new IndexRecord.NoTaxonomy("ssdv").sortedSetDocValue("c").sortedSetDocValue("d"));
	}

	private void test(AbstractQuery query, String expectedId, Consumer<IndexRecord> recordCheck) {
		ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result =
				indexService.searchQuery(QueryDefinition.of(query).returnedField("*").build());
		Assert.assertEquals(Long.valueOf(1), result.total_hits);
		final IndexRecord record = result.getDocuments().get(0).record;
		Assert.assertEquals(expectedId, record.id);
		recordCheck.accept(record);
	}

	@Test
	public void intExact() {
		test(new IntDocValuesExactQuery("intDocValue", 1), "i1",
				record -> Assert.assertEquals(1, record.intDocValue, 0));
	}

	@Test
	public void sortedIntExact() {
		test(new SortedIntDocValuesExactQuery("sortedIntDocValue", 1), "si1",
				record -> Assert.assertEquals(1, record.sortedIntDocValue, 0));
	}

	@Test
	public void intRange() {
		test(new IntDocValuesRangeQuery("intDocValue", 0, 2), "i1",
				record -> Assert.assertEquals(1, record.intDocValue, 0));
	}

	@Test
	public void sortedIntRange() {
		test(new SortedIntDocValuesRangeQuery("sortedIntDocValue", 0, 2), "si1",
				record -> Assert.assertEquals(1, record.sortedIntDocValue, 0));
	}

	@Test
	public void longExact() {
		test(new LongDocValuesExactQuery("longDocValue", 2L), "l2",
				record -> Assert.assertEquals(2, record.longDocValue, 0));
	}

	@Test
	public void sortedLongExact() {
		test(new SortedLongDocValuesExactQuery("sortedLongDocValue", 2L), "sl2",
				record -> Assert.assertEquals(2, record.sortedLongDocValue, 0));
	}

	@Test
	public void longRange() {
		test(new LongDocValuesRangeQuery("longDocValue", 1L, 3L), "l2",
				record -> Assert.assertEquals(2, record.longDocValue, 0));
	}

	@Test
	public void sortedLongRange() {
		test(new SortedLongDocValuesRangeQuery("sortedLongDocValue", 1L, 3L), "sl2",
				record -> Assert.assertEquals(2L, record.sortedLongDocValue, 0));
	}

	@Test
	public void floatExact() {
		test(new FloatDocValuesExactQuery("floatDocValue", 3F), "f3",
				record -> Assert.assertEquals(3F, record.floatDocValue, 0));
	}

	@Test
	public void sortedFloatExact() {
		test(new SortedFloatDocValuesExactQuery("sortedFloatDocValue", 3F), "sf3",
				record -> Assert.assertEquals(3F, record.sortedFloatDocValue, 0));
	}

	@Test
	public void floatRange() {
		test(new FloatDocValuesRangeQuery("floatDocValue", 2F, 4F), "f3",
				record -> Assert.assertEquals(3F, record.floatDocValue, 0));
	}

	@Test
	public void sortedFloatRange() {
		test(new SortedFloatDocValuesRangeQuery("sortedFloatDocValue", 2F, 4F), "sf3",
				record -> Assert.assertEquals(3F, record.sortedFloatDocValue, 0));
	}

	@Test
	public void doubleExact() {
		test(new DoubleDocValuesExactQuery("doubleDocValue", 4D), "d4",
				record -> Assert.assertEquals(4D, record.doubleDocValue, 0));
	}

	@Test
	public void sortedDoubleExact() {
		test(new SortedDoubleDocValuesExactQuery("sortedDoubleDocValue", 4D), "sd4",
				record -> Assert.assertEquals(4D, record.sortedDoubleDocValue, 0));
	}

	@Test
	public void doubleRange() {
		test(new DoubleDocValuesRangeQuery("doubleDocValue", 3D, 5D), "d4",
				record -> Assert.assertEquals(4D, record.doubleDocValue, 0));
	}

	@Test
	public void sortedDoubleRange() {
		test(new SortedDoubleDocValuesRangeQuery("sortedDoubleDocValue", 3D, 5D), "sd4",
				record -> Assert.assertEquals(4D, record.sortedDoubleDocValue, 0));
	}

	@Test
	public void sortedExact() {
		test(new SortedDocValuesExactQuery("sortedDocValue", "b"), "sdv",
				record -> Assert.assertEquals("b", record.sortedDocValue));
	}

	@Test
	public void sortedRange() {
		test(new SortedDocValuesRangeQuery("sortedDocValue", "b", "c", null, null), "sdv",
				record -> Assert.assertEquals("b", record.sortedDocValue));
		test(new SortedDocValuesRangeQuery("sortedDocValue", "b", "b", true, true), "sdv",
				record -> Assert.assertEquals("b", record.sortedDocValue));
		test(new SortedDocValuesRangeQuery("sortedDocValue", "a", "b", false, true), "sdv",
				record -> Assert.assertEquals("b", record.sortedDocValue));
	}

	@Test
	public void sortedSetExact() {
		final LinkedHashSet<String> set = new LinkedHashSet<>(Arrays.asList("c", "d"));
		test(new SortedSetDocValuesExactQuery("sortedSetDocValue", "c"), "ssdv",
				record -> Assert.assertEquals(set, record.sortedSetDocValue));
		test(new SortedSetDocValuesExactQuery("sortedSetDocValue", "d"), "ssdv",
				record -> Assert.assertEquals(set, record.sortedSetDocValue));
	}

	@Test
	public void sortedSetRange() {
		final LinkedHashSet<String> set = new LinkedHashSet<>(Arrays.asList("c", "d"));
		test(new SortedSetDocValuesRangeQuery("sortedSetDocValue", "d", "e", null, null), "ssdv",
				record -> Assert.assertEquals(set, record.sortedSetDocValue));
		test(new SortedSetDocValuesRangeQuery("sortedSetDocValue", "c", "c", true, true), "ssdv",
				record -> Assert.assertEquals(set, record.sortedSetDocValue));
		test(new SortedSetDocValuesRangeQuery("sortedSetDocValue", "c", "d", false, true), "ssdv",
				record -> Assert.assertEquals(set, record.sortedSetDocValue));
	}
}
