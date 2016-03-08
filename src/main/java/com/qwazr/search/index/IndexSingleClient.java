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
import com.qwazr.utils.http.HttpResponseEntityException;
import com.qwazr.utils.http.HttpUtils;
import com.qwazr.utils.json.client.JsonClientAbstract;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndexSingleClient extends JsonClientAbstract implements IndexServiceInterface {

	public IndexSingleClient(String url, int msTimeOut) throws URISyntaxException {
		super(url, msTimeOut);
	}

	public final static TypeReference<Set<String>> SetStringTypeRef = new TypeReference<Set<String>>() {
	};

	@Override
	public SchemaSettingsDefinition createUpdateSchema(String schema_name) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name);
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, null, null, SchemaSettingsDefinition.class, 200);
	}

	@Override
	public SchemaSettingsDefinition createUpdateSchema(String schema_name, SchemaSettingsDefinition settings) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name);
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, settings, null, SchemaSettingsDefinition.class, 200);
	}

	@Override
	public Set<String> getSchemas() {
		UBuilder uriBuilder = new UBuilder("/indexes");
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, SetStringTypeRef, 200);
	}

	@Override
	public Response deleteSchema(String schema_name) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", schema_name);
			Request request = Request.Delete(uriBuilder.build());
			HttpResponse response = execute(request, null, null);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Set<String> getIndexes(String schema_name) {
		UBuilder uriBuilder = new UBuilder("/indexes/" + schema_name);
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, SetStringTypeRef, 200);
	}

	@Override
	public IndexStatus createUpdateIndex(String schema_name, String index_name) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name);
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, null, null, IndexStatus.class, 200);
	}

	@Override
	public IndexStatus createUpdateIndex(String schema_name, String index_name, IndexSettingsDefinition settings) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name);
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, settings, null, IndexStatus.class, 200);
	}

	@Override
	public LinkedHashMap<String, FieldDefinition> getFields(String schema_name, String index_name) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/fields");
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, FieldDefinition.MapStringFieldTypeRef, 200);
	}

	@Override
	public LinkedHashMap<String, FieldDefinition> setFields(String schema_name, String index_name,
			LinkedHashMap<String, FieldDefinition> fields) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/fields");
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, fields, null, FieldDefinition.MapStringFieldTypeRef, 200);
	}

	@Override
	public List<TermDefinition> doAnalyzeQuery(String schema_name, String index_name, String field_name, String text) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/fields/", field_name,
				"/analyzer/query");
		uriBuilder.setParameter("text", text);
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, TermDefinition.MapListTermDefinitionRef, 200);
	}

	@Override
	public List<TermDefinition> doAnalyzeIndex(String schema_name, String index_name, String field_name, String text) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/fields/", field_name,
				"/analyzer/index");
		uriBuilder.setParameter("text", text);
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, TermDefinition.MapListTermDefinitionRef, 200);
	}

	@Override
	public FieldDefinition getField(String schema_name, String index_name, String field_name) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/fields/", field_name);
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, FieldDefinition.class, 200);
	}

	@Override
	public FieldDefinition setField(String schema_name, String index_name, String field_name, FieldDefinition field) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/fields/", field_name);
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, field, null, FieldDefinition.class, 200);
	}

	@Override
	public Response deleteField(String schema_name, String index_name, String field_name) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/fields/", field_name);
			Request request = Request.Delete(uriBuilder.build());
			HttpResponse response = execute(request, null, null);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public LinkedHashMap<String, AnalyzerDefinition> getAnalyzers(String schema_name, String index_name) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/analyzers");
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, AnalyzerDefinition.MapStringAnalyzerTypeRef, 200);
	}

	@Override
	public AnalyzerDefinition getAnalyzer(String schema_name, String index_name, String analyzer_name) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/analyzers/", analyzer_name);
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, AnalyzerDefinition.class, 200);
	}

	@Override
	public AnalyzerDefinition setAnalyzer(String schema_name, String index_name, String analyzer_name,
			AnalyzerDefinition analyzer) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/analyzers/", analyzer_name);
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, analyzer, null, AnalyzerDefinition.class, 200);
	}

	@Override
	public LinkedHashMap<String, AnalyzerDefinition> setAnalyzers(String schema_name, String index_name,
			LinkedHashMap<String, AnalyzerDefinition> analyzers) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/analyzers");
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, analyzers, null, AnalyzerDefinition.MapStringAnalyzerTypeRef, 200);
	}

	@Override
	public Response deleteAnalyzer(String schema_name, String index_name, String analyzer_name) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/analyzers/", analyzer_name);
			Request request = Request.Delete(uriBuilder.build());
			HttpResponse response = execute(request, null, null);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public IndexStatus getIndex(String schema_name, String index_name) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name);
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, IndexStatus.class, 200);
	}

	@Override
	public Response deleteIndex(String schema_name, String index_name) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name);
			Request request = Request.Delete(uriBuilder.build());
			HttpResponse response = execute(request, null, null);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Response postDocument(String schema_name, String index_name, Map<String, Object> document) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/doc");
			Request request = Request.Post(uriBuilder.build());
			HttpResponse response = execute(request, document, null);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public BackupStatus doBackup(String schema_name, String index_name, Integer keep_last_count) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/backup")
				.setParameterObject("keep_last", keep_last_count);
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, null, null, BackupStatus.class, 200);
	}

	public final static TypeReference<List<BackupStatus>> ListBackupStatusTypeRef = new TypeReference<List<BackupStatus>>() {
	};

	@Override
	public List<BackupStatus> getBackups(String schema_name, String index_name) {
		UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/backup");
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, ListBackupStatusTypeRef, 200);
	}

	public final static TypeReference<List<Map<String, Object>>> ListMapStringObjectTypeRef = new TypeReference<List<Map<String, Object>>>() {
	};

	@Override
	public Response postDocuments(String schema_name, String index_name, List<Map<String, Object>> documents) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/docs");
			Request request = Request.Post(uriBuilder.build());
			HttpResponse response = execute(request, documents, null);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Response updateDocumentValues(String schema_name, String index_name, Map<String, Object> document) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/doc/values");
			Request request = Request.Post(uriBuilder.build());
			HttpResponse response = execute(request, document, null);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Response updateDocumentsValues(String schema_name, String index_name, List<Map<String, Object>> documents) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/docs/values");
			Request request = Request.Post(uriBuilder.build());
			HttpResponse response = execute(request, documents, null);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Response deleteAll(String schema_name, String index_name) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/docs");
			Request request = Request.Delete(uriBuilder.build());
			HttpResponse response = execute(request, null, null);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode()).build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	public final static TypeReference<LinkedHashMap<String, Object>> MapStringObjectTypeRef = new TypeReference<LinkedHashMap<String, Object>>() {
	};

	@Override
	public LinkedHashMap<String, Object> getDocument(String schema_name, String index_name, String doc_id) {
		final UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/doc/", doc_id);
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, null, MapStringObjectTypeRef, 200);
	}

	@Override
	public ResultDefinition searchQuery(String schema_name, String index_name, QueryDefinition query, Boolean delete) {
		final UBuilder uriBuilder = new UBuilder("/indexes/", schema_name, "/", index_name, "/search")
				.setParameterObject("delete", delete);
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, query, null, ResultDefinition.class, 200);
	}

}
