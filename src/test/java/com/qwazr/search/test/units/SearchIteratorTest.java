/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.query.ExactInteger;
import com.qwazr.search.query.MatchAllDocs;
import com.qwazr.search.query.TermQuery;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SearchIteratorTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    private static List<IndexRecord.NoTaxonomy> documents;
    private static List<IndexRecord.NoTaxonomy> subset;

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        initIndexService();
        documents = new ArrayList<>();
        subset = new ArrayList<>();
        for (int i = 0; i < RandomUtils.nextInt(300, 500); i++)
            documents.add(new IndexRecord.NoTaxonomy(Integer.toString(i)).intPoint(RandomUtils.nextInt(0, 2)));
        indexService.postDocuments(documents);
    }

    void checkIterate(Iterator<? extends IndexRecord<?>> iterator, Set<String> ids) {
        int initialSize = ids.size();
        int count = 0;
        while (iterator.hasNext()) {
            final IndexRecord<?> record = iterator.next();
            Assert.assertNotNull(record);
            Assert.assertTrue(ids.remove(record.id));
            count++;
        }

        Assert.assertEquals(initialSize, count);
        Assert.assertEquals(0, ids.size());

        try {
            iterator.next();
            Assert.fail("NoSuchElementException not thrown");
        } catch (NoSuchElementException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void iterateAll() {
        final Iterator<? extends IndexRecord<?>> iterator =
            indexService.searchIterator(QueryDefinition.of(MatchAllDocs.INSTANCE).returnedField("*").build(),
                IndexRecord.NoTaxonomy.class);
        Assert.assertNotNull(iterator);

        final Set<String> ids = new HashSet<>();
        documents.forEach(r -> ids.add(r.id));
        Assert.assertEquals(ids.size(), documents.size());

        checkIterate(iterator, ids);
    }

    @Test
    public void iterateSubSet() {

        final Iterator<? extends IndexRecord<?>> iterator = indexService.searchIterator(
            QueryDefinition.of(new ExactInteger("intPoint", 0)).returnedField("*").build(),
            IndexRecord.NoTaxonomy.class);
        Assert.assertNotNull(iterator);

        final Set<String> ids = new HashSet<>();
        documents.stream().filter(doc -> doc.intPoint == 0).forEach(r -> ids.add(r.id));
        Assert.assertNotEquals(0, ids.size());
        Assert.assertNotEquals(documents.size(), ids.size());

        checkIterate(iterator, ids);
    }

    @Test
    public void iterateNone() {

        final Iterator<? extends IndexRecord<?>> iterator = indexService.searchIterator(
            QueryDefinition.of(new TermQuery(FieldDefinition.ID_FIELD, -1)).returnedField("*").build(),
            IndexRecord.NoTaxonomy.class);
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
