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
package com.qwazr.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

public class StaticResourceServlet extends HttpServlet {

    private final String resourcePrefix;

    private final MimetypesFileTypeMap mimeTypeMap;

    private final int expirationTimeSec;

    private final long lastModified = System.currentTimeMillis();

    public StaticResourceServlet(final String resourcePrefix, final MimetypesFileTypeMap mimeTypeMap,
                                 final int expirationTimeSec) {
        this.resourcePrefix = resourcePrefix;
        this.mimeTypeMap = Objects.requireNonNull(mimeTypeMap, "The mimeTypeMap is missing");
        this.expirationTimeSec = expirationTimeSec;
    }

    private String getResourcePath(final HttpServletRequest request) {
        final String path = request.getPathInfo();
        return path == null ? resourcePrefix : resourcePrefix + path;
    }

    private InputStream findResource(final String resourcePath) throws IOException {
        final InputStream input = StaticResourceServlet.class.getResourceAsStream(resourcePath);
        if (input == null)
            throw new FileNotFoundException("File not found: " + resourcePath);
        return input;
    }

    @Override
    final protected void doHead(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        final String resourcePath = getResourcePath(request);
        try (final InputStream input = findResource(resourcePath)) {
            final String type = mimeTypeMap.getContentType(resourcePath);
            StaticFileServlet.head(resourcePath, null, type, lastModified, expirationTimeSec, response);
        } catch (FileNotFoundException e) {
            response.sendError(404, e.getMessage());
        }
    }

    @Override
    final protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        final String resourcePath = getResourcePath(request);
        try (final InputStream input = findResource(resourcePath)) {
            final String type = mimeTypeMap.getContentType(resourcePath);
            StaticFileServlet.head(resourcePath, null, type, lastModified, expirationTimeSec, response);
            IOUtils.copy(input, response.getOutputStream());
        } catch (FileNotFoundException e) {
            response.sendError(404, e.getMessage());
        }
    }
}
