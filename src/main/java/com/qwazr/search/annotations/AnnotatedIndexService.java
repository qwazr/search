/*
 * Copyright 2016-2020 Emmanuel Keller / QWAZR
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
import com.qwazr.search.query.QueryInterface;
import com.qwazr.utils.AnnotationsUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
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
import java.util.Set;
import java.util.SortedMap;
import javax.ws.rs.NotAcceptableException;
import org.apache.commons.lang3.NotImplementedException;

public class AnnotatedIndexService<T> {

    protected final AnnotatedServiceInterface annotatedService;

    protected final IndexServiceInterface indexService;

    protected final String schemaName;

    protected final String indexName;

    protected final IndexSettingsDefinition settings;

    private final FieldMapWrappers fieldMapWrappers;

    private final FieldMapWrapper<T> schemaFieldMapWrapper;

    private final Map<String, Field> fieldMap;

    private final Map<String, FieldDefinition> fieldDefinitions;

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
    public AnnotatedIndexService(final IndexServiceInterface indexService,
                                 final Class<T> indexDefinitionClass,
                                 final String schemaName,
                                 final String indexName,
                                 final IndexSettingsDefinition settings)
        throws URISyntaxException {
        Objects.requireNonNull(indexService, "The indexService parameter is null");
        Objects.requireNonNull(indexDefinitionClass, "The indexDefinition parameter is null");
        this.indexService = indexService;
        this.annotatedService =
            indexService instanceof AnnotatedServiceInterface ? (AnnotatedServiceInterface) indexService : null;
        final Index index = indexDefinitionClass.getAnnotation(Index.class);
        Objects.requireNonNull(index, "This class does not declare any Index annotation: " + indexDefinitionClass);

        this.schemaName = schemaName != null ? schemaName : index.schema();
        this.indexName = indexName != null ? indexName : index.name();

        if (StringUtils.isEmpty(this.schemaName))
            throw new RuntimeException("The schema name is empty");
        if (StringUtils.isEmpty(this.indexName))
            throw new RuntimeException("The index name is empty");

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
                fieldMap.put(fieldName, field);
            }
            if (field.isAnnotationPresent(SmartField.class)) {
                field.setAccessible(true);
                final SmartField smartField = field.getDeclaredAnnotation(SmartField.class);
                final String fieldName = FieldMapWrappers.getFieldName(smartField.name(), field);
                putCheckNotTwice(smartFieldMap, fieldName, smartField);
                fieldMap.put(fieldName, field);
            }
            if (field.isAnnotationPresent(Copy.class)) {
                field.setAccessible(true);
                final Copy copy = field.getDeclaredAnnotation(Copy.class);
                final String fieldName = FieldMapWrappers.getFieldName(copy.name(), field);
                putCheckNotTwice(copyMap, fieldName, copy);
                fieldMap.put(fieldName, field);
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

    public IndexSettingsDefinition getSettings() {
        return indexService.getIndexSettings(schemaName, indexName);
    }

    /**
     * Create a new schema or update an existing one
     *
     * @return the schema settings
     */
    public SchemaSettingsDefinition createUpdateSchema() {
        return indexService.createUpdateSchema(schemaName);
    }

    /**
     * Create a new schema or update an existing one
     *
     * @param settings the settings to set
     * @return the schema settings
     */
    public SchemaSettingsDefinition createUpdateSchema(final SchemaSettingsDefinition settings) {
        return indexService.createUpdateSchema(schemaName, settings);
    }

    /**
     * Delete the schema
     */
    public void deleteSchema() {
        indexService.deleteSchema(schemaName);
    }

    /**
     * Create a new index or update an existing one.
     *
     * @return the index status
     */
    public IndexStatus createUpdateIndex() {
        return indexService.createUpdateIndex(schemaName, indexName, settings);
    }

    /**
     * Check if an index is valid
     *
     * @return the checked index status
     */
    public IndexCheckStatus checkIndex() {
        return indexService.checkIndex(schemaName, indexName);
    }

    public void deleteIndex() {
        indexService.deleteIndex(schemaName, indexName);
    }

    /**
     * Set a collection of fields by reading the annotated fields.
     *
     * @return the field map
     */
    public Map<String, FieldDefinition> createUpdateFields() {
        return indexService.setFields(schemaName, indexName, fieldDefinitions);
    }

    /**
     * Reload the analyzers. Especially for resources reloading (synonyms map, stopwords, ...)
     */
    public void refreshAnalyzers() {
        indexService.refreshAnalyzers(schemaName, indexName);
    }

    /**
     * @return the list of available query types./
     */
    public Set<String> getQueryTypes() {
        return indexService.getQueryTypes(schemaName, indexName);
    }

    /**
     * @param queryType The type of the query to get the sample of
     * @return a sample of query based on the type and the current index settings, field and analyzer.
     */
    public QueryInterface getQuerySample(final String queryType) {
        return indexService.getQuerySample(schemaName, indexName, queryType);
    }

    public Map<String, Object> getJsonSample() {
        return indexService.getJsonSample(schemaName, indexName);
    }

    public List<Map<String, Object>> getJsonSamples(Integer count) {
        return indexService.getJsonSamples(schemaName, indexName, count);
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
        final Map<String, FieldDefinition> indexFields = indexService.getFields(schemaName, indexName);
        final Map<String, FieldStatus> fieldChanges = new HashMap<>();
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
        indexService.postResource(schemaName, indexName, resourceName, System.currentTimeMillis(), input);
    }

    public void postTextResource(final String resourceName, final String text) throws IOException {
        try (final InputStream input = IOUtils.toInputStream(text, StandardCharsets.UTF_8)) {
            postResource(resourceName, input);
        }
    }

    public InputStream getResource(final String resourceName) {
        return indexService.getResource(schemaName, indexName, resourceName);
    }

    public String getTextResource(final String resourceName) throws IOException {
        try (final InputStream input = getResource(resourceName)) {
            return IOUtils.toString(input, StandardCharsets.UTF_8);
        }
    }

    public void deleteResource(final String resourceName) {
        indexService.deleteResource(schemaName, indexName, resourceName);
    }

    /**
     * Add a document to the index
     *
     * @param row            the document to index
     * @param commitUserData the optional user data
     * @throws IOException if any I/O error occurs
     */
    public void addDocument(final T row, final Map<String, String> commitUserData)
        throws IOException {
        Objects.requireNonNull(row, "The document (row) cannot be null");
        if (annotatedService != null)
            annotatedService.addDocument(schemaName, indexName, fieldMap, row, commitUserData);
        else
            indexService.postMappedDocument(schemaName, indexName,
                PostDefinition.of(schemaFieldMapWrapper.newMap(row), commitUserData));
    }

    /**
     * Add a document to the index
     *
     * @param row the document to index
     * @throws IOException if any I/O error occurs
     */
    public void addDocument(final T row) throws IOException {
        addDocument(row, null);
    }

    /**
     * Add a documents to the index
     *
     * @param row            the document to index
     * @param commitUserData the optional user data
     * @throws IOException if any I/O error occurs
     */
    public void addDocuments(final Collection<T> row, final Map<String, String> commitUserData)
        throws IOException {
        Objects.requireNonNull(row, "The document (row) cannot be null");
        if (annotatedService != null)
            annotatedService.addDocuments(schemaName, indexName, fieldMap, row, commitUserData);
        else
            indexService.postMappedDocuments(schemaName, indexName,
                PostDefinition.of(schemaFieldMapWrapper.newMapCollection(row), commitUserData));
    }

    public void addDocuments(final Collection<T> documents) throws IOException {
        addDocuments(documents, null);
    }

    /**
     * Post a document to the index
     *
     * @param row            the document to index
     * @param commitUserData the optional user data
     * @throws IOException if any I/O error occurs
     */
    public void postDocument(final T row, final Map<String, String> commitUserData)
        throws IOException {
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
     * @throws IOException if any I/O error occurs
     */
    public void postDocument(final T row) throws IOException {
        postDocument(row, null);
    }

    /**
     * Post a collection of document to the index
     *
     * @param rows           a collection of document to index
     * @param commitUserData the optional user data
     * @throws IOException if any I/O error occurs
     */
    public void postDocuments(final Collection<T> rows, final Map<String, String> commitUserData)
        throws IOException {
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
     * @throws IOException if any I/O error occurs
     */
    public void postDocuments(final Collection<T> rows) throws IOException {
        postDocuments(rows, null);
    }

    /**
     * Update the DocValues of one document
     *
     * @param row            a collection of DocValues to update
     * @param commitUserData the optional user data
     * @throws IOException if any I/O error occurs
     */
    public void updateDocumentValues(final T row, final Map<String, String> commitUserData) throws IOException {
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
     * @throws IOException if any I/O error occurs
     */
    public void updateDocumentValues(final T row) throws IOException {
        updateDocumentValues(row, null);
    }

    /**
     * Update the DocValues of a collection of document
     *
     * @param rows           a collection of document with a collection of DocValues to update
     * @param commitUserData the optional user data
     * @throws IOException if any I/O error occurs
     */
    public void updateDocumentsValues(final Collection<T> rows,
                                      final Map<String, String> commitUserData) throws IOException {
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
     * @throws IOException if any I/O error occurs
     */
    public void updateDocumentsValues(final Collection<T> rows) throws IOException {
        updateDocumentsValues(rows, null);
    }

    private <C> C getDocument(final Object id, final FieldMapWrapper<C> wrapper)
        throws ReflectiveOperationException, IOException {
        Objects.requireNonNull(id, "The id cannot be empty");
        if (annotatedService != null)
            return annotatedService.getDocument(schemaName, indexName, id, wrapper);
        else {
            return wrapper.toRecord(indexService.getDocument(schemaName, indexName, id.toString()));
        }
    }

    /**
     * @param <C>         the expected type of the returned instance
     * @param id          The ID of the document
     * @param objectClass the type of the instance to return
     * @return an filled object or null if the document does not exist
     * @throws ReflectiveOperationException if the document cannot be created
     * @throws IOException                  if any I/O error occurs
     */
    public <C> C getDocument(final Object id, final Class<C> objectClass)
        throws ReflectiveOperationException, IOException {
        return getDocument(id, fieldMapWrappers.get(objectClass));
    }

    /**
     * @param id The ID of the document
     * @return an filled object or null if the document does not exist
     * @throws IOException                  if any I/O error occurs
     * @throws ReflectiveOperationException if the document cannot be created
     */
    public T getDocument(final Object id) throws ReflectiveOperationException, IOException {
        return getDocument(id, schemaFieldMapWrapper);
    }

    private <C> List<C> getDocuments(final Integer start, final Integer rows, final FieldMapWrapper<C> wrapper)
        throws IOException, ReflectiveOperationException {
        if (annotatedService != null)
            return annotatedService.getDocuments(schemaName, indexName, start, rows, wrapper);
        else
            return wrapper.toRecords(indexService.getDocuments(schemaName, indexName, start, rows));
    }

    public <C> List<C> getDocuments(final Integer start, final Integer rows, final Class<C> clazz)
        throws IOException, ReflectiveOperationException {
        return getDocuments(start, rows, fieldMapWrappers.get(clazz));
    }

    public List<T> getDocuments(final Integer start, final Integer rows)
        throws IOException, ReflectiveOperationException {
        return getDocuments(start, rows, schemaFieldMapWrapper);
    }

    /**
     * @return the status of the index
     */
    public IndexStatus getIndexStatus() {
        return indexService.getIndex(schemaName, indexName);
    }

    public Map<String, FieldDefinition> getFields() {
        return indexService.getFields(schemaName, indexName);
    }

    public FieldDefinition getField(final String fieldName) {
        return indexService.getField(schemaName, indexName, fieldName);
    }

    public FieldStats getFieldStats(final String fieldName) {
        return indexService.getFieldStats(schemaName, indexName, fieldName);
    }

    public void setField(final String fieldName, final FieldDefinition fieldDefinition) {
        indexService.setField(schemaName, indexName, fieldName, fieldDefinition);
    }

    public void deleteField(final String fieldName) {
        indexService.deleteField(schemaName, indexName, fieldName);
    }

    public Map<String, AnalyzerDefinition> getAnalyzers() {
        return indexService.getAnalyzers(schemaName, indexName);
    }

    public AnalyzerDefinition getAnalyzer(final String analyzerName) {
        return indexService.getAnalyzer(schemaName, indexName, analyzerName);
    }

    public AnalyzerDefinition setAnalyzer(final String analyzerName, final AnalyzerDefinition analyzerDefinition) {
        return indexService.setAnalyzer(schemaName, indexName, analyzerName, analyzerDefinition);
    }

    public Map<String, AnalyzerDefinition> setAnalyzers(final LinkedHashMap<String, AnalyzerDefinition> analyzers) {
        return indexService.setAnalyzers(schemaName, indexName, analyzers);
    }

    public void deleteAnalyzer(final String analyzerName) {
        indexService.deleteAnalyzer(schemaName, indexName, analyzerName);
    }

    public List<TermDefinition> testAnalyzer(final String analyzerName, final String text) {
        return indexService.testAnalyzer(schemaName, indexName, analyzerName, text);
    }

    public List<TermDefinition> doAnalyzeIndex(final String fieldName, final String text) {
        return indexService.doAnalyzeIndex(schemaName, indexName, fieldName, text);
    }

    public List<TermDefinition> doAnalyzeQuery(final String fieldName, final String text) {
        return indexService.doAnalyzeQuery(schemaName, indexName, fieldName, text);
    }

    public SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>> getBackups(final String backupName,
                                                                                            final boolean extractVersion) {
        return indexService.getBackups(schemaName, indexName, backupName, extractVersion);
    }

    public SortedMap<String, SortedMap<String, BackupStatus>> doBackup(final String backupName) {
        return indexService.doBackup(schemaName, indexName, backupName);
    }

    public Integer deleteBackups(final String backupName) {
        return indexService.deleteBackups(schemaName, indexName, backupName);
    }

    private <C> ResultDefinition.WithObject<C> searchQuery(final QueryDefinition query,
                                                           final FieldMapWrapper<C> wrapper) {
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
     * @param recordClass the type of the objects to return
     * @param <C>         the type of the objects
     * @return the results
     */
    public <C> ResultDefinition.WithObject<C> searchQuery(final QueryDefinition query, final Class<C> recordClass) {
        return searchQuery(query, fieldMapWrappers.get(recordClass));
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
        return indexService.explainQueryDot(schemaName, indexName, query, docId, descriptionWrapSize);
    }

    public ResultDefinition.WithMap searchQueryWithMap(final QueryDefinition query) {
        return indexService.searchQuery(schemaName, indexName, query, false);
    }

    public List<TermEnumDefinition> doExtractTerms(final String fieldName, final Integer start, final Integer rows) {
        return indexService.doExtractTerms(schemaName, indexName, fieldName, start, rows);
    }

    public List<TermEnumDefinition> doExtractTerms(final String fieldName, final String prefix, final Integer start,
                                                   final Integer rows) {
        return indexService.doExtractTerms(schemaName, indexName, fieldName, prefix, start, rows);
    }

    /**
     * Delete the documents matching the query
     *
     * @param query the query to execute
     * @return the results
     */
    public ResultDefinition<?> deleteByQuery(final QueryDefinition query) {
        return indexService.searchQuery(schemaName, indexName, query, true);
    }

    public void registerClass(final Class<?> objectClass) throws NoSuchMethodException {
        fieldMapWrappers.newFieldMapWrapper(objectClass);
    }

    public <C> FieldMapWrapper<C> getWrapper(final Class<C> recordClass) {
        return fieldMapWrappers.get(recordClass);
    }

    public ReplicationStatus replicationCheck() {
        return indexService.replicationCheck(schemaName, indexName);
    }

    private <C> ResultDefinition.WithObject<C> toRecords(final ResultDefinition<?> result,
                                                         final FieldMapWrapper<C> wrapper) {
        if (result == null)
            return null;
        if (!(result instanceof ResultDefinition.WithMap))
            return new ResultDefinition.WithObject<>(result.totalHits);
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
        return indexService.write(schemaName, indexName, actions);
    }

    public <R> R query(final IndexServiceInterface.QueryActions<R> actions) throws IOException {
        return indexService.query(schemaName, indexName, actions);
    }

    public void deleteAll() {
        indexService.deleteAll(schemaName, indexName);
    }

    public IndexStatus mergeIndex(final String mergedIndex, final Map<String, String> commitUserData) {
        return indexService.mergeIndex(schemaName, indexName, mergedIndex, commitUserData);
    }
}
