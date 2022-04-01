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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ObjectMappersTest {

	final static Path DIRECTORY = Paths.get("src/test/resources/com/qwazr/utils/json");
	final static MapperTest DEFAULT_TEST = new MapperTest();

	@Test
	public void loadYamlOrJson() throws IOException {

		ObjectMappers mappers =
				ObjectMappers.of().add("yaml", ObjectMappers.YAML).add("json", ObjectMappers.JSON).build();

		final MapperTest test = mappers.readFileValue(DIRECTORY, "test", DEFAULT_TEST, MapperTest.class);
		Assert.assertNotNull(test);
		Assert.assertNotEquals(test, DEFAULT_TEST);
		Assert.assertEquals("yaml", test.test);

		Assert.assertEquals(DEFAULT_TEST, mappers.readFileValue(DIRECTORY, "no-file", DEFAULT_TEST, MapperTest.class));
	}

	@Test
	public void loadJsonOrYaml() throws IOException {

		ObjectMappers mappers =
				ObjectMappers.of().add("json", ObjectMappers.JSON).add("yaml", ObjectMappers.YAML).build();

		final MapperTest test = mappers.readFileValue(DIRECTORY, "test", DEFAULT_TEST, MapperTest.class);
		Assert.assertNotNull(test);
		Assert.assertNotEquals(test, DEFAULT_TEST);
		Assert.assertEquals("json", test.test);

		Assert.assertEquals(DEFAULT_TEST, mappers.readFileValue(DIRECTORY, "no-file", DEFAULT_TEST, MapperTest.class));
	}

	@Test
	public void loadXmlOrYaml() throws IOException {

		ObjectMappers mappers =
				ObjectMappers.of().add("xml", ObjectMappers.XML).add("yaml", ObjectMappers.YAML).build();

		final MapperTest test = mappers.readFileValue(DIRECTORY, "test", DEFAULT_TEST, MapperTest.class);
		Assert.assertNotNull(test);
		Assert.assertNotEquals(test, DEFAULT_TEST);
		Assert.assertEquals("xml", test.test);

		Assert.assertEquals(DEFAULT_TEST, mappers.readFileValue(DIRECTORY, "no-file", DEFAULT_TEST, MapperTest.class));
	}

	public static class MapperTest {

		public final String test = null;
	}
}
