/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.TermQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class DocValuesCacheTest extends AbstractIndexTest {

	@Before
	public void setup() throws IOException, URISyntaxException {
		initIndexService();
	}

	void checkDocValue(String id, long expectedHits, Double expectedValue) {
		ResultDefinition.WithObject<IndexRecord> result = indexService.searchQuery(QueryDefinition.of(
				new TermQuery(FieldDefinition.ID_FIELD, id)).returnedField("*").build());
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.total_hits);
		Assert.assertEquals(expectedHits, result.total_hits, 0);
		if (expectedHits == 0)
			return;
		IndexRecord record = result.documents.get(0).record;
		Assert.assertEquals(expectedValue, record.doubleDocValue);
	}

	@Test
	public void checkUpdateDv() throws IOException, InterruptedException {
		indexService.postDocument(new IndexRecord("1"));
		checkDocValue("1", 1, null);
		indexService.postDocument(new IndexRecord("1").doubleDocValue(1.11d));
		checkDocValue("1", 1, 1.11d);
		checkDocValue("1", 1, 1.11d);
		indexService.postDocument(new IndexRecord("1").doubleDocValue(2.22d));
		checkDocValue("1", 1, 2.22d);
		indexService.updateDocumentValues(new IndexRecord("1").doubleDocValue(3.33d));
		checkDocValue("1", 1, 3.33d);
		indexService.deleteByQuery(QueryDefinition.of(new TermQuery(FieldDefinition.ID_FIELD, "1")).build());
		checkDocValue("1", 0, null);
		indexService.postDocument(new IndexRecord("1").doubleDocValue(4.44d));
		checkDocValue("1", 1, 4.44d);
		indexService.postDocument(new IndexRecord("2").doubleDocValue(5.55d));
		checkDocValue("2", 1, 5.55d);
		indexService.deleteAll();
		checkDocValue("1", 0, null);
		checkDocValue("2", 0, null);
	}
}
