/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import com.qwazr.utils.server.ServerException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndexServiceImpl implements IndexServiceInterface {

	private static final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);

	private static final String QWAZR_INDEX_ROOT_USER;

	static {
		String v = System.getProperty("QWAZR_INDEX_ROOT_USER");
		if (v == null)
			v = System.getenv("QWAZR_INDEX_ROOT_USER");
		QWAZR_INDEX_ROOT_USER = v;
	}

	@Context
	private HttpServletRequest request;

	/**
	 * Check the right permissions
	 *
	 * @param index_name
	 * @throws ServerException
	 */
	private void checkRight(String index_name) throws ServerException {
		if (QWAZR_INDEX_ROOT_USER == null)
			return;
		Principal principal = request.getUserPrincipal();
		if (principal == null)
			throw new ServerException(Response.Status.UNAUTHORIZED);
		String name = principal.getName();
		if (name == null)
			throw new ServerException(Response.Status.UNAUTHORIZED);
		if (name.equals(QWAZR_INDEX_ROOT_USER))
			return;
		if (name.equals(index_name))
			return;
		throw new ServerException(Response.Status.UNAUTHORIZED);
	}

	@Override
	public Set<String> getIndexes(Boolean local) {
		try {
			checkRight(null);
			return IndexManager.INSTANCE.nameSet();
		} catch (ServerException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
		// TODO Local
	}

	@Override
	public IndexStatus createUpdateIndex(String index_name, Boolean local, Map<String, FieldDefinition> fields) {
		try {
			checkRight(null);
			return IndexManager.INSTANCE.createUpdate(index_name, fields);
		} catch (ServerException | IOException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
		// TODO Local
	}

	@Override
	public IndexStatus getIndex(String index_name) {
		try {
			checkRight(index_name);
			return IndexManager.INSTANCE.get(index_name).getStatus();
		} catch (ServerException | IOException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response deleteIndex(String index_name, Boolean local) {
		try {
			checkRight(null);
			IndexManager.INSTANCE.delete(index_name);
			return Response.ok().build();
		} catch (ServerException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
		// TODO Local
	}

	@Override
	public SettingsDefinition getSettings(@PathParam("index_name") String index_name) {
		try {
			checkRight(index_name);
			return IndexManager.INSTANCE.get(index_name).getSettings();
		} catch (ServerException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public SettingsDefinition setSettings(@PathParam("index_name") String index_name, SettingsDefinition settings) {
		try {
			checkRight(null);
			IndexManager.INSTANCE.get(index_name).setSettings(settings);
			return settings;
		} catch (ServerException | IOException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response deleteSettings(@PathParam("index_name") String index_name) {
		try {
			checkRight(null);
			IndexManager.INSTANCE.get(index_name).setSettings(null);
			return Response.ok().build();
		} catch (ServerException | IOException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response postDocument(String index_name, Map<String, Object> document) {
		try {
			checkRight(index_name);
			if (document == null || document.isEmpty())
				return Response.notModified().build();
			IndexManager.INSTANCE.get(index_name).postDocument(document);
			return Response.ok().build();
		} catch (ServerException | IOException | IllegalArgumentException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response postDocuments(String index_name, List<Map<String, Object>> documents) {
		try {
			checkRight(index_name);
			if (documents == null || documents.isEmpty())
				return Response.notModified().build();
			IndexManager.INSTANCE.get(index_name).postDocuments(documents);
			return Response.ok().build();
		} catch (ServerException | IOException | IllegalArgumentException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response deleteAll(String index_name, Boolean local) {
		try {
			checkRight(index_name);
			IndexManager.INSTANCE.get(index_name).deleteAll();
			return Response.ok().build();
		} catch (ServerException | IOException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response deleteByQuery(String index_name, QueryDefinition query) {
		try {
			checkRight(index_name);
			IndexManager.INSTANCE.get(index_name).deleteByQuery(query);
			return Response.ok().build();
		} catch (ServerException | IOException | ParseException | QueryNodeException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public ResultDefinition searchQuery(String index_name, QueryDefinition query) {
		try {
			checkRight(index_name);
			return IndexManager.INSTANCE.get(index_name).search(query);
		} catch (ServerException | IOException | ParseException | QueryNodeException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public ResultDefinition mltQuery(String index_name, MltQueryDefinition query) {
		try {
			checkRight(index_name);
			return IndexManager.INSTANCE.get(index_name).mlt(query);
		} catch (ServerException | IOException | QueryNodeException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}
}
