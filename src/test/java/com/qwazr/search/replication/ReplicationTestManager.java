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

package com.qwazr.search.replication;

import com.qwazr.search.index.IndexManager;
import com.qwazr.search.index.ReplicationStatus;
import com.qwazr.search.test.AnnotatedRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ReplicationTestManager extends ReplicationTestBase<AnnotatedRecord> {

	protected static ExecutorService executorService;
	protected static IndexManager indexManager;
	protected static Path rootDirectory;

	public ReplicationTestManager() {
		super(indexManager.getService(), AnnotatedRecord.class);
	}

	@BeforeClass
	public static void setupClass() throws IOException {
		executorService = Executors.newCachedThreadPool();
		rootDirectory = Files.createTempDirectory("ReplicationTestManager");
		indexManager = new IndexManager(rootDirectory, executorService);
		indexManager.registerAnalyzerFactory(AnnotatedRecord.INJECTED_ANALYZER_NAME,
				resourceLoader -> new AnnotatedRecord.TestAnalyzer(new AtomicInteger()));
	}

	@AfterClass
	public static void cleanupClass() throws InterruptedException {
		indexManager.close();
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.MINUTES);
	}

	public void test() throws IOException, InterruptedException {
		master.postDocuments(AnnotatedRecord.randomList(1000, count -> count));
		checkReplicationStatus(slaves.get(0).replicationCheck(), ReplicationStatus.Strategy.incremental, 100);
		compareMasterAndSlaveRecords(null);

		master.checkIndex();
		slaves.get(0).checkIndex();

		master.postDocuments(AnnotatedRecord.randomList(1000, count -> count + 1000));
		checkReplicationStatus(slaves.get(0).replicationCheck(), ReplicationStatus.Strategy.incremental, null);
		compareMasterAndSlaveRecords(null);

	}
}
