/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.search.test.units;

import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.BoostQuery;
import com.qwazr.search.query.ConstantScoreQuery;
import com.qwazr.search.query.TermQuery;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class ConstantScoreAndBoostQueryTest extends AbstractIndexTest {

	@BeforeClass
	public static void setup() throws IOException, InterruptedException {
		indexService.postDocument(new IndexRecord("1").textField("Hello World"));
		indexService.postDocument(new IndexRecord("2").textField("How are you ?"));
	}

	@Test
	public void test() {
		final Float scoreValue = Float.MAX_VALUE - 1;
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new BoostQuery(new ConstantScoreQuery(new TermQuery("textField", "hello")), scoreValue)).build());
		Assert.assertNotNull(result);
		Assert.assertEquals(Long.valueOf(1), result.total_hits);
		Assert.assertEquals(scoreValue, result.getDocuments().get(0).getScore());
	}
}
