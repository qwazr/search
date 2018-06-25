/*
 * Copyright 2016-2018 Emmanuel Keller / QWAZR
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
package com.qwazr.search.annotations;

import com.qwazr.binder.FieldMapWrapper;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.CustomFieldDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.SmartFieldDefinition;
import com.qwazr.search.index.AnnotatedServiceInterface;
import com.qwazr.search.index.BackupStatus;
import com.qwazr.search.index.ExplainDefinition;
import com.qwazr.search.index.FieldStats;
import com.qwazr.search.index.IndexCheckStatus;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.search.index.PostDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.QueryDocumentsIterator;
import com.qwazr.search.index.ReplicationStatus;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.index.ResultDocumentMap;
import com.qwazr.search.index.ResultDocumentObject;
import com.qwazr.search.index.ResultDocumentsInterface;
import com.qwazr.search.index.SchemaSettingsDefinition;
import com.qwazr.search.index.TermDefinition;
import com.qwazr.search.index.TermEnumDefinition;
import com.qwazr.utils.AnnotationsUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
import org.apache.commons.lang3.NotImplementedException;

import javax.ws.rs.NotAcceptableException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.concurrent.ExecutionException;

public class AnnotatedIndexService<T> {

    protected final AnnotatedServiceInterface annotatedService;

    protected final IndexServiceInterface indexService;

    protected final String schemaName;

    protected final String indexName;

    protected final IndexSettingsDefinition settings;

    private final FieldMapWrappers fieldMapWrappers;

    private final FieldMapWrapper<T> schemaFieldMapWrapper;

    private final LinkedHashMap<String, Field> fieldMap;

    private final LinkedHashMap<String, FieldDefinition> fieldDefinitions;

    /**
     * Create a new index service. A class with Index and IndexField annotations.
     *
     * @param indexService         the IndexServiceInterface to use
     * @param indexDefinitionClass the class with define the index
     * @param schemaName           the name of the schema
     * @param indexName            the name of the index
     * @param settings             any additional settings
     * @throws URISyntaxException if the syntax of the remote URI is wrong
     */
    public AnnotatedIndexService(final IndexServiceInterface indexService, final Class<T> indexDefinitionClass,
            final String schemaName, final String indexName, final IndexSettingsDefinition settings)
            throws URISyntaxException {
        Objects.requireNonNull(indexService, "The indexService parameter is null");
        Objects.requireNonNull(indexDefinitionClass, "The indexDefinition parameter is null");
        this.indexService = indexService;
        this.annotatedService =
                indexService instanceof AnnotatedServiceInterface ? (AnnotatedServiceInterface) indexService : null;
        Index index = indexDefinitionClass.getAnnotation(Index.class);
        Objects.requireNonNull(index, "This class does not declare any Index annotation: " + indexDefinitionClass);

        this.schemaName = schemaName != null ? schemaName : index.schema();
        this.indexName = indexName != null ? indexName : index.name();
        this.settings = settings != null ? settings : IndexSettingsDefinition.of(index).build();

        this.fieldMap = new LinkedHashMap<>();
        this.fieldDefinitions = new LinkedHashMap<>();
        final Map<String, IndexField> indexFieldMap = new LinkedHashMap<>();
        final Map<String, SmartField> smartFieldMap = new LinkedHashMap<>();
        final Map<String, Copy> copyMap = new LinkedHashMap<>();

        AnnotationsUtils.browseFieldsRecursive(indexDefinitionClass, field -> {
            if (field.isAnnotationPresent(IndexField.class)) {
                field.setAccessible(true);
                final IndexField indexField = field.getDeclaredAnnotation(IndexField.class);
                final String fieldName = FieldMapWrappers.getFieldName(indexField.name(), field);
                putCheckNotTwice(indexFieldMap, fieldName, indexField);
                fieldMap.putIfAbsent(fieldName, field);
            }
            if (field.isAnnotationPresent(SmartField.class)) {
                field.setAccessible(true);
                final SmartField smartField = field.getDeclaredAnnotation(SmartField.class);
                final String fieldName = FieldMapWrappers.getFieldName(smartField.name(), field);
                putCheckNotTwice(smartFieldMap, fieldName, smartField);
                fieldMap.putIfAbsent(fieldName, field);
            }
            if (field.isAnnotationPresent(Copy.class)) {
                field.setAccessible(true);
                final Copy copy = field.getDeclaredAnnotation(Copy.class);
                final String fieldName = FieldMapWrappers.getFieldName(copy.name(), field);
                putCheckNotTwice(copyMap, fieldName, copy);
                fieldMap.putIfAbsent(fieldName, field);
            }
        });

        smartFieldMap.forEach((name, propertyField) -> fieldDefinitions.put(name,
                new SmartFieldDefinition(name, propertyField, copyMap)));
        indexFieldMap.forEach((name, propertyField) -> fieldDefinitions.put(name,
                new CustomFieldDefinition(name, propertyField, copyMap)));

        this.fieldMapWrappers = new FieldMapWrappers(fieldMap.keySet());
        this.schemaFieldMapWrapper = fieldMapWrappers.get(indexDefinitionClass);
    }

    private <F> void putCheckNotTwice(Map<String, F> map, String fieldName, F newField) {
        final F duplicateField = map.put(fieldName, newField);
        if (duplicateField != null)
            throw new NotAcceptableException(
                    "This field name has been defined twice: " + fieldName + " - Fields: " + duplicateField + "/" +
                            newField);
    }

    public AnnotatedIndexService(final IndexServiceInterface indexService, final Class<T> indexDefinitionClass)
            throws URISyntaxException {
        this(indexService, indexDefinitionClass, null, null, null);
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getIndexName() {
        return indexName;
    }

    private void checkParameters() {
        if (StringUtils.isEmpty(schemaName))
            throw new RuntimeException("The schema name is empty");
        if (StringUtils.isEmpty(indexName))
            throw new RuntimeException("The index name is empty");
    }

    /**
     * Create a new schema or update an existing one
     *
     * @return the schema settings
     */
    public SchemaSettingsDefinition createUpdateSchema() {
        checkParameters();
        return indexService.createUpdateSchema(schemaName);
    }

    /**
     * Create a new schema or update an existing one
     *
     * @param settings the settings to set
     * @return the schema settings
     */
    public SchemaSettingsDefinition createUpdateSchema(final SchemaSettingsDefinition settings) {
        checkParameters();
        return indexService.createUpdateSchema(schemaName, settings);
    }

    /**
     * Delete the schema
     */
    public void deleteSchema() {
        checkParameters();
        indexService.deleteSchema(schemaName);
    }

    /**
     * Create a new index or update an existing one.
     *
     * @return the index status
     */
    public IndexStatus createUpdateIndex() {
        checkParameters();
        return indexService.createUpdateIndex(schemaName, indexName, settings);
    }

    /**
     * Check if an index is valid
     *
     * @return the checked index status
     */
    public IndexCheckStatus checkIndex() {
        checkParameters();
        return indexService.checkIndex(schemaName, indexName);
    }

    public void deleteIndex() {
        checkParameters();
        indexService.deleteIndex(schemaName, indexName);
    }

    /**
     * Set a collection of fields by reading the annotated fields.
     *
     * @return the field map
     */
    public LinkedHashMap<String, FieldDefinition> createUpdateFields() {
        checkParameters();
        return indexService.setFields(schemaName, indexName, fieldDefinitions);
    }

    /**
     * Reload the analyzers. Especially for resources reloading (synonyms map, stopwords, ...)
     */
    public void refreshAnalyzers() {
        checkParameters();
        indexService.refreshAnalyzers(schemaName, indexName);
    }

    public enum FieldStatus {
        NOT_IDENTICAL, EXISTS_ONLY_IN_INDEX, EXISTS_ONLY_IN_ANNOTATION
    }

    /**
     * Check if the there is differences between the annotated fields and the fields already declared
     *
     * @return the changed fields
     */
    public Map<String, FieldStatus> getFieldChanges() {
        checkParameters();
        final LinkedHashMap<String, FieldDefinition> indexFields = indexService.getFields(schemaName, indexName);
        final HashMap<String, FieldStatus> fieldChanges = new HashMap<>();
        fieldMap.forEach((name, propertyField) -> {
            final FieldDefinition annotatedField = fieldDefinitions.get(name);
            final FieldDefinition indexField = indexFields == null ? null : indexFields.get(name);
            if (indexField == null)
                fieldChanges.put(name, FieldStatus.EXISTS_ONLY_IN_ANNOTATION);
            else if (!indexField.equals(annotatedField))
                fieldChanges.put(name, FieldStatus.NOT_IDENTICAL);
        });
        if (indexFields != null) {
            indexFields.forEach((name, indexField) -> {
                if (!fieldDefinitions.containsKey(name))
                    fieldChanges.put(name, FieldStatus.EXISTS_ONLY_IN_INDEX);
            });
        }
        return fieldChanges;
    }

    public void postResource(final String resourceName, final InputStream input) {
        checkParameters();
        indexService.postResource(schemaName, indexName, resourceName, System.currentTimeMillis(), input);
    }

    public void postTextResource(final String resourceName, final String text) throws IOException {
        try (final InputStream input = IOUtils.toInputStream(text, StandardCharsets.UTF_8)) {
            postResource(resourceName, input);
        }
    }

    public InputStream getResource(final String resourceName) throws IOException {
        checkParameters();
        return indexService.getResource(schemaName, indexName, resourceName);
    }

    public String getTextResource(final String resourceName) throws IOException {
        try (final InputStream input = getResource(resourceName)) {
            return IOUtils.toString(input, StandardCharsets.UTF_8);
        }
    }

    public void deleteResource(final String resourceName) {
        checkParameters();
        indexService.deleteResource(schemaName, indexName, resourceName);
    }

    /**
     * Add a document to the index
     *
     * @param row            the document to index
     * @param commitUserData the optional user data
     * @throws IOException          if any I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public void addDocument(final T row, final Map<String, String> commitUserData)
            throws IOException, InterruptedException {
        checkParameters();
        Objects.requireNonNull(row, "The document (row) cannot be null");
        if (annotatedService != null)
            annotatedService.addDocument(schemaName, indexName, fieldMap, row, commitUserData);
        else
            indexService.postMappedDocument(schemaName, indexName,
                    PostDefinition.of(schemaFieldMapWrapper.newMap(row), commitUserData, false));
    }

    /**
     * Add a document to the index
     *
     * @param row the document to index
     * @throws IOException          if any I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public void addDocument(final T row) throws IOException, InterruptedException {
        addDocument(row, null);
    }

    /**
     * Add a documents to the index
     *
     * @param row            the document to index
     * @param commitUserData the optional user data
     * @throws IOException          if any I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public void addDocuments(final Collection<T> row, final Map<String, String> commitUserData)
            throws IOException, InterruptedException {
        checkParameters();
        Objects.requireNonNull(row, "The document (row) cannot be null");
        if (annotatedService != null)
            annotatedService.addDocuments(schemaName, indexName, fieldMap, row, commitUserData);
        else
            indexService.postMappedDocuments(schemaName, indexName,
                    PostDefinition.of(schemaFieldMapWrapper.newMapCollection(row), commitUserData, false));
    }

    public void addDocuments(final Collection<T> documents) throws IOException, InterruptedException {
        addDocuments(documents, null);
    }

    /**
     * Post a document to the index
     *
     * @param row            the document to index
     * @param commitUserData the optional user data
     * @throws IOException          if any I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public void postDocument(final T row, final Map<String, String> commitUserData)
            throws IOException, InterruptedException {
        checkParameters();
        Objects.requireNonNull(row, "The document (row) cannot be null");
        if (annotatedService != null)
            annotatedService.postDocument(schemaName, indexName, fieldMap, row, commitUserData);
        else
            indexService.postMappedDocument(schemaName, indexName,
                    PostDefinition.of(schemaFieldMapWrapper.newMap(row), commitUserData));
    }

    /**
     * Post a document to the index
     *
     * @param row the document to index
     * @throws IOException          if any I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public void postDocument(final T row) throws IOException, InterruptedException {
        postDocument(row, null);
    }

    /**
     * Post a collection of document to the index
     *
     * @param rows           a collection of document to index
     * @param commitUserData the optional user data
     * @throws IOException          if any I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public void postDocuments(final Collection<T> rows, final Map<String, String> commitUserData)
            throws IOException, InterruptedException {
        checkParameters();
        Objects.requireNonNull(rows, "The documents collection (rows) cannot be null");
        if (annotatedService != null)
            annotatedService.postDocuments(schemaName, indexName, fieldMap, rows, commitUserData);
        else
            indexService.postMappedDocuments(schemaName, indexName,
                    PostDefinition.of(schemaFieldMapWrapper.newMapCollection(rows), commitUserData));
    }

    /**
     * Post a collection of document to the index
     *
     * @param rows a collection of document to index
     * @throws IOException          if any I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public void postDocuments(final Collection<T> rows) throws IOException, InterruptedException {
        postDocuments(rows, null);
    }

    /**
     * Update the DocValues of one document
     *
     * @param row            a collection of DocValues to update
     * @param commitUserData the optional user data
     * @throws IOException          if any I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public void updateDocumentValues(final T row, final Map<String, String> commitUserData) throws IOException {
        checkParameters();
        Objects.requireNonNull(row, "The document (row) cannot be null");
        if (annotatedService != null)
            annotatedService.updateDocValues(schemaName, indexName, fieldMap, row, commitUserData);
        else
            indexService.updateMappedDocValues(schemaName, indexName,
                    PostDefinition.of(schemaFieldMapWrapper.newMap(row), commitUserData));
    }

    /**
     * Update the DocValues of one document
     *
     * @param row a collection of DocValues to update
     * @throws IOException          if any I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public void updateDocumentValues(final T row) throws IOException, InterruptedException {
        updateDocumentValues(row, null);
    }

    /**
     * Update the DocValues of a collection of document
     *
     * @param rows           a collection of document with a collection of DocValues to update
     * @param commitUserData the optional user data
     * @throws IOException          if any I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public void updateDocumentsValues(final Collection<T> rows, final Map<String, String> commitUserData)
            throws IOException, InterruptedException {
        checkParameters();
        Objects.requireNonNull(rows, "The documents collection (rows) cannot be null");
        if (annotatedService != null)
            annotatedService.updateDocsValues(schemaName, indexName, fieldMap, rows, commitUserData);
        else
            indexService.updateMappedDocsValues(schemaName, indexName,
                    PostDefinition.of(schemaFieldMapWrapper.newMapCollection(rows), commitUserData));
    }

    /**
     * Update the DocValues of a collection of document
     *
     * @param rows a collection of document with a collection of DocValues to update
     * @throws IOException          if any I/O error occurs
     * @throws InterruptedException if the process is interrupted
     */
    public void updateDocumentsValues(final Collection<T> rows) throws IOException, InterruptedException {
        updateDocumentsValues(rows, null);
    }

    private <C> C getDocument(final Object id, final FieldMapWrapper<C> wrapper)
            throws ReflectiveOperationException, IOException {
        checkParameters();
        Objects.requireNonNull(id, "The id cannot be empty");
        if (annotatedService != null)
            return annotatedService.getDocument(schemaName, indexName, id, wrapper);
        else {
            return wrapper.toRecord(indexService.getDocument(schemaName, indexName, id.toString()));
        }
    }

    /**
     * @param id          The ID of the document
     * @param objectClass the type of the instance to return
     * @return an filled object or null if the document does not exist
     * @throws ReflectiveOperationException if the document cannot be created
     */
    public <C> C getDocument(final Object id, final Class<C> objectClass)
            throws ReflectiveOperationException, IOException {
        return getDocument(id, fieldMapWrappers.get(objectClass));
    }

    /**
     * @param id The ID of the document
     * @return an filled object or null if the document does not exist
     * @throws ReflectiveOperationException if the document cannot be created
     */
    public T getDocument(final Object id) throws ReflectiveOperationException, IOException {
        return getDocument(id, schemaFieldMapWrapper);
    }

    private <C> List<C> getDocuments(final Integer start, final Integer rows, final FieldMapWrapper<C> wrapper)
            throws IOException, ReflectiveOperationException {
        checkParameters();
        if (annotatedService != null)
            return annotatedService.getDocuments(schemaName, indexName, start, rows, wrapper);
        else
            return wrapper.toRecords(indexService.getDocuments(schemaName, indexName, start, rows));
    }

    public <C> List<C> getDocuments(final Integer start, final Integer rows, final Class<C> clazz)
            throws IOException, ReflectiveOperationException {
        checkParameters();
        return getDocuments(start, rows, fieldMapWrappers.get(clazz));
    }

    public List<T> getDocuments(final Integer start, final Integer rows)
            throws IOException, ReflectiveOperationException {
        checkParameters();
        return getDocuments(start, rows, schemaFieldMapWrapper);
    }

    /**
     * @return the status of the index
     */
    public IndexStatus getIndexStatus() {
        checkParameters();
        return indexService.getIndex(schemaName, indexName);
    }

    public LinkedHashMap<String, FieldDefinition> getFields() {
        checkParameters();
        return indexService.getFields(schemaName, indexName);
    }

    public FieldDefinition getField(final String fieldName) {
        checkParameters();
        return indexService.getField(schemaName, indexName, fieldName);
    }

    public FieldStats getFieldStats(final String fieldName) {
        checkParameters();
        return indexService.getFieldStats(schemaName, indexName, fieldName);
    }

    public void setField(final String fieldName, final FieldDefinition fieldDefinition) {
        checkParameters();
        indexService.setField(schemaName, indexName, fieldName, fieldDefinition);
    }

    public void deleteField(final String fieldName) {
        checkParameters();
        indexService.deleteField(schemaName, indexName, fieldName);
    }

    public LinkedHashMap<String, AnalyzerDefinition> getAnalyzers() {
        checkParameters();
        return indexService.getAnalyzers(schemaName, indexName);
    }

    public AnalyzerDefinition getAnalyzer(final String analyzerName) {
        checkParameters();
        return indexService.getAnalyzer(schemaName, indexName, analyzerName);
    }

    public AnalyzerDefinition setAnalyzer(final String analyzerName, final AnalyzerDefinition analyzerDefinition) {
        checkParameters();
        return indexService.setAnalyzer(schemaName, indexName, analyzerName, analyzerDefinition);
    }

    public LinkedHashMap<String, AnalyzerDefinition> setAnalyzers(final String analyzerName,
            final LinkedHashMap<String, AnalyzerDefinition> analyzers) {
        checkParameters();
        return indexService.setAnalyzers(schemaName, indexName, analyzers);
    }

    public void deleteAnalyzer(final String analyzerName) {
        checkParameters();
        indexService.deleteAnalyzer(schemaName, indexName, analyzerName);
    }

    public List<TermDefinition> testAnalyzer(final String analyzerName, final String text) {
        checkParameters();
        return indexService.testAnalyzer(schemaName, indexName, analyzerName, text);
    }

    public List<TermDefinition> doAnalyzeIndex(final String fieldName, final String text) {
        checkParameters();
        return indexService.doAnalyzeIndex(schemaName, indexName, fieldName, text);
    }

    public List<TermDefinition> doAnalyzeQuery(final String fieldName, final String text) {
        checkParameters();
        return indexService.doAnalyzeQuery(schemaName, indexName, fieldName, text);
    }

    public SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>> getBackups(final String backupName,
            final boolean extractVersion) {
        checkParameters();
        return indexService.getBackups(schemaName, indexName, backupName, extractVersion);
    }

    public SortedMap<String, SortedMap<String, BackupStatus>> doBackup(final String backupName)
            throws ExecutionException, InterruptedException {
        checkParameters();
        return indexService.doBackup(schemaName, indexName, backupName);
    }

    public Integer deleteBackups(final String backupName) {
        checkParameters();
        return indexService.deleteBackups(schemaName, indexName, backupName);
    }

    private <C> ResultDefinition.WithObject<C> searchQuery(final QueryDefinition query,
            final FieldMapWrapper<C> wrapper) {
        checkParameters();
        if (annotatedService != null)
            return annotatedService.searchQuery(schemaName, indexName, query, wrapper);
        else
            return toRecords(indexService.searchQuery(schemaName, indexName, query, false), wrapper);
    }

    public ResultDefinition.WithObject<T> searchQuery(final QueryDefinition query) {
        return searchQuery(query, schemaFieldMapWrapper);
    }

    /**
     * Execute a search query
     *
     * @param query       the query to execute
     * @param objectClass the type of the objects to return
     * @param <C>         the type of the objects
     * @return the results
     */
    public <C> ResultDefinition.WithObject<C> searchQuery(final QueryDefinition query, final Class<C> objectClass)
            throws IOException, ReflectiveOperationException {
        checkParameters();
        return searchQuery(query, fieldMapWrappers.get(objectClass));
    }

    /**
     * Iterator over any document who is matching the query
     *
     * @param query       the query to execute
     * @param objectClass the type of the objects to return
     * @param <C>         the type of the objects
     * @return a new iterator
     */
    public <C> Iterator<C> searchIterator(final QueryDefinition query, final Class<C> objectClass) {
        checkParameters();
        return new QueryDocumentsIterator<>(this, query, objectClass);
    }

    /**
     * Execute a search query
     *
     * @param query           the query to execute
     * @param resultDocuments the consumer which obtain the results
     * @return the results
     */
    public ResultDefinition.Empty searchQuery(final QueryDefinition query,
            final ResultDocumentsInterface resultDocuments) {
        checkParameters();
        if (annotatedService != null)
            return annotatedService.searchQuery(schemaName, indexName, query, resultDocuments);
        else
            throw new NotImplementedException("Method not available");
    }

    /**
     * Explain a query applied to a given DocId
     *
     * @param query the query definition
     * @param docId the ID of the document
     * @return the score computation for document and query
     */
    public ExplainDefinition explainQuery(final QueryDefinition query, final int docId) {
        checkParameters();
        return indexService.explainQuery(schemaName, indexName, query, docId);
    }

    /**
     * Explain a query applied to a given DocId using plain text
     *
     * @param query the query definition
     * @param docId the ID of the document
     * @return the score computation for document and query
     */
    public String explainQueryText(final QueryDefinition query, final int docId) {
        checkParameters();
        return indexService.explainQueryText(schemaName, indexName, query, docId);
    }

    /**
     * Explain a query applied to a given DocId using dot format
     *
     * @param query               the query definition
     * @param docId               the ID of the document
     * @param descriptionWrapSize the maximum number of character per line
     * @return the score computation for document and query
     */
    public String explainQueryDot(final QueryDefinition query, final int docId, final Integer descriptionWrapSize) {
        checkParameters();
        return indexService.explainQueryDot(schemaName, indexName, query, docId, descriptionWrapSize);
    }

    public ResultDefinition.WithMap searchQueryWithMap(final QueryDefinition query) {
        checkParameters();
        return indexService.searchQuery(schemaName, indexName, query, false);
    }

    public List<TermEnumDefinition> doExtractTerms(final String fieldName, final Integer start, final Integer rows) {
        checkParameters();
        return indexService.doExtractTerms(schemaName, indexName, fieldName, start, rows);
    }

    public List<TermEnumDefinition> doExtractTerms(final String fieldName, final String prefix, final Integer start,
            final Integer rows) {
        checkParameters();
        return indexService.doExtractTerms(schemaName, indexName, fieldName, prefix, start, rows);
    }

    /**
     * Delete the documents matching the query
     *
     * @param query the query to execute
     * @return the results
     */
    public ResultDefinition<?> deleteByQuery(final QueryDefinition query) {
        checkParameters();
        return indexService.searchQuery(schemaName, indexName, query, true);
    }

    public void registerClass(final Class<?> objectClass) throws NoSuchMethodException {
        fieldMapWrappers.newFieldMapWrapper(objectClass);
    }

    public ReplicationStatus replicationCheck() throws ExecutionException, InterruptedException {
        return indexService.replicationCheck(schemaName, indexName);
    }

    private <C> ResultDefinition.WithObject<C> toRecords(final ResultDefinition<?> result,
            final FieldMapWrapper<C> wrapper) {
        if (result == null)
            return null;
        if (!(result instanceof ResultDefinition.WithMap))
            return new ResultDefinition.WithObject<>(result.total_hits);
        final ResultDefinition.WithMap resultWithMap = (ResultDefinition.WithMap) result;
        final List<ResultDocumentObject<C>> documents = new ArrayList<>();
        try {
            if (resultWithMap.documents != null)
                for (ResultDocumentMap resultDocMap : resultWithMap.documents)
                    documents.add(new ResultDocumentObject<>(resultDocMap, wrapper.toRecord(resultDocMap.fields)));
        } catch (ReflectiveOperationException | IOException e) {
            throw new RuntimeException(e);
        }
        return new ResultDefinition.WithObject<>(resultWithMap, documents);
    }

    public <R> R write(final IndexServiceInterface.WriteActions<R> actions) throws IOException {
        checkParameters();
        return indexService.write(schemaName, indexName, actions);
    }

    public <R> R query(final IndexServiceInterface.QueryActions<R> actions) throws IOException {
        checkParameters();
        return indexService.query(schemaName, indexName, fieldMapWrappers, actions);
    }

    public void deleteAll() {
        checkParameters();
        indexService.deleteAll(schemaName, indexName);
    }

    public IndexStatus mergeIndex(final String mergedIndex, final Map<String, String> commitUserData) {
        checkParameters();
        return indexService.mergeIndex(schemaName, indexName, mergedIndex, commitUserData);
    }
}
