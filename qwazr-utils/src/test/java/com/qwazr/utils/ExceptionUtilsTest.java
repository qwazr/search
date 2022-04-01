/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils;

import com.google.common.util.concurrent.AtomicDouble;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExceptionUtilsTest {

    @Test
    public void booleanBypassTest() throws IOException {
        final Path homeDir = FileUtils.getUserDirectory().toPath();
        try (final Stream<Path> stream = Files.list(homeDir)) {
            Assert.assertNotNull(stream
                    .filter(p -> Files.isDirectory(p))
                    .filter(p -> ExceptionUtils.bypass(() -> Files.isHidden(p)))
                    .collect(Collectors.toSet()));
        }
    }

    boolean booleanEx() throws IOException {
        throw new IOException("test");
    }

    @Test(expected = RuntimeException.class)
    public void booleanBypassExTest() {
        ExceptionUtils.bypass(this::booleanEx);
    }

    @Test
    public void longBypassTest() throws IOException {
        final Path homeDir = FileUtils.getUserDirectory().toPath();
        final AtomicLong totalSize = new AtomicLong();
        try (final Stream<Path> stream = Files.list(homeDir)) {
            stream.filter(p -> Files.isRegularFile(p))
                    .forEach(p -> totalSize.addAndGet(ExceptionUtils.bypass(() -> Files.size(p))));
        }
    }

    long longEx() throws IOException {
        throw new IOException("test");
    }

    @Test(expected = RuntimeException.class)
    public void longBypassExTest() {
        ExceptionUtils.bypass(this::longEx);
    }

    @Test
    public void intBypassTest() throws IOException {
        final Path homeDir = FileUtils.getUserDirectory().toPath();
        final AtomicInteger totalSize = new AtomicInteger();
        try (final Stream<Path> stream = Files.list(homeDir)) {
            stream.filter(p -> Files.isRegularFile(p))
                    .forEach(p -> totalSize.addAndGet(ExceptionUtils.bypass(() -> (int) Files.size(p))));
        }
    }

    int intEx() throws IOException {
        throw new IOException("test");
    }

    @Test(expected = RuntimeException.class)
    public void intBypassExTest() {
        ExceptionUtils.bypass(this::intEx);
    }

    @Test
    public void doubleBypassTest() throws IOException {
        final Path homeDir = FileUtils.getUserDirectory().toPath();
        final AtomicDouble totalSize = new AtomicDouble();
        try (final Stream<Path> stream = Files.list(homeDir)) {
            stream.filter(p -> Files.isRegularFile(p))
                    .forEach(p -> totalSize.addAndGet(ExceptionUtils.bypass(() -> (double) Files.size(p))));
        }
    }

    double doubleEx() throws IOException {
        throw new IOException("test");
    }

    @Test(expected = RuntimeException.class)
    public void doubleBypassExTest() {
        ExceptionUtils.bypass(this::doubleEx);
    }

    @Test
    public void floatBypassTest() throws IOException {
        final Path homeDir = FileUtils.getUserDirectory().toPath();
        final AtomicDouble totalSize = new AtomicDouble();
        try (final Stream<Path> stream = Files.list(homeDir)) {
            stream.filter(p -> Files.isRegularFile(p))
                    .forEach(p -> totalSize.addAndGet(ExceptionUtils.bypass(() -> (float) Files.size(p))));
        }
    }

    float floatEx() throws IOException {
        throw new IOException("test");
    }

    @Test(expected = RuntimeException.class)
    public void floatBypassExTest() {
        ExceptionUtils.bypass(this::floatEx);
    }

    @Test
    public void objectBypassTest() throws IOException {
        final Path homeDir = FileUtils.getUserDirectory().toPath();
        final Map<Path, FileTime> fileTimes = new HashMap<>();
        try (final Stream<Path> stream = Files.list(homeDir)) {
            stream.filter(p -> Files.isRegularFile(p))
                    .forEach(p -> fileTimes.put(p, ExceptionUtils.bypass(() -> Files.getLastModifiedTime(p))));
        }
    }

    Object objectEx() throws IOException {
        throw new IOException("test");
    }

    @Test(expected = RuntimeException.class)
    public void objectBypassExTest() {
        ExceptionUtils.bypass(this::objectEx);
    }
}
