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

package com.qwazr.search.docs;

import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.SmartField;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.FacetDefinition;
import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.index.ResultDocumentObject;
import com.qwazr.search.query.MultiFieldQuery;
import com.qwazr.search.query.QueryParserOperator;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GettingStarted {

    // This class defines both an index, and a record for this index
    @Index(name = "my_index")
    public static class MyIndexRecord {

        @SmartField(name = FieldDefinition.ID_FIELD, index = true, stored = true)
        String id;

        @SmartField(index = true,
            sort = true,
            stored = true,
            analyzer = SmartAnalyzerSet.english)
        String title;

        @SmartField(index = true,
            stored = true,
            indexAnalyzerClass = SmartAnalyzerSet.EnglishIndex.class,
            queryAnalyzerClass = SmartAnalyzerSet.EnglishQuery.class)
        String content;

        @SmartField(index = true, facet = true)
        String[] tags;

        // Public no argument constructor is mandatory
        public MyIndexRecord() {
        }

        MyIndexRecord(String id, String title, String content, String... tags) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.tags = tags;
        }
    }

    @Test
    public void test() throws IOException, URISyntaxException {

        /* INDEX MANAGER SETUP */

        ExecutorService executorService = Executors.newCachedThreadPool(); // we need a pool of thread
        Path indexesDirectory = Files.createTempDirectory("my_indexes"); // The directory where the indexes are stored
        IndexManager indexManager =
            new IndexManager(indexesDirectory, executorService); // Let's build our index manager

        /* INDEX SETUP */

        AnnotatedIndexService<MyIndexRecord> myIndexService =
            indexManager.getService(MyIndexRecord.class); // Get the service related to our index class (MyIndex)
        myIndexService.createUpdateIndex(); // We create the index (nothing is done if the index already exists)
        myIndexService.createUpdateFields(); // We create the fields (nothing is done if the fields already exist)

        /* INDEXING RECORD */

        MyIndexRecord indexRecord1 = new MyIndexRecord("1", "First news", "My first article", "news", "infos");
        MyIndexRecord indexRecord2 = new MyIndexRecord("2", "Second news", "My second article", "news", "infos");
        myIndexService.postDocuments(Arrays.asList(indexRecord1, indexRecord2)); // Let's index them

        /* SEARCH THE INDEX */

        // We create a user query
        MultiFieldQuery multiFieldQuery =
            MultiFieldQuery.of().defaultOperator(QueryParserOperator.AND) // The operator will be AND
                .fieldBoost("title", 3.0f) // The title field has a boost of 3
                .fieldBoost("content", 1.0f) // The content field has a boost of 1
                .queryString("first article") // We look for the terms "my article"
                .build();

        // We also would love to have a facet
        FacetDefinition facet = FacetDefinition.of(10).sort(FacetDefinition.Sort.value_descending).build();

        // We can build our final request
        QueryDefinition queryDefinition =
            QueryDefinition.of(multiFieldQuery).start(0).rows(10).facet("tags", facet).returnedField("*").build();

        // We can now do the search
        ResultDefinition.WithObject<MyIndexRecord> result = myIndexService.searchQuery(queryDefinition);

        // And here is our search result
        assert result.getTotalHits() == 1L; // The number of results
        List<ResultDocumentObject<MyIndexRecord>> records = result.getDocuments(); // Retrieve our found records
        assert indexRecord1.id.equals(
            records.get(0).getRecord().id); // Check we found the right record by checking the ID
        Map<String, Number> facets = result.getFacet("tags"); // Retrieve our facet resuls
        assert facets.get("infos").intValue() == 1; // Check we have our facet result for the tag "infos"
        assert facets.get("news").intValue() == 1; // Same for the tag "news"

        /* FREE RESOURCES */
        indexManager.close(); // IndexManager is closeable (close it only if you will not use the service anymore)
    }

}
