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

import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.server.AbstractStreamingOutput;
import com.qwazr.server.RemoteService;
import com.qwazr.server.client.JsonClientAbstract;
import com.qwazr.server.response.ResponseValidator;
import com.qwazr.utils.UBuilder;
import com.qwazr.utils.http.HttpRequest;
import org.apache.http.entity.ContentType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;

public class IndexSingleClient extends JsonClientAbstract implements IndexServiceInterface {

	public IndexSingleClient(final RemoteService remote) {
		super(remote);
	}

	private final static String PATH = "/" + IndexServiceInterface.PATH;
	private final static String PATH_SLASH = PATH + "/";

	public static final ContentType CONTENTTYPE_TEXT_GRAPHVIZ =
			ContentType.create(MEDIATYPE_TEXT_GRAPHVIZ, (Charset) null);

	private static final ResponseValidator valid200TextGraphviz =
			ResponseValidator.create().status(200).content(CONTENTTYPE_TEXT_GRAPHVIZ);

	@Override
	public SchemaSettingsDefinition createUpdateSchema(final String schema_name) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, null, null, SchemaSettingsDefinition.class, valid200Json);
	}

	@Override
	public SchemaSettingsDefinition createUpdateSchema(final String schema_name,
			final SchemaSettingsDefinition settings) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, settings, null, SchemaSettingsDefinition.class, valid200Json);
	}

	@Override
	public Set<String> getSchemas() {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH);
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, SetStringTypeRef, valid200Json);
	}

	@Override
	public Response deleteSchema(final String schema_name) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name);
		final HttpRequest request = HttpRequest.Delete(uriBuilder.buildNoEx());
		return Response.status(executeStatusCode(request, null, null, valid200)).build();
	}

	@Override
	public Set<String> getIndexes(final String schema_name) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name);
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, SetStringTypeRef, valid200Json);
	}

	@Override
	public IndexStatus createUpdateIndex(final String schema_name, final String index_name) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, null, null, IndexStatus.class, valid200Json);
	}

	@Override
	public IndexStatus createUpdateIndex(final String schema_name, final String index_name,
			final IndexSettingsDefinition settings) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, settings, null, IndexStatus.class, valid200Json);
	}

	@Override
	public LinkedHashMap<String, FieldDefinition> getFields(final String schema_name, final String index_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields");
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, FieldDefinition.MapStringFieldTypeRef, valid200Json);
	}

	@Override
	public LinkedHashMap<String, FieldDefinition> setFields(final String schema_name, final String index_name,
			final LinkedHashMap<String, FieldDefinition> fields) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields");
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, fields, null, FieldDefinition.MapStringFieldTypeRef, valid200Json);
	}

	@Override
	public List<TermDefinition> doAnalyzeQuery(final String schema_name, final String index_name,
			final String field_name, final String text) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields/", field_name,
						"/analyzer/query");
		uriBuilder.setParameter("text", text);
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, TermDefinition.ListTermDefinitionRef, valid200Json);
	}

	@Override
	public List<TermDefinition> doAnalyzeIndex(final String schema_name, final String index_name,
			final String field_name, final String text) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields/", field_name,
						"/analyzer/index");
		uriBuilder.setParameter("text", text);
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, TermDefinition.ListTermDefinitionRef, valid200Json);
	}

	@Override
	public FieldStats getFieldStats(String schema_name, String index_name, String field_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields/", field_name,
						"/stats/");
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, FieldStats.class, valid200Json);
	}

	@Override
	public List<TermEnumDefinition> doExtractTerms(final String schema_name, final String index_name,
			final String field_name, final Integer start, final Integer rows) {
		return this.doExtractTerms(schema_name, index_name, field_name, null, start, rows);
	}

	@Override
	public List<TermEnumDefinition> doExtractTerms(final String schema_name, final String index_name,
			final String field_name, final String prefix, final Integer start, final Integer rows) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields/", field_name,
						"/terms/", prefix);
		uriBuilder.setParameter("start", start).setParameter("rows", rows);
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, TermEnumDefinition.ListTermEnumDefinitionRef, valid200Json);
	}

	@Override
	public FieldDefinition getField(final String schema_name, final String index_name, final String field_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields/", field_name);
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, FieldDefinition.class, valid200Json);
	}

	@Override
	public FieldDefinition setField(final String schema_name, final String index_name, final String field_name,
			final FieldDefinition field) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields/", field_name);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, field, null, FieldDefinition.class, valid200Json);
	}

	@Override
	public Response deleteField(final String schema_name, final String index_name, final String field_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields/", field_name);
		final HttpRequest request = HttpRequest.Delete(uriBuilder.buildNoEx());
		return Response.status(executeStatusCode(request, null, null, valid200)).build();
	}

	@Override
	public LinkedHashMap<String, AnalyzerDefinition> getAnalyzers(final String schema_name, final String index_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/analyzers");
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, AnalyzerDefinition.MapStringAnalyzerTypeRef, valid200Json);
	}

	@Override
	public AnalyzerDefinition getAnalyzer(final String schema_name, final String index_name,
			final String analyzer_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/analyzers/",
						analyzer_name);
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, AnalyzerDefinition.class, valid200Json);
	}

	@Override
	public void refreshAnalyzers(String schema_name, String index_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/analyzers");
		final HttpRequest request = HttpRequest.Patch(uriBuilder.buildNoEx());
		execute(request, null, null, valid200);
	}

	@Override
	public AnalyzerDefinition setAnalyzer(final String schema_name, final String index_name, final String analyzer_name,
			final AnalyzerDefinition analyzer) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/analyzers/",
						analyzer_name);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, analyzer, null, AnalyzerDefinition.class, valid200Json);
	}

	@Override
	public LinkedHashMap<String, AnalyzerDefinition> setAnalyzers(final String schema_name, final String index_name,
			final LinkedHashMap<String, AnalyzerDefinition> analyzers) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/analyzers");
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, analyzers, null, AnalyzerDefinition.MapStringAnalyzerTypeRef, valid200Json);
	}

	@Override
	public Response deleteAnalyzer(final String schema_name, final String index_name, final String analyzer_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/analyzers/",
						analyzer_name);
		final HttpRequest request = HttpRequest.Delete(uriBuilder.buildNoEx());
		return Response.status(executeStatusCode(request, null, null, valid200)).build();
	}

	@Override
	public List<TermDefinition> testAnalyzer(final String schema_name, final String index_name,
			final String analyzer_name, final String text) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/analyzers/",
						analyzer_name);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, text, null, TermDefinition.ListTermDefinitionRef, valid200Json);
	}

	@Override
	public String testAnalyzerDot(final String schema_name, final String index_name, final String analyzer_name,
			final String text) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/analyzers/",
						analyzer_name + "/dot").setParameter("text", text);
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeString(request, null, null, valid200TextPlain);
	}

	@Override
	public IndexStatus getIndex(final String schema_name, final String index_name) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name);
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, IndexStatus.class, valid200Json);
	}

	@Override
	public IndexStatus mergeIndex(final String schema_name, final String index_name, String merged_index,
			final Map<String, String> commitUserData) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/merge/", merged_index);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, commitUserData, null, IndexStatus.class, valid200Json);
	}

	@Override
	public IndexCheckStatus checkIndex(String schema_name, String index_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/check");
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, null, null, IndexCheckStatus.class, valid200Json);
	}

	@Override
	public Response deleteIndex(final String schema_name, final String index_name) {

		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name);
		final HttpRequest request = HttpRequest.Delete(uriBuilder.buildNoEx());
		return Response.status(executeStatusCode(request, null, null, valid200)).build();
	}

	@Override
	public SortedMap<String, SortedMap<String, BackupStatus>> doBackup(final String schema_name,
			final String index_name, final String backup_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/backup/", backup_name);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, null, null, MapStringMapStringBackupStatusTypeRef, valid200Json);
	}

	@Override
	public SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>> getBackups(final String schema_name,
			final String index_name, final String backup_name, final Boolean extractVersion) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/backup/", backup_name)
						.setParameterObject("extractVersion", extractVersion);
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, MapStringMapStringMapStringBackupStatusTypeRef, valid200Json);
	}

	@Override
	public Integer deleteBackups(String schema_name, String index_name, String backup_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/backup/", backup_name);
		final HttpRequest request = HttpRequest.Delete(uriBuilder.buildNoEx());
		return executeJson(request, null, null, Integer.class, valid200Json);
	}

	@Override
	public AbstractStreamingOutput replicationObtain(final String schema_name, final String index_name,
			final String masterUuid, final String sessionID, final String source, final String fileName) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/replication/",
						masterUuid, "/", sessionID, "/", source, "/", fileName);
		return executeStream(HttpRequest.Get(uriBuilder.buildNoEx()), null, null, valid200Stream);
	}

	@Override
	public Response replicationRelease(final String schema_name, final String index_name, final String masterUuid,
			final String sessionID) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/replication/",
						masterUuid, "/", sessionID);
		final HttpRequest request = HttpRequest.Delete(uriBuilder.buildNoEx());
		return Response.status(executeStatusCode(request, null, null, valid200)).build();
	}

	@Override
	public AbstractStreamingOutput replicationUpdate(final String schema_name, final String index_name,
			final String masterUuid, final String current_version) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/replication/",
						masterUuid).setParameter("current_version", current_version);
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeStream(request, null, null, valid200204);
	}

	@Override
	public Response replicationCheck(final String schema_name, final String index_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/replication");
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return Response.status(executeStatusCode(request, null, null, valid200)).build();
	}

	@Override
	public LinkedHashMap<String, IndexInstance.ResourceInfo> getResources(final String schema_name,
			final String index_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/resources");
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, MapStringResourceInfoTypeRef, valid200Json);
	}

	@Override
	public AbstractStreamingOutput getResource(final String schema_name, final String index_name,
			final String resourceName) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/resources/",
						resourceName);
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeStream(request, null, null, valid200);
	}

	@Override
	public Response postResource(final String schema_name, final String index_name, final String resourceName,
			final long lastModified, final InputStream inputStream) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/resources/",
						resourceName).setParameter("lastModified", lastModified);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return Response.status(executeStatusCode(request, inputStream, null, valid200)).build();
	}

	@Override
	public Response deleteResource(final String schema_name, final String index_name, final String resourceName) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/resources/",
						resourceName);
		final HttpRequest request = HttpRequest.Delete(uriBuilder.buildNoEx());
		return Response.status(executeStatusCode(request, null, null, valid200)).build();
	}

	@Override
	public Integer postMappedDocument(final String schema_name, final String index_name,
			final PostDefinition.Document post) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/doc");
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, post, null, Integer.class, valid200Json);
	}

	@Override
	public Integer postMappedDocuments(final String schema_name, final String index_name,
			final PostDefinition.Documents post) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/docs");
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, post, null, Integer.class, valid200Json);
	}

	@Override
	public Integer updateMappedDocValues(final String schema_name, final String index_name,
			final PostDefinition.Document post) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/doc/values");
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, post, null, Integer.class, valid200Json);
	}

	@Override
	public Integer updateMappedDocsValues(final String schema_name, final String index_name,
			final PostDefinition.Documents post) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/docs/values");
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, post, null, Integer.class, valid200Json);
	}

	@Override
	public Response deleteAll(final String schema_name, final String index_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/docs");
		final HttpRequest request = HttpRequest.Delete(uriBuilder.buildNoEx());
		return Response.status(executeStatusCode(request, null, null, valid200)).build();
	}

	@Override
	public LinkedHashMap<String, ?> getDocument(final String schema_name, final String index_name,
			final String doc_id) {
		Objects.requireNonNull(doc_id, "The document must not be empty");
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/doc/", doc_id);
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, MapStringCaptureTypeRef, valid200Json);
	}

	@Override
	public List<Map<String, ?>> getDocuments(final String schema_name, final String index_name, final Integer start,
			final Integer rows) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/doc");
		uriBuilder.addParameter("start", start == null ? null : start.toString())
				.addParameter("rows", rows == null ? null : rows.toString());
		final HttpRequest request = HttpRequest.Get(uriBuilder.buildNoEx());
		return executeJson(request, null, null, ListMapStringCaptureTypeRef, valid200Json);
	}

	@Override
	public ResultDefinition.WithMap searchQuery(final String schema_name, final String index_name,
			final QueryDefinition query, final Boolean delete) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/search")
						.setParameterObject("delete", delete);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, query, null, ResultDefinition.WithMap.class, valid200Json);
	}

	@Override
	public ExplainDefinition explainQuery(final String schema_name, final String index_name,
			final QueryDefinition query, int docId) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/search/explain/",
						Integer.toString(docId));
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		return executeJson(request, query, null, ExplainDefinition.class, valid200Json);
	}

	@Override
	public String explainQueryText(String schema_name, String index_name, QueryDefinition query, int docId) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/search/explain/",
						Integer.toString(docId));
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		request.addHeader("Accept", MediaType.TEXT_PLAIN);
		return executeString(request, query, null, valid200TextPlain);
	}

	@Override
	public String explainQueryDot(String schemaName, String indexName, QueryDefinition query, int docId,
			Integer descriptionWrapSize) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schemaName, "/", indexName, "/search/explain/",
						Integer.toString(docId)).setParameter("wrap", descriptionWrapSize);
		final HttpRequest request = HttpRequest.Post(uriBuilder.buildNoEx());
		request.addHeader("Accept", MEDIATYPE_TEXT_GRAPHVIZ);
		return executeString(request, query, null, valid200TextGraphviz);
	}

}
