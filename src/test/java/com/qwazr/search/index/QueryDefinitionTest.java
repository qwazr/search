/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import com.qwazr.utils.ObjectMappers;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QueryDefinitionTest {

	private void checkQuery(Path path) {
		try {
			final QueryDefinition query1 = ObjectMappers.JSON.readValue(path.toFile(), QueryDefinition.class);
			final File smiFile = Files.createTempFile("querydef", ".smi").toFile();
			ObjectMappers.SMILE.writeValue(smiFile, query1);
			final QueryDefinition query2 = ObjectMappers.SMILE.readValue(smiFile, QueryDefinition.class);
			//if (!query1.equals(query2))
			//	query1.equals(query2);
			Assert.assertEquals(query1, query2);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void checkQueries() throws IOException {
		Files.list(Paths.get("src", "test", "resources", "com", "qwazr", "search", "test"))
				.filter(p -> p.getFileName().toString().matches("^query_.*\\.json$"))
				.forEach(this::checkQuery);
	}

}
