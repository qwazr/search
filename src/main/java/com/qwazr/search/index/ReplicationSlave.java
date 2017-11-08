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
import org.apache.lucene.store.Directory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.LinkedHashMap;

interface ReplicationSlave {

	LinkedHashMap<String, AnalyzerDefinition> getMasterAnalyzers();

	LinkedHashMap<String, FieldDefinition> getMasterFields();

	LinkedHashMap<String, IndexInstance.ResourceInfo> getMasterResources();

	IndexStatus getMasterStatus();

	InputStream getResource(final String resourceName) throws IOException;

	void update(final ReplicationStatus.Builder currentStatus, final WriterAndSearcher writerAndSearcher)
			throws IOException;

	abstract class Base implements ReplicationSlave {

		private final SlaveNode slaveNode;
		private final IndexServiceInterface indexService;
		private final RemoteIndex master;

		private Base(final IndexServiceInterface localService, final RemoteIndex master, final SlaveNode slaveNode) {
			this.slaveNode = slaveNode;
			this.master = master;
			this.indexService =
					master == null ? null : master.host == null ? localService : new IndexSingleClient(master);
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

		public final void update(final ReplicationStatus.Builder currentStatus,
				final WriterAndSearcher writerAndSearcher) throws IOException {
			final ReplicationSession replicationSession =
					indexService.replicationUpdate(master.schema, master.index, null, null);
			if (currentStatus != null)
				currentStatus.session(replicationSession);
			try (final ReplicationProcess replicationProcess = slaveNode.newReplicationProcess(replicationSession,
					(source, file) -> {
						if (currentStatus != null)
							currentStatus.countSize(source, file);
						return indexService.replicationObtain(master.schema, master.index, null, replicationSession.id,
								source.name(), file);
					})) {
				replicationProcess.obtainNewFiles();
				//TODO refresh sync in case of file conflicts
				replicationProcess.moveInPlaceNewFiles();
				writerAndSearcher.refresh();
				replicationProcess.deleteOldFiles();
			} finally {
				indexService.replicationRelease(master.schema, master.index, null, replicationSession.id);
			}
		}

	}

	final class WithIndexAndTaxo extends Base {

		WithIndexAndTaxo(final IndexServiceInterface localService, final RemoteIndex master,
				final Directory dataDirectory, final Path dataDirectoryPath, final Directory taxonomyDirectory,
				final Path taxoDirectoryPath, final Path replWorkDirectory) throws IOException, URISyntaxException {
			super(localService, master,
					new SlaveNode.WithIndexAndTaxo(dataDirectory, dataDirectoryPath, taxonomyDirectory,
							taxoDirectoryPath, replWorkDirectory));
		}
	}

	final class WithIndex extends Base {

		WithIndex(final IndexServiceInterface localService, final RemoteIndex master, final Directory dataDirectory,
				final Path dataDirectoryPath, final Path replWorkDirectory) throws IOException, URISyntaxException {
			super(localService, master, new SlaveNode.WithIndex(dataDirectory, dataDirectoryPath, replWorkDirectory));
		}
	}
}
