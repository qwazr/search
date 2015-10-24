/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.test;

import com.google.common.io.Files;
import com.qwazr.search.SearchServer;
import com.qwazr.search.index.*;
import com.qwazr.utils.IOUtils;
import org.apache.commons.cli.ParseException;
import org.junit.*;
import org.junit.runners.MethodSorters;

import javax.servlet.ServletException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FullTest {

	private static volatile boolean started;

	public static final String BASE_URL = "http://localhost:9091";
	public static final String SCHEMA_NAME = "schema-test";
	public static final String INDEX_NAME = "index-test";
	public static final String FIELDS_JSON = "fields.json";
	public static final String MATCH_ALL_QUERY = "query_match_all.json";

	@Before
	public void startServer() throws IOException, ParseException, ServletException, IllegalAccessException,
					InstantiationException {
		if (started)
			return;
		// start the server
		File dataDir = Files.createTempDir();
		SearchServer.main(new String[] { "-d", dataDir.getAbsolutePath() });
		started = true;
	}

	private IndexServiceInterface getClient() throws URISyntaxException {
		return new IndexSingleClient(BASE_URL, 60000);
	}

	@Test
	public void test000CreateSchema() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		Response response = client.createUpdateSchema(SCHEMA_NAME, null);
		Assert.assertNotNull(response);
		Assert.assertEquals(201, response.getStatusInfo().getStatusCode());
	}

	@Test
	public void test001UpdateSchema() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		Response response = client.createUpdateSchema(SCHEMA_NAME, null);
		Assert.assertNotNull(response);
		Assert.assertEquals(202, response.getStatusInfo().getStatusCode());
	}

	@Test
	public void test002ListSchema() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		Set<String> schemas = client.getSchemas(null);
		Assert.assertNotNull(schemas);
		Assert.assertEquals(schemas.size(), 1);
		Assert.assertTrue(schemas.contains(SCHEMA_NAME));
	}

	private Map<String, FieldDefinition> getFieldMap() throws IOException {
		InputStream is = this.getClass().getResourceAsStream(FIELDS_JSON);
		try {
			return FieldDefinition.newFieldMap(IOUtils.toString(is));
		} finally {
			IOUtils.close(is);
		}
	}

	@Test
	public void test100CreateIndex() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		Map<String, FieldDefinition> fieldMap = getFieldMap();
		IndexStatus indexStatus = client.createUpdateIndex(SCHEMA_NAME, INDEX_NAME, null, fieldMap);
		Assert.assertNotNull(indexStatus);
		Assert.assertNotNull(indexStatus.fields);
		Assert.assertEquals(fieldMap.size(), indexStatus.fields.size());
	}

	private QueryDefinition getQuery(String res) throws IOException {
		InputStream is = this.getClass().getResourceAsStream(res);
		try {
			return QueryDefinition.newQuery(IOUtils.toString(is));
		} finally {
			IOUtils.close(is);
		}
	}

	@Test
	public void test110SearchSchema() throws URISyntaxException, IOException {
		IndexServiceInterface client = getClient();
		ResultDefinition result = client.searchQuery(SCHEMA_NAME, getQuery(MATCH_ALL_QUERY), null);
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.total_hits);
		Assert.assertEquals(result.total_hits, 0);
	}

	@Ignore
	public void test980DeleteIndex() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		Response response = client.deleteIndex(SCHEMA_NAME, INDEX_NAME, null);
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatusInfo().getStatusCode());
	}

	@Ignore
	public void test990DeleteSchema() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		Response response = client.deleteSchema(SCHEMA_NAME, null);
		Assert.assertNotNull(response);
		Assert.assertEquals(200, response.getStatusInfo().getStatusCode());
	}

	@Ignore
	public void test991EmptySchema() throws URISyntaxException {
		IndexServiceInterface client = getClient();
		Set<String> schemas = client.getSchemas(null);
		Assert.assertNotNull(schemas);
		Assert.assertEquals(schemas.size(), 0);
		Assert.assertFalse(schemas.contains(SCHEMA_NAME));
	}
}
