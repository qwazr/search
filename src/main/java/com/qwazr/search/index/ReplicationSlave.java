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
import com.qwazr.server.ServerException;
import com.qwazr.utils.IOUtils;
import org.apache.lucene.store.Directory;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.UUID;

interface ReplicationSlave {

	LinkedHashMap<String, AnalyzerDefinition> getMasterAnalyzers();

	LinkedHashMap<String, FieldDefinition> getMasterFields();

	LinkedHashMap<String, IndexInstance.ResourceInfo> getMasterResources();

	InputStream getResource(final String resourceName) throws IOException;

	ReplicationStatus update(final WriterAndSearcher writerAndSearcher) throws IOException;

	UUID getMasterUuid();

	abstract class Base implements ReplicationSlave {

		private final File masterUuidFile;
		private volatile UUID masterUuid;
		private final SlaveNode slaveNode;
		private final IndexServiceInterface indexService;
		private final RemoteIndex master;

		private Base(final File masterUuidFile, final IndexServiceInterface localService, final RemoteIndex master,
				final SlaveNode slaveNode) throws IOException {
			this.masterUuidFile = masterUuidFile;
			this.slaveNode = slaveNode;
			this.master = master;
			this.indexService =
					master == null ? null : master.host == null ? localService : new IndexSingleClient(master);
			readMasterUuid();
		}

		void readMasterUuid() throws IOException {
			if (masterUuidFile.exists() && masterUuidFile.length() > 0)
				masterUuid = UUID.fromString(IOUtils.readFileAsString(masterUuidFile));
			else
				masterUuid = null;
		}

		void setNewMasterUuid(final UUID newMasterUuid) throws IOException {
			if (newMasterUuid.equals(masterUuid))
				return;
			IOUtils.writeStringAsFile(newMasterUuid.toString(), masterUuidFile);
			masterUuid = newMasterUuid;
		}

		public UUID getMasterUuid() {
			return masterUuid;
		}

		private IndexServiceInterface checkService() {
			if (indexService == null)
				throw new ServerException(Response.Status.NOT_ACCEPTABLE, "The remote master has not been set");
			return indexService;
		}

		public final LinkedHashMap<String, AnalyzerDefinition> getMasterAnalyzers() {
			return checkService().getAnalyzers(master.schema, master.index);
		}

		public final LinkedHashMap<String, FieldDefinition> getMasterFields() {
			return checkService().getFields(master.schema, master.index);
		}

		public final LinkedHashMap<String, IndexInstance.ResourceInfo> getMasterResources() {
			return checkService().getResources(master.schema, master.index);
		}

		public final IndexStatus getMasterStatus() {
			return checkService().getIndex(master.schema, master.index);
		}

		public final InputStream getResource(final String resourceName) throws IOException {
			return checkService().getResource(master.schema, master.index, resourceName);
		}

		public final ReplicationStatus update(final WriterAndSearcher writerAndSearcher) throws IOException {

			final ReplicationSession replicationSession =
					indexService.replicationUpdate(master.schema, master.index, null);

			final UUID remoteMasterUuid = UUID.fromString(replicationSession.masterUuid);

			final ReplicationStatus.Strategy strategy = remoteMasterUuid.equals(masterUuid) ?
					ReplicationStatus.Strategy.incremental :
					ReplicationStatus.Strategy.full;

			final ReplicationStatus.Builder currentStatus = ReplicationStatus.of(strategy).session(replicationSession);

			try (final ReplicationProcess replicationProcess = slaveNode.newReplicationProcess(strategy,
					replicationSession, (source, file) -> {
						if (currentStatus != null)
							currentStatus.countSize(source, file);
						return indexService.replicationObtain(master.schema, master.index,
								replicationSession.sessionUuid, source.name(), file);
					})) {
				replicationProcess.obtainNewFiles();
				replicationProcess.moveInPlaceNewFiles();
				if (strategy == ReplicationStatus.Strategy.incremental)
					writerAndSearcher.refresh();
				else
					writerAndSearcher.reload();
				replicationProcess.deleteOldFiles();

				setNewMasterUuid(remoteMasterUuid);
				return currentStatus.build();
			} finally {
				indexService.replicationRelease(master.schema, master.index, replicationSession.sessionUuid);
			}
		}

	}

	final class WithIndexAndTaxo extends Base {

		WithIndexAndTaxo(final IndexFileSet fileSet, final IndexServiceInterface localService, final RemoteIndex master,
				final Directory dataDirectory, final Directory taxonomyDirectory)
				throws IOException, URISyntaxException {
			super(fileSet.uuidMasterFile, localService, master,
					new SlaveNode.WithIndexAndTaxo(dataDirectory, fileSet.dataDirectory, taxonomyDirectory,
							fileSet.taxonomyDirectory, fileSet.replWorkPath));
		}
	}

	final class WithIndex extends Base {

		WithIndex(final IndexFileSet fileSet, final IndexServiceInterface localService, final RemoteIndex master,
				final Directory dataDirectory) throws IOException, URISyntaxException {
			super(fileSet.uuidMasterFile, localService, master,
					new SlaveNode.WithIndex(dataDirectory, fileSet.dataDirectory, fileSet.replWorkPath));
		}
	}
}
