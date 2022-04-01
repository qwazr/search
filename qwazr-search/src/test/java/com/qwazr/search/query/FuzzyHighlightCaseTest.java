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
package com.qwazr.search.query;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.annotations.SmartField;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.HighlighterDefinition;
import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.index.ResultDocumentObject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FuzzyHighlightCaseTest {

    private AnnotatedIndexService<MyIndexRecord> myIndexService;
    private IndexManager indexManager;
    private ExecutorService executorService;

    public static final String CONTENT = "content";

    @Index(name = "my_index")
    public static class MyIndexRecord {

        @SmartField(name = FieldDefinition.ID_FIELD, index = true, stored = true)
        String id;

        @IndexField(
            name = CONTENT,
            stored = true,
            tokenized = true,
            analyzerClass = WhitespaceAnalyzer.class,
            indexOptions = IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS)
        String content;

        public MyIndexRecord() {
        }

        MyIndexRecord(String id, String content) {
            this.id = id;
            this.content = content;
        }
    }

    @Before
    public void before() throws IOException, URISyntaxException {

        executorService = Executors.newCachedThreadPool();
        Path indexesDirectory = Files.createTempDirectory("my_indexes");
        indexManager = new IndexManager(indexesDirectory, executorService);

        myIndexService = indexManager.getService(MyIndexRecord.class);
        myIndexService.createUpdateIndex();
        myIndexService.createUpdateFields();

        MyIndexRecord indexRecord1 = new MyIndexRecord("1", "film cinema");
        MyIndexRecord indexRecord2 = new MyIndexRecord("2", "film disney");
        myIndexService.postDocuments(List.of(indexRecord1, indexRecord2));

    }

    @After
    public void after() {
        indexManager.close();
        executorService.shutdown();
    }

    /*
     * Bug Description:
     * highlight on a FuzzyQuery doesn't handle correctly result with more than 1 document
     * see shouldHighlightWithFuzzyLimitedTo1Result where results are limited to 1 and highlight is handled correctly
     */
    @Test
    public void shouldHighlightWithFuzzy() {
        Query query = new FuzzyQuery(new Term(CONTENT, "fim"));

        QueryDefinition queryDefinition =
            QueryDefinition.of(query).start(0).rows(10)
                .highlighter(CONTENT, HighlighterDefinition.of(CONTENT).build())
                .returnedField("*").build();

        ResultDefinition.WithObject<MyIndexRecord> result = myIndexService.searchQuery(queryDefinition);

        assertThat(result.getTotalHits(), equalTo(2L));
        List<ResultDocumentObject<MyIndexRecord>> records = result.getDocuments();
        assertThat(records.get(0).getRecord().id, equalTo("1"));
        assertThat(records.get(1).getRecord().id, equalTo("2"));
        assertThat(records.get(0).getHighlights(), hasEntry(equalTo(CONTENT), equalTo("<b>film</b> cinema")));
        assertThat(records.get(1).getHighlights(), hasEntry(equalTo(CONTENT), equalTo("<b>film</b> disney")));
    }

    @Test
    public void shouldHighlightWithFuzzyLimitedTo1Result() {
        Query query = new FuzzyQuery(new Term(CONTENT, "fim"));

        QueryDefinition queryDefinition =
            QueryDefinition.of(query).start(0).rows(1)
                .highlighter(CONTENT, HighlighterDefinition.of(CONTENT).build())
                .returnedField("*").build();

        ResultDefinition.WithObject<MyIndexRecord> result = myIndexService.searchQuery(queryDefinition);

        assertThat(result.getTotalHits(), equalTo(2L));
        List<ResultDocumentObject<MyIndexRecord>> records = result.getDocuments();
        assertThat(records.get(0).getRecord().id, equalTo("1"));
        assertThat(records.get(0).getHighlights(), hasEntry(equalTo(CONTENT), equalTo("<b>film</b> cinema")));
    }

}
