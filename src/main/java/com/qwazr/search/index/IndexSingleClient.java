/**
 * Copyright 2015 Emmanuel Keller / QWAZR
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
import com.qwazr.utils.http.HttpResponseEntityException;
import com.qwazr.utils.http.HttpUtils;
import com.qwazr.utils.json.client.JsonClientAbstract;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.URISyntaxException;
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
	public Set<String> getIndexes(Boolean local) {
		UBuilder uriBuilder = new UBuilder("/indexes").setParameters(local,
				null);
		Request request = Request.Get(uriBuilder.build());
		return (Set<String>) commonServiceRequest(request, null, msTimeOut,
				SetStringTypeRef, 200);
	}

	@Override
	public IndexStatus createUpdateIndex(String index_name, Boolean local, Map<String, FieldDefinition> fields) {
		UBuilder uriBuilder = new UBuilder("/indexes/", index_name).setParameters(local, null);
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, fields, msTimeOut, IndexStatus.class, 200);
	}

	@Override
	public IndexStatus getIndex(String index_name) {
		UBuilder uriBuilder = new UBuilder("/indexes/", index_name);
		Request request = Request.Get(uriBuilder.build());
		return commonServiceRequest(request, null, msTimeOut, IndexStatus.class, 200);
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
	public Response postDocument(String index_name,
								 Map<String, Object> document) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", index_name,
					"/doc");
			Request request = Request.Post(uriBuilder.build());
			HttpResponse response = execute(request, document, msTimeOut);
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
								  List<Map<String, Object>> documents) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", index_name,
					"/docs");
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

	@Override
	public Response deleteAll(String index_name, Boolean local) {
		try {
			UBuilder uriBuilder = new UBuilder("/indexes/", index_name, "/docs")
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
	public ResultDefinition searchQuery(String index_name, QueryDefinition query) {
		UBuilder uriBuilder = new UBuilder("/indexes/", index_name, "/search");
		Request request = Request.Post(uriBuilder.build());
		return commonServiceRequest(request, query, msTimeOut, ResultDefinition.class, 200);
	}

}
