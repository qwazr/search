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

package com.qwazr.search.test;

import com.qwazr.search.index.RemoteIndex;
import org.junit.Assert;
import org.junit.Test;

import java.net.URISyntaxException;

public class RemoteIndexTest {

	@Test
	public void localPattern() throws URISyntaxException {
		final RemoteIndex remoteIndex = RemoteIndex.build("my_schema/my_index");
		Assert.assertNotNull(remoteIndex);
		Assert.assertEquals("my_schema", remoteIndex.schema);
		Assert.assertEquals("my_index", remoteIndex.index);
		Assert.assertEquals("http://localhost:9091", remoteIndex.serverAddress);
		Assert.assertEquals("http://localhost:9091/indexes", remoteIndex.serviceAddress);
	}

	@Test(expected = IllegalArgumentException.class)
	public void wrongLocalPattern() throws URISyntaxException {
		RemoteIndex.build("my_schema");
	}

	@Test
	public void remoteUrl() throws URISyntaxException {
		final RemoteIndex remoteIndex = RemoteIndex.build("http://birdie:1234/indexes/my_schema/my_index");
		Assert.assertNotNull(remoteIndex);
		Assert.assertEquals("my_schema", remoteIndex.schema);
		Assert.assertEquals("my_index", remoteIndex.index);
		Assert.assertEquals("http://birdie:1234", remoteIndex.serverAddress);
		Assert.assertEquals("http://birdie:1234/indexes", remoteIndex.serviceAddress);
	}

	@Test(expected = URISyntaxException.class)
	public void wrongRemoteUrl() throws URISyntaxException {
		RemoteIndex.build("http://birdie:1234/my_schema");
	}

}
