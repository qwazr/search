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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

/**
 * Created by ekeller on 08/07/2017.
 */
public class FileInstanceCacheTest {

	private Path cacheFilePath;

	@Before
	public void setup() throws IOException {
		cacheFilePath = Files.createTempFile("filecache", "test");
		Files.delete(cacheFilePath);
	}

	@After
	public void after() throws IOException {
		if (cacheFilePath != null)
			Files.deleteIfExists(cacheFilePath);
	}

	@Test
	public void fullTest() throws IOException {
		final String instance = RandomUtils.alphanumeric(10);

		final FileInstanceCache<String> cache =
				FileInstanceCache.<String>of(cacheFilePath).reader(IOUtils::readFileAsString)
						.writer(IOUtils::writeStringToFile)
						.build();

		// Read no cache
		Assert.assertFalse(Files.exists(cacheFilePath));
		Assert.assertNull(cache.get());

		// Set the cache and check the value
		Assert.assertFalse(Files.exists(cacheFilePath));
		cache.set(instance);
		Assert.assertTrue(Files.exists(cacheFilePath));
		final FileTime fileTime = Files.getLastModifiedTime(cacheFilePath);
		Assert.assertEquals(instance, IOUtils.readFileAsString(cacheFilePath.toFile()));
		Assert.assertEquals(instance, cache.get());

		// Set the cache with the same value (the file should not be updated)
		Assert.assertEquals(cache, cache.set(instance));
		Assert.assertEquals("Wrong filetime", fileTime, Files.getLastModifiedTime(cacheFilePath));

		// Check we still have the same value
		Assert.assertEquals(instance, cache.get());
		Assert.assertEquals("Wrong filetime", fileTime, Files.getLastModifiedTime(cacheFilePath));

		// Read the existing value with a new cache
		final FileInstanceCache<String> cache2 =
				FileInstanceCache.<String>of(cacheFilePath).reader(IOUtils::readFileAsString)
						.writer(IOUtils::writeStringToFile)
						.build();
		Assert.assertEquals(instance, cache2.get());
		Assert.assertEquals("Wrong filetime", fileTime, Files.getLastModifiedTime(cacheFilePath));
	}

	private FileInstanceCache<String> readOnly() throws IOException {
		final String instance = RandomUtils.alphanumeric(10);
		IOUtils.writeStringToFile(instance, cacheFilePath.toFile());

		final FileInstanceCache<String> cache =
				FileInstanceCache.<String>of(cacheFilePath).reader(IOUtils::readFileAsString).build();
		Assert.assertEquals(instance, cache.get());
		return cache;
	}

	@Test
	public void readOnlyTest() throws IOException {
		readOnly();
	}

	@Test(expected = IOException.class)
	public void readOnlyErrorTest() throws IOException {
		readOnly().set("test");
	}

	private FileInstanceCache<String> writeOnly() throws IOException {
		final String instance = RandomUtils.alphanumeric(10);

		final FileInstanceCache<String> cache =
				FileInstanceCache.<String>of(cacheFilePath).writer(IOUtils::writeStringToFile).build();

		Assert.assertEquals(cache, cache.set(instance));
		Assert.assertEquals(instance, IOUtils.readFileAsString(cacheFilePath.toFile()));
		return cache;
	}

	@Test
	public void writeOnlyTest() throws IOException {
		writeOnly();
	}

	@Test(expected = IOException.class)
	public void writeOnlyErrorTest() throws IOException {
		writeOnly().get();
	}
}
