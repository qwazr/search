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

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.query.MatchAllDocsQuery;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplicationContentParanoidTest {

	private static final Logger LOGGER = LoggerUtils.getLogger(ReplicationContentParanoidTest.class);

	private final static String SCHEMA = "repli-content";

	private static AnnotatedIndexService<AnnotatedIndex> master;
	private static AnnotatedIndexService<AnnotatedIndex> slave1;
	private static AnnotatedIndexService<AnnotatedIndex> slave2;

	@BeforeClass
	public static void beforeClass() throws Exception {

		LOGGER.setLevel(Level.INFO);

		TestServer.startServer();

		// Get the master service
		master = new AnnotatedIndexService<>(TestServer.service, AnnotatedIndex.class, SCHEMA, "replication-master",
				IndexSettingsDefinition.of()
						.mergeScheduler(IndexSettingsDefinition.MergeScheduler.CONCURRENT)
						.mergedSegmentWarmer(true)
						.indexReaderWarmer(false)
						.useCompoundFile(false)
						.build());
		master.createUpdateSchema();
		master.createUpdateIndex();
		master.createUpdateFields();

		slave1 = new AnnotatedIndexService<>(TestServer.service, AnnotatedIndex.class, SCHEMA, "replication-slave1",
				IndexSettingsDefinition.of()
						.master(master.getSchemaName(), master.getIndexName())
						.indexReaderWarmer(false)
						.build());
		slave1.createUpdateIndex();

		slave2 = new AnnotatedIndexService<>(TestServer.service, AnnotatedIndex.class, SCHEMA, "replication-slave2",
				IndexSettingsDefinition.of()
						.master(master.getSchemaName(), master.getIndexName())
						.indexReaderWarmer(true)
						.build());
		slave2.createUpdateIndex();

	}

	private Map<String, String> getCommitData(UUID version) {
		final Map<String, String> commitData = new HashMap<>();
		commitData.put("version", version.toString());
		return commitData;
	}

	final private static int ITERATION_COUNT = 10;
	final private static int BATCH_SIZE = 5_000;
	final private static int ID_RANGE = 10_000;

	private static long timeTracker(final Runnable runnable) {
		long t = System.currentTimeMillis();
		runnable.run();
		return System.currentTimeMillis() - t;
	}

	private static IndexStatus replicationAndCheck(final IndexStatus masterStatus,
			final AnnotatedIndexService<AnnotatedIndex> slave) {
		// Do the replication
		slave.replicationCheck();
		// Check we have the same status and commit data
		final IndexStatus slaveStatus = slave.getIndexStatus();
		Assert.assertTrue(CollectionsUtils.equals(masterStatus.commit_user_data, slaveStatus.commit_user_data));
		Assert.assertEquals(masterStatus.num_docs, slaveStatus.num_docs);
		Assert.assertEquals(masterStatus.num_deleted_docs, slaveStatus.num_deleted_docs);
		Assert.assertEquals(masterStatus.number_of_segment, slaveStatus.number_of_segment);
		return slaveStatus;
	}

	int indexMinTime(long... values) {
		int winner = 0;
		long current = Long.MAX_VALUE;
		int idx = 0;
		for (long value : values) {
			idx++;
			if (value < current) {
				current = value;
				winner = idx;
			}
		}
		return winner;
	}

	@Test
	public void contentTest() throws IOException, URISyntaxException, InterruptedException {

		Assert.assertNotNull(master);
		Assert.assertNotNull(slave1);
		Assert.assertNotNull(slave2);

		Assert.assertEquals(true, master.getIndexStatus().settings.mergedSegmentWarmer);
		Assert.assertEquals(false, slave1.getIndexStatus().settings.indexReaderWarmer);
		Assert.assertEquals(true, slave2.getIndexStatus().settings.indexReaderWarmer);

		int masterTotalQueryTimeWins = 0;
		int slave1TotalQueryTimeWins = 0;
		int slave2TotalQueryTimeWins = 0;

		for (int i = 0; i < ITERATION_COUNT; i++) {

			// Post the documents
			final List<AnnotatedIndex> records =
					AnnotatedIndex.randomList(BATCH_SIZE, val -> RandomUtils.nextInt(0, ID_RANGE));
			final Map<String, String> commitData = getCommitData(HashUtils.newTimeBasedUUID());
			master.postDocuments(records, commitData);
			final IndexStatus masterStatus = master.getIndexStatus();
			Assert.assertTrue(CollectionsUtils.equals(commitData, masterStatus.commit_user_data));

			// Do the replication
			final IndexStatus slave1Status = replicationAndCheck(masterStatus, slave1);
			final IndexStatus slave2Status = replicationAndCheck(masterStatus, slave2);

			// Compare content

			final QueryDefinition queryIterator = QueryDefinition.of(new MatchAllDocsQuery())
					.returnedField(FieldDefinition.ID_FIELD, "title")
					.build();

			switch (indexMinTime(timeTracker(() -> master.searchQuery(queryIterator)),
					timeTracker(() -> slave1.searchQuery(queryIterator)),
					timeTracker(() -> slave2.searchQuery(queryIterator)))) {
			case 1:
				masterTotalQueryTimeWins++;
				break;
			case 2:
				slave1TotalQueryTimeWins++;
				break;
			case 3:
				slave2TotalQueryTimeWins++;
				break;
			}

			final Iterator<AnnotatedIndex> masterIterator = master.searchIterator(queryIterator, AnnotatedIndex.class);
			final Iterator<AnnotatedIndex> slave1Iterator = slave1.searchIterator(queryIterator, AnnotatedIndex.class);
			final Iterator<AnnotatedIndex> slave2Iterator = slave2.searchIterator(queryIterator, AnnotatedIndex.class);

			masterIterator.forEachRemaining(masterRecord -> {
				final AnnotatedIndex slave1Record = slave1Iterator.next();
				final AnnotatedIndex slave2Record = slave2Iterator.next();
				Assert.assertNotNull(masterRecord.id);
				Assert.assertNotNull(masterRecord.title);
				Assert.assertEquals(masterRecord.id, slave1Record.id);
				Assert.assertEquals(masterRecord.title, slave1Record.title);
				Assert.assertEquals(masterRecord.id, slave2Record.id);
				Assert.assertEquals(masterRecord.title, slave2Record.title);
			});

			LOGGER.info(() -> "num_docs: " + masterStatus.num_docs + " - num_deleted_docs: " +
					masterStatus.num_deleted_docs + " - number_of_segments: " + masterStatus.number_of_segment);

			if (!Objects.equals(masterStatus.version, slave1Status.version))
				LOGGER.warning(() -> "Master version: " + masterStatus.version + " - Slave 1 version: " +
						slave1Status.version);
			if (!Objects.equals(masterStatus.version, slave2Status.version))
				LOGGER.warning(() -> "Master version: " + masterStatus.version + " - Slave 2 version: " +
						slave2Status.version);

			LOGGER.info("Master first Query wins: " + masterTotalQueryTimeWins);
			LOGGER.info("Slave 1 first Query wins: " + slave1TotalQueryTimeWins);
			LOGGER.info("Slave 2 first Query wins: " + slave2TotalQueryTimeWins);
		}

		//Assert.assertTrue(masterTotalQueryTime > slave1TotalQueryTime);
		//Assert.assertTrue(slave1TotalQueryTime > slave2TotalQueryTime);
	}

}
