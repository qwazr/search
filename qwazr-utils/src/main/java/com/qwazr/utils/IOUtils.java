/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IOUtils extends org.apache.commons.io.IOUtils {

    private final static Logger logger = LoggerUtils.getLogger(IOUtils.class);

    public static void closeQuietly(final AutoCloseable autoCloseable) {
        if (autoCloseable == null)
            return;
        try {
            autoCloseable.close();
        } catch (Exception e) {
            logger.log(Level.WARNING, e, () -> "Close failure on " + autoCloseable);
        }
    }

    public static void closeQuietly(final Closeable closeable) {
        closeQuietly((AutoCloseable) closeable);
    }

    public static void closeQuietly(final AutoCloseable... autoCloseables) {
        if (autoCloseables == null)
            return;
        for (AutoCloseable autoCloseable : autoCloseables)
            closeQuietly(autoCloseable);
    }

    public static void closeQuietly(final Closeable... closeables) {
        if (closeables == null)
            return;
        for (Closeable closeable : closeables)
            closeQuietly((AutoCloseable) closeable);
    }

    public static void closeQuietly(final Collection<? extends AutoCloseable> autoCloseables) {
        if (autoCloseables == null)
            return;
        autoCloseables.forEach(IOUtils::closeQuietly);
    }

    public static void closeObjects(final Collection<?> objects) {
        if (objects == null)
            return;
        objects.forEach(object -> {
            if (object instanceof AutoCloseable)
                closeQuietly((AutoCloseable) object);
        });
    }

    public static int copy(final InputStream inputStream, final Path destFile) throws IOException {
        try (final OutputStream out = Files.newOutputStream(destFile);
             final BufferedOutputStream bOut = new BufferedOutputStream(out);) {
            return copy(inputStream, bOut);
        }
    }

    public static StringBuilder copy(final InputStream inputStream, StringBuilder sb, final Charset charset,
                                     boolean bCloseInputStream) throws IOException {
        if (inputStream == null)
            return sb;
        if (sb == null)
            sb = new StringBuilder();
        byte[] buffer = new byte[16384];
        int length;
        while ((length = inputStream.read(buffer)) != -1)
            sb.append(new String(buffer, 0, length, charset));
        if (bCloseInputStream)
            inputStream.close();
        return sb;
    }

    public static void appendLines(File file, String... lines) throws IOException {
        try (final FileWriter fw = new FileWriter(file, true)) {
            try (final PrintWriter pw = new PrintWriter(fw)) {
                for (String line : lines)
                    pw.println(line);
            }
        }
    }

    public static void readLines(final InputStream input, final Charset charset, final Consumer<String> lineConsumer)
            throws IOException {
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, charset == null ? Charset.defaultCharset() : charset))) {
            String line;
            while ((line = reader.readLine()) != null)
                lineConsumer.accept(line);
        }
    }

    public interface CloseableContext extends Closeable {

        <T extends AutoCloseable> T add(T autoCloseable);

        void close(AutoCloseable autoCloseable);
    }

    public static class CloseableList implements CloseableContext {

        private final LinkedHashSet<AutoCloseable> autoCloseables;

        public CloseableList() {
            autoCloseables = new LinkedHashSet<>();
        }

        @Override
        public <T extends AutoCloseable> T add(T autoCloseable) {
            synchronized (autoCloseables) {
                autoCloseables.add(autoCloseable);
                return autoCloseable;
            }
        }

        @Override
        public void close(AutoCloseable autoCloseable) {
            IOUtils.closeQuietly(autoCloseable);
            synchronized (autoCloseables) {
                autoCloseables.remove(autoCloseable);
            }
        }

        @Override
        public void close() {
            synchronized (autoCloseables) {
                IOUtils.closeQuietly(autoCloseables);
                autoCloseables.clear();
            }
        }

    }

    /**
     * Extract the content of a file to a string
     *
     * @param path    the path to a regular file
     * @param charset the charset to use
     * @return the content of the file as a string
     * @throws IOException if any I/O error occured
     */
    public static String readPathAsString(final Path path, final Charset charset) throws IOException {
        try (final BufferedReader reader = Files.newBufferedReader(path, charset)) {
            return toString(reader);
        }
    }

    /**
     * Extract the content of a file to a string
     *
     * @param file    the file
     * @param charset the charset to use (default charset if null)
     * @return the content of the file as a string
     * @throws IOException if any I/O error occured
     */
    public static String readFileAsString(final File file, final Charset charset) throws IOException {
        return readPathAsString(Objects.requireNonNull(file, "The file is null").toPath(), charset);
    }

    /**
     * Extract the content of a file to a string using default charset
     *
     * @param file the file
     * @return the content of the file as a string
     * @throws IOException if any I/O error occured
     */
    public static String readFileAsString(final File file) throws IOException {
        return readFileAsString(file, Charset.defaultCharset());
    }

    /**
     * Write the string to a path
     *
     * @param content the text to write
     * @param charset the charset to use
     * @param path    the destination file
     * @throws IOException if any I/O error occured
     */
    public static void writeStringToPath(final String content, final Charset charset, final Path path)
            throws IOException {
        try (final BufferedWriter writer = Files.newBufferedWriter(path,
                Objects.requireNonNull(charset, "The charset is missing"))) {
            writer.write(content);
        }
    }

    /**
     * Write the string to a file
     *
     * @param content the text to write
     * @param charset the charset to use
     * @param file    the destination file
     * @throws IOException if any I/O error occured
     */
    public static void writeStringToFile(final String content, final Charset charset, final File file)
            throws IOException {
        writeStringToPath(content, charset, Objects.requireNonNull(file, "The file is missing").toPath());
    }

    /**
     * Write the string to a file using the default charset
     *
     * @param content the text to write
     * @param file    the destination file
     * @throws IOException if any I/O error occured
     */
    public static void writeStringToFile(final String content, final File file) throws IOException {
        writeStringToFile(content, Charset.defaultCharset(), file);
    }

}
