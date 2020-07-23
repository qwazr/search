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
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.qwazr.binder.FieldMapWrapper;
import com.qwazr.search.analysis.AnalyzerContext;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.analysis.AnalyzerFactory;
import com.qwazr.search.analysis.CustomAnalyzer;
import com.qwazr.search.analysis.UpdatableAnalyzers;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.field.SmartFieldDefinition;
import com.qwazr.search.query.Join;
import com.qwazr.search.query.QueryInterface;
import com.qwazr.search.query.QuerySampler;
import com.qwazr.search.query.TermQuery;
import com.qwazr.search.replication.ReplicationProcess;
import com.qwazr.search.replication.ReplicationSession;
import com.qwazr.server.ServerException;
import com.qwazr.utils.Equalizer;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.AutoLockSemaphore;
import com.qwazr.utils.concurrent.FunctionEx;
import com.qwazr.utils.reflection.ConstructorParametersImpl;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiTerms;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.store.Directory;

final public class IndexInstance implements Closeable {

    private final static Logger LOGGER = LoggerUtils.getLogger(IndexInstance.class);

    @FunctionalInterface
    public interface Provider {
        IndexInstance get(String name);
    }

    private final IndexFileSet fileSet;
    private final UUID indexUuid;
    private final String indexName;

    private final AutoLockSemaphore writeSemaphore;
    private final AutoLockSemaphore readSemaphore;
    private final Directory dataDirectory;
    private final Directory taxonomyDirectory;
    private final WriterAndSearcher writerAndSearcher;

    private final ExecutorService executorService;
    private final IndexSettingsDefinition settings;
    private final ConstructorParametersImpl instanceFactory;
    private final FileResourceLoader fileResourceLoader;
    private final Provider indexProvider;

    private final ReentrantLock replicationLock;
    private final ReentrantLock commitLock;
    private final ReentrantLock backupLock;

    private volatile AnalyzerContext analyzerContext;
    private final UpdatableAnalyzers indexAnalyzers;

    private final ReentrantLock fieldMapLock;
    private volatile FieldMap fieldMap;

    private volatile LinkedHashMap<String, AnalyzerDefinition> analyzerDefinitionMap;
    private final LinkedHashMap<String, CustomAnalyzer.Factory> localAnalyzerFactoryMap;
    private final Map<String, AnalyzerFactory> globalAnalyzerFactoryMap;

    private final ReplicationMaster replicationMaster;
    private final ReplicationSlave replicationSlave;

    private final Set<AnalyzerContext> activeAnalyzerContexts;
    private final ReindexThread reindexThread;

    IndexInstance(final IndexInstanceBuilder builder) {
        this.writeSemaphore = builder.writeSemaphore;
        this.readSemaphore = builder.readSemaphore;
        this.indexProvider = builder.indexProvider;
        this.fileSet = builder.fileSet;
        this.indexName = builder.indexName;
        this.indexUuid = builder.indexUuid;
        this.dataDirectory = builder.dataDirectory;
        this.taxonomyDirectory = builder.taxonomyDirectory;
        this.localAnalyzerFactoryMap = builder.localAnalyzerFactoryMap;
        this.analyzerDefinitionMap = CustomAnalyzer.createDefinitionMap(localAnalyzerFactoryMap);
        this.globalAnalyzerFactoryMap = builder.globalAnalyzerFactoryMap;
        this.fieldMapLock = new ReentrantLock(true);
        this.fieldMap = builder.fieldMap;
        this.writerAndSearcher = builder.writerAndSearcher;
        this.analyzerContext = builder.analyzerContext;
        this.indexAnalyzers = builder.indexAnalyzers;
        this.settings = builder.settings;
        this.executorService = builder.executorService;
        this.instanceFactory = builder.instanceFactory;
        this.fileResourceLoader = builder.fileResourceLoader;
        this.replicationLock = new ReentrantLock(true);
        this.commitLock = new ReentrantLock(true);
        this.backupLock = new ReentrantLock(true);
        this.replicationMaster = builder.replicationMaster;
        this.replicationSlave = builder.replicationSlave;
        this.activeAnalyzerContexts = builder.activeAnalyzerContexts;
        this.reindexThread =
            builder.replicationMaster != null && !StringUtils.isBlank(settings.recordField)
                ? new ReindexThread(executorService, this) : null;
    }

    public IndexSettingsDefinition getSettings() {
        return settings;
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(writerAndSearcher, replicationMaster, analyzerContext);

        if (taxonomyDirectory != null)
            IOUtils.closeQuietly(taxonomyDirectory);

        if (dataDirectory != null)
            IOUtils.closeQuietly(dataDirectory);
    }

    private IndexStatus getIndexStatus() throws IOException {
        return writerAndSearcher.search((indexSearcher, taxonomyReader) -> new IndexStatus(indexUuid,
            replicationSlave == null ? null : replicationSlave.getClientMasterUuid(), dataDirectory, indexSearcher,
            writerAndSearcher.getIndexWriter(), settings, localAnalyzerFactoryMap.keySet(),
            fieldMap.getFields().keySet(), activeAnalyzerContexts.size()));
    }

    Map<String, FieldDefinition> getFields() {
        return fieldMap.getFields();
    }

    FieldStats getFieldStats(final String fieldName) throws IOException {
        try (final AutoLockSemaphore.Lock lock = readSemaphore.acquire()) {
            return writerAndSearcher.search((indexSearcher, taxonomyReader) -> {
                final Terms terms = MultiTerms.getTerms(indexSearcher.getIndexReader(), fieldName);
                return terms == null ? new FieldStats() : new FieldStats(terms, fieldMap.getFieldType(null, fieldName));
            });
        }
    }

    IndexStatus getStatus() throws IOException {
        try (final AutoLockSemaphore.Lock lock = readSemaphore.acquire()) {
            return getIndexStatus();
        }
    }

    private Collection<String> reloadAnalyzersAndFields() throws IOException {
        fieldMapLock.lock();
        try {
            synchronized (localAnalyzerFactoryMap) {
                setAnalyzers(CustomAnalyzer.createDefinitionMap(fileSet.loadAnalyzerDefinitionMap()));
                setFields(fileSet.loadFieldMap());
                return refreshFieldsAnalyzers();
            }
        } finally {
            fieldMapLock.unlock();
        }
    }

    private Collection<String> refreshFieldsAnalyzers() {
        final Collection<String> errors = new ArrayList<>();
        final AnalyzerContext oldAnalyzerContext = analyzerContext;
        analyzerContext = new AnalyzerContext(
            activeAnalyzerContexts,
            instanceFactory,
            fileResourceLoader,
            fieldMap,
            globalAnalyzerFactoryMap,
            localAnalyzerFactoryMap,
            errors);
        indexAnalyzers.update(analyzerContext.getIndexAnalyzers());
        oldAnalyzerContext.close();
        return errors;
    }

    Collection<String> setFields(final Map<String, FieldDefinition> fields) throws ServerException, IOException {
        final Collection<String> errors;
        final boolean fieldChanged;
        fieldMapLock.lock();
        try {
            final FieldsContext newFieldsContext = new FieldsContext(settings, fields);
            fileSet.writeFieldMap(fields);
            fieldChanged = !Objects.equals(newFieldsContext, fieldMap.fieldsContext);
            if (fieldChanged)
                fieldMap = new FieldMap(newFieldsContext);
            errors = refreshFieldsAnalyzers();
        } finally {
            fieldMapLock.unlock();
        }
        if (fieldChanged)
            reindex();
        return errors;
    }

    void setField(final String field_name, final FieldDefinition field) throws IOException, ServerException {
        final Map<String, FieldDefinition> fields = new LinkedHashMap<>(fieldMap.getFields());
        fields.put(field_name, field);
        setFields(fields);
    }

    void deleteField(final String field_name) throws IOException, ServerException {
        final Map<String, FieldDefinition> fields = new LinkedHashMap<>(fieldMap.getFields());
        if (fields.remove(field_name) == null)
            throw new ServerException(Response.Status.NOT_FOUND,
                "Field not found: " + field_name + " - Index: " + indexName);
        setFields(fields);
    }

    LinkedHashMap<String, AnalyzerDefinition> getAnalyzers() {
        return analyzerDefinitionMap;
    }

    void reindex() {
        //TODO;
    }

    private void updateLocalAnalyzers(boolean writeConfigFile) throws IOException {
        refreshFieldsAnalyzers();
        analyzerDefinitionMap = CustomAnalyzer.createDefinitionMap(localAnalyzerFactoryMap);
        if (writeConfigFile)
            fileSet.writeAnalyzerDefinitionMap(analyzerDefinitionMap);
    }

    void refreshAnalyzers() throws IOException {
        synchronized (localAnalyzerFactoryMap) {
            updateLocalAnalyzers(false);
        }
    }

    void setAnalyzer(final String analyzerName, final AnalyzerDefinition analyzerDefinition) throws IOException {
        Objects.requireNonNull(analyzerName, "The analyzer name is missing");
        Objects.requireNonNull(analyzerDefinition, () -> "The analyzer definition is missing: " + analyzerName);
        synchronized (localAnalyzerFactoryMap) {
            localAnalyzerFactoryMap.put(analyzerName, new CustomAnalyzer.Factory(analyzerDefinition));
            updateLocalAnalyzers(true);
        }
    }

    void setAnalyzers(final Map<String, AnalyzerDefinition> analyzerDefinitionMap) throws IOException {
        Objects.requireNonNull(analyzerDefinitionMap, "The analyzer map is null");
        synchronized (localAnalyzerFactoryMap) {
            localAnalyzerFactoryMap.putAll(CustomAnalyzer.createFactoryMap(analyzerDefinitionMap, LinkedHashMap::new));
            updateLocalAnalyzers(true);
        }
    }

    void deleteAnalyzer(final String analyzerName) throws IOException, ServerException {
        synchronized (localAnalyzerFactoryMap) {
            if (localAnalyzerFactoryMap.remove(analyzerName) == null)
                throw new ServerException(Response.Status.NOT_FOUND,
                    "Analyzer not found: " + analyzerName + " - Index: " + indexName);
            updateLocalAnalyzers(true);
        }
    }

    List<TermDefinition> testAnalyzer(final String analyzerName, final String inputText)
        throws ServerException, ReflectiveOperationException, IOException {
        AnalyzerFactory factory;
        synchronized (localAnalyzerFactoryMap) {
            factory = localAnalyzerFactoryMap.get(analyzerName);
        }
        if (factory == null)
            factory = globalAnalyzerFactoryMap.get(analyzerName);
        if (factory == null)
            throw new ServerException(Response.Status.NOT_FOUND,
                "Analyzer not found: " + analyzerName + " - Index: " + indexName);
        try (final Analyzer analyzer = factory.createAnalyzer(fileResourceLoader)) {
            return TermDefinition.buildTermList(analyzer, StringUtils.EMPTY, inputText);
        }
    }

    <T> T useQueryAnalyzer(final FunctionEx<Analyzer, T, IOException> analyzerFunction) throws IOException {
        return analyzerFunction.apply(analyzerContext.resolveQueryAnalyzer(null));
    }

    <T> T useIndexAnalyzer(final FunctionEx<Analyzer, T, IOException> analyzerFunction) throws IOException {
        return analyzerFunction.apply(indexAnalyzers);
    }

    public Query createJoinQuery(final Join joinQuery) throws IOException {
        try (final AutoLockSemaphore.Lock lock = readSemaphore.acquire()) {
            return writerAndSearcher.search((indexSearcher, taxonomyReader) -> {
                try (final QueryContext queryContext = buildQueryContext(indexSearcher, taxonomyReader)) {
                    final Query fromQuery = joinQuery.fromQuery == null ?
                        new MatchAllDocsQuery() :
                        joinQuery.fromQuery.getQuery(queryContext);
                    return JoinUtil.createJoinQuery(joinQuery.fromField, joinQuery.multipleValuesPerDocument,
                        joinQuery.toField, fromQuery, indexSearcher,
                        joinQuery.scoreMode == null ? ScoreMode.None : joinQuery.scoreMode);
                } catch (ParseException | QueryNodeException | ReflectiveOperationException e) {
                    throw ServerException.of(e);
                }

            });
        }
    }

    private void nrtCommit() throws IOException {
        commitLock.lock();
        try {
            writerAndSearcher.commit();
        } finally {
            commitLock.unlock();
        }
    }

    final BackupStatus backup(final Path backupIndexDirectory) throws IOException {
        backupLock.lock();
        try {
            // check the backup directory existence
            if (!Files.exists(backupIndexDirectory))
                Files.createDirectory(backupIndexDirectory);
            if (!Files.isDirectory(backupIndexDirectory))
                throw new IOException(
                    "The backup path is not a directory: " + backupIndexDirectory.toAbsolutePath() + " " +
                        Thread.currentThread().getId());
            try (final AutoLockSemaphore.Lock lock = readSemaphore.acquire()) {
                return new ReplicationBackup(this, backupIndexDirectory, taxonomyDirectory != null).backup();
            } catch (IOException e) {
                // If any error occurred, we delete the backup directory
                if (Files.exists(backupIndexDirectory)) {
                    try {
                        FileUtils.deleteDirectory(backupIndexDirectory);
                    } catch (IOException ioe) {
                        LOGGER.log(Level.WARNING, e,
                            () -> "Cannot delete the backup directory: " + backupIndexDirectory);
                    }
                }
                throw e;
            }
        } finally {
            backupLock.unlock();
        }
    }

    final boolean deleteBackup(final Path backupIndexDirectory) throws IOException {
        backupLock.lock();
        try {
            if (Files.notExists(backupIndexDirectory))
                return false;
            FileUtils.deleteDirectory(backupIndexDirectory);
            return true;
        } finally {
            backupLock.unlock();
        }
    }

    final BackupStatus getBackup(final Path backupIndexDirectory, final boolean extractVersion) throws IOException {
        checkIsMaster();
        try (final AutoLockSemaphore.Lock lock = readSemaphore.acquire()) {
            return BackupStatus.newBackupStatus(backupIndexDirectory, extractVersion);
        }
    }

    final ReplicationMaster checkIsMaster() {
        if (writerAndSearcher.getIndexWriter() == null)
            throw new UnsupportedOperationException(
                "Writing in a read only index (slave) is not allowed: " + indexName);
        if (replicationMaster == null)
            throw new ServerException(Response.Status.NOT_ACCEPTABLE,
                "This node is not a master - Index: " + indexName);
        return replicationMaster;
    }

    ReplicationSession replicationUpdate(String currentVersion) throws IOException {
        //TODO check current version to avoid non useful replication
        final ReplicationMaster master = checkIsMaster();
        master.expireInactiveSessions(TimeUnit.MINUTES, 30);
        return master.newReplicationSession();
    }

    void replicationRelease(String sessionID) throws IOException {
        final ReplicationMaster master = checkIsMaster();
        master.releaseSession(sessionID);
        master.expireInactiveSessions(TimeUnit.MINUTES, 30);
    }

    InputStream replicationObtain(String sessionID, ReplicationProcess.Source source, String fileName)
        throws FileNotFoundException {
        return checkIsMaster().getItem(sessionID, source, fileName);
    }

    ReplicationStatus replicationCheck() throws IOException {
        if (replicationSlave == null)
            throw new ServerException(Response.Status.NOT_ACCEPTABLE,
                "No replication master has been setup - Index: " + indexName);

        try (final AutoLockSemaphore.Lock lock = writeSemaphore.acquire()) {
            // We only want one replication at a time
            replicationLock.lock();
            try {
                return replicationSlave.replicate(((strategy, remoteMasterUuid) -> {
                    if (strategy == ReplicationStatus.Strategy.incremental)
                        writerAndSearcher.refresh();
                    else
                        writerAndSearcher.reload();
                    reloadAnalyzersAndFields();
                    replicationSlave.setClientMasterUuid(remoteMasterUuid);
                    // Add fields and analyzers reload
                }));
            } finally {
                replicationLock.unlock();
            }
        }
    }

    final void deleteAll(Map<String, String> commitUserData) throws IOException {
        checkIsMaster();
        try (final AutoLockSemaphore.Lock lock = writeSemaphore.acquire()) {
            writerAndSearcher.write((indexWriter, taxonomyWriter) -> {
                indexWriter.deleteAll();
                if (commitUserData != null)
                    indexWriter.setLiveCommitData(commitUserData.entrySet());
                return null;
            });
            nrtCommit();
        }
    }

    final IndexStatus merge(final IndexInstance mergedIndex, final Map<String, String> commitUserData)
        throws IOException {
        checkIsMaster();
        try (final AutoLockSemaphore.Lock writeLock = writeSemaphore.acquire()) {
            writerAndSearcher.write((indexWriter, taxonomyWriter) -> {
                try (final AutoLockSemaphore.Lock readLock = mergedIndex.readSemaphore.acquire()) {
                    indexWriter.addIndexes(mergedIndex.dataDirectory);
                    if (commitUserData != null)
                        indexWriter.setLiveCommitData(commitUserData.entrySet());
                }
                return null;
            });
            nrtCommit();
            return getIndexStatus();
        }
    }

    private WriteContextImpl buildWriteContext(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) {
        return new WriteContextImpl(indexProvider, fileResourceLoader, executorService, analyzerContext,
            fieldMap, indexWriter, taxonomyWriter);
    }

    final <T> T write(final IndexServiceInterface.WriteActions<T> writeActions) throws IOException {
        try (final AutoLockSemaphore.Lock lock = writeSemaphore.acquire()) {
            return writerAndSearcher.write(((indexWriter, taxonomyWriter) -> {
                try (final WriteContext context = buildWriteContext(indexWriter, taxonomyWriter)) {
                    return writeActions.apply(context);
                }
            }));
        }
    }

    private int checkCommit(final int results) throws IOException {
        if (results > 0)
            nrtCommit();
        return results;
    }

    private int checkCommit(final int results, final Map<String, String> commitUserData) throws IOException {
        if (results > 0 || (commitUserData != null && !commitUserData.isEmpty()))
            nrtCommit();
        return results;
    }

    private int checkCommit(final int results, final PostDefinition post) throws IOException {
        return checkCommit(results, post == null ? null : post.commitUserData);
    }

    final <T> int postDocument(final Map<String, Field> fields, final T document,
                               final Map<String, String> commitUserData) throws IOException {
        checkIsMaster();
        return write(
            context -> checkCommit(context.postDocument(fields, document, commitUserData), commitUserData));
    }

    final <T> int postDocuments(final Map<String, Field> fields, final Collection<T> documents,
                                final Map<String, String> commitUserData) throws IOException {
        checkIsMaster();
        return write(context -> checkCommit(context.postDocuments(fields, documents, commitUserData),
            commitUserData));
    }

    final int postMappedDocument(final PostDefinition.Document post) throws IOException {
        checkIsMaster();
        return write(context -> checkCommit(context.postMappedDocument(post), post));
    }

    final int postMappedDocuments(final PostDefinition.Documents post) throws IOException {
        checkIsMaster();
        return write(context -> checkCommit(context.postMappedDocuments(post), post));
    }

    final IndexJsonResult postJsonNodes(final Collection<JsonNode> jsonNodes) throws IOException {
        checkIsMaster();
        final SortedMap<String, SortedSet<JsonNodeType>> fieldTypes = new TreeMap<>();
        final Integer count = write(context -> checkCommit(context.postJsonNodes(jsonNodes, fieldTypes)));
        return new IndexJsonResult(count, fieldTypes);
    }

    final IndexJsonResult postJsonNode(final JsonNode jsonNode, final boolean withFieldTypes) throws IOException {
        checkIsMaster();
        final SortedMap<String, SortedSet<JsonNodeType>> fieldTypes = withFieldTypes ? new TreeMap<>() : null;
        final Integer count = write(context -> checkCommit(context.postJsonNode(jsonNode, fieldTypes)));
        return new IndexJsonResult(count, fieldTypes);
    }

    final <T> int updateDocValues(final Map<String, Field> fields, final T document,
                                  final Map<String, String> commitUserData) throws IOException {
        checkIsMaster();
        return write(context -> checkCommit(context.updateDocValues(fields, document, commitUserData), commitUserData));
    }

    final <T> int updateDocsValues(final Map<String, Field> fields, final Collection<T> documents,
                                   final Map<String, String> commitUserData) throws IOException {
        checkIsMaster();
        return write(
            context -> checkCommit(context.updateDocsValues(fields, documents, commitUserData), commitUserData));
    }

    final int updateMappedDocValues(final PostDefinition.Document post) throws IOException {
        checkIsMaster();
        return write(context -> checkCommit(context.updateMappedDocValues(post), post));
    }

    final int updateMappedDocsValues(final PostDefinition.Documents post) throws IOException {
        checkIsMaster();
        return write(context -> checkCommit(context.updateMappedDocsValues(post), post));
    }

    final ResultDefinition.WithMap deleteByQuery(final QueryDefinition queryDefinition) throws IOException {
        checkIsMaster();
        Objects.requireNonNull(queryDefinition, "The queryDefinition is missing - Index: " + indexName);
        final QueryInterface queryInterface = Objects.requireNonNull(queryDefinition.getQuery(), "The query is missing - Index: " + indexName);
        try (final AutoLockSemaphore.Lock lock = writeSemaphore.acquire()) {
            return writerAndSearcher.search((indexSearcher, taxonomyReader) -> {
                try (final QueryContext queryContext = buildQueryContext(indexSearcher, taxonomyReader)) {
                    final Query query = queryInterface.getQuery(queryContext);
                    final IndexWriter indexWriter = writerAndSearcher.getIndexWriter();
                    int docs = indexWriter.getDocStats().numDocs;
                    indexWriter.deleteDocuments(query);
                    final Map<String, String> commitUserData = queryDefinition.getCommitUserData();
                    if (commitUserData != null && !commitUserData.isEmpty())
                        indexWriter.setLiveCommitData(commitUserData.entrySet());
                    nrtCommit();
                    docs -= indexWriter.getDocStats().numDocs;
                    return new ResultDefinition.WithMap(docs);
                } catch (ParseException | ReflectiveOperationException | QueryNodeException e) {
                    throw ServerException.of(e);
                }
            });
        }
    }

    final List<TermEnumDefinition> getTermsEnum(final String fieldName, final String prefix, final Integer start,
                                                final Integer rows) throws IOException {
        Objects.requireNonNull(fieldName, "The field name is missing - Index: " + indexName);
        try (final AutoLockSemaphore.Lock lock = readSemaphore.acquire()) {
            return writerAndSearcher.search((indexSearcher, taxonomyReader) -> {
                final FieldTypeInterface fieldType = fieldMap.getFieldType(null, fieldName);
                final Terms terms = MultiTerms.getTerms(indexSearcher.getIndexReader(), fieldName);
                if (terms == null)
                    return Collections.emptyList();
                return TermEnumDefinition.buildTermList(fieldType, terms.iterator(), prefix, start == null ? 0 : start,
                    rows == null ? 20 : rows);
            });
        }
    }

    private QueryContextImpl buildQueryContext(final IndexSearcher indexSearcher,
                                               final TaxonomyReader taxonomyReader) {
        return new QueryContextImpl(indexProvider, fileResourceLoader, executorService, analyzerContext, fieldMap,
            indexSearcher, taxonomyReader);
    }

    final <T> T query(final IndexServiceInterface.QueryActions<T> queryActions) throws IOException {
        try (final AutoLockSemaphore.Lock lock = readSemaphore.acquire()) {
            return writerAndSearcher.search((indexSearcher, taxonomyReader) -> {
                try (final QueryContextImpl context = buildQueryContext(indexSearcher, taxonomyReader)) {
                    return queryActions.apply(context);
                }
            });
        }
    }

    private String checkPrimaryKey() {
        final String primaryKey = fieldMap.getPrimaryKey();
        if (StringUtils.isEmpty(primaryKey))
            throw new NotAcceptableException("There is no primary key for this index.");
        return primaryKey;
    }

    private QueryDefinition getDocumentQuery(final Object id) {
        final QueryBuilder builder = QueryDefinition.of(new TermQuery(checkPrimaryKey(), id));
        builder.rows(1);
        builder.returnedField("*");
        return builder.build();
    }

    final <T> ResultDefinition.WithObject<T> getDocument(final Object id,
                                                         final FieldMapWrapper<T> fieldMapWrapper) throws IOException {
        return query(queryContext -> queryContext.searchObject(getDocumentQuery(id), fieldMapWrapper));
    }

    final ResultDefinition.WithMap getDocument(final Object id) throws IOException {
        return query(queryContext -> queryContext.searchMap(getDocumentQuery(id)));
    }

    final Explanation explain(final QueryDefinition queryDefinition, final String id) throws IOException {
        try (final AutoLockSemaphore.Lock lock = readSemaphore.acquire()) {
            return writerAndSearcher.search((indexSearcher, taxonomyReader) -> {
                try (final QueryContextImpl context = buildQueryContext(indexSearcher, taxonomyReader)) {
                    final ResultDocumentsInterface.ForScoreDoc resultDocs = new ResultDocumentsInterface.ForScoreDoc();
                    final ResultDefinition.Empty result = context.searchInterface(getDocumentQuery(id), resultDocs);
                    if (result == null || result.totalHits != 1)
                        throw new NotFoundException("Document not found: " + id);
                    return new QueryExecution<>(context, queryDefinition).explain(resultDocs.getDoc(0));
                } catch (ReflectiveOperationException | ParseException | QueryNodeException e) {
                    throw ServerException.of(e);
                }
            });
        }
    }

    Directory getDataDirectory() {
        return dataDirectory;
    }

    void fillFields(final Map<String, FieldDefinition> fields) {
        if (fields == null)
            return;
        this.fieldMap.getFields().forEach((name, fieldDef) -> {
            if (!fields.containsKey(name))
                fields.put(name, fieldDef);
        });
    }

    void fillAnalyzers(final Map<String, AnalyzerFactory> analyzers) {
        if (analyzers == null)
            return;
        this.localAnalyzerFactoryMap.forEach((name, factory) -> {
            if (!analyzers.containsKey(name))
                analyzers.put(name, factory);
        });
    }

    public static class ResourceInfo extends Equalizer.Immutable<ResourceInfo> {

        public final long lastModified;
        public final long length;

        public ResourceInfo() {
            super(ResourceInfo.class);
            lastModified = 0;
            length = 0;
        }

        private ResourceInfo(final File file) {
            super(ResourceInfo.class);
            lastModified = file.lastModified();
            length = file.length();
        }

        @Override
        protected int computeHashCode() {
            return Objects.hash(lastModified, length);
        }

        @Override
        protected boolean isEqual(final ResourceInfo info) {
            return lastModified == info.lastModified && length == info.length;
        }

    }

    final void postResource(final String resourceName, final Long lastModified, final InputStream inputStream)
        throws IOException {
        if (!Files.exists(fileSet.resourcesDirectoryPath))
            Files.createDirectory(fileSet.resourcesDirectoryPath);
        final Path resourceFile = fileResourceLoader.checkResourceName(resourceName);
        IOUtils.copy(inputStream, resourceFile);
        if (lastModified != null)
            Files.setLastModifiedTime(resourceFile, FileTime.fromMillis(lastModified));
        refreshFieldsAnalyzers();
    }

    final Map<String, ResourceInfo> getResources() throws IOException {
        if (!Files.exists(fileSet.resourcesDirectoryPath))
            return Collections.emptyMap();
        final LinkedHashMap<String, ResourceInfo> map = new LinkedHashMap<>();
        try (final Stream<Path> stream = Files.list(fileSet.resourcesDirectoryPath)) {
            stream.filter(p -> Files.isRegularFile(p))
                .forEach(p -> map.put(p.getFileName().toString(), new ResourceInfo(p.toFile())));
            return map;
        }
    }

    final InputStream getResource(final String resourceName) throws IOException {
        if (!Files.exists(fileSet.resourcesDirectoryPath))
            throw new ServerException(Response.Status.NOT_FOUND,
                "Resource not found : " + resourceName + " - Index: " + indexName);
        return fileResourceLoader.openResource(resourceName);
    }

    final void deleteResource(final String resourceName) throws IOException {
        if (!Files.exists(fileSet.resourcesDirectoryPath))
            throw new ServerException(Response.Status.NOT_FOUND,
                "Resource not found : " + resourceName + " - Index: " + indexName);
        final Path resourceFile = fileResourceLoader.checkResourceName(resourceName);
        if (!Files.exists(resourceFile))
            throw new ServerException(Response.Status.NOT_FOUND,
                "Resource not found : " + resourceName + " - Index: " + indexName);
        Files.delete(resourceFile);
    }

    final FileResourceLoader newResourceLoader(final FileResourceLoader resourceLoader) {
        return new FileResourceLoader(resourceLoader, fileSet.resourcesDirectoryPath);
    }

    final QueryInterface getQuerySample(final String queryType) {
        final QuerySampler.Factory queryFactory = QuerySampler.TYPES_FACTORY.get(queryType);
        if (queryFactory == null)
            throw new NotFoundException("The type does not exist: " + queryType);
        return queryFactory.create(settings, getAnalyzers(), getFields());
    }

    Map<String, Object> getJsonSample() {
        final Supplier<String> autoIdProvider = StringUtils.isBlank(settings.primaryKey) ? null : WriteContextImpl.getAutoIdProvider();
        final Map<String, Object> sample = new LinkedHashMap<>();
        getFields().forEach((name, def) -> {
            final Object value;
            if (autoIdProvider != null && name.equals(settings.primaryKey))
                value = autoIdProvider.get();
            else {
                final SmartFieldDefinition.Type type = def.getType();
                if (type != null) {
                    switch (type) {
                        case TEXT:
                            value = "Hello world";
                            break;
                        case INTEGER:
                            value = 123;
                            break;
                        case LONG:
                            value = System.currentTimeMillis();
                            break;
                        case FLOAT:
                            value = 3.14f;
                            break;
                        case DOUBLE:
                            value = 3.141592654d;
                            break;
                        default:
                            value = "value";
                            break;
                    }
                } else
                    value = "value";
            }
            sample.put(name, value);
        });
        if (autoIdProvider != null) {
            if (!sample.containsKey(settings.primaryKey))
                sample.put(settings.primaryKey, autoIdProvider.get());
        }
        return sample;
    }

    final List<Map<String, Object>> getJsonSamples(int count) {
        final List<Map<String, Object>> list = new ArrayList<>(count);
        while (count-- > 0)
            list.add(getJsonSample());
        return list;
    }

    ReindexThread getReindexThread() {
        if (reindexThread == null)
            throw new NotAcceptableException("Reindexing is not available on slave indexes.");
        return reindexThread;
    }
}
