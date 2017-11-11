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

package com.qwazr.search.index;

import com.qwazr.search.replication.ReplicationProcess;
import com.qwazr.search.replication.ReplicationSession;
import com.qwazr.search.replication.SlaveNode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Base replication class shared by the ReplicationBackup and ReplicationSlave
 */
abstract class ReplicationClient {

	final private SlaveNode slaveNode;
	volatile private ReplicationStatus lastStatus;

	protected ReplicationClient(final SlaveNode slaveNode) {
		this.slaveNode = slaveNode;
	}

	ReplicationStatus getLastStatus() {
		return lastStatus;
	}

	abstract InputStream getItem(final String sessionUuid, final ReplicationProcess.Source source,
			final String itemName) throws FileNotFoundException;

	final ReplicationStatus replicate(final ReplicationSession session, final UUID clientMasterUuid,
			final Switcher switcher) throws IOException {

		final UUID remoteMasterUuid = UUID.fromString(session.masterUuid);

		final ReplicationStatus.Strategy strategy = remoteMasterUuid.equals(clientMasterUuid) ?
				ReplicationStatus.Strategy.incremental :
				ReplicationStatus.Strategy.full;

		final ReplicationStatus.Builder currentStatus = ReplicationStatus.of(strategy).session(session);

		try (final ReplicationProcess replicationProcess = slaveNode.newReplicationProcess(strategy, session,
				(source, file) -> {
					currentStatus.countSize(source, file);
					lastStatus = currentStatus.build();
					return getItem(session.sessionUuid, source, file);
				})) {
			replicationProcess.obtainNewFiles();
			replicationProcess.moveInPlaceNewFiles();

			// New files are in place, the client may switch to the new replicat
			if (switcher != null)
				switcher.switcher(strategy, remoteMasterUuid);

			// Finaly we can clean the old files
			replicationProcess.deleteOldFiles();
		}

		return lastStatus = currentStatus.build();
	}

	@FunctionalInterface
	interface Switcher {
		void switcher(final ReplicationStatus.Strategy strategy, final UUID remoteMasterUuid) throws IOException;
	}
}
