/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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
package com.qwazr.server.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.server.RemoteService;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;

public class JsonClientTest extends JsonClient {

	final WebTarget target;

	public JsonClientTest() throws URISyntaxException {
		super(RemoteService.of("https://jsonplaceholder.typicode.com:443").build());
		target = client.target(remote.serviceAddress);
	}

	public JsonTest get() {
		return target.path("/posts/1").request(MediaType.APPLICATION_JSON).get(JsonTest.class);
	}

	@Test
	public void test() throws URISyntaxException {

		final JsonClientTest jsonClient = new JsonClientTest();
		final JsonTest jsonTest = jsonClient.get();
		Assert.assertNotNull(jsonTest);
		Assert.assertEquals(Integer.valueOf(1), jsonTest.userId);
		Assert.assertEquals("sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
				jsonTest.title);

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class JsonTest {

		@JsonProperty("userId")
		final Integer userId;

		@JsonProperty("title")
		final String title;

		@JsonCreator
		public JsonTest(@JsonProperty("userId") Integer userId, @JsonProperty("title") String title) {
			this.userId = userId;
			this.title = title;
		}
	}
}
