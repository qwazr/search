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

package com.qwazr.utils.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class JsonMapperTest {

	void writeReadTest(ObjectMapper mapper) throws IOException {
		File jsonFile = Files.createTempFile("test", ".json").toFile();
		Item item1 = new Item(RandomUtils.nextInt(), RandomUtils.alphanumeric(10), null);
		mapper.writeValue(jsonFile, item1);
		Item item2 = mapper.readValue(jsonFile, Item.class);
		Assert.assertEquals(item1, item2);
	}

	@Test
	public void jsonMapper() throws IOException {
		writeReadTest(ObjectMappers.JSON);
	}

	@Test
	public void xmlMapper() throws IOException {
		writeReadTest(ObjectMappers.XML);
	}

	@Test
	public void yamlMapper() throws IOException {
		writeReadTest(ObjectMappers.YAML);
	}

	@Test
	public void smileMapper() throws IOException {
		writeReadTest(ObjectMappers.SMILE);
	}

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public static class Item {

		public final Integer id;
		@JsonProperty("full_name")
		public final String fullName;
		@JsonProperty("null_value")
		public final String nullValue;

		@JsonCreator
		Item(@JsonProperty("id") Integer id, @JsonProperty("full_name") String fullName,
				@JsonProperty("null_value") String nullValue) {
			this.id = id;
			this.fullName = fullName;
			this.nullValue = nullValue;
		}

		public String getFullName() {
			return fullName;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Item))
				return false;
			final Item i = (Item) o;
			return Objects.equals(id, i.id) && Objects.equals(fullName, i.fullName) &&
					Objects.equals(nullValue, i.nullValue);
		}
	}

}
