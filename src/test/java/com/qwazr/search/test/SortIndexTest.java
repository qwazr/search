/*
 * Copyright 2015-2019 Emmanuel Keller / QWAZR
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

package com.qwazr.search.test;

import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.search.test.units.AbstractIndexTest;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class SortIndexTest extends AbstractIndexTest {

    @Test
    public void sortIndexTest() {
        final IndexServiceInterface indexService = initIndexManager(true).getService();
        final Sort sort = new Sort(new SortField("id", SortField.Type.STRING));
        indexManager.registerSort("sortById", sort);
        indexService.createUpdateSchema("schema");
        final IndexSettingsDefinition settings = IndexSettingsDefinition.of().sort("sortById").build();
        final IndexStatus status = indexService.createUpdateIndex("schema", "index", settings);
        Assert.assertNotNull(status);
        Assert.assertEquals(status.settings.sort, "sortById");
        Assert.assertEquals(status.indexSortFields, Set.of("id"));
    }
}
