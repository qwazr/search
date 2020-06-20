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
import com.qwazr.search.query.MatchAllDocsQuery;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.search.test.units.IndexRecord;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class ByteArrayStoredFieldTest extends AbstractIndexTest.WithIndexRecord<ByteArrayStoredFieldTest.ByteArrayRecord> {

    public ByteArrayStoredFieldTest() {
        super(ByteArrayRecord.class);
    }

    @Test
    public void shouldLoadByteArray() throws URISyntaxException, IOException {
        AnnotatedIndexService<ByteArrayRecord> indexService;
        indexService = initIndexService(ByteArrayRecord.class);
        indexService.postDocument(new ByteArrayRecord("1", new byte[]{109, 97, 114, 116, 105, 110, 101}));
        ResultDefinition.WithObject<ByteArrayRecord> result = indexService.searchQuery(QueryDefinition.of(new MatchAllDocsQuery())
            .returnedField("*")
            .build());
        checkResult(result, 1L);
        Byte[] loadedData = result.documents.get(0).record.data;
        Assert.assertEquals(loadedData[0].byteValue(), 109);
        Assert.assertEquals(loadedData[1].byteValue(), 97);
        Assert.assertEquals(loadedData[2].byteValue(), 114);
        Assert.assertEquals(loadedData[3].byteValue(), 116);
        Assert.assertEquals(loadedData[4].byteValue(), 105);
        Assert.assertEquals(loadedData[5].byteValue(), 110);
        Assert.assertEquals(loadedData[6].byteValue(), 101);
    }

    @Index(name = "ByteArrayRecord", useCompoundFile = false)
    public static class ByteArrayRecord extends IndexRecord<ByteArrayRecord> {

        @IndexField(name = "data", template = FieldDefinition.Template.StoredField, stored = true)
        Byte[] data;

        public ByteArrayRecord() {
        }

        @Override
        protected ByteArrayRecord me() {
            return this;
        }

        public ByteArrayRecord(final String id, final byte[] data) {
            super(id);
            this.data = new Byte[data.length];
            for (int i = 0; i != data.length; ++i) {
                this.data[i] = data[i];
            }
        }

    }

}
