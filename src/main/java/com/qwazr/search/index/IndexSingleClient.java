/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.utils.http.HttpResponseEntityException;
import com.qwazr.utils.http.HttpUtils;
import com.qwazr.utils.json.client.JsonClientAbstract;

public class IndexSingleClient extends JsonClientAbstract implements
		IndexServiceInterface {

	IndexSingleClient(String url, int msTimeOut) throws URISyntaxException {
		super(url, msTimeOut);
	}

	public final static TypeReference<Set<String>> SetStringTypeRef = new TypeReference<Set<String>>() {
	};

	@Override
	public Set<String> getIndexes(Boolean local) {
		UBuilder uriBuilder = new UBuilder("/indexes").setParameters(local,
				null);
		Request request = Request.Get(uriBuilder.build());
		return (Set<String>) commonServiceRequest(request, null, msTimeOut,
				SetStringTypeRef, 200);
	}

	@Override
	public String getVersion() {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/version");
			Request request = Request.Get(uriBuilder.build());
			return (String) execute(request, null, msTimeOut, (Class<?>) null,
					200);
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e,
					Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public IndexStatus createIndex(String index_name, Boolean local,
			Map<String, FieldDefinition> fields) {
		UBuilder uriBuilder = new UBuilder("/indexes/", index_name)
				.setParameters(local, null);
		Request request = Request.Post(uriBuilder.build());
		if (fields == null)
			fields = Collections.emptyMap();
		return commonServiceRequest(request, fields, msTimeOut,
				IndexStatus.class, 200);
	}

	@Override
	public IndexStatus getIndex(String index_name) {
		UBuilder uriBuilder = new UBuilder("/indexes/", index_name);
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, msTimeOut,
				IndexStatus.class, 200);
	}

	@Override
	public Response deleteIndex(String index_name, Boolean local) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", index_name)
					.setParameters(local, null);
			Request request = Request.Delete(uriBuilder.build());
			HttpResponse response = execute(request, null, msTimeOut);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode())
					.build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e,
					Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public FieldDefinition createField(String index_name, String field_name,
			FieldDefinition field) {
		UBuilder uriBuilder = new UBuilder("/indexes/", index_name, "/fields/",
				field_name);
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, field, msTimeOut,
				FieldDefinition.class, 200);
	}

	public final static TypeReference<Map<String, FieldDefinition>> mapStringFieldDefinitionTypeRef = new TypeReference<Map<String, FieldDefinition>>() {
	};

	@Override
	public Map<String, FieldDefinition> createFields(String index_name,
			Map<String, FieldDefinition> fields) {
		UBuilder uriBuilder = new UBuilder("/indexes/", index_name, "/fields");
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, fields, msTimeOut,
				mapStringFieldDefinitionTypeRef, 200);
	}

	@Override
	public Response deleteField(String index_name, String field_name) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", index_name,
					"/fields/", field_name);
			Request request = Request.Delete(uriBuilder.build());
			HttpResponse response = execute(request, null, msTimeOut);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode())
					.build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e,
					Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Response postDocuments(String index_name,
			List<Map<String, FieldContent>> documents) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", index_name,
					"/documents");
			Request request = Request.Post(uriBuilder.build());
			HttpResponse response = execute(request, documents, msTimeOut);
			HttpUtils.checkStatusCodes(response, 200);
			return Response.status(response.getStatusLine().getStatusCode())
					.build();
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e,
					Status.INTERNAL_SERVER_ERROR);
		}
	}

	public final static TypeReference<List<ResultDefinition>> listResultDefinitionTypeRef = new TypeReference<List<ResultDefinition>>() {
	};

	@Override
	public List<ResultDefinition> findDocuments(String index_name,
			List<QueryDefinition> queries, Boolean delete) {
		UBuilder uriBuilder = new UBuilder("/indexes/", index_name, "/queries")
				.setParameterObject("delete", delete);
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, queries, msTimeOut,
				listResultDefinitionTypeRef, 200);
	}

	@Override
	public ResultDefinition findDocuments(String index_name,
			QueryDefinition query, Boolean delete) {
		UBuilder uriBuilder = new UBuilder("/indexes/", index_name, "/query")
				.setParameterObject("delete", delete);
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, query, msTimeOut,
				ResultDefinition.class, 200);
	}

}
