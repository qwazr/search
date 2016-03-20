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
import com.qwazr.search.query.TermQuery;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.analysis.Analyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class IndexServiceImpl implements IndexServiceInterface {

	private static final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);

	private static final String QWAZR_INDEX_ROOT_USER;

	static {
		String v = System.getProperty("QWAZR_INDEX_ROOT_USER");
		if (v == null)
			v = System.getenv("QWAZR_INDEX_ROOT_USER");
		if (v == null)
			v = System.getenv("QWAZR_ROOT_USER");
		QWAZR_INDEX_ROOT_USER = v;
		if (QWAZR_INDEX_ROOT_USER != null)
			logger.info("QWAZR_ROOT_USER: " + QWAZR_INDEX_ROOT_USER);
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
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public SchemaSettingsDefinition createUpdateSchema(String schema_name, SchemaSettingsDefinition settings) {
		try {
			checkRight(null);
			return IndexManager.INSTANCE.createUpdate(schema_name, settings);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Set<String> getSchemas() {
		try {
			checkRight(null);
			return IndexManager.INSTANCE.nameSet();
		} catch (Exception e) {
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
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public IndexStatus createUpdateIndex(String schema_name, String index_name, IndexSettingsDefinition settings) {
		try {
			checkRight(schema_name);
			return IndexManager.INSTANCE.get(schema_name).createUpdate(index_name, settings);
		} catch (Exception e) {
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
		} catch (Exception e) {
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
		} catch (Exception e) {
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
		} catch (Exception e) {
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
		return TermDefinition.buildTermList(analyzer, field_name, text);
	}

	@Override
	public List<TermDefinition> doAnalyzeIndex(String schema_name, String index_name, String field_name, String text) {
		try {
			return doAnalyzer(schema_name, index_name, field_name, text, true);
		} catch (Exception e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public List<TermDefinition> doAnalyzeQuery(String schema_name, String index_name, String field_name, String text) {
		try {
			return doAnalyzer(schema_name, index_name, field_name, text, false);
		} catch (Exception e) {
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
		} catch (Exception e) {
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
		} catch (Exception e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public LinkedHashMap<String, AnalyzerDefinition> getAnalyzers(String schema_name, String index_name) {
		try {
			checkRight(schema_name);
			return IndexManager.INSTANCE.get(schema_name).get(index_name).getAnalyzers();
		} catch (Exception e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public AnalyzerDefinition getAnalyzer(String schema_name, String index_name, String analyzer_name) {
		try {
			checkRight(schema_name);
			Map<String, AnalyzerDefinition> analyzerMap = IndexManager.INSTANCE.get(schema_name).get(index_name)
					.getAnalyzers();
			AnalyzerDefinition analyzerDef = (analyzerMap != null) ? analyzerMap.get(analyzer_name) : null;
			if (analyzerDef == null)
				throw new ServerException(Response.Status.NOT_FOUND, "Analyzer not found: " + analyzer_name);
			return analyzerDef;
		} catch (Exception e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public AnalyzerDefinition setAnalyzer(String schema_name, String index_name, String analyzer_name,
			AnalyzerDefinition analyzer) {
		try {
			checkRight(schema_name);
			IndexManager.INSTANCE.get(schema_name).get(index_name).setAnalyzer(analyzer_name, analyzer);
			return analyzer;
		} catch (Exception e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	public LinkedHashMap<String, AnalyzerDefinition> setAnalyzers(String schema_name, String index_name,
			LinkedHashMap<String, AnalyzerDefinition> analyzers) {
		try {
			checkRight(schema_name);
			IndexManager.INSTANCE.get(schema_name).get(index_name).setAnalyzers(analyzers);
			return analyzers;
		} catch (Exception e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Response deleteAnalyzer(String schema_name, String index_name, String analyzer_name) {
		try {
			checkRight(schema_name);
			IndexManager.INSTANCE.get(schema_name).get(index_name).deleteAnalyzer(analyzer_name);
			return Response.ok().build();
		} catch (Exception e) {
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
		} catch (Exception e) {
			if (logger.isWarnEnabled())
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
		} catch (Exception e) {
			if (logger.isWarnEnabled())
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
		} catch (Exception e) {
			if (logger.isWarnEnabled())
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
		} catch (Exception e) {
			if (logger.isWarnEnabled())
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
		} catch (Exception e) {
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
		} catch (Exception e) {
			if (logger.isWarnEnabled())
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
		} catch (Exception e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public List<BackupStatus> getBackups(String schema_name, String index_name) {
		try {
			checkRight(null);
			return IndexManager.INSTANCE.get(schema_name).get(index_name).getBackups();
		} catch (Exception e) {
			if (logger.isWarnEnabled())
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
		} catch (Exception e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}

	@Override
	public Map<String, Object> getDocument(String schema_name, String index_name, String doc_id) {
		try {
			checkRight(schema_name);
			IndexInstance index = IndexManager.INSTANCE.get(schema_name).get(index_name);
			QueryBuilder builder = new QueryBuilder();
			builder.setQuery(new TermQuery(FieldDefinition.ID_FIELD, doc_id));
			builder.setRows(1);
			Map<String, FieldDefinition> fields = index.getFields();
			if (fields != null)
				builder.addReturned_field(fields.keySet());
			ResultDefinition result = index.search(builder.build());
			if (result != null) {
				List<ResultDocument> docs = result.getDocuments();
				if (docs != null && !docs.isEmpty())
					return docs.get(0).getFields();
			}
			throw new ServerException(Response.Status.NOT_FOUND, "Document not found: " + doc_id);
		} catch (Exception e) {
			if (logger.isWarnEnabled())
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
		} catch (Exception e) {
			if (logger.isWarnEnabled())
				logger.warn(e.getMessage(), e);
			throw ServerException.getJsonException(e);
		}
	}
}
