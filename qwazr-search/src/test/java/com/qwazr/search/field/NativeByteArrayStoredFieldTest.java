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
package com.qwazr.search.field;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.MatchAllDocs;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import org.junit.Test;
import org.junit.Assert;

import java.io.IOException;
import java.net.URISyntaxException;

public class NativeByteArrayStoredFieldTest extends AbstractIndexTest.WithIndexRecord<NativeByteArrayStoredFieldTest.NativeByteArrayRecord> {

    public NativeByteArrayStoredFieldTest() {
        super(NativeByteArrayRecord.class);
    }

    @Test
    public void shouldLoadNativeByteArray() throws URISyntaxException, IOException {
        AnnotatedIndexService<NativeByteArrayRecord> indexService;
        indexService = initIndexService(NativeByteArrayRecord.class);
        indexService.postDocument(new NativeByteArrayRecord("1", new byte[]{109, 97, 114, 116, 105, 110, 101}));
        ResultDefinition.WithObject<NativeByteArrayRecord> result = indexService.searchQuery(
            QueryDefinition.of(MatchAllDocs.INSTANCE)
                .returnedField("*")
                .build());
        checkResult(result, 1L);
        byte[] loadedData = result.documents.get(0).record.data;
        Assert.assertEquals(loadedData[0], 109);
        Assert.assertEquals(loadedData[1], 97);
        Assert.assertEquals(loadedData[2], 114);
        Assert.assertEquals(loadedData[3], 116);
        Assert.assertEquals(loadedData[4], 105);
        Assert.assertEquals(loadedData[5], 110);
        Assert.assertEquals(loadedData[6], 101);
    }

    @Index(name = "NativeByteArrayRecord", useCompoundFile = false)
    public static class NativeByteArrayRecord extends IndexRecord<NativeByteArrayRecord> {

        @IndexField(name = "data", template = FieldDefinition.Template.StoredField, stored = true)
        byte[] data;

        public NativeByteArrayRecord() {
        }

        @Override
        protected NativeByteArrayRecord me() {
            return this;
        }

        public NativeByteArrayRecord(final String id, final byte[] data) {
            super(id);
            this.data = data;
        }

    }

}
