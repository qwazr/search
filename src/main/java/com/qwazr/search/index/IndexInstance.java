/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import com.qwazr.binder.FieldMapWrapper;
import com.qwazr.search.analysis.AnalyzerContext;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.analysis.AnalyzerFactory;
import com.qwazr.search.analysis.CustomAnalyzer;
import com.qwazr.search.analysis.UpdatableAnalyzers;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.query.JoinQuery;
import com.qwazr.server.ServerException;
import com.qwazr.utils.FileUtils;
import com.qwazr.utils.FunctionUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.ReadWriteSemaphores;
import com.qwazr.utils.reflection.ConstructorParametersImpl;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.replicator.LocalReplicator;
import org.apache.lucene.replicator.PerSessionDirectoryFactory;
import org.apache.lucene.replicator.ReplicationClient;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

final public class IndexInstance implements Closeable {

	@FunctionalInterface
	public interface Provider {
		IndexInstance getIndex(String name);
	}

	private final IndexFileSet fileSet;
	private final UUID indexUuid;
	private final String indexName;

	private final ReadWriteSemaphores readWriteSemaphores;
	private final Directory dataDirectory;
	private final Directory taxonomyDirectory;
	private final WriterAndSearcher writerAndSearcher;

	private final Set<MultiSearchInstance> multiSearchInstances;

	private final ExecutorService executorService;
	private final IndexSettingsDefinition settings;
	private final ConstructorParametersImpl instanceFactory;
	private final FileResourceLoader fileResourceLoader;
	private final Provider indexProvider;

	private final ReentrantLock replicationLock;
	private final ReentrantLock commitLock;
	private final ReentrantLock backupLock;

	private final UpdatableAnalyzers indexAnalyzers;
	private final UpdatableAnalyzers queryAnalyzers;

	private final ReentrantLock fieldMapLock;
	private volatile FieldMap fieldMap;

	private volatile LinkedHashMap<String, AnalyzerDefinition> analyzerDefinitionMap;
	private final LinkedHashMap<String, CustomAnalyzer.Factory> localAnalyzerFactoryMap;
	private final Map<String, AnalyzerFactory> globalAnalyzerFactoryMap;

	private final IndexReplicator indexReplicator;
	private final LocalReplicator localReplicator;

	IndexInstance(final IndexInstanceBuilder builder) {
		this.readWriteSemaphores = builder.readWriteSemaphores;
		this.indexProvider = builder.indexProvider;
		this.fileSet = builder.fileSet;
		this.indexName = builder.fileSet.mainDirectory.getName();
		this.indexUuid = builder.indexUuid;
		this.dataDirectory = builder.dataDirectory;
		this.taxonomyDirectory = builder.taxonomyDirectory;
		this.localAnalyzerFactoryMap = builder.localAnalyzerFactoryMap;
		this.analyzerDefinitionMap = CustomAnalyzer.createDefinitionMap(localAnalyzerFactoryMap);
		this.globalAnalyzerFactoryMap = builder.globalAnalyzerFactoryMap;
		this.fieldMapLock = new ReentrantLock(true);
		this.fieldMap = builder.fieldMap;
		this.writerAndSearcher = builder.writerAndSearcher;
		this.indexAnalyzers = builder.indexAnalyzers;
		this.queryAnalyzers = builder.queryAnalyzers;
		this.settings = builder.settings;
		this.multiSearchInstances = ConcurrentHashMap.newKeySet();
		this.executorService = builder.executorService;
		this.instanceFactory = builder.instanceFactory;
		this.fileResourceLoader = builder.fileResourceLoader;
		this.replicationLock = new ReentrantLock(true);
		this.commitLock = new ReentrantLock(true);
		this.backupLock = new ReentrantLock(true);
		this.indexReplicator = writerAndSearcher instanceof IndexReplicator.Slave ?
				((IndexReplicator.Slave) writerAndSearcher).getIndexReplicator() :
				null;
		this.localReplicator = writerAndSearcher instanceof Replication.Master ?
				((Replication.Master) writerAndSearcher).getLocalReplicator() :
				null;
	}

	public IndexSettingsDefinition getSettings() {
		return settings;
	}

	@Override
	public void close() {
		IOUtils.closeQuietly(writerAndSearcher, indexAnalyzers, queryAnalyzers);

		if (taxonomyDirectory != null)
			IOUtils.closeQuietly(taxonomyDirectory);

		if (dataDirectory != null)
			IOUtils.closeQuietly(dataDirectory);
	}

	boolean register(final MultiSearchInstance multiSearchInstance) {
		return multiSearchInstances.add(multiSearchInstance);
	}

	boolean unregister(final MultiSearchInstance multiSearchInstance) {
		return multiSearchInstances.remove(multiSearchInstance);
	}

	private IndexStatus getIndexStatus() throws IOException {
		return writerAndSearcher.search((indexSearcher, taxonomyReader) -> new IndexStatus(indexUuid,
				indexReplicator != null ? indexReplicator.getMasterUuid() : null, dataDirectory, indexSearcher,
				writerAndSearcher.getIndexWriter(), settings, localAnalyzerFactoryMap.keySet(),
				fieldMap.getFieldDefinitionMap().keySet(), indexAnalyzers.getActiveAnalyzers(),
				queryAnalyzers.getActiveAnalyzers()));
	}

	LinkedHashMap<String, FieldDefinition> getFields() {
		return fieldMap.getFieldDefinitionMap();
	}

	FieldStats getFieldStats(String fieldName) throws IOException {
		try (final ReadWriteSemaphores.Lock lock = readWriteSemaphores.acquireReadSemaphore()) {
			return writerAndSearcher.search((indexSearcher, taxonomyReader) -> {
				final Terms terms = MultiFields.getFields(indexSearcher.getIndexReader()).terms(fieldName);
				return terms == null ? new FieldStats() : new FieldStats(terms, fieldMap.getFieldType(null, fieldName));
			});
		}
	}

	IndexStatus getStatus() throws IOException, InterruptedException {
		try (final ReadWriteSemaphores.Lock lock = readWriteSemaphores.acquireReadSemaphore()) {
			return getIndexStatus();
		}
	}

	private void refreshFieldsAnalyzers() throws IOException {
		final AnalyzerContext analyzerContext =
				new AnalyzerContext(instanceFactory, fileResourceLoader, fieldMap, true, globalAnalyzerFactoryMap,
						localAnalyzerFactoryMap);
		indexAnalyzers.update(analyzerContext.indexAnalyzerMap);
		queryAnalyzers.update(analyzerContext.queryAnalyzerMap);
		multiSearchInstances.forEach(MultiSearchInstance::refresh);
	}

	void setFields(final LinkedHashMap<String, FieldDefinition> fields) throws ServerException, IOException {
		fieldMapLock.lock();
		try {
			fileSet.writeFieldMap(fields);
			fieldMap = new FieldMap(fields, settings.sortedSetFacetField);
			refreshFieldsAnalyzers();
		} finally {
			fieldMapLock.unlock();
		}
	}

	void setField(final String field_name, final FieldDefinition field) throws IOException, ServerException {
		final LinkedHashMap<String, FieldDefinition> fields = new LinkedHashMap<>(fieldMap.getFieldDefinitionMap());
		fields.put(field_name, field);
		setFields(fields);
	}

	void deleteField(final String field_name) throws IOException, ServerException {
		final LinkedHashMap<String, FieldDefinition> fields =
				(LinkedHashMap<String, FieldDefinition>) fieldMap.getFieldDefinitionMap().clone();
		if (fields.remove(field_name) == null)
			throw new ServerException(Response.Status.NOT_FOUND,
					"Field not found: " + field_name + " - Index: " + indexName);
		setFields(fields);
	}

	LinkedHashMap<String, AnalyzerDefinition> getAnalyzers() {
		return analyzerDefinitionMap;
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
			throws ServerException, InterruptedException, ReflectiveOperationException, IOException {
		AnalyzerFactory factory;
		synchronized (localAnalyzerFactoryMap) {
			factory = localAnalyzerFactoryMap.get(analyzerName);
		}
		if (factory == null && globalAnalyzerFactoryMap != null)
			factory = globalAnalyzerFactoryMap.get(analyzerName);
		if (factory == null)
			throw new ServerException(Response.Status.NOT_FOUND,
					"Analyzer not found: " + analyzerName + " - Index: " + indexName);
		try (final Analyzer analyzer = factory.createAnalyzer(fileResourceLoader)) {
			return TermDefinition.buildTermList(analyzer, StringUtils.EMPTY, inputText);
		}
	}

	private <T> T useAnalyzer(final UpdatableAnalyzers updatableAnalyzers, final String field,
			final FunctionUtils.FunctionEx<Analyzer, T, IOException> analyzerConsumer)
			throws ServerException, IOException {
		try (final UpdatableAnalyzers.Analyzers analyzers = updatableAnalyzers.getAnalyzers()) {
			return analyzerConsumer.apply(analyzers.getWrappedAnalyzer(field));
		}
	}

	<T> T useQueryAnalyzer(final String field,
			final FunctionUtils.FunctionEx<Analyzer, T, IOException> analyzerFunction) throws IOException {
		return useAnalyzer(queryAnalyzers, field, analyzerFunction);
	}

	<T> T useIndexAnalyzer(final String field,
			final FunctionUtils.FunctionEx<Analyzer, T, IOException> analyzerFunction) throws IOException {
		return useAnalyzer(indexAnalyzers, field, analyzerFunction);
	}

	public Query createJoinQuery(final JoinQuery joinQuery) throws IOException {
		try (final ReadWriteSemaphores.Lock lock = readWriteSemaphores.acquireReadSemaphore()) {
			return writerAndSearcher.search((indexSearcher, taxonomyReader) -> {
				try (final QueryContext queryContext = buildQueryContext(indexSearcher, taxonomyReader, null)) {
					final Query fromQuery = joinQuery.from_query == null ?
							new MatchAllDocsQuery() :
							joinQuery.from_query.getQuery(queryContext);
					return JoinUtil.createJoinQuery(joinQuery.from_field, joinQuery.multiple_values_per_document,
							joinQuery.to_field, fromQuery, indexSearcher,
							joinQuery.score_mode == null ? ScoreMode.None : joinQuery.score_mode);
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
			multiSearchInstances.forEach(MultiSearchInstance::refresh);
		} finally {
			commitLock.unlock();
		}
	}

	final BackupStatus backup(final Path backupIndexDirectory) throws IOException {
		backupLock.lock();
		try {
			checkIsMaster();
			try (final ReadWriteSemaphores.Lock lock = readWriteSemaphores.acquireReadSemaphore()) {

				// Create (or check) the backup directory
				if (Files.notExists(backupIndexDirectory))
					Files.createDirectory(backupIndexDirectory);
				if (!Files.isDirectory(backupIndexDirectory))
					throw new IOException(
							"Cannot create the backup directory: " + backupIndexDirectory + " - Index: " + indexName);

				// Copy the UUID
				IOUtils.writeStringAsFile(indexUuid.toString(),
						backupIndexDirectory.resolve(IndexFileSet.UUID_FILE).toFile());

				// Copy the option UUID Master File
				if (fileSet.uuidMasterFile != null && fileSet.uuidMasterFile.exists() &&
						fileSet.uuidMasterFile.isFile())
					Files.copy(fileSet.uuidMasterFile.toPath(),
							backupIndexDirectory.resolve(IndexFileSet.UUID_MASTER_FILE),
							StandardCopyOption.REPLACE_EXISTING);

				// Copy the settings, field definitions analyzer definitions
				IndexSettingsDefinition.save(settings,
						backupIndexDirectory.resolve(IndexFileSet.SETTINGS_FILE).toFile());
				FieldDefinition.saveMap(fieldMap.getFieldDefinitionMap(),
						backupIndexDirectory.resolve(IndexFileSet.FIELDS_FILE).toFile());
				AnalyzerDefinition.saveMap(analyzerDefinitionMap,
						backupIndexDirectory.resolve(IndexFileSet.ANALYZERS_FILE).toFile());

				// Copy the data using replication
				try (final Directory dataDir = FSDirectory.open(backupIndexDirectory.resolve(IndexFileSet.INDEX_DATA));
						final Directory taxoDir = taxonomyDirectory == null ?
								null :
								FSDirectory.open(backupIndexDirectory.resolve(IndexFileSet.INDEX_TAXONOMY))) {

					final PerSessionDirectoryFactory sourceDirFactory =
							new PerSessionDirectoryFactory(backupIndexDirectory.resolve(IndexFileSet.REPL_WORK));
					try (final ReplicationClient replicationClient = new ReplicationClient(localReplicator,
							IndexReplicator.getNewReplicationHandler(dataDir, taxoDir, () -> false),
							sourceDirFactory)) {
						replicationClient.updateNow();
					} catch (IOException e) {
						FileUtils.deleteDirectoryQuietly(backupIndexDirectory);
						throw e;
					}
					return BackupStatus.newBackupStatus(backupIndexDirectory, false);
				}
			}
		} finally {
			backupLock.unlock();
		}
	}

	final BackupStatus getBackup(final Path backupIndexDirectory, final boolean extractVersion) throws IOException {
		checkIsMaster();
		try (final ReadWriteSemaphores.Lock lock = readWriteSemaphores.acquireReadSemaphore()) {
			return BackupStatus.newBackupStatus(backupIndexDirectory, extractVersion);
		}
	}

	final void checkIsMaster() {
		if (writerAndSearcher.getIndexWriter() == null)
			throw new UnsupportedOperationException(
					"Writing in a read only index (slave) is not allowed: " + indexName);
	}

	final UUID checkRemoteMasterUUID(final String remoteMasterUuid, final UUID localUuid) {
		final UUID uuid = UUID.fromString(remoteMasterUuid);
		if (!Objects.equals(uuid, localUuid))
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"The UUID of the local index and the remote index does not match: " + localUuid + " <> " +
							remoteMasterUuid + " - Index: " + indexName);
		return uuid;
	}

	final LocalReplicator getLocalReplicator(final String remoteMasterUuid) {
		checkRemoteMasterUUID(remoteMasterUuid, indexUuid);
		return Objects.requireNonNull(localReplicator, () -> "FILE replication not available: " + indexName);
	}

	void replicationCheck() throws IOException {
		if (indexReplicator == null)
			throw new ServerException(Response.Status.NOT_ACCEPTABLE,
					"No replication master has been setup - Index: " + indexName);

		try (final ReadWriteSemaphores.Lock lock = readWriteSemaphores.acquireWriteSemaphore()) {

			// We only want one replication at a time
			replicationLock.lock();
			try {

				// Check that the master is the right one
				indexReplicator.checkRemoteMasterUuid();

				//Sync resources
				final Map<String, ResourceInfo> localResources = getResources();
				indexReplicator.getMasterResources().forEach((remoteName, remoteInfo) -> {
					final ResourceInfo localInfo = localResources.remove(remoteName);
					if (localInfo != null && localInfo.equals(remoteInfo))
						return;
					try (final InputStream input = indexReplicator.getResource(remoteName)) {
						postResource(remoteName, remoteInfo.lastModified, input);
					} catch (IOException e) {
						throw ServerException.of(
								"Cannot replicate the resource " + remoteName + " - Index: " + indexName, e);
					}
				});
				localResources.forEach((resourceName, resourceInfo) -> deleteResource(resourceName));

				//Sync analyzer and fields
				setAnalyzers(indexReplicator.getMasterAnalyzers());
				setFields(indexReplicator.getMasterFields());

				indexReplicator.updateNow();
				writerAndSearcher.refresh();

			} finally {
				replicationLock.unlock();
			}
		}
	}

	final void deleteAll(Map<String, String> commitUserData) throws IOException {
		checkIsMaster();
		try (final ReadWriteSemaphores.Lock lock = readWriteSemaphores.acquireWriteSemaphore()) {
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
		try (final ReadWriteSemaphores.Lock writeLock = readWriteSemaphores.acquireWriteSemaphore()) {
			writerAndSearcher.write((indexWriter, taxonomyWriter) -> {
				try (final ReadWriteSemaphores.Lock readLock = mergedIndex.readWriteSemaphores.acquireReadSemaphore()) {
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

	private WriteContextImpl buildWriteContext(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter)
			throws IOException {
		return new WriteContextImpl(indexProvider, fileResourceLoader, executorService, indexAnalyzers, queryAnalyzers,
				fieldMap, indexWriter, taxonomyWriter);
	}

	final <T> T write(final IndexServiceInterface.WriteActions<T> writeActions) throws IOException {
		try (final ReadWriteSemaphores.Lock lock = readWriteSemaphores.acquireWriteSemaphore()) {
			return writerAndSearcher.write(((indexWriter, taxonomyWriter) -> {
				try (final WriteContext context = buildWriteContext(indexWriter, taxonomyWriter)) {
					return writeActions.apply(context);
				}
			}));
		}
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
			final Map<String, String> commitUserData, boolean update) throws IOException {
		checkIsMaster();
		return write(
				context -> checkCommit(context.postDocument(fields, document, commitUserData, update), commitUserData));
	}

	final <T> int postDocuments(final Map<String, Field> fields, final Collection<T> documents,
			final Map<String, String> commitUserData, final boolean update) throws IOException {
		checkIsMaster();
		return write(context -> checkCommit(context.postDocuments(fields, documents, commitUserData, update),
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
		Objects.requireNonNull(queryDefinition.query, "The query is missing - Index: " + indexName);
		try (final ReadWriteSemaphores.Lock lock = readWriteSemaphores.acquireWriteSemaphore()) {
			return writerAndSearcher.search((indexSearcher, taxonomyReader) -> {
				try (final QueryContext queryContext = buildQueryContext(indexSearcher, taxonomyReader, null)) {
					final Query query = queryDefinition.query.getQuery(queryContext);
					final IndexWriter indexWriter = writerAndSearcher.getIndexWriter();
					int docs = indexWriter.numDocs();
					indexWriter.deleteDocuments(query);
					if (queryDefinition.commitUserData != null)
						indexWriter.setLiveCommitData(queryDefinition.commitUserData.entrySet());
					nrtCommit();
					docs -= indexWriter.numDocs();
					return new ResultDefinition.WithMap(docs);
				} catch (ParseException | ReflectiveOperationException | QueryNodeException e) {
					throw ServerException.of(e);
				}
			});
		}
	}

	final List<TermEnumDefinition> getTermsEnum(final String fieldName, final String prefix, final Integer start,
			final Integer rows) throws InterruptedException, IOException {
		Objects.requireNonNull(fieldName, "The field name is missing - Index: " + indexName);
		try (final ReadWriteSemaphores.Lock lock = readWriteSemaphores.acquireReadSemaphore()) {
			return writerAndSearcher.search((indexSearcher, taxonomyReader) -> {
				final FieldTypeInterface fieldType = fieldMap.getFieldType(null, fieldName);
				if (fieldType == null)
					throw new ServerException(Response.Status.NOT_FOUND,
							"Field not found: " + fieldName + " - Index: " + indexName);
				final Terms terms = MultiFields.getTerms(indexSearcher.getIndexReader(), fieldName);
				if (terms == null)
					return Collections.emptyList();
				return TermEnumDefinition.buildTermList(fieldType, terms.iterator(), prefix, start == null ? 0 : start,
						rows == null ? 20 : rows);
			});
		}
	}

	private QueryContextImpl buildQueryContext(final IndexSearcher indexSearcher, final TaxonomyReader taxonomyReader,
			final FieldMapWrapper.Cache fieldMapWrappers) throws IOException {
		return new QueryContextImpl(indexProvider, fileResourceLoader, executorService, indexAnalyzers, queryAnalyzers,
				fieldMap, fieldMapWrappers, indexSearcher, taxonomyReader);
	}

	final <T> T query(final FieldMapWrapper.Cache fieldMapWrappers,
			final IndexServiceInterface.QueryActions<T> queryActions) throws IOException {
		try (final ReadWriteSemaphores.Lock lock = readWriteSemaphores.acquireReadSemaphore()) {
			return writerAndSearcher.search((indexSearcher, taxonomyReader) -> {
				try (final QueryContextImpl context = buildQueryContext(indexSearcher, taxonomyReader,
						fieldMapWrappers)) {
					return queryActions.apply(context);
				}
			});
		}
	}

	final Explanation explain(final QueryDefinition queryDefinition, final int docId)
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {
		try (final ReadWriteSemaphores.Lock lock = readWriteSemaphores.acquireReadSemaphore()) {
			return writerAndSearcher.search((indexSearcher, taxonomyReader) -> {
				try (final QueryContextImpl context = buildQueryContext(indexSearcher, taxonomyReader, null)) {
					return new QueryExecution<>(context, queryDefinition).explain(docId);
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
		this.fieldMap.getFieldDefinitionMap().forEach((name, fieldDef) -> {
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

	public static class ResourceInfo {

		public final long lastModified;
		public final long length;

		public ResourceInfo() {
			lastModified = 0;
			length = 0;
		}

		private ResourceInfo(final File file) {
			lastModified = file.lastModified();
			length = file.length();
		}

		@Override
		public boolean equals(final Object o) {
			if (o == null || !(o instanceof ResourceInfo))
				return false;
			final ResourceInfo info = (ResourceInfo) o;
			return lastModified == info.lastModified && length == info.length;
		}

	}

	final void postResource(final String resourceName, final Long lastModified, final InputStream inputStream)
			throws IOException {
		if (!fileSet.resourcesDirectory.exists())
			fileSet.resourcesDirectory.mkdir();
		final File resourceFile = fileResourceLoader.checkResourceName(resourceName);
		IOUtils.copy(inputStream, resourceFile);
		if (lastModified != null)
			resourceFile.setLastModified(lastModified);
		refreshFieldsAnalyzers();
	}

	final LinkedHashMap<String, ResourceInfo> getResources() {
		final LinkedHashMap<String, ResourceInfo> map = new LinkedHashMap<>();
		if (!fileSet.resourcesDirectory.exists())
			return map;
		final File[] files = fileSet.resourcesDirectory.listFiles();
		if (files == null)
			return map;
		for (File file : files)
			map.put(file.getName(), new ResourceInfo(file));
		return map;
	}

	final InputStream getResource(final String resourceName) throws IOException {
		if (!fileSet.resourcesDirectory.exists())
			throw new ServerException(Response.Status.NOT_FOUND,
					"Resource not found : " + resourceName + " - Index: " + indexName);
		return fileResourceLoader.openResource(resourceName);
	}

	final void deleteResource(final String resourceName) {
		if (!fileSet.resourcesDirectory.exists())
			throw new ServerException(Response.Status.NOT_FOUND,
					"Resource not found : " + resourceName + " - Index: " + indexName);
		final File resourceFile = fileResourceLoader.checkResourceName(resourceName);
		if (!resourceFile.exists())
			throw new ServerException(Response.Status.NOT_FOUND,
					"Resource not found : " + resourceName + " - Index: " + indexName);
		resourceFile.delete();
	}

	final FileResourceLoader newResourceLoader(final FileResourceLoader resourceLoader) {
		return new FileResourceLoader(resourceLoader, fileSet.resourcesDirectory);
	}

}
