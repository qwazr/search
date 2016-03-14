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
import com.qwazr.utils.AnnotationsUtils;
import com.qwazr.utils.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Consumer;

public class AnnotatedIndexService {

	private final IndexServiceInterface indexService;

	private final String schemaName;

	private final String indexName;

	private final String similarityClass;

	/**
	 * Create a new index service. The Index annotations of the indexDefinition object is read.
	 *
	 * @param indexService    the IndexServiceInterface to use
	 * @param indexDefinition an annotated object
	 */
	public AnnotatedIndexService(IndexServiceInterface indexService, Object indexDefinition) {
		Objects.requireNonNull(indexService, "The indexService parameter is null");
		Objects.requireNonNull(indexService, "The indexDefinition parameter is null");
		this.indexService = indexService;
		Index index = indexDefinition.getClass().getAnnotation(Index.class);
		Objects.requireNonNull(index, "This object does not declare any Index annotation: " + indexDefinition);
		schemaName = index.schema();
		indexName = index.name();
		similarityClass = index.similarityClass();
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
		return null;
	}

}
