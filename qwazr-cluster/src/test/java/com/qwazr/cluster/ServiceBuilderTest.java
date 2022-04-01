/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.cluster;

import com.qwazr.server.RemoteService;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.Collection;

public class ServiceBuilderTest {

	static ClusterServiceBuilder builder;

	@BeforeClass
	public static void before() {
		builder = new ClusterServiceBuilder(null);
	}

	@Test
	public void local() {
		Assert.assertNull(builder.local());
	}

	@Test
	public void remote() throws URISyntaxException {
		final ClusterSingleClient client =
				(ClusterSingleClient) builder.remote(RemoteService.of("http://localhost:9091").build());
		Assert.assertNotNull(client);
		Assert.assertEquals("http://localhost:9091", client.serverAddress);
	}

	@Test
	public void remotes() {
		try {
			builder.remotes();
			Assert.fail("NotImplementedException not thrown");
		} catch (NotImplementedException e) {
			//OK
		}
	}

	@Test(expected = NotImplementedException.class)
	public void defaultLocal() {
		new TestService().local();
	}

	@Test(expected = NotImplementedException.class)
	public void defaultRemote() {
		new TestService().remote(null);
	}

	class TestService implements ServiceBuilderInterface {

		@Override
		public Object getService(String node) throws URISyntaxException {
			return null;
		}

		@Override
		public Object getActive(String group) throws URISyntaxException {
			return null;
		}

		@Override
		public Object getRandom(String group) throws URISyntaxException {
			return null;
		}

		@Override
		public Object getLeader(String group) throws URISyntaxException {
			return null;
		}

		@Override
		public Object getService(Collection nodes) throws URISyntaxException {
			return null;
		}
	}
}
