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
	public void publish(final Revision revision) throws IOException {
		throw new UnsupportedOperationException(
				"this replicator implementation does not support remote publishing of revisions");
	}

	@Override
	public SessionToken checkForUpdate(final String currVersion) throws IOException {
		try (final InputStream inputStream = indexService
				.replicationUpdate(schemaName, indexName, currVersion)
				.getInputStream()) {
			final DataInput input = new DataInputStream(inputStream);
			return new SessionToken(input);
		}
	}

	@Override
	public void release(final String sessionID) throws IOException {
		indexService.replicationRelease(schemaName, indexName, sessionID);
	}

	@Override
	public InputStream obtainFile(final String sessionID, final String source, final String fileName)
			throws IOException {
		final InputStream stream =
				indexService.replicationObtain(schemaName, indexName, sessionID, source, fileName).getInputStream();
		inputStreams.add(stream);
		return stream;
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

	final IndexStatus getMasterStatus() {
		return indexService.getIndex(schemaName, indexName);
	}
}
