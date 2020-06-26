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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class QueryAndJsonSampleTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws URISyntaxException {
        initIndexManager();
        initIndexService();
    }

    @Test
    public void getTypesTest() {
        assertThat(QuerySampler.TYPES_FACTORY, notNullValue());
        assertThat(QuerySampler.TYPES_URI_DOC, notNullValue());
        assertThat(QuerySampler.TYPES_CAMEL_KEYWORDS, notNullValue());
        assertThat(QuerySampler.TYPES_FACTORY.size(), equalTo(39));
        assertThat(QuerySampler.TYPES_URI_DOC.size(), equalTo(QuerySampler.TYPES_FACTORY.size()));
        assertThat(QuerySampler.TYPES_URI_DOC.size(), equalTo(QuerySampler.TYPES_CAMEL_KEYWORDS.size()));
        assertThat(QuerySampler.TYPES_FACTORY.keySet(), equalTo(QuerySampler.TYPES_URI_DOC.keySet()));
        assertThat(QuerySampler.TYPES_FACTORY.keySet(), equalTo(QuerySampler.TYPES_CAMEL_KEYWORDS.keySet()));

        final Map<String, URI> types = indexService.getQueryTypes(null);
        types.forEach((type, docUri) -> {
            try {
                final QueryInterface sampleQuery = indexService.getQuerySample(type);
                assertThat(sampleQuery, notNullValue());
                assertThat(docUri.toString(),
                    ((HttpURLConnection) docUri.toURL().openConnection()).getResponseCode(),
                    equalTo(200));
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        });

        assertThat(types.keySet(), equalTo(
            new TreeSet<>(Arrays.asList(
                "BlendedTerm",
                "Bool",
                "Boost",
                "CommonTerms",
                "ConstantScore",
                "DisjunctionMax",
                "DoubleMultiRange",
                "DoubleRange",
                "DoubleSet",
                "DrillDown",
                "ExactDouble",
                "ExactFloat",
                "ExactInteger",
                "ExactLong",
                "FacetPath",
                "FloatMultiRange",
                "FloatRange",
                "FloatSet",
                "Function",
                "FunctionScore",
                "Fuzzy",
                "HasTerm",
                "IntegerMultiRange",
                "IntegerRange",
                "IntegerSet",
                "Join",
                "LongMultiRange",
                "LongRange",
                "LongSet",
                "MatchAllDocs",
                "MatchNoDocs",
                "MultiPhrase",
                "NGramPhrase",
                "Phrase",
                "Prefix",
                "Regexp",
                "SimpleQueryParser",
                "TermRange",
                "Wildcard"))
        ));
    }

    private void checkFuzzyStartsWith(final String lookup, String... types) {
        final Set<String> founds = indexService.getQueryTypes(lookup).keySet();
        if (founds.size() < types.length)
            assertThat(founds, equalTo(types));
        final String[] foundArray = new String[types.length];
        int i = 0;
        for (final String found : founds) {
            if (i == types.length)
                break;
            foundArray[i++] = found;
        }
        assertThat(lookup + " -> " + founds.toString(), foundArray, equalTo(types));
    }

    @Test
    public void getQueryTypeLookupTest() {
        checkFuzzyStartsWith("matchall", "MatchAllDocs", "MatchNoDocs", "MultiPhrase");
        checkFuzzyStartsWith("Match", "MatchAllDocs", "MatchNoDocs", "FacetPath");
        checkFuzzyStartsWith("docs", "MatchAllDocs", "MatchNoDocs", "DoubleSet");
        checkFuzzyStartsWith("all", "MatchAllDocs", "ExactFloat", "ExactLong");
        checkFuzzyStartsWith("nodoc", "MatchNoDocs", "MatchAllDocs", "FunctionScore");
        checkFuzzyStartsWith("term", "HasTerm", "TermRange", "BlendedTerm");
        checkFuzzyStartsWith("int", "IntegerRange", "IntegerSet", "ExactInteger");
        checkFuzzyStartsWith("intrange", "IntegerRange", "TermRange", "FloatRange");
        checkFuzzyStartsWith("setint", "IntegerSet", "LongSet", "FloatSet");
        checkFuzzyStartsWith("floatexact", "ExactFloat", "FloatSet", "FloatRange", "ExactLong");
        checkFuzzyStartsWith("DoubleExact", "ExactDouble", "DoubleSet", "DoubleRange", "ExactFloat");
        checkFuzzyStartsWith("Max", "DisjunctionMax", "TermRange", "MatchAllDocs");
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
