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
package com.qwazr.search.collector;

import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.IntExactQuery;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

public class FilterCollectorQueryTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, InterruptedException, URISyntaxException {
        initIndexService();
        Collection<IndexRecord.NoTaxonomy> indexRecords = new ArrayList<>();
        int k = 0;
        for (int i = 0; i < 10; i++) {
            indexRecords.clear();
            for (int j = 0; j < 100; j++)
                indexRecords.add(new IndexRecord.NoTaxonomy(Integer.toString(k)).intPoint((k++) % 2 == 0 ? 1 : 0));
            indexService.postDocuments(indexRecords);
        }
    }

    @Test
    public void test() {
        final QueryDefinition queryDef1 =
                QueryDefinition.of(new IntExactQuery("intPoint", 0)).collector("filter", FilterCollector.class).build();
        final FilterCollector.Query filterQuery =
                indexService.searchQuery(queryDef1).getCollector("filter", FilterCollector.Query.class);
        Assert.assertNotNull(filterQuery);

        final QueryDefinition queryDef2 = QueryDefinition.of(filterQuery).queryDebug(true).build();
        final ResultDefinition.WithObject<? extends IndexRecord<?>> results = indexService.searchQuery(queryDef2);
        Assert.assertNotNull(results);
        Assert.assertEquals(500, results.totalHits);
        Assert.assertEquals("(f)", results.getQuery());
    }

}
