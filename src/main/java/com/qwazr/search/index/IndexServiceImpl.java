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

import com.qwazr.utils.IOUtils;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.LinkedHashMap;
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
	public Set<String> getIndexes(String schema_name) {
		try {
			checkRight(schema_name);
			return IndexManager.INSTANCE.get(schema_name).nameSet();
		} catch (ServerException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public SchemaSettingsDefinition createUpdateSchema(String schema_name) {
		try {
			checkRight(null);
			IndexManager.INSTANCE.createUpdate(schema_name, null);
			return IndexManager.INSTANCE.get(schema_name).getSettings();
		} catch (ServerException | IOException | ReflectiveOperationException | InterruptedException | URISyntaxException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public SchemaSettingsDefinition createUpdateSchema(String schema_name, SchemaSettingsDefinition settings) {
		try {
			checkRight(null);
			return IndexManager.INSTANCE.createUpdate(schema_name, settings);
		} catch (ServerException | IOException | ReflectiveOperationException | InterruptedException | URISyntaxException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Set<String> getSchemas() {
		try {
			checkRight(null);
			return IndexManager.INSTANCE.nameSet();
		} catch (ServerException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response deleteSchema(String schema_name) {
		try {
			checkRight(null);
			IndexManager.INSTANCE.delete(schema_name);
			return Response.ok().build();
		} catch (ServerException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public IndexStatus createUpdateIndex(String schema_name, String index_name, IndexSettingsDefinition settings) {
		try {
			checkRight(schema_name);
			return IndexManager.INSTANCE.get(schema_name).createUpdate(index_name, settings);
		} catch (ServerException | IOException | ReflectiveOperationException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public IndexStatus createUpdateIndex(String schema_name, String index_name) {
		return createUpdateIndex(schema_name, index_name, null);
	}

	@Override
	public LinkedHashMap<String, FieldDefinition> getFields(String schema_name, String index_name) {
		try {
			checkRight(schema_name);
			return IndexManager.INSTANCE.get(schema_name).get(index_name).getFields();
		} catch (ServerException e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public FieldDefinition getField(String schema_name, String index_name, String field_name) {
		try {
			checkRight(schema_name);
			Map<String, FieldDefinition> fieldMap = IndexManager.INSTANCE.get(schema_name).get(index_name).getFields();
			FieldDefinition fieldDef = (fieldMap != null) ? fieldMap.get(field_name) : null;
			if (fieldDef == null)
				throw new ServerException(Response.Status.NOT_FOUND, "Field not found: " + field_name);
			return fieldDef;
		} catch (ServerException e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	public LinkedHashMap<String, FieldDefinition> setFields(String schema_name, String index_name,
			LinkedHashMap<String, FieldDefinition> fields) {
		try {
			checkRight(schema_name);
			IndexManager.INSTANCE.get(schema_name).get(index_name).setFields(fields);
			return fields;
		} catch (ServerException | IOException e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	private List<TermDefinition> doAnalyzer(String schema_name, String index_name, String field_name, String text,
			boolean index) throws ServerException, IOException {
		checkRight(schema_name);
		IndexInstance indexInstance = IndexManager.INSTANCE.get(schema_name).get(index_name);
		Analyzer analyzer = index ?
				indexInstance.getIndexAnalyzer(field_name) :
				indexInstance.getQueryAnalyzer(field_name);
		if (analyzer == null)
			throw new ServerException("No analyzer found for " + field_name);
		try {
			return TermDefinition.buildTermList(analyzer, field_name, text);
		} finally {
			IOUtils.closeQuietly(analyzer);
		}
	}

	@Override
	public List<TermDefinition> doAnalyzeIndex(String schema_name, String index_name, String field_name, String text) {
		try {
			return doAnalyzer(schema_name, index_name, field_name, text, true);
		} catch (ServerException | IOException e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public List<TermDefinition> doAnalyzeQuery(String schema_name, String index_name, String field_name, String text) {
		try {
			return doAnalyzer(schema_name, index_name, field_name, text, false);
		} catch (ServerException | IOException e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public FieldDefinition setField(String schema_name, String index_name, String field_name, FieldDefinition field) {
		try {
			checkRight(schema_name);
			IndexManager.INSTANCE.get(schema_name).get(index_name).setField(field_name, field);
			return field;
		} catch (ServerException | IOException e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response deleteField(String schema_name, String index_name, String field_name) {
		try {
			checkRight(schema_name);
			IndexManager.INSTANCE.get(schema_name).get(index_name).deleteField(field_name);
			return Response.ok().build();
		} catch (ServerException | IOException e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
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
	public Response deleteIndex(String schema_name, String index_name) {
		try {
			checkRight(schema_name);
			IndexManager.INSTANCE.get(schema_name).delete(index_name);
			return Response.ok().build();
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
	public Response updateDocumentValues(String schema_name, String index_name, Map<String, Object> document) {
		try {
			checkRight(schema_name);
			if (document == null || document.isEmpty())
				return Response.notModified().build();
			IndexManager.INSTANCE.get(schema_name).get(index_name).updateDocumentValues(document);
			return Response.ok().build();
		} catch (ServerException | IOException | IllegalArgumentException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response updateDocumentsValues(String schema_name, String index_name, List<Map<String, Object>> documents) {
		try {
			checkRight(schema_name);
			if (documents == null || documents.isEmpty())
				return Response.notModified().build();
			IndexManager.INSTANCE.get(schema_name).get(index_name).updateDocumentsValues(documents);
			return Response.ok().build();
		} catch (ServerException | IOException | IllegalArgumentException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public BackupStatus doBackup(String schema_name, String index_name, Integer keep_last_count) {
		try {
			checkRight(null);
			if ("*".equals(schema_name) && "*".equals(index_name)) {
				IndexManager.INSTANCE.backups(keep_last_count);
				return new BackupStatus();
			} else
				return IndexManager.INSTANCE.get(schema_name).get(index_name).backup(keep_last_count);
		} catch (ServerException | IOException | IllegalArgumentException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public List<BackupStatus> getBackups(String schema_name, String index_name) {
		try {
			checkRight(null);
			return IndexManager.INSTANCE.get(schema_name).get(index_name).getBackups();
		} catch (ServerException | IllegalArgumentException | InterruptedException e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response deleteAll(String schema_name, String index_name) {
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
	public ResultDefinition searchQuery(String schema_name, String index_name, QueryDefinition query, Boolean delete) {
		try {
			checkRight(schema_name);
			if ("*".equals(index_name))
				return IndexManager.INSTANCE.get(schema_name).search(query);
			IndexInstance index = IndexManager.INSTANCE.get(schema_name).get(index_name);
			if (delete != null && delete)
				return index.deleteByQuery(query);
			else
				return index.search(query);
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
