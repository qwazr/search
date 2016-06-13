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

import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.UBuilder;
import com.qwazr.utils.http.HttpResponseEntityException;
import com.qwazr.utils.http.HttpUtils;
import com.qwazr.utils.json.client.JsonClientAbstract;
import com.qwazr.utils.server.RemoteService;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class IndexSingleClient extends JsonClientAbstract implements IndexServiceInterface {

	public IndexSingleClient(final RemoteService remote) {
		super(remote);
	}

	private final static String PATH = "/" + IndexServiceInterface.PATH;
	private final static String PATH_SLASH = PATH + "/";

	public final static TypeReference<Set<String>> SetStringTypeRef = new TypeReference<Set<String>>() {
	};

	@Override
	public SchemaSettingsDefinition createUpdateSchema(final String schema_name) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name);
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, SchemaSettingsDefinition.class, 200);
	}

	@Override
	public SchemaSettingsDefinition createUpdateSchema(final String schema_name,
			final SchemaSettingsDefinition settings) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name);
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, settings, null, SchemaSettingsDefinition.class, 200);
	}

	@Override
	public Set<String> getSchemas() {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH);
		final Request request = Request.Get(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, SetStringTypeRef, 200);
	}

	@Override
	public Response deleteSchema(final String schema_name) {
		try {
			final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name);
			final Request request = Request.Delete(uriBuilder.buildNoEx());
			final HttpResponse response = execute(request, null, null);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Set<String> getIndexes(final String schema_name) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name);
		final Request request = Request.Get(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, SetStringTypeRef, 200);
	}

	@Override
	public IndexStatus createUpdateIndex(final String schema_name, final String index_name) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, " / ", index_name);
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, IndexStatus.class, 200);
	}

	@Override
	public IndexStatus createUpdateIndex(final String schema_name, final String index_name,
			final IndexSettingsDefinition settings) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name);
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, settings, null, IndexStatus.class, 200);
	}

	@Override
	public LinkedHashMap<String, FieldDefinition> getFields(final String schema_name, final String index_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields");
		final Request request = Request.Get(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, FieldDefinition.MapStringFieldTypeRef, 200);
	}

	@Override
	public LinkedHashMap<String, FieldDefinition> setFields(final String schema_name, final String index_name,
			final LinkedHashMap<String, FieldDefinition> fields) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields");
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, fields, null, FieldDefinition.MapStringFieldTypeRef, 200);
	}

	@Override
	public List<TermDefinition> doAnalyzeQuery(final String schema_name, final String index_name,
			final String field_name, final String text) {
		final UBuilder uriBuilder = RemoteService
				.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields/", field_name,
						"/analyzer/query");
		uriBuilder.setParameter("text", text);
		final Request request = Request.Get(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, TermDefinition.ListTermDefinitionRef, 200);
	}

	@Override
	public List<TermDefinition> doAnalyzeIndex(final String schema_name, final String index_name,
			final String field_name, final String text) {
		final UBuilder uriBuilder = RemoteService
				.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields/", field_name,
						"/analyzer/index");
		uriBuilder.setParameter("text", text);
		final Request request = Request.Get(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, TermDefinition.ListTermDefinitionRef, 200);
	}

	@Override
	public List<TermEnumDefinition> doExtractTerms(final String schema_name, final String index_name,
			final String field_name, final Integer start, final Integer rows) {
		return this.doExtractTerms(schema_name, index_name, field_name, null, start, rows);
	}

	@Override
	public List<TermEnumDefinition> doExtractTerms(final String schema_name, final String index_name,
			final String field_name, final String prefix, final Integer start, final Integer rows) {
		final UBuilder uriBuilder = RemoteService
				.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields/", field_name, "/terms/",
						prefix);
		uriBuilder.setParameter("start", start).setParameter("rows", rows);
		final Request request = Request.Get(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, TermEnumDefinition.ListTermEnumDefinitionRef, 200);
	}

	@Override
	public FieldDefinition getField(final String schema_name, final String index_name, final String field_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields/", field_name);
		final Request request = Request.Get(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, FieldDefinition.class, 200);
	}

	@Override
	public FieldDefinition setField(final String schema_name, final String index_name, final String field_name,
			final FieldDefinition field) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields/", field_name);
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, field, null, FieldDefinition.class, 200);
	}

	@Override
	public Response deleteField(final String schema_name, final String index_name, final String field_name) {
		try {
			final UBuilder uriBuilder = RemoteService
					.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/fields/", field_name);
			final Request request = Request.Delete(uriBuilder.buildNoEx());
			final HttpResponse response = execute(request, null, null);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public LinkedHashMap<String, AnalyzerDefinition> getAnalyzers(final String schema_name, final String index_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/analyzers");
		final Request request = Request.Get(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, AnalyzerDefinition.MapStringAnalyzerTypeRef, 200);
	}

	@Override
	public AnalyzerDefinition getAnalyzer(final String schema_name, final String index_name,
			final String analyzer_name) {
		final UBuilder uriBuilder = RemoteService
				.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/analyzers/", analyzer_name);
		final Request request = Request.Get(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, AnalyzerDefinition.class, 200);
	}

	@Override
	public AnalyzerDefinition setAnalyzer(final String schema_name, final String index_name, final String analyzer_name,
			final AnalyzerDefinition analyzer) {
		final UBuilder uriBuilder = RemoteService
				.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/analyzers/", analyzer_name);
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, analyzer, null, AnalyzerDefinition.class, 200);
	}

	@Override
	public LinkedHashMap<String, AnalyzerDefinition> setAnalyzers(final String schema_name, final String index_name,
			final LinkedHashMap<String, AnalyzerDefinition> analyzers) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/analyzers");
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, analyzers, null, AnalyzerDefinition.MapStringAnalyzerTypeRef, 200);
	}

	@Override
	public Response deleteAnalyzer(final String schema_name, final String index_name, final String analyzer_name) {
		try {
			final UBuilder uriBuilder = RemoteService
					.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/analyzers/", analyzer_name);
			final Request request = Request.Delete(uriBuilder.buildNoEx());
			final HttpResponse response = execute(request, null, null);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public List<TermDefinition> testAnalyzer(final String schema_name, final String index_name,
			final String analyzer_name, final String text) {
		final UBuilder uriBuilder = RemoteService
				.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/analyzers/", analyzer_name);
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, text, null, TermDefinition.ListTermDefinitionRef, 200);
	}

	@Override
	public IndexStatus getIndex(final String schema_name, final String index_name) {
		final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name);
		final Request request = Request.Get(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, IndexStatus.class, 200);
	}

	@Override
	public Response deleteIndex(final String schema_name, final String index_name) {
		try {
			final UBuilder uriBuilder = RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name);
			final Request request = Request.Delete(uriBuilder.buildNoEx());
			final HttpResponse response = execute(request, null, null);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public BackupStatus doBackup(final String schema_name, final String index_name, final Integer keep_last_count) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/backup")
						.setParameterObject("keep_last", keep_last_count);
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, BackupStatus.class, 200);
	}

	public final static TypeReference<List<BackupStatus>> ListBackupStatusTypeRef =
			new TypeReference<List<BackupStatus>>() {
			};

	@Override
	public List<BackupStatus> getBackups(final String schema_name, final String index_name) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/backup");
		final Request request = Request.Get(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, ListBackupStatusTypeRef, 200);
	}

	@Override
	public InputStream replicationObtain(final String schema_name, final String index_name, final String sessionID,
			final String source, final String fileName) {
		try {
			final UBuilder uriBuilder = RemoteService
					.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/replication/", sessionID, "/",
							source, "/", fileName);
			final Request request = Request.Get(uriBuilder.buildNoEx());
			return execute(request, null, null).getEntity().getContent();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Response replicationRelease(final String schema_name, final String index_name, final String sessionID) {
		try {
			final UBuilder uriBuilder = RemoteService
					.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/replication/", sessionID);
			final Request request = Request.Delete(uriBuilder.buildNoEx());
			final HttpResponse response = execute(request, null, null);
			if (response == null)
				return Response.serverError().build();
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Response replicationUpdate(final String schema_name, final String index_name, final String current_version) {
		try {
			final UBuilder uriBuilder = RemoteService
					.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/replication/", current_version);
			final Request request = Request.Get(uriBuilder.buildNoEx());
			final HttpResponse response = execute(request, null, null);
			if (response == null)
				return Response.serverError().build();
			if (response.getStatusLine().getStatusCode() != 200)
				return Response.status(response.getStatusLine().getStatusCode()).build();
			return Response.ok(response.getEntity().getContent()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Response replicationCheck(final String schema_name, final String index_name) {
		try {
			final UBuilder uriBuilder =
					RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/replication");
			final Request request = Request.Get(uriBuilder.buildNoEx());
			final HttpResponse response = execute(request, null, null);
			if (response == null)
				return Response.serverError().build();
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	public final static TypeReference<Collection<Map<String, Object>>> CollectionMapStringObjectTypeRef =
			new TypeReference<Collection<Map<String, Object>>>() {
			};

	@Override
	public Integer postMappedDocument(final String schema_name, final String index_name,
			final Map<String, Object> document) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/doc");
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, document, null, Integer.class, 200);
	}

	@Override
	public Integer postMappedDocuments(final String schema_name, final String index_name,
			final Collection<Map<String, Object>> documents) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/docs");
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, documents, null, Integer.class, 200);
	}

	@Override
	public Integer updateMappedDocValues(final String schema_name, final String index_name,
			final Map<String, Object> document) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/doc/values");
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, document, null, Integer.class, 200);
	}

	@Override
	public Integer updateMappedDocsValues(final String schema_name, final String index_name,
			final Collection<Map<String, Object>> documents) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/docs/values");
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, documents, null, Integer.class, 200);
	}

	@Override
	public Response deleteAll(final String schema_name, final String index_name) {
		try {
			final UBuilder uriBuilder =
					RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/docs");
			final Request request = Request.Delete(uriBuilder.buildNoEx());
			final HttpResponse response = execute(request, null, null);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	public final static TypeReference<LinkedHashMap<String, Object>> MapStringObjectTypeRef =
			new TypeReference<LinkedHashMap<String, Object>>() {
			};

	@Override
	public LinkedHashMap<String, Object> getDocument(final String schema_name, final String index_name,
			final String doc_id) {
		Objects.requireNonNull(doc_id, "The document must not be empty");
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/doc/", doc_id);
		final Request request = Request.Get(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, MapStringObjectTypeRef, 200);
	}

	public final static TypeReference<ArrayList<LinkedHashMap<String, Object>>> ListMapStringObjectTypeRef =
			new TypeReference<ArrayList<LinkedHashMap<String, Object>>>() {
			};

	@Override
	public List<LinkedHashMap<String, Object>> getDocuments(final String schema_name, final String index_name,
			final Integer start, final Integer rows) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/doc");
		uriBuilder.addParameter("start", start == null ? null : start.toString())
				.addParameter("rows", rows == null ? null : rows.toString());
		final Request request = Request.Get(uriBuilder.buildNoEx());
		return commonServiceRequest(request, null, null, ListMapStringObjectTypeRef, 200);
	}

	@Override
	public ResultDefinition.WithMap searchQuery(final String schema_name, final String index_name,
			final QueryDefinition query, final Boolean delete) {
		final UBuilder uriBuilder =
				RemoteService.getNewUBuilder(remote, PATH_SLASH, schema_name, "/", index_name, "/search")
						.setParameterObject("delete", delete);
		final Request request = Request.Post(uriBuilder.buildNoEx());
		return commonServiceRequest(request, query, null, ResultDefinition.WithMap.class, 200);
	}

}
