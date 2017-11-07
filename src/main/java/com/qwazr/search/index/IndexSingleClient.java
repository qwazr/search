/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.jaxrs.smile.SmileMediaTypes;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.replication.ReplicationSession;
import com.qwazr.server.RemoteService;
import com.qwazr.server.ServerException;
import com.qwazr.server.client.JsonClient;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class IndexSingleClient extends JsonClient implements IndexServiceInterface {

	private final WebTarget indexTarget;

	public IndexSingleClient(final RemoteService remote) {
		super(remote);
		indexTarget = client.target(remote.serviceAddress).path(IndexServiceInterface.PATH);
	}

	@Override
	public SchemaSettingsDefinition createUpdateSchema(final String schemaName) {
		return createUpdateSchema(schemaName, null);
	}

	@Override
	public SchemaSettingsDefinition createUpdateSchema(final String schemaName,
			final SchemaSettingsDefinition settings) {
		return indexTarget.path(schemaName)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(settings, SmileMediaTypes.APPLICATION_JACKSON_SMILE),
						SchemaSettingsDefinition.class);
	}

	@Override
	public Set<String> getSchemas() {
		return indexTarget.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE).get(setStringType);
	}

	@Override
	public boolean deleteSchema(final String schemaName) {
		return indexTarget.path(schemaName).request(MediaType.TEXT_PLAIN).delete(boolean.class);
	}

	@Override
	public Set<String> getIndexes(final String schemaName) {
		return indexTarget.path(schemaName).request(SmileMediaTypes.APPLICATION_JACKSON_SMILE).get(setStringType);
	}

	@Override
	public IndexStatus createUpdateIndex(final String schemaName, final String indexName) {
		return createUpdateIndex(schemaName, indexName, null);
	}

	@Override
	public IndexStatus createUpdateIndex(final String schemaName, final String indexName,
			final IndexSettingsDefinition settings) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(settings, SmileMediaTypes.APPLICATION_JACKSON_SMILE), IndexStatus.class);
	}

	@Override
	public LinkedHashMap<String, FieldDefinition> getFields(final String schemaName, final String indexName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("fields")
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.get(mapStringFieldType);
	}

	@Override
	public LinkedHashMap<String, FieldDefinition> setFields(final String schemaName, final String indexName,
			final LinkedHashMap<String, FieldDefinition> fields) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("fields")
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(fields, SmileMediaTypes.APPLICATION_JACKSON_SMILE), mapStringFieldType);
	}

	@Override
	public List<TermDefinition> doAnalyzeQuery(final String schemaName, final String indexName, final String fieldName,
			final String text) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("fields")
				.path(fieldName)
				.path("analyzer/query")
				.queryParam("text", text == null ? StringUtils.EMPTY : text)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.get(listTermDefinitionType);
	}

	@Override
	public List<TermDefinition> doAnalyzeIndex(final String schemaName, final String indexName, final String fieldName,
			final String text) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("fields")
				.path(fieldName)
				.path("analyzer/index")
				.queryParam("text", text == null ? StringUtils.EMPTY : text)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.get(listTermDefinitionType);
	}

	@Override
	public FieldStats getFieldStats(final String schemaName, final String indexName, final String fieldName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("fields")
				.path(fieldName)
				.path("stats")
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.get(FieldStats.class);
	}

	@Override
	public List<TermEnumDefinition> doExtractTerms(final String schemaName, final String indexName,
			final String fieldName, final Integer start, final Integer rows) {
		return doExtractTerms(schemaName, indexName, fieldName, null, start, rows);
	}

	@Override
	public List<TermEnumDefinition> doExtractTerms(final String schemaName, final String indexName,
			final String fieldName, final String prefix, final Integer start, final Integer rows) {
		WebTarget target = indexTarget.path(schemaName).path(indexName).path("fields").path(fieldName).path("terms");
		if (prefix != null)
			target = target.path(prefix);
		if (start != null)
			target = target.queryParam("start", start);
		if (rows != null)
			target = target.queryParam("rows", rows);
		return target.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE).get(listTermEnumDefinitionType);
	}

	@Override
	public FieldDefinition getField(final String schemaName, final String indexName, final String fieldName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("fields")
				.path(fieldName == null ? StringUtils.EMPTY : fieldName)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.get(FieldDefinition.class);
	}

	@Override
	public FieldDefinition setField(final String schemaName, final String indexName, final String fieldName,
			final FieldDefinition field) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("fields")
				.path(fieldName == null ? StringUtils.EMPTY : fieldName)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(field, SmileMediaTypes.APPLICATION_JACKSON_SMILE), FieldDefinition.class);
	}

	@Override
	public boolean deleteField(final String schemaName, final String indexName, final String fieldName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("fields")
				.path(fieldName == null ? StringUtils.EMPTY : fieldName)
				.request()
				.delete(Boolean.class);
	}

	@Override
	public LinkedHashMap<String, AnalyzerDefinition> getAnalyzers(final String schemaName, final String indexName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("analyzers")
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.get(mapStringAnalyzerType);
	}

	@Override
	public AnalyzerDefinition getAnalyzer(final String schemaName, final String indexName, final String analyzerName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("analyzers")
				.path(analyzerName)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.get(AnalyzerDefinition.class);
	}

	@Override
	public void refreshAnalyzers(String schemaName, String indexName) {
		final Response.StatusType statusType = indexTarget.path(schemaName)
				.path(indexName)
				.path("analyzers")
				.request()
				.method("PATCH")
				.getStatusInfo();
		if (statusType.getFamily() != Response.Status.Family.SUCCESSFUL)
			throw new ServerException("Analyzer refresh failed: " + statusType.getReasonPhrase());
	}

	@Override
	public AnalyzerDefinition setAnalyzer(final String schemaName, final String indexName, final String analyzerName,
			final AnalyzerDefinition analyzer) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("analyzers")
				.path(analyzerName)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(analyzer, SmileMediaTypes.APPLICATION_JACKSON_SMILE), AnalyzerDefinition.class);
	}

	@Override
	public LinkedHashMap<String, AnalyzerDefinition> setAnalyzers(final String schemaName, final String indexName,
			final LinkedHashMap<String, AnalyzerDefinition> analyzers) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("analyzers")
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(analyzers, SmileMediaTypes.APPLICATION_JACKSON_SMILE), mapStringAnalyzerType);
	}

	@Override
	public boolean deleteAnalyzer(final String schemaName, final String indexName, final String analyzerName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("analyzers")
				.path(analyzerName)
				.request()
				.delete(Boolean.class);
	}

	@Override
	public List<TermDefinition> testAnalyzer(final String schemaName, final String indexName, final String analyzerName,
			final String text) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("analyzers")
				.path(analyzerName)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.text(text == null ? StringUtils.EMPTY : text), listTermDefinitionType);
	}

	@Override
	public String testAnalyzerDot(final String schemaName, final String indexName, final String analyzerName,
			final String text) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("analyzers")
				.path(analyzerName)
				.path("dot")
				.queryParam("text", text == null ? StringUtils.EMPTY : text)
				.request(MediaType.TEXT_PLAIN)
				.get(String.class);
	}

	@Override
	public IndexStatus getIndex(final String schemaName, final String indexName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.get(IndexStatus.class);
	}

	@Override
	public IndexStatus mergeIndex(final String schemaName, final String indexName, String mergedIndex,
			final Map<String, String> commitUserData) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("merge")
				.path(mergedIndex)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(commitUserData, SmileMediaTypes.APPLICATION_JACKSON_SMILE), IndexStatus.class);
	}

	@Override
	public IndexCheckStatus checkIndex(final String schemaName, final String indexName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("check")
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(null, SmileMediaTypes.APPLICATION_JACKSON_SMILE), IndexCheckStatus.class);
	}

	@Override
	public boolean deleteIndex(final String schemaName, final String indexName) {
		return indexTarget.path(schemaName).path(indexName).request(MediaType.TEXT_PLAIN).delete(boolean.class);
	}

	@Override
	public SortedMap<String, SortedMap<String, BackupStatus>> doBackup(final String schemaName, final String indexName,
			final String backupName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("backup")
				.path(backupName)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(null, SmileMediaTypes.APPLICATION_JACKSON_SMILE),
						mapStringMapStringBackupStatusType);
	}

	@Override
	public SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>> getBackups(final String schemaName,
			final String indexName, final String backupName, final Boolean extractVersion) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("backup")
				.path(backupName)
				.queryParam("extractVersion", extractVersion == null ? false : extractVersion)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.get(mapStringMapStringMapStringBackupStatusType);
	}

	@Override
	public Integer deleteBackups(final String schemaName, final String indexName, final String backupName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("backup")
				.path(backupName)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.delete(Integer.class);
	}

	@Override
	public InputStream replicationObtain(final String schemaName, final String indexName, final String masterUuid,
			final String sessionID, final String source, final String fileName) {
		return new AutoCloseInputStream(indexTarget.path(schemaName)
				.path(indexName)
				.path("replication")
				.path(masterUuid)
				.path(sessionID)
				.path(source)
				.path(fileName)
				.request(MediaType.APPLICATION_OCTET_STREAM)
				.get(InputStream.class));
	}

	@Override
	public boolean replicationRelease(final String schemaName, final String indexName, final String masterUuid,
			final String sessionID) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("replication")
				.path(masterUuid)
				.path(sessionID)
				.request(MediaType.TEXT_PLAIN)
				.delete(boolean.class);
	}

	@Override
	public ReplicationSession replicationUpdate(final String schemaName, final String indexName,
			final String masterUuid, final String currentVersion) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("replication")
				.path(masterUuid)
				.queryParam("current_version", currentVersion)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.get(ReplicationSession.class);
	}

	@Override
	public ReplicationStatus replicationCheck(final String schemaName, final String indexName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("replication")
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.get(ReplicationStatus.class);
	}

	@Override
	public LinkedHashMap<String, IndexInstance.ResourceInfo> getResources(final String schemaName,
			final String indexName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("resources")
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.get(mapStringResourceInfoType);
	}

	@Override
	public InputStream getResource(final String schemaName, final String indexName, final String resourceName) {
		return new AutoCloseInputStream(indexTarget.path(schemaName)
				.path(indexName)
				.path("resources")
				.path(resourceName)
				.request(MediaType.APPLICATION_OCTET_STREAM)
				.get(InputStream.class));
	}

	@Override
	public boolean postResource(final String schemaName, final String indexName, final String resourceName,
			final Long lastModified, final InputStream inputStream) {
		WebTarget target = indexTarget.path(schemaName).path(indexName).path("resources").path(resourceName);
		if (lastModified != null)
			target = target.queryParam("lastModified", lastModified);
		return target.request(MediaType.TEXT_PLAIN).post(Entity.text(inputStream), boolean.class);
	}

	@Override
	public boolean deleteResource(final String schemaName, final String indexName, final String resourceName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("resources")
				.path(resourceName)
				.request(MediaType.TEXT_PLAIN)
				.delete(boolean.class);
	}

	@Override
	public Integer postMappedDocument(final String schemaName, final String indexName,
			final PostDefinition.Document post) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("doc")
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(post, SmileMediaTypes.APPLICATION_JACKSON_SMILE), Integer.class);
	}

	@Override
	public Integer postMappedDocuments(final String schemaName, final String indexName,
			final PostDefinition.Documents post) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("docs")
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(post, SmileMediaTypes.APPLICATION_JACKSON_SMILE), Integer.class);
	}

	@Override
	public Integer updateMappedDocValues(final String schemaName, final String indexName,
			final PostDefinition.Document post) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("doc")
				.path("values")
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(post, SmileMediaTypes.APPLICATION_JACKSON_SMILE), Integer.class);
	}

	@Override
	public Integer updateMappedDocsValues(final String schemaName, final String indexName,
			final PostDefinition.Documents post) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("docs")
				.path("values")
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(post, SmileMediaTypes.APPLICATION_JACKSON_SMILE), Integer.class);
	}

	@Override
	public boolean deleteAll(final String schemaName, final String indexName) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("docs")
				.request(MediaType.TEXT_PLAIN)
				.delete(boolean.class);
	}

	@Override
	public LinkedHashMap<String, Object> getDocument(final String schemaName, final String indexName,
			final String docId) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("doc")
				.path(docId)
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.get(mapStringObjectType);
	}

	@Override
	public List<Map<String, Object>> getDocuments(final String schemaName, final String indexName, final Integer start,
			final Integer rows) {
		WebTarget target = indexTarget.path(schemaName).path(indexName).path("doc");
		if (start != null)
			target = target.queryParam("start", start);
		if (rows != null)
			target = target.queryParam("rows", rows);
		return target.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE).get(listMapStringObjectType);
	}

	@Override
	public ResultDefinition.WithMap searchQuery(final String schemaName, final String indexName,
			final QueryDefinition query, final Boolean delete) {
		WebTarget target = indexTarget.path(schemaName).path(indexName).path("search");
		if (delete != null)
			target = target.queryParam("delete", delete);
		return target.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(query, SmileMediaTypes.APPLICATION_JACKSON_SMILE), ResultDefinition.WithMap.class);
	}

	@Override
	public ExplainDefinition explainQuery(final String schemaName, final String indexName, final QueryDefinition query,
			int docId) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("search")
				.path("explain")
				.path(Integer.toString(docId))
				.request(SmileMediaTypes.APPLICATION_JACKSON_SMILE)
				.post(Entity.entity(query, SmileMediaTypes.APPLICATION_JACKSON_SMILE), ExplainDefinition.class);
	}

	@Override
	public String explainQueryText(String schemaName, String indexName, QueryDefinition query, int docId) {
		return indexTarget.path(schemaName)
				.path(indexName)
				.path("search")
				.path("explain")
				.path(Integer.toString(docId))
				.request(MediaType.TEXT_PLAIN)
				.post(Entity.entity(query, SmileMediaTypes.APPLICATION_JACKSON_SMILE), String.class);
	}

	@Override
	public String explainQueryDot(String schemaName, String indexName, QueryDefinition query, int docId,
			Integer descriptionWrapSize) {
		WebTarget target = indexTarget.path(schemaName)
				.path(indexName)
				.path("search")
				.path("explain")
				.path(Integer.toString(docId));
		if (descriptionWrapSize != null)
			target = target.queryParam("wrap", descriptionWrapSize);
		return target.request(MEDIATYPE_TEXT_GRAPHVIZ)
				.post(Entity.entity(query, SmileMediaTypes.APPLICATION_JACKSON_SMILE), String.class);
	}

}
