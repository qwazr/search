/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

public class Bitmap64Provider implements
        MessageBodyReader<Roaring64NavigableMap>,
        MessageBodyWriter<Roaring64NavigableMap> {

    @Override
    public boolean isReadable(final Class<?> type, Type genericType,
                              final Annotation[] annotations,
                              final MediaType mediaType) {
        return type == Roaring64NavigableMap.class && mediaType.isCompatible(MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    @Override
    public Roaring64NavigableMap readFrom(final Class<Roaring64NavigableMap> type,
                                          final Type genericType,
                                          final Annotation[] annotations,
                                          final MediaType mediaType,
                                          final MultivaluedMap<String, String> httpHeaders,
                                          final InputStream entityStream) throws IOException, WebApplicationException {
        final Roaring64NavigableMap bitmap = new Roaring64NavigableMap();
        try (final DataInputStream input = new DataInputStream(new ByteArrayInputStream(entityStream.readAllBytes()))) {
            bitmap.deserialize(input);
        }
        return bitmap;
    }

    @Override
    public boolean isWriteable(final Class<?> type,
                               final Type genericType,
                               final Annotation[] annotations,
                               final MediaType mediaType) {
        return type == Roaring64NavigableMap.class && mediaType.isCompatible(MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    @Override
    public void writeTo(final Roaring64NavigableMap bitmap,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {
        try (final ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream()) {
            try (final DataOutputStream dataOutput = new DataOutputStream(bytesOutput)) {
                bitmap.serialize(dataOutput);
            }
            entityStream.write(bytesOutput.toByteArray());
        }
    }
}
