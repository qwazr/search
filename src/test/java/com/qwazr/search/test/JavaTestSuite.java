/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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
package com.qwazr.search.test;

import com.qwazr.search.index.IndexServiceInterface;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.net.URISyntaxException;

@RunWith(Suite.class)
@Suite.SuiteClasses({JavaTestSuite.JavaLocalTest.class, JavaTestSuite.JavaRemoteTest.class})
public class JavaTestSuite {

	public static class JavaLocalTest extends JavaAbstractTest {

		@Override
		protected IndexServiceInterface getIndexService() throws URISyntaxException {
			return IndexServiceInterface.getClient();
		}
	}

	public static class JavaRemoteTest extends JavaAbstractTest {

		@Override
		protected IndexServiceInterface getIndexService() throws URISyntaxException {
			return TestServer.getSingleClient();
		}
	}
}
