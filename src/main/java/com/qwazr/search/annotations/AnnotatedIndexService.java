/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.*;
import com.qwazr.utils.StringUtils;

import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AnnotatedIndexService<T> {

	protected final IndexServiceInterface indexService;

	protected final String schemaName;

	protected final String indexName;

	protected final String similarityClass;

	private final Map<String, IndexField> indexFieldMap;

	private final Map<String, Field> fieldMap;

	/**
	 * Create a new index service. A class with Index and IndexField annotations.
	 *
	 * @param indexService         the IndexServiceInterface to use
	 * @param indexDefinitionClass an annotated class
	 */
	public AnnotatedIndexService(IndexServiceInterface indexService, Class<T> indexDefinitionClass) {
		Objects.requireNonNull(indexService, "The indexService parameter is null");
		Objects.requireNonNull(indexDefinitionClass, "The indexDefinition parameter is null");
		this.indexService = indexService;
		Index index = indexDefinitionClass.getAnnotation(Index.class);
		Objects.requireNonNull(index, "This class does not declare any Index annotation: " + indexDefinitionClass);
		schemaName = index.schema();
		indexName = index.name();
		similarityClass = index.similarityClass();
		Field[] fields = indexDefinitionClass.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			fieldMap = new HashMap<>();
			indexFieldMap = new HashMap<>();
			for (Field field : fields) {
				if (!field.isAnnotationPresent(IndexField.class))
					continue;
				IndexField indexField = field.getDeclaredAnnotation(IndexField.class);
				String indexName = StringUtils.isEmpty(indexField.name()) ? field.getName() : indexField.name();
				indexFieldMap.put(indexName, indexField);
				fieldMap.put(indexName, field);
			}
		} else {
			fieldMap = null;
			indexFieldMap = null;
		}
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
	 * Create a new index or update an existing one.
	 *
	 * @return the index status
	 */
	public IndexStatus createUpdateIndex() {
		checkParameters();
		if (StringUtils.isEmpty(similarityClass))
			return indexService.createUpdateIndex(schemaName, indexName);
		IndexSettingsDefinition settings = new IndexSettingsDefinition(similarityClass);
		return indexService.createUpdateIndex(schemaName, indexName, settings);
	}

	/**
	 * Set a collection of fields by reading the annotated fields.
	 *
	 * @return the field map
	 */
	public LinkedHashMap<String, FieldDefinition> createUpdateFields() {
		checkParameters();
		final LinkedHashMap<String, FieldDefinition> indexFields = new LinkedHashMap<>();
		if (indexFieldMap != null)
			indexFieldMap.forEach((name, indexField) -> indexFields.put(name, new FieldDefinition(indexField)));
		return indexService.setFields(schemaName, indexName, indexFields);
	}

	/**
	 * Post a document to the index
	 *
	 * @param row the document to index
	 * @return the status of the request
	 */
	public Response postDocument(T row) {
		checkParameters();
		Objects.requireNonNull(row, "The document (row) cannot be null");
		return indexService.postDocument(schemaName, indexName, newMap(row));
	}

	/**
	 * Post a collection of document to the index
	 *
	 * @param rows a collection of document to index
	 * @return the status of the request
	 */
	public Response postDocuments(Collection<T> rows) {
		checkParameters();
		Objects.requireNonNull(rows, "The documents collection (rows) cannot be null");
		return indexService.postDocuments(schemaName, indexName, newListMap(rows));
	}

	/**
	 * Update the DocValues of one document
	 *
	 * @param row a collection of DocValues to update
	 * @return the status of the request
	 */
	public Response updateDocumentValues(T row) {
		checkParameters();
		Objects.requireNonNull(row, "The document (row) cannot be null");
		return indexService.updateDocumentValues(schemaName, indexName, newMap(row));
	}

	/**
	 * Update the DocValues of a collection of document
	 *
	 * @param rows a collection of document with a collection of DocValues to update
	 * @return the status of the request
	 */
	public Response updateDocumentsValues(Collection<T> rows) {
		checkParameters();
		Objects.requireNonNull(rows, "The documents collection (rows) cannot be null");
		return indexService.updateDocumentsValues(schemaName, indexName, newListMap(rows));
	}

	/**
	 * @return the status of the index
	 */
	public IndexStatus getIndexStatus() {
		checkParameters();
		return indexService.getIndex(schemaName, indexName);
	}

	/**
	 * Execute a search query
	 *
	 * @param query the query to execute
	 * @return the results
	 */
	public ResultDefinition searchQuery(QueryDefinition query) {
		checkParameters();
		return indexService.searchQuery(schemaName, indexName, query, false);
	}

	/**
	 * Delete the documents matching the query
	 *
	 * @param query the query to execute
	 * @return the results
	 */
	public ResultDefinition deleteByQuery(QueryDefinition query) {
		checkParameters();
		return indexService.searchQuery(schemaName, indexName, query, true);
	}

	/**
	 * Build a new Map by reading the IndexField annotations
	 *
	 * @param row the record
	 * @return a new Map
	 */
	private Map<String, Object> newMap(final T row) {
		final Map<String, Object> map = new HashMap<>();
		fieldMap.forEach(new BiConsumer<String, Field>() {
			@Override
			public void accept(String name, Field field) {
				try {
					Object value = field.get(row);
					if (value == null)
						return;
					map.put(name, value);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return map.isEmpty() ? null : map;
	}

	/**
	 * Buid a collection of Map by reading the IndexFields of the annotated documents
	 *
	 * @param rows a collection of records
	 * @return a new collection of map
	 */
	private List<Map<String, Object>> newListMap(final Collection<T> rows) {
		if (rows == null)
			return null;
		final List<Map<String, Object>> list = new ArrayList<>();
		rows.forEach(new Consumer<T>() {

			@Override
			public void accept(T row) {
				Map<String, Object> map = newMap(row);
				if (map == null)
					return;
				list.add(map);
			}
		});
		return list.isEmpty() ? null : list;
	}
}
