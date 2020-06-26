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
import com.qwazr.search.query.QueryInterface;
import com.qwazr.search.query.QuerySampler;
import com.qwazr.search.replication.ReplicationProcess;
import com.qwazr.search.replication.ReplicationSession;
import com.qwazr.server.AbstractServiceImpl;
import com.qwazr.server.ServerException;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.FunctionEx;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.text.similarity.JaccardDistance;
import org.apache.commons.text.similarity.LongestCommonSubsequence;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.MatchAllDocsQuery;

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
     */
    private void checkRight() {
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
        throw new ServerException(Response.Status.UNAUTHORIZED);
    }

    @Override
    final public Map<String, UUID> getIndexes() {
        try {
            checkRight();
            return indexManager.getIndexMap();
        } catch (ServerException e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public IndexStatus createUpdateIndex(final String indexName,
                                               final IndexSettingsDefinition settings) {
        try {
            checkRight();
            return indexManager.createUpdate(indexName, settings == null ? IndexSettingsDefinition.EMPTY : settings)
                .getStatus();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public IndexStatus createUpdateIndex(final String indexName) {
        return createUpdateIndex(indexName, IndexSettingsDefinition.EMPTY);
    }

    @Override
    final public Map<String, FieldDefinition> getFields(final String indexName) {
        try {
            checkRight();
            return indexManager.get(indexName).getFields();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public FieldDefinition getField(final String indexName, final String fieldName) {
        try {
            checkRight();
            Map<String, FieldDefinition> fieldMap = indexManager.get(indexName).getFields();
            final FieldDefinition fieldDef = (fieldMap != null) ? fieldMap.get(fieldName) : null;
            if (fieldDef == null)
                throw new ServerException(Response.Status.NOT_FOUND,
                    "Field not found: " + fieldName + " - Index:" + indexName);
            return fieldDef;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public Map<String, FieldDefinition> setFields(final String indexName,
                                                        final Map<String, FieldDefinition> fields) {
        try {
            checkRight();
            indexManager.get(indexName).setFields(fields);
            return fields;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    private List<TermDefinition> doAnalyzer(final String indexName,
                                            final String fieldName,
                                            final String text,
                                            final boolean index) throws IOException {
        checkRight();
        final IndexInstance indexInstance = indexManager.get(indexName);
        final FunctionEx<Analyzer, List<TermDefinition>, IOException> analyzerFunction = analyzer -> {
            if (analyzer == null)
                throw new ServerException(
                    "No analyzer found for " + fieldName + " - Index: " + indexName);
            return TermDefinition.buildTermList(analyzer, fieldName, text);
        };
        return index ?
            indexInstance.useIndexAnalyzer(fieldName, analyzerFunction) :
            indexInstance.useQueryAnalyzer(fieldName, analyzerFunction);

    }

    @Override
    final public List<TermDefinition> doAnalyzeIndex(final String indexName,
                                                     final String fieldName,
                                                     final String text) {
        try {
            return doAnalyzer(indexName, fieldName, text, true);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public FieldStats getFieldStats(final String indexName, final String fieldName) {
        checkRight();
        try {
            return indexManager.get(indexName).getFieldStats(fieldName);
        } catch (IOException e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public List<TermEnumDefinition> doExtractTerms(final String indexName,
                                                         final String fieldName,
                                                         final Integer start,
                                                         final Integer rows) {
        return doExtractTerms(indexName, fieldName, null, start, rows);
    }

    @Override
    final public List<TermEnumDefinition> doExtractTerms(final String indexName,
                                                         final String fieldName,
                                                         final String prefix,
                                                         final Integer start,
                                                         final Integer rows) {
        try {
            checkRight();
            return indexManager.get(indexName).getTermsEnum(fieldName, prefix, start, rows);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public List<TermDefinition> doAnalyzeQuery(final String indexName,
                                                     final String fieldName,
                                                     final String text) {
        try {
            return doAnalyzer(indexName, fieldName, text, false);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public FieldDefinition setField(final String indexName,
                                          final String fieldName,
                                          final FieldDefinition field) {
        try {
            checkRight();
            indexManager.get(indexName).setField(fieldName, field);
            return field;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public boolean deleteField(final String indexName,
                                     final String fieldName) {
        try {
            checkRight();
            indexManager.get(indexName).deleteField(fieldName);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public Map<String, AnalyzerDefinition> getAnalyzers(final String indexName) {
        try {
            checkRight();
            return indexManager.get(indexName).getAnalyzers();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public AnalyzerDefinition getAnalyzer(final String indexName,
                                                final String analyzerName) {
        try {
            checkRight();
            final Map<String, AnalyzerDefinition> analyzerMap =
                indexManager.get(indexName).getAnalyzers();
            final AnalyzerDefinition analyzerDef = (analyzerMap != null) ? analyzerMap.get(analyzerName) : null;
            if (analyzerDef == null)
                throw new ServerException(Response.Status.NOT_FOUND,
                    "Analyzer not found: " + analyzerName + " - Index: " + indexName);
            return analyzerDef;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public void refreshAnalyzers(final String indexName) {
        try {
            checkRight();
            indexManager.get(indexName).refreshAnalyzers();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public AnalyzerDefinition setAnalyzer(final String indexName,
                                                final String analyzerName,
                                                final AnalyzerDefinition analyzer) {
        try {
            checkRight();
            indexManager.get(indexName).setAnalyzer(analyzerName, analyzer);
            return analyzer;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public Map<String, AnalyzerDefinition> setAnalyzers(final String indexName,
                                                              final Map<String, AnalyzerDefinition> analyzers) {
        try {
            checkRight();
            indexManager.get(indexName).setAnalyzers(analyzers);
            return analyzers;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public boolean deleteAnalyzer(final String indexName,
                                        final String analyzerName) {
        try {
            checkRight();
            indexManager.get(indexName).deleteAnalyzer(analyzerName);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public List<TermDefinition> testAnalyzer(final String indexName,
                                                   final String analyzerName,
                                                   final String text) {
        try {
            checkRight();
            return indexManager.get(indexName).testAnalyzer(analyzerName, text);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public String testAnalyzerDot(final String indexName,
                                  final String analyzerName,
                                  final String text) {
        try {
            checkRight();
            return TermDefinition.toDot(
                indexManager.get(indexName).testAnalyzer(analyzerName, text));
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public IndexStatus getIndex(final String indexName) {
        try {
            checkRight();
            return indexManager.get(indexName).getStatus();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public IndexSettingsDefinition getIndexSettings(final String indexName) {
        checkRight();
        return indexManager.get(indexName).getSettings();
    }

    @Override
    final public IndexStatus mergeIndex(final String indexName,
                                        final String mergedIndexName,
                                        final Map<String, String> commitUserData) {
        try {
            checkRight();
            return indexManager.mergeIndex(indexName, mergedIndexName, commitUserData);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public IndexCheckStatus checkIndex(final String indexName) {
        try {
            checkRight();
            return indexManager.checkIndex(indexName);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public boolean deleteIndex(final String indexName) {
        try {
            checkRight();
            indexManager.delete(indexName);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public Integer postMappedDocument(final String indexName,
                                            final PostDefinition.Document post) {
        try {
            checkRight();
            return indexManager.get(indexName).postMappedDocument(post);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public Integer postJson(final String indexName, final JsonNode jsonNode) {
        try {
            checkRight();
            return indexManager.get(indexName).postJsonNode(jsonNode);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public List<Map<String, Object>> getJsonSamples(final String indexName,
                                                    final Integer count) {
        try {
            checkRight();
            return indexManager.get(indexName).getJsonSamples(count == null ? 2 : count);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public Map<String, Object> getJsonSample(final String indexName) {
        try {
            checkRight();
            return indexManager.get(indexName).getJsonSample();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public Integer postMappedDocuments(final String indexName,
                                             final PostDefinition.Documents post) {
        try {
            checkRight();
            return indexManager.get(indexName).postMappedDocuments(post);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public <T> int postDocument(final String indexName,
                                      final Map<String, Field> fields,
                                      final T document,
                                      final Map<String, String> commitUserData) throws IOException {
        checkRight();
        return indexManager.get(indexName).postDocument(fields, document, commitUserData);
    }

    @Override
    final public <T> int postDocuments(final String indexName,
                                       final Map<String, Field> fields,
                                       final Collection<T> documents,
                                       final Map<String, String> commitUserData) throws IOException {
        checkRight();
        return indexManager.get(indexName).postDocuments(fields, documents, commitUserData);
    }

    @Override
    final public <T> int addDocument(final String indexName,
                                     final Map<String, Field> fields,
                                     final T document,
                                     final Map<String, String> commitUserData) throws IOException {
        checkRight();
        return indexManager.get(indexName).postDocument(fields, document, commitUserData);
    }

    @Override
    final public <T> int addDocuments(final String indexName,
                                      final Map<String, Field> fields,
                                      final Collection<T> documents,
                                      final Map<String, String> commitUserData) throws IOException {
        checkRight();
        return indexManager.get(indexName).postDocuments(fields, documents, commitUserData);
    }

    @Override
    final public Integer updateMappedDocValues(final String indexName,
                                               final PostDefinition.Document post) {
        try {
            checkRight();
            return indexManager.get(indexName).updateMappedDocValues(post);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public Integer updateMappedDocsValues(final String indexName,
                                                final PostDefinition.Documents post) {
        try {
            checkRight();
            return indexManager.get(indexName).updateMappedDocsValues(post);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public <T> int updateDocValues(final String indexName,
                                         final Map<String, Field> fields,
                                         final T document,
                                         final Map<String, String> commitUserData)
        throws IOException {
        checkRight();
        return indexManager.get(indexName).updateDocValues(fields, document, commitUserData);
    }

    @Override
    final public <T> int updateDocsValues(final String indexName,
                                          final Map<String, Field> fields,
                                          final Collection<T> documents,
                                          final Map<String, String> commitUserData)
        throws IOException {
        checkRight();
        return indexManager.get(indexName).updateDocsValues(fields, documents, commitUserData);
    }

    @Override
    final public SortedMap<String, BackupStatus> doBackup(final String indexName,
                                                          final String backupName) {
        try {
            checkRight();
            return indexManager.backups(indexName, backupName);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public ReindexDefinition getReindexStatus(final String indexName) {
        try {
            checkRight();
            return indexManager.get(indexName).getReindexThread().getStatus();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public ReindexDefinition startReindex(final String indexName,
                                          final Integer bufferSize) {
        try {
            checkRight();
            return indexManager.get(indexName).getReindexThread().start(bufferSize == null ? 50 : bufferSize);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public ReindexDefinition stopReindex(final String indexName) {
        try {
            checkRight();
            return indexManager.get(indexName).getReindexThread().abort();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public SortedMap<String, SortedMap<String, BackupStatus>> getBackups(final String indexName,
                                                                         final String backupName,
                                                                         final Boolean extractVersion) {
        try {
            checkRight();
            return indexManager.getBackups(indexName, backupName, extractVersion != null && extractVersion);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public Integer deleteBackups(final String indexName,
                                 final String backupName) {
        try {
            checkRight();
            return indexManager.deleteBackups(indexName, backupName);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public InputStream replicationObtain(final String indexName,
                                               final String sessionID,
                                               final String source,
                                               final String fileName) {
        try {
            checkRight();
            final InputStream input = indexManager
                .get(indexName)
                .replicationObtain(sessionID, ReplicationProcess.Source.valueOf(source), fileName);
            if (input == null)
                throw new ServerException(Response.Status.NOT_FOUND,
                    "File not found: " + fileName + " - Index: " + indexName);
            return new AutoCloseInputStream(input);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public boolean replicationRelease(final String indexName,
                                            final String sessionID) {
        try {
            checkRight();
            indexManager.get(indexName).replicationRelease(sessionID);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public ReplicationSession replicationUpdate(final String indexName,
                                                      final String currentVersion) {
        try {
            checkRight();
            return indexManager.get(indexName).replicationUpdate(currentVersion);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public ReplicationStatus replicationCheck(final String indexName) {
        try {
            checkRight();
            LOGGER.info(() -> "Start replication on \"" + indexName + "\"");
            final ReplicationStatus status = indexManager.get(indexName).replicationCheck();
            LOGGER.info(() -> "End replication on \"" + indexName + "\" - time: " + status.time +
                "ms - size: " + status.size);
            return status;
        } catch (final Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public Map<String, IndexInstance.ResourceInfo> getResources(final String indexName) {
        try {
            checkRight();
            return indexManager.get(indexName).getResources();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public InputStream getResource(final String indexName,
                                   final String resourceName) {
        try {
            checkRight();
            final InputStream input = indexManager.get(indexName).getResource(resourceName);
            if (input == null)
                throw new ServerException(Response.Status.NOT_FOUND,
                    "Resource not found: " + resourceName + " - Index: " + indexName);
            return new AutoCloseInputStream(input);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public boolean postResource(final String indexName,
                                final String resourceName,
                                final Long lastModified,
                                final InputStream inputStream) {
        try {
            checkRight();
            indexManager.get(indexName).postResource(resourceName, lastModified, inputStream);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public boolean deleteResource(final String indexName, final String resourceName) {
        try {
            checkRight();
            indexManager.get(indexName).deleteResource(resourceName);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public boolean deleteAll(final String indexName) {
        try {
            checkRight();
            indexManager.get(indexName).deleteAll(null);
            return true;
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    private QueryDefinition getMatchAllDocQuery(final Integer start,
                                                final Integer rows) {
        return QueryDefinition.of(new MatchAllDocsQuery()).start(start).rows(rows).returnedField("*").build();
    }

    private ResultDefinition.WithMap doSearchMap(final String indexName, final QueryDefinition query)
        throws IOException {
        checkRight();
        return indexManager.get(indexName).query(context -> context.searchMap(query));
    }

    private <T> ResultDefinition.WithObject<T> doSearchObject(final String indexName,
                                                              final QueryDefinition query,
                                                              final FieldMapWrapper<T> wrapper) throws IOException {
        checkRight();
        return indexManager.get(indexName).query(context -> context.searchObject(query, wrapper));
    }

    @Override
    final public Map<String, Object> getDocument(final String indexName,
                                                 final String id) {
        try {
            checkRight();
            if (StringUtils.isEmpty(id))
                throw new ServerException(Response.Status.NOT_ACCEPTABLE,
                    "The id is empty - Index: " + indexName);
            final ResultDefinition.WithMap result = indexManager.get(indexName).getDocument(id);
            if (result != null) {
                final List<ResultDocumentMap> docs = result.getDocuments();
                if (docs != null && !docs.isEmpty())
                    return docs.get(0).getFields();
            }
            throw new ServerException(Response.Status.NOT_FOUND,
                "Document not found: " + id + " - Index: " + indexName);

        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public List<Map<String, Object>> getDocuments(final String indexName,
                                                        final Integer start,
                                                        final Integer rows) {
        try {
            final ResultDefinition.WithMap result = doSearchMap(indexName, getMatchAllDocQuery(start, rows));
            if (result == null)
                throw new ServerException(Response.Status.NOT_FOUND,
                    "No document found" + " - Index: " + indexName);
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
    final public <T> T getDocument(final String indexName,
                                   final Object id,
                                   final FieldMapWrapper<T> wrapper) {
        try {
            checkRight();
            if (id == null)
                throw new ServerException(Response.Status.NOT_ACCEPTABLE,
                    "The id is null - Index: " + indexName);
            final ResultDefinition.WithObject<T> result = indexManager.get(indexName).getDocument(id, wrapper);
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
    final public <T> List<T> getDocuments(final String indexName,
                                          final Integer start,
                                          final Integer rows,
                                          final FieldMapWrapper<T> wrapper) {
        try {
            final ResultDefinition.WithObject<T> result =
                doSearchObject(indexName, getMatchAllDocQuery(start, rows), wrapper);
            if (result == null)
                throw new ServerException(Response.Status.NOT_FOUND,
                    "No document found" + " - Index: " + indexName);
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
    final public ResultDefinition.WithMap searchQuery(final String indexName,
                                                      final QueryDefinition query,
                                                      final Boolean delete) {
        try {
            checkRight();
            final IndexInstance index = indexManager.get(indexName);
            if (delete != null && delete)
                return index.deleteByQuery(query);
            else
                return index.query(context -> context.searchMap(query));
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    final public <T> ResultDefinition.WithObject<T> searchQuery(final String indexName,
                                                                final QueryDefinition query,
                                                                final FieldMapWrapper<T> wrapper) {
        try {
            checkRight();
            return indexManager.get(indexName).query(context -> context.searchObject(query, wrapper));
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public ResultDefinition.Empty searchQuery(final String indexName,
                                              final QueryDefinition query,
                                              final ResultDocumentsInterface resultDocuments) {
        try {
            checkRight();
            return indexManager.get(indexName).query(context -> context.searchInterface(query, resultDocuments));
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public ExplainDefinition explainQuery(final String indexName,
                                          final QueryDefinition query,
                                          final String docId) {
        try {
            checkRight();
            final IndexInstance index = indexManager.get(indexName);
            return new ExplainDefinition(index.explain(query, docId));
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public String explainQueryText(final String indexName,
                                   final QueryDefinition query,
                                   final String docId) {
        try {
            checkRight();
            final IndexInstance index = indexManager.get(indexName);
            return index.explain(query, docId).toString();
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

    @Override
    public String explainQueryDot(final String indexName,
                                  final QueryDefinition query,
                                  final String docId,
                                  final Integer descriptionWrapSize) {
        try {
            return ExplainDefinition.toDot(explainQuery(indexName, query, docId),
                descriptionWrapSize == null ? 28 : descriptionWrapSize);
        } catch (IOException e) {
            throw ServerException.getTextException(LOGGER, e);
        }
    }

    @Override
    public <T> T query(final String indexName,
                       final QueryActions<T> actions) throws IOException {
        checkRight();
        return indexManager.get(indexName).query(actions);
    }

    @Override
    public <T> T write(final String indexName,
                       final WriteActions<T> actions)
        throws IOException {
        checkRight();
        final IndexInstance index = indexManager.get(indexName);
        return index.write(actions);
    }

    @Override
    public Map<String, URI> getQueryTypes(final String indexName,
                                          final String lookup) {
        if (StringUtils.isBlank(lookup))
            return QuerySampler.TYPES_URI_DOC;
        final LongestCommonSubsequence longestCommonSubsequence = new LongestCommonSubsequence();
        final JaccardDistance jacardDistance = new JaccardDistance();
        final Map<Integer, Map<Double, List<String>>> ordered = new TreeMap<>(Comparator.reverseOrder());
        final String lowerCaseLookup = lookup.toLowerCase();
        QuerySampler.TYPES_LOWERCASE.forEach((lowercaseType, typeKey) -> {
            final int commonScore;
            {
                final Integer lowerCaseCommonDist = longestCommonSubsequence.apply(lowercaseType, lowerCaseLookup);
                final Integer caseSensitiveCommonDist = longestCommonSubsequence.apply(typeKey, lookup);
                commonScore = Math.max(lowerCaseCommonDist, caseSensitiveCommonDist);
            }
            final double jacardScore;
            {
                final Double lowerCaseJacardDist = jacardDistance.apply(lowerCaseLookup, lowercaseType);
                final Double caseSensitiveJacardDist = jacardDistance.apply(lookup, typeKey);
                jacardScore = Math.min(lowerCaseJacardDist, caseSensitiveJacardDist);
            }
            if (jacardScore == 1D)
                return;
            ordered.computeIfAbsent(commonScore,
                cs -> new TreeMap<>()).computeIfAbsent(jacardScore,
                js -> new ArrayList<>()).add(typeKey);
        });
        final Map<String, URI> result = new LinkedHashMap<>();
        ordered.forEach(
            (commonScore, jacardScores) -> jacardScores.forEach(
                (js, typeList) -> typeList.forEach(
                    type -> result.put(type, QuerySampler.TYPES_URI_DOC.get(type))
                )
            )
        );
        return result;
    }

    @Override
    public QueryInterface getQuerySample(final String indexName,
                                         final String queryType) {
        try {
            checkRight();
            return indexManager.get(indexName).getQuerySample(queryType);
        } catch (Exception e) {
            throw ServerException.getJsonException(LOGGER, e);
        }
    }

}
