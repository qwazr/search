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

import com.qwazr.search.index.ReplicationStatus;
import com.qwazr.search.test.AnnotatedRecord;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ReplicationChangeMasterTest extends ReplicationNoTaxo {

	private void putDocumentAndCheckReplication(int loopNumber, ReplicationStatus.Strategy expectedStrategy,
			Integer expectedRadio) throws IOException, InterruptedException {
		for (int i = 0; i < loopNumber; i++)
			master.postDocuments(AnnotatedRecord.randomList(1000, count -> count));
		checkReplicationStatus(slaves.get(0).replicationCheck(), expectedStrategy, expectedRadio);
		Assert.assertEquals(master.getIndexStatus().num_docs, slaves.get(0).getIndexStatus().num_docs);
		compareMasterAndSlaveRecords(null);
	}

	void sequence() throws IOException, InterruptedException {

		// Make a first replication
		putDocumentAndCheckReplication(5, ReplicationStatus.Strategy.full, 100);

		// Make a second replication
		putDocumentAndCheckReplication(5, ReplicationStatus.Strategy.incremental, null);

		// Make a third useless replication
		putDocumentAndCheckReplication(0, ReplicationStatus.Strategy.incremental, 0);
	}

	@Test
	@Override
	public void test() throws IOException, InterruptedException {

		// First pass tests
		sequence();

		// Get the number of documents
		long numberOfDoc = master.getIndexStatus().num_docs;
		Assert.assertEquals(numberOfDoc, slaves.get(0).getIndexStatus().num_docs, 0);

		// Recreate the master index (changing UUID)
		master.deleteIndex();
		master.createUpdateIndex();
		master.createUpdateFields();

		// Second pass tests
		sequence();
	}
}
