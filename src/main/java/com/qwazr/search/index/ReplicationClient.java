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

import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.replication.ReplicationProcess;
import com.qwazr.search.replication.ReplicationSession;
import com.qwazr.search.replication.SlaveNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.UUID;

class ReplicationClient {

	LinkedHashMap<String, AnalyzerDefinition> getMasterAnalyzers();

	void setClientAnalyzers(final LinkedHashMap<String, AnalyzerDefinition> analyzers) throws IOException;

	LinkedHashMap<String, FieldDefinition> getMasterFields();

	void setClientFields(final LinkedHashMap<String, FieldDefinition> fields) throws IOException;

	UUID getClientMasterUuid() throws IOException;

	void setClientMasterUuid(final UUID masterUuid) throws IOException;

	void switchRefresh(ReplicationStatus.Strategy strategy) throws IOException;

	InputStream getSourceItem(final String sessionId, final ReplicationProcess.Source source, final String name);

	ReplicationStatus getLastStatus();

	ReplicationStatus replicate(ReplicationSession session) throws IOException;

	/**
	 * Base replication class shared by the ReplicationBackup and ReplicationSlave
	 */
	abstract class Base implements ReplicationClient {

		final private SlaveNode slaveNode;
		volatile private ReplicationStatus lastStatus;

		protected Base(final SlaveNode slaveNode) {
			this.slaveNode = slaveNode;
		}

		@Override
		public ReplicationStatus getLastStatus() {
			return lastStatus;
		}

		@Override
		public final ReplicationStatus replicate(ReplicationSession session) throws IOException {

			final UUID remoteMasterUuid = UUID.fromString(session.masterUuid);

			final ReplicationStatus.Strategy strategy = remoteMasterUuid.equals(getClientMasterUuid()) ?
					ReplicationStatus.Strategy.incremental :
					ReplicationStatus.Strategy.full;

			// Sync analyzers
			setClientAnalyzers(getMasterAnalyzers());

			//Sync fields
			setClientFields(getMasterFields());

			// Sync index & resource files
			final ReplicationStatus.Builder currentStatus = ReplicationStatus.of(strategy).session(session);

			try (final ReplicationProcess replicationProcess = slaveNode.newReplicationProcess(strategy, session,
					(source, file) -> {
						currentStatus.countSize(source, file);
						lastStatus = currentStatus.build();
						return getIndexFile(session.sessionUuid, source, file);
					})) {
				replicationProcess.obtainNewFiles();
				replicationProcess.moveInPlaceNewFiles();

				// We're done, let's set the remoteMasterUUID
				setClientMasterUuid(remoteMasterUuid);

				// New files are in place, the client may switch to the new replicat
				switchRefresh(strategy);

				// And we can clean the old files
				replicationProcess.deleteOldFiles();
			}

			lastStatus = currentStatus.build();
			return getLastStatus();
		}
	}
}
