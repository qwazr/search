package com.qwazr.search.index;

import org.apache.lucene.replicator.Replicator;
import org.apache.lucene.replicator.Revision;
import org.apache.lucene.replicator.SessionToken;

import javax.ws.rs.core.Response;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

class IndexReplicator implements Replicator {

	private final IndexServiceInterface indexService;
	private final String schemaName;
	private final String indexName;

	IndexReplicator(final RemoteIndex... masters) throws URISyntaxException {
		this.indexService = masters[0].uri == null ?
				new IndexServiceImpl() :
				new IndexSingleClient(masters[0].uri, masters[0].timeout);
		this.schemaName = masters[0].schema;
		this.indexName = masters[0].index;
	}

	@Override
	public void publish(Revision revision) throws IOException {
		throw new UnsupportedOperationException(
				"this replicator implementation does not support remote publishing of revisions");
	}

	@Override
	public SessionToken checkForUpdate(String currVersion) throws IOException {
		Response response = indexService.replicationUpdate(schemaName, indexName, currVersion);
		if (response == null || response.getStatus() == Response.Status.NO_CONTENT.getStatusCode())
			return null;
		Object entity = response.getEntity();
		DataInput input = new DataInputStream((InputStream) entity);
		return new SessionToken(input);
	}

	@Override
	public void release(String sessionID) throws IOException {
		indexService.replicationRelease(schemaName, indexName, sessionID);
	}

	@Override
	public InputStream obtainFile(String sessionID, String source, String fileName) throws IOException {
		return indexService.replicationObtain(schemaName, indexName, sessionID, source, fileName);
	}

	@Override
	public void close() throws IOException {
	}
}
