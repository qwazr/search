package com.qwazr.search.index;

import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.IOUtils;
import org.apache.lucene.replicator.Replicator;
import org.apache.lucene.replicator.Revision;
import org.apache.lucene.replicator.SessionToken;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

class IndexReplicator implements Replicator {

	private final IndexServiceInterface indexService;
	private final String schemaName;
	private final String indexName;
	private final Set<InputStream> inputStreams;

	IndexReplicator(final RemoteIndex... masters) throws URISyntaxException {
		this.indexService = masters[0].host == null || "localhost".equals(masters[0]) ?
				new IndexServiceImpl() :
				new IndexSingleClient(masters[0]);
		this.schemaName = masters[0].schema;
		this.indexName = masters[0].index;
		this.inputStreams = new LinkedHashSet<>();
	}

	@Override
	public void publish(Revision revision) throws IOException {
		throw new UnsupportedOperationException(
				"this replicator implementation does not support remote publishing of revisions");
	}

	@Override
	public SessionToken checkForUpdate(String currVersion) throws IOException {
		final DataInput input = new DataInputStream(
				indexService.replicationUpdate(schemaName, indexName, currVersion).getInputStream());
		return new SessionToken(input);
	}

	@Override
	public void release(String sessionID) throws IOException {
		indexService.replicationRelease(schemaName, indexName, sessionID);
	}

	@Override
	public InputStream obtainFile(String sessionID, String source, String fileName) throws IOException {
		return indexService.replicationObtain(schemaName, indexName, sessionID, source, fileName).getInputStream();
	}

	@Override
	public void close() throws IOException {
		inputStreams.forEach(IOUtils::closeQuietly);
	}

	final LinkedHashMap<String, AnalyzerDefinition> getMasterAnalyzers() {
		return indexService.getAnalyzers(schemaName, indexName);
	}

	final LinkedHashMap<String, FieldDefinition> getMasterFields() {
		return indexService.getFields(schemaName, indexName);
	}
}
