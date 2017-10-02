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

public class MultiPhraseQueryTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexService();
		indexService.postDocument(new IndexRecord.NoTaxonomy("1").textField("Hello World"));
		indexService.postDocument(new IndexRecord.NoTaxonomy("2").textField("How are you ?"));
	}

	@Test
	public void withoutPositions() {
		ResultDefinition result = indexService.searchQuery(
				QueryDefinition.of(new MultiPhraseQuery("textField", 1).add("hello", "world")).build());
		Assert.assertNotNull(result);
		Assert.assertEquals(Long.valueOf(1), result.total_hits);
	}

	@Test
	public void withPositions() {
		ResultDefinition result = indexService.searchQuery(
				QueryDefinition.of(new MultiPhraseQuery("textField", 1).add(1, "how", "are")).build());
		Assert.assertNotNull(result);
		Assert.assertEquals(Long.valueOf(1), result.total_hits);
	}
}
