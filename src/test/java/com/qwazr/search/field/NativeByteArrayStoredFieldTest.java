package com.qwazr.search.field;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.IndexField;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.index.ResultDocumentObject;
import com.qwazr.search.query.MatchAllDocsQuery;
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
    public void shouldLoadNativeByteArray() throws URISyntaxException, IOException, InterruptedException {
        AnnotatedIndexService<NativeByteArrayRecord> indexService;
        indexService = initIndexService(NativeByteArrayRecord.class);
        indexService.postDocument(new NativeByteArrayRecord("1", new byte[]{109, 97, 114, 116, 105, 110, 101}));
        ResultDefinition.WithObject<NativeByteArrayRecord> result = indexService.searchQuery(QueryDefinition.of(new MatchAllDocsQuery())
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

    @Index(schema = "TestQueries", name = "NativeByteArrayRecord", useCompoundFile = false)
    public static class NativeByteArrayRecord extends IndexRecord<NativeByteArrayRecord> {

        @IndexField(name = "data", template = FieldDefinition.Template.StoredField, stored = true)
        byte[] data;

        public NativeByteArrayRecord() {
        }

        public NativeByteArrayRecord(final String id, final byte[] data) {
            super(id);
            this.data = data;
        }

    }

}
