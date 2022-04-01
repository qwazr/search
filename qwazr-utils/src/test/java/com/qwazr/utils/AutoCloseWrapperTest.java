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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class AutoCloseWrapperTest {

    private final static Logger LOGGER = LoggerUtils.getLogger(AutoCloseWrapperTest.class);

    @Test
    public void tempFileTest() throws IOException {
        final Path tempFile;
        try (final AutoCloseWrapper<Path> a = AutoCloseWrapper.of(
                Files.createTempFile("test", "tmp"), LOGGER, Files::deleteIfExists)) {
            tempFile = a.get();
            Assert.assertNotNull(tempFile);
            Assert.assertTrue(Files.exists(tempFile));
        }
        Assert.assertTrue(Files.notExists(tempFile));
    }

    @Test
    public void tempFileNoExceptionOnCloseTest() throws IOException {
        final Path tempFile;
        try (final AutoCloseWrapper<Path> a = AutoCloseWrapper.of(
                Files.createTempFile("test", "tmp"), LOGGER,
                f -> {
                    throw new Exception();
                })) {
            tempFile = a.get();
            Assert.assertNotNull(tempFile);
            Assert.assertTrue(Files.exists(tempFile));
        }
        Assert.assertTrue(Files.exists(tempFile));
    }
}
