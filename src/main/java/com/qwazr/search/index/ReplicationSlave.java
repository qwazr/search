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

abstract class ReplicationSlave extends ReplicationClient.Base {

	private final File masterUuidFile;
	private volatile UUID masterUuid;
	private final IndexServiceInterface indexService;
	private final RemoteIndex master;
	private final WriterAndSearcher writerAndSearcher;

	private ReplicationSlave(final File masterUuidFile, final IndexServiceInterface localService,
			final RemoteIndex master, final SlaveNode slaveNode, final WriterAndSearcher writerAndSearcher)
			throws IOException {
		super(slaveNode);
		this.masterUuidFile = masterUuidFile;
		this.writerAndSearcher = writerAndSearcher;
		this.master = master;
		this.indexService = master == null ? null : master.host == null ? localService : new IndexSingleClient(master);
		getClientMasterUuid();
	}

	@Override
	public UUID getClientMasterUuid() throws IOException {
		if (masterUuidFile.exists() && masterUuidFile.length() > 0)
			masterUuid = UUID.fromString(IOUtils.readFileAsString(masterUuidFile));
		else
			masterUuid = null;
		return masterUuid;
	}

	@Override
	public void setClientMasterUuid(final UUID newMasterUuid) throws IOException {
		if (newMasterUuid.equals(masterUuid))
			return;
		IOUtils.writeStringAsFile(newMasterUuid.toString(), masterUuidFile);
		masterUuid = newMasterUuid;
	}

	private IndexServiceInterface checkService() {
		if (indexService == null)
			throw new ServerException(Response.Status.NOT_ACCEPTABLE, "The remote master has not been set");
		return indexService;
	}

	@Override
	public final LinkedHashMap<String, AnalyzerDefinition> getMasterAnalyzers() {
		return checkService().getAnalyzers(master.schema, master.index);
	}

	@Override
	public final LinkedHashMap<String, FieldDefinition> getMasterFields() {
		return checkService().getFields(master.schema, master.index);
	}

	@Override
	public void setClientAnalyzers(LinkedHashMap<String, AnalyzerDefinition> analyzers) throws IOException {
		indexInstance.setAnalyzers(analyzers);
	}

	@Override
	public void setClientFields(LinkedHashMap<String, FieldDefinition> fields) throws IOException {
		indexInstance.setFields(fields);
	}

	@Override
	public InputStream getIndexFile(String sessionId, ReplicationProcess.Source source, String file) {
		switch (source) {
		case resources:
			return checkService().getResource(master.schema, master.index, file);
		case data:
		case taxonomy:
			return checkService().replicationObtain(master.schema, master.index, sessionId, source.name(), file);
		}
		return null;
	}

	@Override
	public final void switchRefresh(ReplicationStatus.Strategy strategy) throws IOException {
		if (strategy == ReplicationStatus.Strategy.incremental)
			writerAndSearcher.refresh();
		else
			writerAndSearcher.reload();
	}

	ReplicationStatus replicate() throws IOException {
		final ReplicationSession session = checkService().replicationUpdate(master.schema, master.index, null);
		try {
			return replicate(session);
		} finally {
			checkService().replicationRelease(master.schema, master.index, session.sessionUuid);
		}
	}

	final static class WithIndexAndTaxo extends ReplicationSlave {

		WithIndexAndTaxo(final IndexFileSet fileSet, final IndexServiceInterface localService, final RemoteIndex master,
				final Directory dataDirectory, final Directory taxonomyDirectory,
				final WriterAndSearcher writerAndSearcher) throws IOException, URISyntaxException {
			super(fileSet.uuidMasterFile, indexInstance, localService, master,
					new SlaveNode.WithIndexAndTaxo(fileSet.resourcesDirectoryPath, dataDirectory, fileSet.dataDirectory,
							taxonomyDirectory, fileSet.taxonomyDirectory, fileSet.replWorkPath), writerAndSearcher);
		}
	}

	final static class WithIndex extends ReplicationSlave {

		WithIndex(final IndexFileSet fileSet, final IndexInstance indexInstance,
				final IndexServiceInterface localService, final RemoteIndex master, final Directory dataDirectory,
				final WriterAndSearcher writerAndSearcher) throws IOException, URISyntaxException {
			super(fileSet.uuidMasterFile, indexInstance, localService, master,
					new SlaveNode.WithIndex(fileSet.resourcesDirectoryPath, dataDirectory, fileSet.dataDirectory,
							fileSet.replWorkPath), writerAndSearcher);
		}

	}
}