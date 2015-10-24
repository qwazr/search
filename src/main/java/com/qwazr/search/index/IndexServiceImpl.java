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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
		if (v == null)
			v = System.getenv("QWAZR_ROOT_USER");
		QWAZR_INDEX_ROOT_USER = v;
	}

	@Context
	private HttpServletRequest request;

	/**
	 * Check the right permissions
	 *
	 * @param schemaName
	 * @throws ServerException
	 */
	private void checkRight(String schemaName) throws ServerException {
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
		if (name.equals(schemaName))
			return;
		throw new ServerException(Response.Status.UNAUTHORIZED);
	}

	@Override
	public Set<String> getIndexes(String schema_name, Boolean local) {
		try {
			checkRight(schema_name);
			return IndexManager.INSTANCE.get(schema_name).nameSet();
		} catch (ServerException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
		// TODO Local
	}

	@Override
	public Response createUpdateSchema(String schema_name, Boolean local) {
		try {
			checkRight(null);
			final Response.ResponseBuilder rp;
			if (IndexManager.INSTANCE.createUpdate(schema_name))
				rp = Response.created(new URI(schema_name));
			else
				rp = Response.accepted();
			return rp.build();
		} catch (ServerException | IOException | URISyntaxException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
		// TODO Local
	}

	@Override
	public Set<String> getSchemas(Boolean local) {
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
	public Response deleteSchema(String schema_name, Boolean local) {
		try {
			checkRight(null);
			IndexManager.INSTANCE.delete(schema_name);
			return Response.ok().build();
		} catch (ServerException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
		// TODO Local
	}

	public ResultDefinition searchQuery(String schema_name, QueryDefinition query, Boolean local) {
		try {
			checkRight(schema_name);
			return IndexManager.INSTANCE.get(schema_name).search(query);
		} catch (ServerException | IOException | ParseException | QueryNodeException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
		// TODO Local
	}

	@Override
	public IndexStatus createUpdateIndex(String schema_name, String index_name, Boolean local,
					Map<String, FieldDefinition> fields) {
		try {
			checkRight(schema_name);
			return IndexManager.INSTANCE.get(schema_name).createUpdate(index_name, fields);
		} catch (ServerException | IOException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
		// TODO Local
	}

	@Override
	public IndexStatus getIndex(String schema_name, String index_name) {
		try {
			checkRight(schema_name);
			return IndexManager.INSTANCE.get(schema_name).get(index_name).getStatus();
		} catch (ServerException | IOException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response deleteIndex(String schema_name, String index_name, Boolean local) {
		try {
			checkRight(schema_name);
			IndexManager.INSTANCE.get(schema_name).delete(index_name);
			return Response.ok().build();
		} catch (ServerException | IOException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
		// TODO Local
	}

	@Override
	public SettingsDefinition getSettings(String schema_name) {
		try {
			checkRight(schema_name);
			return IndexManager.INSTANCE.get(schema_name).getSettings();
		} catch (ServerException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public SettingsDefinition setSettings(String schema_name, SettingsDefinition settings) {
		try {
			checkRight(null);
			IndexManager.INSTANCE.get(schema_name).setSettings(settings);
			return settings;
		} catch (ServerException | IOException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response postDocument(String schema_name, String index_name, Map<String, Object> document) {
		try {
			checkRight(schema_name);
			if (document == null || document.isEmpty())
				return Response.notModified().build();
			IndexManager.INSTANCE.get(schema_name).get(index_name).postDocument(document);
			return Response.ok().build();
		} catch (ServerException | IOException | IllegalArgumentException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response postDocuments(String schema_name, String index_name, List<Map<String, Object>> documents) {
		try {
			checkRight(schema_name);
			if (documents == null || documents.isEmpty())
				return Response.notModified().build();
			IndexManager.INSTANCE.get(schema_name).get(index_name).postDocuments(documents);
			return Response.ok().build();
		} catch (ServerException | IOException | IllegalArgumentException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response deleteAll(String schema_name, String index_name, Boolean local) {
		try {
			checkRight(schema_name);
			IndexManager.INSTANCE.get(schema_name).get(index_name).deleteAll();
			return Response.ok().build();
		} catch (ServerException | IOException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response deleteByQuery(String schema_name, String index_name, QueryDefinition query) {
		try {
			checkRight(schema_name);
			IndexManager.INSTANCE.get(schema_name).get(index_name).deleteByQuery(query);
			return Response.ok().build();
		} catch (ServerException | IOException | ParseException | QueryNodeException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public ResultDefinition searchQuery(String schema_name, String index_name, QueryDefinition query) {
		try {
			checkRight(schema_name);
			return IndexManager.INSTANCE.get(schema_name).get(index_name).search(query);
		} catch (ServerException | IOException | ParseException | QueryNodeException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public ResultDefinition mltQuery(String schema_name, String index_name, MltQueryDefinition query) {
		try {
			checkRight(schema_name);
			return IndexManager.INSTANCE.get(schema_name).get(index_name).mlt(query);
		} catch (ServerException | IOException | QueryNodeException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}
}
