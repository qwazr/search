/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.server;

import org.apache.commons.fileupload.RequestContext;

import java.io.InputStream;

public class StreamRequestContext implements RequestContext {

    private final String contentType;
    private final int contentLength;
    private final String encoding;
    private final InputStream inputStream;

    public StreamRequestContext(final String contentType, final long contentLength, final String encoding,
            final InputStream inputStream) {
        this.contentType = contentType;
        this.contentLength = (int) contentLength;
        this.encoding = encoding;
        this.inputStream = inputStream;
    }

    @Override
    public String getCharacterEncoding() {
        return encoding;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    @Deprecated
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }
}
