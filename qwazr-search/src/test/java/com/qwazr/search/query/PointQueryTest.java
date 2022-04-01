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

package com.qwazr.search.query;

import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class PointQueryTest extends AbstractIndexTest.WithIndexRecord.NoTaxonomy {

    @BeforeClass
    public static void setup() throws IOException, InterruptedException, URISyntaxException {
        initIndexService();
        indexService.postDocument(new IndexRecord.NoTaxonomy("i1").intPoint(1));
        indexService.postDocument(new IndexRecord.NoTaxonomy("l2").longPoint(2L));
        indexService.postDocument(new IndexRecord.NoTaxonomy("f3").floatPoint(3f));
        indexService.postDocument(new IndexRecord.NoTaxonomy("d4").doublePoint(4d));
    }

    @Test
    public void intExact() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new ExactInteger("intPoint", 1)).returnedField("*").build());
        Assert.assertEquals("i1", result.getDocuments().get(0).record.id);
    }

    @Test
    public void intRange() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new IntegerRange("intPoint", 0, 2)).returnedField("*").build());
        Assert.assertEquals("i1", result.getDocuments().get(0).record.id);
    }

    @Test
    public void intMultiRange() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new IntegerMultiRange("intPoint", 0, 2)).returnedField("*").build());
        Assert.assertEquals("i1", result.getDocuments().get(0).record.id);
    }

    @Test
    public void intSet() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result =
            indexService.searchQuery(QueryDefinition.of(new IntegerSet("intPoint", 1)).returnedField("*").build());
        Assert.assertEquals("i1", result.getDocuments().get(0).record.id);
    }

    @Test
    public void longExact() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new ExactLong("longPoint", 2)).returnedField("*").build());
        Assert.assertEquals("l2", result.getDocuments().get(0).record.id);
    }

    @Test
    public void longSet() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new LongSet("longPoint", 2)).returnedField("*").build());
        Assert.assertEquals("l2", result.getDocuments().get(0).record.id);
    }

    @Test
    public void longRange() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new LongRange("longPoint", 1L, 3L)).returnedField("*").build());
        Assert.assertEquals("l2", result.getDocuments().get(0).record.id);
    }

    @Test
    public void longMultiRange() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new LongMultiRange("longPoint", 1, 3)).returnedField("*").build());
        Assert.assertEquals("l2", result.getDocuments().get(0).record.id);
    }

    @Test
    public void floatExact() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new ExactFloat("floatPoint", 3F)).returnedField("*").build());
        Assert.assertEquals("f3", result.getDocuments().get(0).record.id);
    }

    @Test
    public void floatSet() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new FloatSet("floatPoint", 3)).returnedField("*").build());
        Assert.assertEquals("f3", result.getDocuments().get(0).record.id);
    }

    @Test
    public void floatRange() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new FloatRange("floatPoint", 2F, 4F)).returnedField("*").build());
        Assert.assertEquals("f3", result.getDocuments().get(0).record.id);
    }

    @Test
    public void floatMultiRange() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new FloatMultiRange("floatPoint", 2F, 4F)).returnedField("*").build());
        Assert.assertEquals("f3", result.getDocuments().get(0).record.id);
    }

    @Test
    public void doubleExact() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new ExactDouble("doublePoint", 4D)).returnedField("*").build());
        Assert.assertEquals("d4", result.getDocuments().get(0).record.id);
    }

    @Test
    public void doubleSet() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new DoubleSet("doublePoint", 4)).returnedField("*").build());
        Assert.assertEquals("d4", result.getDocuments().get(0).record.id);
    }

    @Test
    public void doubleRange() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new DoubleRange("doublePoint", 3D, 5D)).returnedField("*").build());
        Assert.assertEquals("d4", result.getDocuments().get(0).record.id);
    }

    @Test
    public void doubleMultiRange() {
        ResultDefinition.WithObject<IndexRecord.NoTaxonomy> result = indexService.searchQuery(
            QueryDefinition.of(new DoubleMultiRange("doublePoint", 3D, 5D)).returnedField("*").build());
        Assert.assertEquals("d4", result.getDocuments().get(0).record.id);
    }
}
