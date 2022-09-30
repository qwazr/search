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

import com.qwazr.utils.HashUtils;
import com.qwazr.utils.SystemUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nonapi.io.github.classgraph.utils.FileUtils;
import org.apache.commons.io.IOUtils;

public class StaticFileServlet extends HttpServlet {

    private final MimetypesFileTypeMap mimeTypeMap;
    private final Path staticPath;
    private final int expirationSecTime;

    public StaticFileServlet(final MimetypesFileTypeMap mimeTypeMap, final Path staticPath,
                             final int expirationSecTime) {
        this.mimeTypeMap = mimeTypeMap;
        this.expirationSecTime = expirationSecTime;
        if (staticPath == null)
            throw new ServerException("The path is empty");
        if (!Files.exists(staticPath))
            throw new ServerException("Cannot initialize the static path: " + staticPath.toAbsolutePath() +
                    " - The path does not exists.");
        this.staticPath = staticPath;
    }

    private File handleFile(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String contextPath = request.getContextPath();
        final String servletPath = request.getServletPath();
        final String pathInfo = request.getPathInfo();
        final Path staticFile;
        final String fullPath;
        if (pathInfo == null) {
            staticFile = staticPath;
            fullPath = contextPath + servletPath;
        } else {
            final String securePathInfo = FileUtils.sanitizeEntryPath(pathInfo, false, false);
            staticFile = staticPath.resolve(securePathInfo.startsWith("/") ? securePathInfo.substring(1) : securePathInfo);
            fullPath = contextPath + servletPath + pathInfo;
        }
        if (Files.isDirectory(staticFile)) {
            if (Files.exists(staticFile.resolve("index.html"))) {
                final boolean slashEnd = fullPath.endsWith("/");
                response.sendRedirect(fullPath + (slashEnd ? "index.html" : "/index.html"));
            }
            return null;
        }
        if (!Files.exists(staticFile) || !Files.isRegularFile(staticFile)) {
            response.sendError(404, "File not found: " + fullPath);
            return null;
        }
        return staticFile.toFile();
    }

    static void head(final String fileName, final Long length, final String mimeType, final Long lastModified,
                     final long expirationSecTime, final HttpServletResponse response) {
        if (mimeType != null) {
            final String contentType;
            if (SystemUtils.FILE_ENCODING != null && mimeType.startsWith("text/")) {
                contentType = mimeType + "; charset=" + SystemUtils.FILE_ENCODING;
            } else
                contentType = mimeType;
            response.setContentType(contentType);
        }
        if (length != null)
            response.setContentLengthLong(length);
        if (lastModified != null) {
            response.setDateHeader("Last-Modified", lastModified);
            if (fileName != null)
                response.setHeader("ETag",
                        HashUtils.getMurmur3Hash32Hex(fileName) + '-' + Long.toHexString(lastModified));
        }
        response.setHeader("Cache-Control", "max-age=" + expirationSecTime);
        response.setDateHeader("Expires", System.currentTimeMillis() + expirationSecTime * 1000);
    }

    @Override
    protected void doHead(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final File staticFile = handleFile(request, response);
        if (staticFile == null)
            return;
        final String type = mimeTypeMap.getContentType(staticFile);
        head(staticFile.toString(), staticFile.length(), type, staticFile.lastModified(), expirationSecTime, response);
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final File staticFile = handleFile(request, response);
        if (staticFile == null)
            return;
        final String type = mimeTypeMap.getContentType(staticFile);
        head(staticFile.toString(), staticFile.length(), type, staticFile.lastModified(), expirationSecTime, response);
        try (final FileInputStream fis = new FileInputStream(staticFile)) {
            final ServletOutputStream out = response.getOutputStream();
            IOUtils.copy(fis, out);
            out.flush();
        }
    }
}
