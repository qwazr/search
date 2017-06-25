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
import com.qwazr.search.index.FacetDefinition;
import com.qwazr.search.index.QueryBuilder;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.index.ResultDocumentMap;
import com.qwazr.search.index.ResultDocumentObject;
import com.qwazr.search.query.MatchAllDocsQuery;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class PagingTest extends AbstractIndexTest {

	private static LinkedHashMap<String, IndexRecord> documents;

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexService();
		documents = new LinkedHashMap<>();
		for (int i = 0; i < RandomUtils.nextInt(201, 299); i++) {
			final IndexRecord record = new IndexRecord(Integer.toString(i)).sortedDocValue(
					RandomUtils.alphanumeric(5)).facetField(Integer.toString(RandomUtils.nextInt(1, 3)));
			documents.put(record.id, record);
			indexService.postDocument(record);
		}
	}

	private int checkEmpty(ResultDefinition result) {
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.total_hits);
		return result.getTotalHits().intValue();
	}

	private void checkPaging(final QueryBuilder builder) {

		final int totalHits = checkEmpty(indexService.searchQuery(builder.build()));
		Assert.assertEquals(totalHits, checkEmpty(indexService.searchQueryWithMap(builder.build())));

		Assert.assertTrue(totalHits > 0);
		final Set<String> idSetObject = new HashSet<>();
		final Set<String> idSetMap = new HashSet<>();
		int start = 0;
		while (start < totalHits) {

			final int rows = RandomUtils.nextInt(8, 20);
			final int expectedRows = start + rows > totalHits ? totalHits - start : rows;
			final QueryDefinition queryDef = builder.start(start).rows(rows).build();

			// Check Object
			final ResultDefinition.WithObject<IndexRecord> resultObject = indexService.searchQuery(queryDef);
			List<ResultDocumentObject<IndexRecord>> objectDocs = resultObject.getDocuments();
			Assert.assertEquals(expectedRows, objectDocs.size());
			int i = 0;
			for (ResultDocumentObject<IndexRecord> objectDoc : objectDocs) {
				ResultDocumentObject<IndexRecord> objectDoc2 = objectDocs.get(i);
				Assert.assertEquals(objectDoc, objectDoc2);
				Assert.assertEquals(i + start, objectDoc.getPos());
				idSetObject.add(objectDoc.record.id);
				i++;
			}

			// Check Map
			final ResultDefinition.WithMap resultMap = indexService.searchQueryWithMap(queryDef);
			List<ResultDocumentMap> mapDocs = resultMap.getDocuments();
			Assert.assertEquals(expectedRows, mapDocs.size());
			i = 0;
			for (ResultDocumentMap mapDoc : mapDocs) {
				ResultDocumentMap mapDoc2 = mapDocs.get(i);
				Assert.assertEquals(mapDoc, mapDoc2);
				Assert.assertEquals(i + start, mapDoc.getPos());
				idSetMap.add((String) mapDoc.getFields().get(FieldDefinition.ID_FIELD));
				i++;
			}

			start += rows;
		}
		Assert.assertEquals(totalHits, idSetObject.size());
		Assert.assertEquals(totalHits, idSetMap.size());
	}

	@Test
	public void pagingWithoutSort() throws URISyntaxException {
		checkPaging(QueryDefinition.of(new MatchAllDocsQuery()).returnedField("*"));
	}

	@Test
	public void pagingSort() throws URISyntaxException {
		checkPaging(QueryDefinition.of(new MatchAllDocsQuery())
				.returnedField("*")
				.sort("sortedDocValue", QueryDefinition.SortEnum.ascending));
	}

	@Test
	public void pagingFacetSort() throws URISyntaxException {
		checkPaging(QueryDefinition.of(new MatchAllDocsQuery())
				.returnedField("*")
				.facet("facetField", new FacetDefinition(10))
				.sort("sortedDocValue", QueryDefinition.SortEnum.ascending));
	}

	@Test
	public void pagingFacetWithoutSort() throws URISyntaxException {
		checkPaging(QueryDefinition.of(new MatchAllDocsQuery()).returnedField("*"));
	}
}
