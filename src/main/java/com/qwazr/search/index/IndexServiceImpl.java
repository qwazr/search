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
import com.qwazr.search.query.MatchAllDocsQuery;
import com.qwazr.search.query.TermQuery;
import com.qwazr.utils.server.ServerException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.replicator.SessionToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.*;

final class IndexServiceImpl implements IndexServiceInterface, AnnotatedServiceInterface {

	private static final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);

	private static final String QWAZR_INDEX_ROOT_USER;

	public final static IndexServiceImpl INSTANCE = new IndexServiceImpl();

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

	@Context
	private HttpServletResponse response;

	/**
	 * Check the right permissions
	 *
	 * @param schemaName
	 * @throws ServerException
	 */
	private void checkRight(final String schemaName) throws ServerException {
		if (QWAZR_INDEX_ROOT_USER == null)
			return;
		final Principal principal = request.getUserPrincipal();
		if (principal == null)
			throw new ServerException(Response.Status.UNAUTHORIZED);
		final String name = principal.getName();
		if (name == null)
			throw new ServerException(Response.Status.UNAUTHORIZED);
		if (name.equals(QWAZR_INDEX_ROOT_USER))
			return;
		if (name.equals(schemaName))
			return;
		throw new ServerException(Response.Status.UNAUTHORIZED);
	}

	@Override
	final public Set<String> getIndexes(final String schemaName) {
		try {
			checkRight(schemaName);
			return IndexManager.INSTANCE.get(schemaName).nameSet();
		} catch (ServerException e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public SchemaSettingsDefinition createUpdateSchema(final String schemaName) {
		try {
			checkRight(null);
			IndexManager.INSTANCE.createUpdate(schemaName, null);
			return IndexManager.INSTANCE.get(schemaName).getSettings();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public SchemaSettingsDefinition createUpdateSchema(final String schemaName,
			final SchemaSettingsDefinition settings) {
		try {
			checkRight(null);
			return IndexManager.INSTANCE.createUpdate(schemaName, settings);
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public Set<String> getSchemas() {
		try {
			checkRight(null);
			return IndexManager.INSTANCE.nameSet();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public Response deleteSchema(final String schemaName) {
		try {
			checkRight(null);
			IndexManager.INSTANCE.delete(schemaName);
			return Response.ok().build();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public IndexStatus createUpdateIndex(final String schemaName, final String indexName,
			final IndexSettingsDefinition settings) {
		try {
			checkRight(schemaName);
			return IndexManager.INSTANCE.get(schemaName).createUpdate(indexName, settings).getStatus();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public IndexStatus createUpdateIndex(final String schemaName, final String indexName) {
		return createUpdateIndex(schemaName, indexName, null);
	}

	@Override
	final public LinkedHashMap<String, FieldDefinition> getFields(final String schemaName, final String indexName) {
		try {
			checkRight(schemaName);
			return IndexManager.INSTANCE.get(schemaName).get(indexName, false).getFields();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public FieldDefinition getField(final String schemaName, final String indexName, final String fieldName) {
		try {
			checkRight(schemaName);
			Map<String, FieldDefinition> fieldMap =
					IndexManager.INSTANCE.get(schemaName).get(indexName, false).getFields();
			FieldDefinition fieldDef = (fieldMap != null) ? fieldMap.get(fieldName) : null;
			if (fieldDef == null)
				throw new ServerException(Response.Status.NOT_FOUND, "Field not found: " + fieldName);
			return fieldDef;
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	final public LinkedHashMap<String, FieldDefinition> setFields(final String schemaName, final String indexName,
			final LinkedHashMap<String, FieldDefinition> fields) {
		try {
			checkRight(schemaName);
			IndexManager.INSTANCE.get(schemaName).get(indexName, false).setFields(fields);
			return fields;
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	private List<TermDefinition> doAnalyzer(final String schemaName, final String indexName, final String fieldName,
			final String text, final boolean index) throws ServerException, IOException {
		checkRight(schemaName);
		IndexInstance indexInstance = IndexManager.INSTANCE.get(schemaName).get(indexName, false);
		final Analyzer analyzer =
				index ? indexInstance.getIndexAnalyzer(fieldName) : indexInstance.getQueryAnalyzer(fieldName);
		if (analyzer == null)
			throw new ServerException("No analyzer found for " + fieldName);
		return TermDefinition.buildTermList(analyzer, fieldName, text);
	}

	@Override
	final public List<TermDefinition> doAnalyzeIndex(final String schemaName, final String indexName,
			final String fieldName, final String text) {
		try {
			return doAnalyzer(schemaName, indexName, fieldName, text, true);
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public List<TermEnumDefinition> doExtractTerms(final String schemaName, final String indexName,
			final String fieldName, final Integer start, final Integer rows) {
		return doExtractTerms(schemaName, indexName, fieldName, null, start, rows);
	}

	@Override
	final public List<TermEnumDefinition> doExtractTerms(final String schemaName, final String indexName,
			final String fieldName, final String prefix, final Integer start, final Integer rows) {
		try {
			checkRight(schemaName);
			return IndexManager.INSTANCE.get(schemaName).get(indexName, false)
					.getTermsEnum(fieldName, prefix, start, rows);
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public List<TermDefinition> doAnalyzeQuery(final String schemaName, final String indexName,
			final String fieldName, final String text) {
		try {
			return doAnalyzer(schemaName, indexName, fieldName, text, false);
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public FieldDefinition setField(final String schemaName, final String indexName, final String fieldName,
			final FieldDefinition field) {
		try {
			checkRight(schemaName);
			IndexManager.INSTANCE.get(schemaName).get(indexName, false).setField(fieldName, field);
			return field;
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public Response deleteField(final String schemaName, final String indexName, final String fieldName) {
		try {
			checkRight(schemaName);
			IndexManager.INSTANCE.get(schemaName).get(indexName, false).deleteField(fieldName);
			return Response.ok().build();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public LinkedHashMap<String, AnalyzerDefinition> getAnalyzers(final String schemaName,
			final String indexName) {
		try {
			checkRight(schemaName);
			return IndexManager.INSTANCE.get(schemaName).get(indexName, false).getAnalyzers();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public AnalyzerDefinition getAnalyzer(final String schemaName, final String indexName,
			final String analyzerName) {
		try {
			checkRight(schemaName);
			final Map<String, AnalyzerDefinition> analyzerMap =
					IndexManager.INSTANCE.get(schemaName).get(indexName, false).getAnalyzers();
			final AnalyzerDefinition analyzerDef = (analyzerMap != null) ? analyzerMap.get(analyzerName) : null;
			if (analyzerDef == null)
				throw new ServerException(Response.Status.NOT_FOUND, "Analyzer not found: " + analyzerName);
			return analyzerDef;
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public AnalyzerDefinition setAnalyzer(final String schemaName, final String indexName,
			final String analyzerName, final AnalyzerDefinition analyzer) {
		try {
			checkRight(schemaName);
			IndexManager.INSTANCE.get(schemaName).get(indexName, false).setAnalyzer(analyzerName, analyzer);
			return analyzer;
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	final public LinkedHashMap<String, AnalyzerDefinition> setAnalyzers(final String schemaName, final String indexName,
			final LinkedHashMap<String, AnalyzerDefinition> analyzers) {
		try {
			checkRight(schemaName);
			IndexManager.INSTANCE.get(schemaName).get(indexName, false).setAnalyzers(analyzers);
			return analyzers;
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public Response deleteAnalyzer(final String schemaName, final String indexName, final String analyzerName) {
		try {
			checkRight(schemaName);
			IndexManager.INSTANCE.get(schemaName).get(indexName, false).deleteAnalyzer(analyzerName);
			return Response.ok().build();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public List<TermDefinition> testAnalyzer(final String schemaName, final String indexName,
			final String analyzerName, final String text) {
		try {
			checkRight(schemaName);
			return IndexManager.INSTANCE.get(schemaName).get(indexName, false).testAnalyzer(analyzerName, text);
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public IndexStatus getIndex(final String schemaName, final String indexName) {
		try {
			checkRight(schemaName);
			return IndexManager.INSTANCE.get(schemaName).get(indexName, false).getStatus();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public Response deleteIndex(final String schemaName, final String indexName) {
		try {
			checkRight(schemaName);
			IndexManager.INSTANCE.get(schemaName).delete(indexName);
			return Response.ok().build();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public Integer postMappedDocument(final String schemaName, final String indexName,
			final Map<String, Object> document) {
		try {
			checkRight(schemaName);
			return IndexManager.INSTANCE.get(schemaName).get(indexName, true).postMappedDocument(document);
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public Integer postMappedDocuments(final String schemaName, final String indexName,
			final Collection<Map<String, Object>> documents) {
		try {
			checkRight(schemaName);
			return IndexManager.INSTANCE.get(schemaName).get(indexName, true).postMappedDocuments(documents);
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public <T> int postDocument(final String schemaName, final String indexName, final Map<String, Field> fields,
			final T document) throws IOException, InterruptedException {
		checkRight(schemaName);
		return IndexManager.INSTANCE.get(schemaName).get(indexName, true).postDocument(fields, document);
	}

	@Override
	final public <T> int postDocuments(final String schemaName, final String indexName, final Map<String, Field> fields,
			final Collection<T> documents) throws IOException, InterruptedException {
		checkRight(schemaName);
		return IndexManager.INSTANCE.get(schemaName).get(indexName, true).postDocuments(fields, documents);
	}

	@Override
	final public Integer updateMappedDocValues(final String schemaName, final String indexName,
			final Map<String, Object> document) {
		try {
			checkRight(schemaName);
			return IndexManager.INSTANCE.get(schemaName).get(indexName, true).updateMappedDocValues(document);
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public Integer updateMappedDocsValues(final String schemaName, final String indexName,
			final Collection<Map<String, Object>> documents) {
		try {
			checkRight(schemaName);
			return IndexManager.INSTANCE.get(schemaName).get(indexName, true).updateMappedDocsValues(documents);
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public <T> int updateDocValues(final String schemaName, final String indexName,
			final Map<String, Field> fields, final T document) throws IOException, InterruptedException {
		checkRight(schemaName);
		return IndexManager.INSTANCE.get(schemaName).get(indexName, true).updateDocValues(fields, document);
	}

	@Override
	final public <T> int updateDocsValues(final String schemaName, final String indexName,
			final Map<String, Field> fields, final Collection<T> documents) throws IOException, InterruptedException {
		checkRight(schemaName);
		return IndexManager.INSTANCE.get(schemaName).get(indexName, true).updateDocsValues(fields, documents);
	}

	@Override
	final public BackupStatus doBackup(final String schemaName, final String indexName, final Integer keep_last_count) {
		try {
			checkRight(null);
			if ("*".equals(schemaName) && "*".equals(indexName)) {
				IndexManager.INSTANCE.backups(keep_last_count);
				return new BackupStatus();
			} else
				return IndexManager.INSTANCE.get(schemaName).get(indexName, false).backup(keep_last_count);
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public List<BackupStatus> getBackups(final String schemaName, final String indexName) {
		try {
			checkRight(null);
			return IndexManager.INSTANCE.get(schemaName).get(indexName, false).getBackups();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public InputStream replicationObtain(final String schemaName, final String indexName, final String sessionID,
			final String source, String fileName) {
		try {
			checkRight(null);
			return IndexManager.INSTANCE.get(schemaName).get(indexName, false).getReplicator()
					.obtainFile(sessionID, source, fileName);
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public Response replicationRelease(final String schemaName, final String indexName, final String sessionID) {
		try {
			checkRight(null);
			IndexManager.INSTANCE.get(schemaName).get(indexName, false).getReplicator().release(sessionID);
			return Response.ok().build();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public Response replicationUpdate(final String schemaName, final String indexName,
			final String currentVersion) {
		try {
			checkRight(null);
			final SessionToken token = IndexManager.INSTANCE.get(schemaName).get(indexName, false).getReplicator()
					.checkForUpdate(currentVersion);
			if (token == null)
				return Response.noContent().build();
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			final DataOutputStream dataOutput = new DataOutputStream(outputStream);
			token.serialize(dataOutput);
			return Response.ok(outputStream.toByteArray()).build();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public Response replicationCheck(final String schemaName, final String indexName) {
		try {
			checkRight(null);
			IndexManager.INSTANCE.get(schemaName).get(indexName, false).replicationCheck();
			return Response.ok().build();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public Response deleteAll(final String schemaName, final String indexName) {
		try {
			checkRight(schemaName);
			IndexManager.INSTANCE.get(schemaName).get(indexName, false).deleteAll();
			return Response.ok().build();
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	private QueryDefinition getDocumentQuery(final Object id) {
		final QueryBuilder builder = new QueryBuilder();
		builder.setQuery(new TermQuery(FieldDefinition.ID_FIELD, BytesRefUtils.fromAny(id)));
		builder.setRows(1);
		builder.addReturned_field("*");
		return builder.build();
	}

	private QueryDefinition getMatchAllDocQuery(final Integer start, final Integer rows) {
		final QueryBuilder builder = new QueryBuilder();
		builder.setQuery(new MatchAllDocsQuery());
		builder.setStart(start).setRows(rows);
		builder.addReturned_field("*");
		return builder.build();
	}

	private ResultDefinition doSearchMap(final String schemaName, final String indexName, final QueryDefinition query)
			throws InterruptedException, ReflectiveOperationException, QueryNodeException, ParseException, IOException {
		checkRight(schemaName);
		IndexInstance index = IndexManager.INSTANCE.get(schemaName).get(indexName, false);
		return index.search(query, ResultDocumentBuilder.MapBuilderFactory.INSTANCE);
	}

	private ResultDefinition doSearchObject(final String schemaName, final String indexName,
			final QueryDefinition query, final Map<String, Field> fields, final Class<?> indexDefinitionClass)
			throws InterruptedException, ReflectiveOperationException, QueryNodeException, ParseException, IOException {
		checkRight(schemaName);
		final IndexInstance index = IndexManager.INSTANCE.get(schemaName).get(indexName, false);
		return index
				.search(query, ResultDocumentBuilder.ObjectBuilderFactory.createFactory(fields, indexDefinitionClass));
	}

	@Override
	final public LinkedHashMap<String, Object> getDocument(final String schemaName, final String indexName,
			final String id) {
		try {
			if (id != null) {
				final ResultDefinition result = doSearchMap(schemaName, indexName, getDocumentQuery(id));
				if (result != null) {
					List<ResultDocumentMap> docs = result.getDocuments();
					if (docs != null && !docs.isEmpty())
						return docs.get(0).getFields();
				}
			}
			throw new ServerException(Response.Status.NOT_FOUND, "Document not found: " + id);

		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public List<LinkedHashMap<String, Object>> getDocuments(final String schemaName, final String indexName,
			final Integer start, final Integer rows) {
		try {
			ResultDefinition result = doSearchMap(schemaName, indexName, getMatchAllDocQuery(start, rows));
			if (result == null)
				throw new ServerException(Response.Status.NOT_FOUND, "No document found");
			List<LinkedHashMap<String, Object>> documents = new ArrayList<>();
			List<ResultDocumentMap> docs = result.getDocuments();
			if (docs != null)
				docs.forEach(resultDocument -> documents.add(resultDocument.fields));
			return documents;
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public <T> T getDocument(final String schemaName, final String indexName, final Object id,
			final Map<String, Field> fields, final Class<T> indexDefinitionClass) {
		try {
			final ResultDefinition result =
					doSearchObject(schemaName, indexName, getDocumentQuery(id), fields, indexDefinitionClass);
			if (result == null)
				return null;
			final List<ResultDocumentObject<T>> docs = result.getDocuments();
			if (docs == null || docs.isEmpty())
				return null;
			return docs.get(0).record;
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public <T> List<T> getDocuments(final String schemaName, final String indexName, final Integer start,
			final Integer rows, final Map<String, Field> fields, final Class<T> indexDefinitionClass) {
		try {
			final ResultDefinition result =
					doSearchObject(schemaName, indexName, getMatchAllDocQuery(start, rows), fields,
							indexDefinitionClass);
			if (result == null)
				throw new ServerException(Response.Status.NOT_FOUND, "No document found");
			final List<T> documents = new ArrayList<>();
			final List<ResultDocumentObject<T>> docs = result.getDocuments();
			if (docs != null)
				docs.forEach(resultDocument -> documents.add(resultDocument.record));
			return documents;
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public ResultDefinition.WithMap searchQuery(final String schemaName, final String indexName,
			final QueryDefinition query, final Boolean delete) {
		try {
			checkRight(schemaName);
			if ("*".equals(indexName))
				return (ResultDefinition.WithMap) IndexManager.INSTANCE.get(schemaName)
						.search(query, ResultDocumentBuilder.MapBuilderFactory.INSTANCE);
			final IndexInstance index = IndexManager.INSTANCE.get(schemaName).get(indexName, delete != null && delete);
			if (delete != null && delete)
				return index.deleteByQuery(query);
			else
				return (ResultDefinition.WithMap) index.search(query, ResultDocumentBuilder.MapBuilderFactory.INSTANCE);
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}

	@Override
	final public <T> ResultDefinition.WithObject<T> searchQuery(final String schemaName, final String indexName,
			final QueryDefinition query, final Map<String, Field> fields, final Class<T> indexDefinitionClass) {
		try {
			checkRight(schemaName);
			final ResultDocumentBuilder.ObjectBuilderFactory documentBuilerFactory =
					ResultDocumentBuilder.ObjectBuilderFactory.createFactory(fields, indexDefinitionClass);
			if ("*".equals(indexName))
				return (ResultDefinition.WithObject<T>) IndexManager.INSTANCE.get(schemaName)
						.search(query, documentBuilerFactory);
			final IndexInstance index = IndexManager.INSTANCE.get(schemaName).get(indexName, false);
			return (ResultDefinition.WithObject<T>) index.search(query, documentBuilerFactory);
		} catch (Exception e) {
			throw ServerException.getJsonException(logger, e);
		}
	}
}
