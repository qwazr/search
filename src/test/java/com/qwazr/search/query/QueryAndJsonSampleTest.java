/*
 *  Copyright 2015-2020 Emmanuel Keller / QWAZR
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.qwazr.search.query;

import com.qwazr.search.test.units.AbstractIndexTest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.WebApplicationException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.BeforeClass;
import org.junit.Test;

public class QueryAndJsonSampleTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws URISyntaxException {
        initIndexManager();
        initIndexService();
    }

    @Test
    public void getTypesTest() throws IOException {
        assertThat(AbstractQuery.TYPES, notNullValue());
        assertThat(AbstractQuery.TYPES.size(), equalTo(81));

        final Set<String> types = indexService.getQueryTypes();
        final List<String> typesWithSample = new ArrayList<>();
        for (final String type : types) {
            try {
                final QueryInterface sampleQuery = indexService.getQuerySample(type);
                assertThat(sampleQuery.getDocUri(), notNullValue());
                assertThat(sampleQuery.getDocUri().toString(),
                    ((HttpURLConnection) sampleQuery.getDocUri().toURL().openConnection()).getResponseCode(),
                    equalTo(200));
                typesWithSample.add(type);
            } catch (WebApplicationException e) {
                assertThat(e.getResponse().getStatus(), equalTo(404));
                assertThat(e.getMessage(), equalTo("This query has no sample: " + type));
            }
        }
        assertThat(typesWithSample, equalTo(List.of(
            "BlendedTermQuery",
            "BooleanQuery",
            "BoostQuery",
            "CommonTermsQuery",
            "ConstantScoreQuery",
            "DisjunctionMaxQuery",
            "DocValuesFieldExistsQuery",
            "DoubleDocValuesExactQuery",
            "DoubleDocValuesRangeQuery",
            "DoubleExactQuery",
            "MatchAllDocsQuery",
            "MatchNoDocsQuery",
            "TermQuery")));
    }

    @Test
    public void getJsonSampleTest() {
        final Map<String, Object> sample = indexService.getJsonSample();
        assertThat(sample, notNullValue());
        assertThat(sample.keySet(), hasSize(30));
        assertThat(sample.keySet(), hasItem("$id$"));
        assertThat(sample.get("$id$").toString(), not(isEmptyOrNullString()));
    }

    private void jsonSamplesTest(List<Map<String, Object>> samples, int expected) {
        assertThat(samples, notNullValue());
        assertThat(samples, hasSize(expected));
        for (int i = 0; i < expected; i++) {
            final Map<String, Object> sample = samples.get(i);
            assertThat(sample.keySet(), hasSize(30));
            assertThat(sample.keySet(), hasItem("$id$"));
            assertThat(sample.get("$id$").toString(), not(isEmptyOrNullString()));
        }
    }

    @Test
    public void getJsonSamplesTest() {
        jsonSamplesTest(indexService.getJsonSamples(null), 2);
        jsonSamplesTest(indexService.getJsonSamples(5), 5);
    }
}
