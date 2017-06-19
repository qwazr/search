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
import com.qwazr.search.index.ResultDocumentsInterface;
import com.qwazr.search.query.TermQuery;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SearchInterfaceTest extends AbstractIndexTest {

	private static List<IndexRecord> documents;

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexService();
		documents = new ArrayList<>();
		for (int i = 0; i < RandomUtils.nextInt(1, 10); i++)
			documents.add(new IndexRecord(RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(2, 5))).intDocValue(
					RandomUtils.nextInt(2, 5)));
		indexService.postDocuments(documents);
	}

	@Test
	public void searchQuery() throws ReflectiveOperationException {
		IndexRecord indexRecord = documents.get(RandomUtils.nextInt(0, documents.size()));
		AtomicReference<String> idRef = new AtomicReference<>();
		ResultDefinition.Empty results = indexService.searchQuery(
				QueryDefinition.of(new TermQuery(FieldDefinition.ID_FIELD, indexRecord.id)).build(),
				new ResultDocumentsInterface() {
					@Override
					public void doc(IndexSearcher searcher, int pos, ScoreDoc scoreDoc) throws IOException {
						idRef.set(searcher.doc(scoreDoc.doc).get(FieldDefinition.ID_FIELD));
					}
				});
		Assert.assertNotNull(results);
		Assert.assertEquals(Long.valueOf(1), results.total_hits);
		Assert.assertEquals(indexRecord.id, idRef.get());
	}
}
