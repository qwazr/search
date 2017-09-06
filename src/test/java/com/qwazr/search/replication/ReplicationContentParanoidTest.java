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

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.query.MatchAllDocsQuery;
import com.qwazr.search.test.AnnotatedRecord;
import com.qwazr.search.test.TestServer;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplicationContentParanoidTest extends ReplicationTestBase<AnnotatedRecord> {

	private static final Logger LOGGER = LoggerUtils.getLogger(ReplicationContentParanoidTest.class);

	private final static String SCHEMA = "repli-content";
	private final static String MASTER = "replication-master";

	public ReplicationContentParanoidTest() {
		super(TestServer.service, AnnotatedRecord.class);
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		LOGGER.setLevel(Level.INFO);
		TestServer.startServer();
	}

	@Override
	public AnnotatedIndexService<AnnotatedRecord> getMaster() throws URISyntaxException {
		return new AnnotatedIndexService<>(service, AnnotatedRecord.class, SCHEMA, MASTER, IndexSettingsDefinition.of()
				.mergeScheduler(IndexSettingsDefinition.MergeScheduler.CONCURRENT)
				.mergedSegmentWarmer(true)
				.indexReaderWarmer(false)
				.useCompoundFile(false)
				//.replication(IndexSettingsDefinition.Replication.NRT)
				.build());
	}

	@Override
	public List<AnnotatedIndexService<AnnotatedRecord>> getSlaves() throws URISyntaxException {
		return Arrays.asList(

				new AnnotatedIndexService<>(service, AnnotatedRecord.class, SCHEMA, "replication-slave1",
						IndexSettingsDefinition.of().master(SCHEMA, MASTER).indexReaderWarmer(false)
								//.replication(IndexSettingsDefinition.Replication.NRT)
								.build()),

				new AnnotatedIndexService<>(service, AnnotatedRecord.class, SCHEMA, "replication-slave2",
						IndexSettingsDefinition.of().master(SCHEMA, MASTER).indexReaderWarmer(true)
								//.replication(IndexSettingsDefinition.Replication.NRT)
								.build()));
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

		final AnnotatedIndexService<AnnotatedRecord> slave1 = slaves.get(0);
		final AnnotatedIndexService<AnnotatedRecord> slave2 = slaves.get(1);

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
			final List<AnnotatedRecord> records =
					AnnotatedRecord.randomList(BATCH_SIZE, val -> RandomUtils.nextInt(0, ID_RANGE));
			final Map<String, String> commitData = getCommitData(HashUtils.newTimeBasedUUID());
			master.postDocuments(records, commitData);
			final IndexStatus masterStatus = master.getIndexStatus();
			Assert.assertTrue(CollectionsUtils.equals(commitData, masterStatus.commit_user_data));

			// Do the replication
			slave1.replicationCheck();
			slave2.replicationCheck();
			checkSlaveStatusEqualsMasterStatus();

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

			compareMasterAndSlaveRecords((m, s) -> {
				Assert.assertNotNull(m.id);
				Assert.assertNotNull(m.title);
			});

			LOGGER.info(() -> "num_docs: " + masterStatus.num_docs + " - num_deleted_docs: " +
					masterStatus.num_deleted_docs + " - segment_count: " + masterStatus.segment_count);

			LOGGER.info("Master first Query wins: " + masterTotalQueryTimeWins);
			LOGGER.info("Slave 1 first Query wins: " + slave1TotalQueryTimeWins);
			LOGGER.info("Slave 2 first Query wins: " + slave2TotalQueryTimeWins);
		}

		//Assert.assertTrue(masterTotalQueryTime > slave1TotalQueryTime);
		//Assert.assertTrue(slave1TotalQueryTime > slave2TotalQueryTime);
	}

}
