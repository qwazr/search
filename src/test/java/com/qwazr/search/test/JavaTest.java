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

import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.utils.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

@RunWith(Suite.class)
@Suite.SuiteClasses({ JavaTest.JavaLibraryTest.class, JavaTest.JavaLocalTest.class, JavaTest.JavaRemoteTest.class })
public class JavaTest {

	public static class JavaLibraryTest extends JavaAbstractTest {

		private static IndexManager indexManager = null;

		public JavaLibraryTest() {
			super(new IndexSettingsDefinition(null, "testSchema", "testIndexMaster"));
		}

		@BeforeClass
		public static void beforeClass() throws IOException {
			final Path rootDirectory = Files.createTempDirectory("qwazr_index_test");
			indexManager = new IndexManager(Executors.newCachedThreadPool(), rootDirectory);
		}

		@Override
		protected IndexServiceInterface getIndexService() {
			return indexManager.getService();
		}

		@AfterClass
		public static void afterClass() {
			IOUtils.close(indexManager);
		}
	}

	final static IndexSettingsDefinition remoteSlaveSettings;

	static {
		try {
			remoteSlaveSettings =
					new IndexSettingsDefinition(null, TestServer.BASE_URL + "/indexes/testSchema/testIndexMaster");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static class JavaLocalTest extends JavaAbstractTest {

		public JavaLocalTest() {
			super(remoteSlaveSettings);
		}

		@Override
		protected IndexServiceInterface getIndexService() throws URISyntaxException {
			return IndexServiceInterface.getClient();
		}
	}

	public static class JavaRemoteTest extends JavaAbstractTest {

		public JavaRemoteTest() {
			super(remoteSlaveSettings);
		}

		@Override
		protected IndexServiceInterface getIndexService() throws URISyntaxException {
			return TestServer.getRemoteClient();
		}
	}

}
