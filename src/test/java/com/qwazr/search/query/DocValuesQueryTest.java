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

public class DocValuesQueryTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexService();
		indexService.postDocument(new IndexRecord.NoTaxonomy("i1").intDocValue(1));
		indexService.postDocument(new IndexRecord.NoTaxonomy("l2").longDocValue(2));
		indexService.postDocument(new IndexRecord.NoTaxonomy("f3").floatDocValue(3f));
		indexService.postDocument(new IndexRecord.NoTaxonomy("d4").doubleDocValue(4d));
	}

	@Test
	public void intExact() {
		ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
				QueryDefinition.of(new IntDocValuesExactQuery("intDocValue", 1)).returnedField("*").build());
		Assert.assertEquals("i1", result.getDocuments().get(0).record.id);
		Assert.assertEquals(1, result.getDocuments().get(0).record.intDocValue, 0);
	}

	@Test
	public void intRange() {
		ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
				QueryDefinition.of(new IntDocValuesRangeQuery("intDocValue", 0, 2)).returnedField("*").build());
		Assert.assertEquals("i1", result.getDocuments().get(0).record.id);
		Assert.assertEquals(1, result.getDocuments().get(0).record.intDocValue, 0);
	}

	@Test
	public void longExact() {
		ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
				QueryDefinition.of(new LongDocValuesExactQuery("longDocValue", 2)).returnedField("*").build());
		Assert.assertEquals("l2", result.getDocuments().get(0).record.id);
		Assert.assertEquals(2, result.getDocuments().get(0).record.longDocValue, 0);
	}

	@Test
	public void longRange() {
		ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
				QueryDefinition.of(new LongDocValuesRangeQuery("longDocValue", 1L, 3L)).returnedField("*").build());
		Assert.assertEquals("l2", result.getDocuments().get(0).record.id);
		Assert.assertEquals(2, result.getDocuments().get(0).record.longDocValue, 0);
	}

	@Test
	public void floatExact() {
		ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
				QueryDefinition.of(new FloatDocValuesExactQuery("floatDocValue", 3)).returnedField("*").build());
		Assert.assertEquals("f3", result.getDocuments().get(0).record.id);
		Assert.assertEquals(3, result.getDocuments().get(0).record.floatDocValue, 0);
	}

	@Test
	public void floatRange() {
		ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
				QueryDefinition.of(new FloatDocValuesRangeQuery("floatDocValue", 2F, 4F)).returnedField("*").build());
		Assert.assertEquals("f3", result.getDocuments().get(0).record.id);
		Assert.assertEquals(3, result.getDocuments().get(0).record.floatDocValue, 0);
	}

	@Test
	public void doubleExact() {
		ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
				QueryDefinition.of(new DoubleDocValuesExactQuery("doubleDocValue", 4)).returnedField("*").build());
		Assert.assertEquals("d4", result.getDocuments().get(0).record.id);
		Assert.assertEquals(4, result.getDocuments().get(0).record.doubleDocValue, 0);
	}

	@Test
	public void doubleRange() {
		ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
				QueryDefinition.of(new DoubleDocValuesRangeQuery("doubleDocValue", 3D, 5D)).returnedField("*").build());
		Assert.assertEquals("d4", result.getDocuments().get(0).record.id);
		Assert.assertEquals(4, result.getDocuments().get(0).record.doubleDocValue, 0);
	}

}
