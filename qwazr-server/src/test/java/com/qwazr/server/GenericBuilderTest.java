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
package com.qwazr.server;

import com.qwazr.server.configuration.ServerConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class GenericBuilderTest {

	@Test
	public void test() throws IOException {
		GenericServerBuilder builder = GenericServer.of(ServerConfiguration.of().build());
		Assert.assertNotNull(builder);
		Assert.assertNotNull(builder.getConfiguration());
		Assert.assertNotNull(builder.getConstructorParameters());
		Assert.assertNotNull(builder.getWebAppContext());
		Assert.assertNotNull(builder.getWebServiceContext());
	}
}