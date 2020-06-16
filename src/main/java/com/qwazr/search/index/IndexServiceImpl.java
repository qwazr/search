/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.databind.JsonNode;
import com.qwazr.binder.FieldMapWrapper;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.search.replication.ReplicationProcess;
import com.qwazr.search.replication.ReplicationSession;
import com.qwazr.server.AbstractServiceImpl;
import com.qwazr.server.ServerException;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.FunctionEx;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.MatchAllDocsQuery;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;
import java.util.logging.Logger;

final class IndexServiceImpl extends AbstractServiceImpl implements IndexServiceInterface, AnnotatedServiceInterface {

    private static final Logger LOGGER = LoggerUtils.getLogger(IndexServiceImpl.class);

    private static final String QWAZR_INDEX_ROOT_USER;

    private final IndexManager indexManager;

    static {
        String v = System.getProperty("QWAZR_INDEX_ROOT_USER");
        if (v == null)
            v = System.getenv("QWAZR_INDEX_ROOT_USER");
        if (v == null)
            v = System.getenv("QWAZR_ROOT_USER");
        QWAZR_INDEX_ROOT_USER = v;
        if (QWAZR_INDEX_ROOT_USER != null)
            LOGGER.info(() -> "QWAZR_ROOT_USER: " + QWAZR_INDEX_ROOT_USER);
    }

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    IndexServiceImpl(final IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    /**
     * Check the right permissions
     *
     * @param schemaName the name of the schema
     */
    private void checkRight(final String schemaName) {
        if (QWAZR_INDEX_ROOT_USER == null || request == null)
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
    final public Map<String, UUID> getIndexes(final String schemaName) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).getIndexMap();
        } catch (ServerException e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public SchemaSettingsDefinition createUpdateSchema(final String schemaName) {
        try {
            checkRight(null);
            indexManager.createUpdate(schemaName, null);
            return indexManager.get(schemaName).getSettings();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public SchemaSettingsDefinition createUpdateSchema(final String schemaName,
                                                             final SchemaSettingsDefinition settings) {
        try {
            checkRight(null);
            return indexManager.createUpdate(schemaName, settings);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public Set<String> getSchemas() {
        try {
            checkRight(null);
            return indexManager.nameSet();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public boolean deleteSchema(final String schemaName) {
        try {
            checkRight(null);
            indexManager.delete(schemaName);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public Response getSchema(final String schemaName) {
        try {
            checkRight(null);
            indexManager.get(schemaName);
            return Response.ok().build();
        } catch (Exception e) {
            throw ServerException.getTextException(LOGGER, e);
        }
    }

    @Override
    final public IndexStatus createUpdateIndex(final String schemaName, final String indexName,
                                               final IndexSettingsDefinition settings) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName)
                .createUpdate(indexName, settings == null ? IndexSettingsDefinition.EMPTY : settings)
                .getStatus();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public IndexStatus createUpdateIndex(final String schemaName, final String indexName) {
        return createUpdateIndex(schemaName, indexName, IndexSettingsDefinition.EMPTY);
    }

    @Override
    final public Map<String, FieldDefinition<?>> getFields(final String schemaName, final String indexName) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).get(indexName).getFields();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public FieldDefinition<?> getField(final String schemaName, final String indexName, final String fieldName) {
        try {
            checkRight(schemaName);
            Map<String, FieldDefinition<?>> fieldMap = indexManager.get(schemaName).get(indexName).getFields();
            final FieldDefinition<?> fieldDef = (fieldMap != null) ? fieldMap.get(fieldName) : null;
            if (fieldDef == null)
                throw new ServerException(Response.Status.NOT_FOUND,
                    "Field not found: " + fieldName + " - Schema/index:" + schemaName + '/' + indexName);
            return fieldDef;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    final public Map<String, FieldDefinition<?>> setFields(final String schemaName,
                                                           final String indexName,
                                                           final Map<String, FieldDefinition<?>> fields) {
        try {
            checkRight(schemaName);
            indexManager.get(schemaName).get(indexName).setFields(fields);
            return fields;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    private List<TermDefinition> doAnalyzer(final String schemaName,
                                            final String indexName,
                                            final String fieldName,
                                            final String text,
                                            final boolean index) throws IOException {
        checkRight(schemaName);
        final IndexInstance indexInstance = indexManager.get(schemaName).get(indexName);
        final FunctionEx<Analyzer, List<TermDefinition>, IOException> analyzerFunction = analyzer -> {
            if (analyzer == null)
                throw new ServerException(
                    "No analyzer found for " + fieldName + " - Schema/index: " + schemaName + '/' + indexName);
            return TermDefinition.buildTermList(analyzer, fieldName, text);
        };
        return index ?
            indexInstance.useIndexAnalyzer(fieldName, analyzerFunction) :
            indexInstance.useQueryAnalyzer(fieldName, analyzerFunction);

    }

    @Override
    final public List<TermDefinition> doAnalyzeIndex(final String schemaName, final String indexName,
                                                     final String fieldName, final String text) {
        try {
            return doAnalyzer(schemaName, indexName, fieldName, text, true);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public FieldStats getFieldStats(String schemaName, String indexName, String fieldName) {
        checkRight(schemaName);
        try {
            return indexManager.get(schemaName).get(indexName).getFieldStats(fieldName);
        } catch (IOException e) {
            throw ServerException.getJsonException(LOGGER, e);
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
            return indexManager.get(schemaName).get(indexName).getTermsEnum(fieldName, prefix, start, rows);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public List<TermDefinition> doAnalyzeQuery(final String schemaName, final String indexName,
                                                     final String fieldName, final String text) {
        try {
            return doAnalyzer(schemaName, indexName, fieldName, text, false);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public FieldDefinition<?> setField(final String schemaName,
                                             final String indexName,
                                             final String fieldName,
                                             final FieldDefinition<?> field) {
        try {
            checkRight(schemaName);
            indexManager.get(schemaName).get(indexName).setField(fieldName, field);
            return field;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public boolean deleteField(final String schemaName, final String indexName, final String fieldName) {
        try {
            checkRight(schemaName);
            indexManager.get(schemaName).get(indexName).deleteField(fieldName);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public Map<String, AnalyzerDefinition> getAnalyzers(final String schemaName,
                                                              final String indexName) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).get(indexName).getAnalyzers();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public AnalyzerDefinition getAnalyzer(final String schemaName,
                                                final String indexName,
                                                final String analyzerName) {
        try {
            checkRight(schemaName);
            final Map<String, AnalyzerDefinition> analyzerMap =
                indexManager.get(schemaName).get(indexName).getAnalyzers();
            final AnalyzerDefinition analyzerDef = (analyzerMap != null) ? analyzerMap.get(analyzerName) : null;
            if (analyzerDef == null)
                throw new ServerException(Response.Status.NOT_FOUND,
                    "Analyzer not found: " + analyzerName + " - Schema/index: " + schemaName + '/' + indexName);
            return analyzerDef;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public void refreshAnalyzers(final String schemaName, final String indexName) {
        try {
            checkRight(schemaName);
            indexManager.get(schemaName).get(indexName).refreshAnalyzers();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public AnalyzerDefinition setAnalyzer(final String schemaName, final String indexName,
                                                final String analyzerName, final AnalyzerDefinition analyzer) {
        try {
            checkRight(schemaName);
            indexManager.get(schemaName).get(indexName).setAnalyzer(analyzerName, analyzer);
            return analyzer;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public Map<String, AnalyzerDefinition> setAnalyzers(final String schemaName,
                                                              final String indexName,
                                                              final Map<String, AnalyzerDefinition> analyzers) {
        try {
            checkRight(schemaName);
            indexManager.get(schemaName).get(indexName).setAnalyzers(analyzers);
            return analyzers;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public boolean deleteAnalyzer(final String schemaName,
                                        final String indexName,
                                        final String analyzerName) {
        try {
            checkRight(schemaName);
            indexManager.get(schemaName).get(indexName).deleteAnalyzer(analyzerName);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public List<TermDefinition> testAnalyzer(final String schemaName,
                                                   final String indexName,
                                                   final String analyzerName,
                                                   final String text) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).get(indexName).testAnalyzer(analyzerName, text);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public String testAnalyzerDot(final String schemaName,
                                  final String indexName,
                                  final String analyzerName,
                                  final String text) {
        try {
            checkRight(schemaName);
            return TermDefinition.toDot(
                indexManager.get(schemaName).get(indexName).testAnalyzer(analyzerName, text));
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public IndexStatus getIndex(final String schemaName, final String indexName) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).get(indexName).getStatus();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public IndexSettingsDefinition getIndexSettings(String schemaName, String indexName) {
        checkRight(schemaName);
        return indexManager.get(schemaName).get(indexName).getSettings();
    }

    @Override
    final public IndexStatus mergeIndex(final String schemaName,
                                        final String indexName,
                                        final String mergedIndexName,
                                        final Map<String, String> commitUserData) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).mergeIndex(indexName, mergedIndexName, commitUserData);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public IndexCheckStatus checkIndex(String schemaName, String indexName) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).checkIndex(indexName);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public boolean deleteIndex(final String schemaName, final String indexName) {
        try {
            checkRight(schemaName);
            indexManager.get(schemaName).delete(indexName);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public Integer postMappedDocument(final String schemaName, final String indexName,
                                            final PostDefinition.Document post) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).get(indexName).postMappedDocument(post);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public Integer postJson(final String schemaName, final String indexName, final JsonNode jsonNode) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).get(indexName).postJsonNode(jsonNode);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public List<Map<String, Object>> getJsonSamples(final String schemaName,
                                                    final String indexName,
                                                    final Integer count) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).get(indexName).getJsonSamples(count == null ? 2 : count);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public Map<String, Object> getJsonSample(final String schemaName, final String indexName) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).get(indexName).getJsonSample();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public Integer postMappedDocuments(final String schemaName,
                                             final String indexName,
                                             final PostDefinition.Documents post) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).get(indexName).postMappedDocuments(post);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public <T> int postDocument(final String schemaName,
                                      final String indexName,
                                      final Map<String, Field> fields,
                                      final T document,
                                      final Map<String, String> commitUserData) throws IOException {
        checkRight(schemaName);
        return indexManager.get(schemaName).get(indexName).postDocument(fields, document, commitUserData);
    }

    @Override
    final public <T> int postDocuments(final String schemaName,
                                       final String indexName,
                                       final Map<String, Field> fields,
                                       final Collection<T> documents,
                                       final Map<String, String> commitUserData) throws IOException {
        checkRight(schemaName);
        return indexManager.get(schemaName).get(indexName).postDocuments(fields, documents, commitUserData);
    }

    @Override
    final public <T> int addDocument(final String schemaName,
                                     final String indexName,
                                     final Map<String, Field> fields,
                                     final T document,
                                     final Map<String, String> commitUserData) throws IOException {
        checkRight(schemaName);
        return indexManager.get(schemaName).get(indexName).postDocument(fields, document, commitUserData);
    }

    @Override
    final public <T> int addDocuments(final String schemaName,
                                      final String indexName,
                                      final Map<String, Field> fields,
                                      final Collection<T> documents,
                                      final Map<String, String> commitUserData) throws IOException {
        checkRight(schemaName);
        return indexManager.get(schemaName).get(indexName).postDocuments(fields, documents, commitUserData);
    }

    @Override
    final public Integer updateMappedDocValues(final String schemaName,
                                               final String indexName,
                                               final PostDefinition.Document post) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).get(indexName).updateMappedDocValues(post);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public Integer updateMappedDocsValues(final String schemaName,
                                                final String indexName,
                                                final PostDefinition.Documents post) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).get(indexName).updateMappedDocsValues(post);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public <T> int updateDocValues(final String schemaName,
                                         final String indexName,
                                         final Map<String, Field> fields,
                                         final T document,
                                         final Map<String, String> commitUserData)
        throws IOException {
        checkRight(schemaName);
        return indexManager.get(schemaName).get(indexName).updateDocValues(fields, document, commitUserData);
    }

    @Override
    final public <T> int updateDocsValues(final String schemaName,
                                          final String indexName,
                                          final Map<String, Field> fields,
                                          final Collection<T> documents,
                                          final Map<String, String> commitUserData)
        throws IOException {
        checkRight(schemaName);
        return indexManager.get(schemaName).get(indexName).updateDocsValues(fields, documents, commitUserData);
    }

    @Override
    final public SortedMap<String, SortedMap<String, BackupStatus>> doBackup(
        final String schemaName, final String indexName, final String backupName) {
        checkRight(null);
        try {
            return indexManager.backups(schemaName, indexName, backupName);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>> getBackups(final String schemaName,
                                                                                            final String indexName,
                                                                                            final String backupName,
                                                                                            final Boolean extractVersion) {
        try {
            checkRight(null);
            return indexManager.getBackups(schemaName, indexName, backupName, extractVersion != null && extractVersion);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public Integer deleteBackups(final String schemaName,
                                 final String indexName,
                                 final String backupName) {
        try {
            checkRight(null);
            return indexManager.deleteBackups(schemaName, indexName, backupName);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public InputStream replicationObtain(final String schemaName,
                                               final String indexName,
                                               final String sessionID,
                                               final String source,
                                               final String fileName) {
        try {
            checkRight(null);
            final InputStream input = indexManager.get(schemaName)
                .get(indexName)
                .replicationObtain(sessionID, ReplicationProcess.Source.valueOf(source), fileName);
            if (input == null)
                throw new ServerException(Response.Status.NOT_FOUND,
                    "File not found: " + fileName + " - Schema/index: " + schemaName + '/' + indexName);
            return new AutoCloseInputStream(input);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public boolean replicationRelease(final String schemaName,
                                            final String indexName,
                                            final String sessionID) {
        try {
            checkRight(null);
            indexManager.get(schemaName).get(indexName).replicationRelease(sessionID);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public ReplicationSession replicationUpdate(final String schemaName,
                                                      final String indexName,
                                                      final String currentVersion) {
        try {
            checkRight(null);
            return indexManager.get(schemaName).get(indexName).replicationUpdate(currentVersion);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public ReplicationStatus replicationCheck(final String schemaName,
                                                    final String indexName) {
        checkRight(null);
        try {
            LOGGER.info(() -> "Start replication " + schemaName + '/' + indexName);
            final ReplicationStatus status = indexManager.get(schemaName).get(indexName).replicationCheck();
            LOGGER.info(() -> "End replication " + schemaName + '/' + indexName + " - time: " + status.time +
                "ms - size: " + status.size);
            return status;
        } catch (final Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public Map<String, IndexInstance.ResourceInfo> getResources(final String schemaName,
                                                                final String indexName) {
        try {
            checkRight(null);
            return indexManager.get(schemaName).get(indexName).getResources();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public InputStream getResource(final String schemaName,
                                   final String indexName,
                                   final String resourceName) {
        try {
            checkRight(null);
            final InputStream input = indexManager.get(schemaName).get(indexName).getResource(resourceName);
            if (input == null)
                throw new ServerException(Response.Status.NOT_FOUND,
                    "Resource not found: " + resourceName + " - Schema/index: " + schemaName + '/' + indexName);
            return new AutoCloseInputStream(input);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public boolean postResource(final String schemaName,
                                final String indexName,
                                final String resourceName,
                                final Long lastModified,
                                final InputStream inputStream) {
        try {
            checkRight(null);
            indexManager.get(schemaName).get(indexName).postResource(resourceName, lastModified, inputStream);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public boolean deleteResource(final String schemaName, final String indexName, final String resourceName) {
        try {
            checkRight(null);
            indexManager.get(schemaName).get(indexName).deleteResource(resourceName);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public boolean deleteAll(final String schemaName, final String indexName) {
        try {
            checkRight(schemaName);
            indexManager.get(schemaName).get(indexName).deleteAll(null);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    private QueryDefinition getMatchAllDocQuery(final Integer start,
                                                final Integer rows) {
        return QueryDefinition.of(new MatchAllDocsQuery()).start(start).rows(rows).returnedField("*").build();
    }

    private ResultDefinition.WithMap doSearchMap(final String schemaName, final String indexName, final QueryDefinition query)
        throws IOException {
        checkRight(schemaName);
        return indexManager.get(schemaName).get(indexName).query(context -> context.searchMap(query));
    }

    private <T> ResultDefinition.WithObject<T> doSearchObject(final String schemaName,
                                                              final String indexName,
                                                              final QueryDefinition query,
                                                              final FieldMapWrapper<T> wrapper) throws IOException {
        checkRight(schemaName);
        return indexManager.get(schemaName).get(indexName).query(context -> context.searchObject(query, wrapper));
    }

    @Override
    final public Map<String, Object> getDocument(final String schemaName,
                                                 final String indexName,
                                                 final String id) {
        try {
            checkRight(schemaName);
            if (StringUtils.isEmpty(id))
                throw new ServerException(Response.Status.NOT_ACCEPTABLE,
                    "The id is empty - Schema/index: " + schemaName + '/' + indexName);
            final ResultDefinition.WithMap result = indexManager.get(schemaName).get(indexName).getDocument(id);
            if (result != null) {
                final List<ResultDocumentMap> docs = result.getDocuments();
                if (docs != null && !docs.isEmpty())
                    return docs.get(0).getFields();
            }
            throw new ServerException(Response.Status.NOT_FOUND,
                "Document not found: " + id + " - Schema/index: " + schemaName + '/' + indexName);

        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public List<Map<String, Object>> getDocuments(final String schemaName,
                                                        final String indexName,
                                                        final Integer start,
                                                        final Integer rows) {
        try {
            final ResultDefinition.WithMap result = doSearchMap(schemaName, indexName, getMatchAllDocQuery(start, rows));
            if (result == null)
                throw new ServerException(Response.Status.NOT_FOUND,
                    "No document found" + " - Schema/index: " + schemaName + '/' + indexName);
            final List<Map<String, Object>> documents = new ArrayList<>();
            final List<ResultDocumentMap> docs = result.getDocuments();
            if (docs != null)
                docs.forEach(resultDocument -> documents.add(resultDocument.fields));
            return documents;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public <T> T getDocument(final String schemaName,
                                   final String indexName,
                                   final Object id,
                                   final FieldMapWrapper<T> wrapper) {
        try {
            checkRight(schemaName);
            if (id == null)
                throw new ServerException(Response.Status.NOT_ACCEPTABLE,
                    "The id is null - Schema/index: " + schemaName + '/' + indexName);
            final ResultDefinition.WithObject<T> result = indexManager.get(schemaName).get(indexName).getDocument(id, wrapper);
            if (result == null)
                return null;
            final List<ResultDocumentObject<T>> docs = result.getDocuments();
            if (docs == null || docs.isEmpty())
                return null;
            return docs.get(0).record;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public <T> List<T> getDocuments(final String schemaName,
                                          final String indexName,
                                          final Integer start,
                                          final Integer rows, final FieldMapWrapper<T> wrapper) {
        try {
            final ResultDefinition.WithObject<T> result =
                doSearchObject(schemaName, indexName, getMatchAllDocQuery(start, rows), wrapper);
            if (result == null)
                throw new ServerException(Response.Status.NOT_FOUND,
                    "No document found" + " - Schema/index: " + schemaName + '/' + indexName);
            final List<T> documents = new ArrayList<>();
            final List<ResultDocumentObject<T>> docs = result.getDocuments();
            if (docs != null)
                docs.forEach(resultDocument -> documents.add(resultDocument.record));
            return documents;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public ResultDefinition.WithMap searchQuery(final String schemaName,
                                                      final String indexName,
                                                      final QueryDefinition query,
                                                      final Boolean delete) {
        try {
            checkRight(schemaName);
            final IndexInstance index = indexManager.get(schemaName).get(indexName);
            if (delete != null && delete)
                return index.deleteByQuery(query);
            else
                return index.query(context -> context.searchMap(query));
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public <T> ResultDefinition.WithObject<T> searchQuery(final String schemaName,
                                                                final String indexName,
                                                                final QueryDefinition query,
                                                                final FieldMapWrapper<T> wrapper) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).get(indexName).query(context -> context.searchObject(query, wrapper));
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public ResultDefinition.Empty searchQuery(final String schemaName,
                                              final String indexName,
                                              final QueryDefinition query,
                                              final ResultDocumentsInterface resultDocuments) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).get(indexName).query(context -> context.searchInterface(query, resultDocuments));
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public ExplainDefinition explainQuery(final String schemaName, final String indexName, final QueryDefinition query,
                                          int docId) {
        try {
            checkRight(schemaName);
            final IndexInstance index = indexManager.get(schemaName).get(indexName);
            return new ExplainDefinition(index.explain(query, docId));
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public String explainQueryText(final String schemaName, final String indexName, final QueryDefinition query,
                                   final int docId) {
        try {
            checkRight(schemaName);
            final IndexInstance index = indexManager.get(schemaName).get(indexName);
            return index.explain(query, docId).toString();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public String explainQueryDot(final String schemaName, final String indexName, final QueryDefinition query,
                                  final int docId, final Integer descriptionWrapSize) {
        try {
            return ExplainDefinition.toDot(explainQuery(schemaName, indexName, query, docId),
                descriptionWrapSize == null ? 28 : descriptionWrapSize);
        } catch (IOException e) {
            throw ServerException.getTextException(LOGGER, e);
        }
    }

    @Override
    public <T> T query(final String schemaName, final String indexName, final QueryActions<T> actions) throws IOException {
        checkRight(schemaName);
        return indexManager.get(schemaName).get(indexName).query(actions);
    }

    @Override
    public <T> T write(final String schemaName, final String indexName, final WriteActions<T> actions)
        throws IOException {
        checkRight(schemaName);
        final IndexInstance index = indexManager.get(schemaName).get(indexName);
        return index.write(actions);
    }

    @Override
    public Set<String> getQueryTypes(final String schemaName, final String indexName) {
        return AbstractQuery.TYPES.keySet();
    }

    @Override
    public AbstractQuery<?> getQuerySample(final String schemaName,
                                           final String indexName,
                                           final String queryType) {
        try {
            checkRight(schemaName);
            return indexManager.get(schemaName).getIndex(indexName).getQuerySample(queryType);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

}
