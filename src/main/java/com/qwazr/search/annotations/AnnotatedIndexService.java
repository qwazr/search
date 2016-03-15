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
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.search.index.SchemaSettingsDefinition;
import com.qwazr.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class AnnotatedIndexService<T> {

	private final IndexServiceInterface indexService;

	private final String schemaName;

	private final String indexName;

	private final String similarityClass;

	private final Map<String, IndexField> indexFieldMap;

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
			indexFieldMap = new HashMap<>();
			for (Field field : fields) {
				if (!field.isAnnotationPresent(IndexField.class))
					continue;
				IndexField indexField = field.getDeclaredAnnotation(IndexField.class);
				String indexName = StringUtils.isEmpty(indexField.name()) ? field.getName() : indexField.name();
				indexFieldMap.put(indexName, indexField);
			}
		} else
			indexFieldMap = null;
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

}
