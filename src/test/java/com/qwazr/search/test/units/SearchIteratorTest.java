/**
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
import com.qwazr.search.query.MatchAllDocsQuery;
import com.qwazr.search.query.TermQuery;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class SearchIteratorTest extends AbstractIndexTest {

	private static List<IndexRecord> documents;

	@BeforeClass
	public static void setup() throws IOException, InterruptedException {
		documents = new ArrayList<>();
		for (int i = 0; i < RandomUtils.nextInt(1, 1000); i++)
			documents.add(new IndexRecord(RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(2, 5))).intDocValue(
					RandomUtils.nextInt(2, 5)));
		indexService.postDocuments(documents);
	}

	@Test
	public void iterateAll() throws ReflectiveOperationException {
		final Iterator<IndexRecord> iterator =
				indexService.searchIterator(QueryDefinition.of(new MatchAllDocsQuery()).build(), IndexRecord.class);
		Assert.assertNotNull(iterator);

		int count = 0;
		final Set<String> ids = new HashSet<>();
		documents.forEach(r -> ids.add(r.id));

		while (iterator.hasNext()) {
			final IndexRecord record = iterator.next();
			Assert.assertNotNull(record);
			Assert.assertTrue(ids.remove(record.id));
			count++;
		}

		Assert.assertEquals(documents.size(), count);
		Assert.assertEquals(0, ids.size());

		try {
			iterator.next();
			Assert.fail("NoSuchElementException not thrown");
		} catch (NoSuchElementException e) {
			Assert.assertNotNull(e);
		}
	}

	@Test
	public void iterateNone() throws ReflectiveOperationException {

		final Iterator<IndexRecord> iterator =
				indexService.searchIterator(QueryDefinition.of(new TermQuery(FieldDefinition.ID_FIELD, 0)).build(),
						IndexRecord.class);
		Assert.assertNotNull(iterator);
		Assert.assertFalse(iterator.hasNext());

		try {
			iterator.next();
			Assert.fail("NoSuchElementException not thrown");
		} catch (NoSuchElementException e) {
			Assert.assertNotNull(e);
		}
	}
}
