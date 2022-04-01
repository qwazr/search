/*
 * Copyright 2016 Emmanuel Keller / QWAZR
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

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FileUtils extends org.apache.commons.io.FileUtils {

    public static File createDirectory(final File directoryFile) throws IOException {
        Objects.requireNonNull(directoryFile, "directoryFile is null");
        if (directoryFile.exists()) {
            if (!directoryFile.isDirectory())
                throw new IOException("Not a directory: " + directoryFile);
            return directoryFile;
        }
        if (!directoryFile.mkdir())
            throw new IOException("Can't create the directory");
        return directoryFile;
    }

    /**
     * Delete a directory and its content
     *
     * @param directoryPath the path of the directory
     * @param preservePath  a list of path to preserve
     * @return the number of item deleted
     * @throws IOException if any I/O error occurs
     */
    public static int deleteDirectory(final Path directoryPath, final Path... preservePath) throws IOException {

        final AtomicInteger counter = new AtomicInteger();
        final HashSet<Path> preservePathSet;
        if (preservePath != null && preservePath.length > 0) {
            preservePathSet = new HashSet<>();
            Collections.addAll(preservePathSet, preservePath);
        } else
            preservePathSet = null;

        Files.walkFileTree(Objects.requireNonNull(directoryPath), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (preservePathSet == null || !preservePathSet.contains(dir))
                    checkExistsAndEnforceWritable(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (preservePathSet == null || !preservePathSet.contains(file))
                    if (checkExistsAndEnforceWritable(file))
                        if (Files.deleteIfExists(file))
                            counter.incrementAndGet();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (preservePathSet == null || !preservePathSet.contains(dir))
                    if (checkExistsAndEnforceWritable(dir))
                        if (Files.deleteIfExists(dir))
                            counter.incrementAndGet();
                return FileVisitResult.CONTINUE;
            }
        });

        return counter.get();
    }

    /**
     * Check that a file exists and that that it is writable. If not, it will try to make it writable.
     *
     * @param filePath the path to the file
     * @return true if the file exists and is writable.
     * @throws IOException if any I/O error occurs
     */
    public static boolean checkExistsAndEnforceWritable(Path filePath) throws IOException {
        if (!Files.exists(filePath))
            return false;
        if (Files.isWritable(filePath))
            return true;
        final File file = filePath.toFile();
        if (!file.setWritable(true))
            throw new IOException("Cannot set the file writable: " + file);
        return true;
    }

    /**
     * Check if the child Path is child or equals the parent Path
     *
     * @param parent the parent path
     * @param child  the path of the child
     * @return true if the child path is a child of the parent path
     */
    public static boolean isParent(final Path parent, Path child) {
        while (child != null) {
            if (child.equals(parent))
                return true;
            child = child.getParent();
        }
        return false;
    }

    /**
     * Wrapper on Files.list Stream#count call embedded in a autoclose try/resource.
     *
     * @param path
     * @return the number of files
     * @throws IOException
     */
    public static long countFiles(@Nonnull final Path path) throws IOException {
        try (final Stream<Path> files = Files.list(path)) {
            return files.count();
        }
    }

    /**
     * Wrapper on Files.list embedded in a autoclose try/resource.
     *
     * @param path
     * @param pathConsumer
     * @throws IOException
     */
    public static void listFiles(@Nonnull final Path path, @Nonnull final Consumer<Path> pathConsumer) throws IOException {
        try (final Stream<Path> files = Files.list(path)) {
            files.forEach(pathConsumer);
        }
    }

}
