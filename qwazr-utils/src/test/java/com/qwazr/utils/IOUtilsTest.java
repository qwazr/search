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
package com.qwazr.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IOUtilsTest {

	@Test
	public void closeQuietlyArray() {
		final CloseableObject c1 = new CloseableObject();
		final CloseableObject c2 = new CloseableObject();

		IOUtils.closeQuietly(c1, c2);

		Assert.assertFalse(c1.open);
		Assert.assertFalse(c2.open);
	}

	@Test
	public void closeQuietlyCollection() {
		final List<CloseableObject> closeables = Arrays.asList(new CloseableObject(), new CloseableObject());
		IOUtils.closeQuietly(closeables);
		closeables.forEach(c -> Assert.assertFalse(c.open));
	}

	class CloseableObject implements Closeable {

		boolean open = true;

		@Override
		public void close() throws IOException {
			open = false;
		}
	}

	@Test
	public void readLines() throws IOException {
		final List<Object> lines = Arrays.asList("line1", "line2", "line3");
		final File file = Files.createTempFile("ioutils", "txt").toFile();
		try (final FileWriter writer = new FileWriter(file)) {
			IOUtils.writeLines(lines, null, writer);
		}
		final List<String> readLines = new ArrayList<>();
		try (final InputStream input = new FileInputStream(file)) {
			IOUtils.readLines(input, null, readLines::add);
		}
		Assert.assertArrayEquals(lines.toArray(), readLines.toArray());
	}
}
