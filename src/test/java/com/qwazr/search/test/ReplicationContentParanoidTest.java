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
import java.util.logging.Logger;

public class ReplicationContentParanoidTest {

	private static final Logger LOGGER = LoggerUtils.getLogger(ReplicationContentParanoidTest.class);

	private final static String SCHEMA = "repli-content";

	private static AnnotatedIndexService<AnnotatedIndex> masterService;
	private static AnnotatedIndexService<AnnotatedIndex> slaveService;

	@BeforeClass
	public static void beforeClass() throws Exception {
		TestServer.startServer();

		// Get the master service
		masterService =
				new AnnotatedIndexService<>(TestServer.service, AnnotatedIndex.class, SCHEMA, "replication-master",
						IndexSettingsDefinition.of()
								.mergeScheduler(IndexSettingsDefinition.MergeScheduler.CONCURRENT)
								.build());
		masterService.createUpdateSchema();
		masterService.createUpdateIndex();
		masterService.createUpdateFields();

		slaveService =
				new AnnotatedIndexService<>(TestServer.service, AnnotatedIndex.class, SCHEMA, "replication-slave",
						IndexSettingsDefinition.of()
								.master(masterService.getSchemaName(), masterService.getIndexName())
								.build());
		slaveService.createUpdateIndex();

	}

	private Map<String, String> getCommitData(UUID version) {
		final Map<String, String> commitData = new HashMap<>();
		commitData.put("version", version.toString());
		return commitData;
	}

	final private static int ITERATION_COUNT = 1000;

	@Test
	public void contentTest() throws IOException, URISyntaxException, InterruptedException {

		Assert.assertNotNull(masterService);
		Assert.assertNotNull(slaveService);

		for (int i = 0; i < 15; i++) {

			// Post the documents
			final List<AnnotatedIndex> records =
					AnnotatedIndex.randomList(ITERATION_COUNT, val -> RandomUtils.nextInt(0, ITERATION_COUNT));
			final Map<String, String> commitData = getCommitData(HashUtils.newTimeBasedUUID());
			masterService.postDocuments(records, commitData);
			final IndexStatus masterStatus = masterService.getIndexStatus();
			Assert.assertTrue(CollectionsUtils.equals(commitData, masterStatus.commit_user_data));

			// Do the replication
			slaveService.replicationCheck();
			// Check we have the same status and commit data
			final IndexStatus slaveStatus = slaveService.getIndexStatus();
			Assert.assertTrue(CollectionsUtils.equals(commitData, slaveStatus.commit_user_data));
			Assert.assertEquals(masterStatus.num_docs, slaveStatus.num_docs);
			Assert.assertEquals(masterStatus.num_deleted_docs, slaveStatus.num_deleted_docs);
			Assert.assertEquals(masterStatus.number_of_segment, slaveStatus.number_of_segment);

			// Compare content

			final QueryDefinition queryIterator = QueryDefinition.of(new MatchAllDocsQuery())
					.returnedField(FieldDefinition.ID_FIELD, "title")
					.build();

			final Iterator<AnnotatedIndex> masterIterator =
					masterService.searchIterator(queryIterator, AnnotatedIndex.class);
			final Iterator<AnnotatedIndex> slaveIterator =
					slaveService.searchIterator(queryIterator, AnnotatedIndex.class);

			masterIterator.forEachRemaining(masterRecord -> {
				final AnnotatedIndex slaveRecord = slaveIterator.next();
				Assert.assertNotNull(masterRecord.id);
				Assert.assertNotNull(masterRecord.title);
				Assert.assertEquals(masterRecord.id, slaveRecord.id);
				Assert.assertEquals(masterRecord.title, slaveRecord.title);
			});

			LOGGER.info(() -> "num_docs: " + masterStatus.num_docs + " - num_deleted_docs: " +
					masterStatus.num_deleted_docs + " - number_of_segments: " + masterStatus.number_of_segment);

			if (!Objects.equals(masterStatus.version, slaveStatus.version))
				LOGGER.warning(
						() -> "Master version: " + masterStatus.version + " - Slave version: " + slaveStatus.version);

		}

	}

}
