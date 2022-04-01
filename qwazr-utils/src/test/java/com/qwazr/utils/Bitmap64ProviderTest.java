/*
 * Copyright 2016-2020 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils;

import org.junit.Test;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class Bitmap64ProviderTest {

    @Test
    public void testIsReadable() {
        final Bitmap64Provider provider = new Bitmap64Provider();
        assertTrue(provider.isReadable(Roaring64NavigableMap.class, null, null, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        assertFalse(provider.isReadable(String.class, null, null, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        assertFalse(provider.isReadable(Roaring64NavigableMap.class, null, null, MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    public void testIsWritable() {
        final Bitmap64Provider provider = new Bitmap64Provider();
        assertTrue(provider.isWriteable(Roaring64NavigableMap.class, null, null, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        assertFalse(provider.isWriteable(String.class, null, null, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        assertFalse(provider.isWriteable(Roaring64NavigableMap.class, null, null, MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    public void testReadWrite() throws IOException {
        final Roaring64NavigableMap bitmap = Bitmap64PartitionerTest.getRandom(1000);
        final Bitmap64Provider provider = new Bitmap64Provider();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (output) {
            provider.writeTo(bitmap, null, null, null, null, null, output);
        }
        final Roaring64NavigableMap result;
        try (final ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray())) {
            result = provider.readFrom(null, null, null, null, null, input);
        }
        assertThat(bitmap, equalTo(result));
    }
}
