/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.IOUtils;
import com.qwazr.server.AbstractStreamingOutput;
import com.qwazr.server.ServerException;
import org.apache.lucene.replicator.Replicator;
import org.apache.lucene.replicator.Revision;
import org.apache.lucene.replicator.SessionToken;

import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

class IndexReplicator implements Replicator {

	private final IndexServiceInterface indexService;
	private final RemoteIndex master;
	private final File masterUuidFile;
	private volatile String masterUuidString;
	private volatile UUID masterUuid;
	private final Set<InputStream> inputStreams;

	IndexReplicator(final IndexServiceInterface service, final RemoteIndex master, final File masterUuidFile)
			throws URISyntaxException, IOException {
		this.master = master;
		this.masterUuidFile = masterUuidFile;
		indexService = master == null ?
				null :
				master.host == null ?
						service :
						"localhost".equals(master.host) ? service : new IndexSingleClient(master);
		this.inputStreams = new LinkedHashSet<>();
		if (masterUuidFile.exists() && masterUuidFile.length() > 0) {
			masterUuid = UUID.fromString(IOUtils.readFileAsString(masterUuidFile));
			masterUuidString = masterUuid.toString();
		}
	}

	private IndexServiceInterface checkService() {
		if (indexService == null)
			throw new ServerException(Response.Status.NOT_ACCEPTABLE, "The remote master has not been set");
		return indexService;
	}

	String checkRemoteMasterUuid() throws IOException {
		final UUID remoteMasterUuid = UUID.fromString(checkService().getIndex(master.schema, master.index).index_uuid);
		if (masterUuid == null) {
			masterUuid = remoteMasterUuid;
			masterUuidString = masterUuid.toString();
			IOUtils.writeStringAsFile(masterUuidString, masterUuidFile);
			return masterUuidString;
		}
		if (!Objects.equals(remoteMasterUuid, masterUuid))
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"The local master index UUID and the remote index UUID does not match: " + masterUuid + " <> "
							+ remoteMasterUuid);
		return masterUuidString;
	}

	UUID getMasterUuid() {
		return masterUuid;
	}

	@Override
	public void publish(final Revision revision) throws IOException {
		throw new UnsupportedOperationException(
				"this replicator implementation does not support remote publishing of revisions");
	}

	@Override
	public SessionToken checkForUpdate(final String currVersion) throws IOException {
		final AbstractStreamingOutput streamingOutput =
				checkService().replicationUpdate(master.schema, master.index, masterUuidString, currVersion);
		if (streamingOutput == null)
			return null;
		try (final InputStream inputStream = streamingOutput.getInputStream()) {
			if (inputStream == null)
				return null;
			final DataInput input = new DataInputStream(inputStream);
			return new SessionToken(input);
		}
	}

	@Override
	public void release(final String sessionID) throws IOException {
		checkService().replicationRelease(master.schema, master.index, masterUuidString, sessionID);
	}

	@Override
	public InputStream obtainFile(final String sessionID, final String source, final String fileName)
			throws IOException {
		final InputStream stream =
				checkService().replicationObtain(master.schema, master.index, masterUuidString, sessionID, source,
						fileName).getInputStream();
		inputStreams.add(stream);
		return stream;
	}

	@Override
	public void close() throws IOException {
		inputStreams.forEach(IOUtils::closeQuietly);
	}

	final LinkedHashMap<String, AnalyzerDefinition> getMasterAnalyzers() {
		return checkService().getAnalyzers(master.schema, master.index);
	}

	final LinkedHashMap<String, FieldDefinition> getMasterFields() {
		return checkService().getFields(master.schema, master.index);
	}

	final LinkedHashMap<String, IndexInstance.ResourceInfo> getMasterResources() {
		return checkService().getResources(master.schema, master.index);
	}

	final IndexStatus getMasterStatus() {
		return checkService().getIndex(master.schema, master.index);
	}

	final InputStream getResource(final String resourceName) throws IOException {
		return checkService().getResource(master.schema, master.index, resourceName).getInputStream();
	}

}
