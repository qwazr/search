/**
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

import com.qwazr.utils.FieldMapWrapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AnnotatedServiceInterface {

	default <T> int postDocument(String schemaName, String indexName, Map<String, Field> fields, T document)
			throws IOException, InterruptedException {
		return postDocument(schemaName, indexName, fields, document, null);
	}

	<T> int postDocument(String schemaName, String indexName, Map<String, Field> fields, T document,
			Map<String, String> commitUserData) throws IOException, InterruptedException;

	default <T> int postDocuments(String schemaName, String indexName, Map<String, Field> fields,
			Collection<T> documents) throws IOException, InterruptedException {
		return postDocuments(schemaName, indexName, fields, documents, null);
	}

	<T> int postDocuments(String schemaName, String indexName, Map<String, Field> fields, Collection<T> documents,
			Map<String, String> commitUserData) throws IOException, InterruptedException;

	default <T> int addDocument(String schemaName, String indexName, Map<String, Field> fields, T document)
			throws IOException, InterruptedException {
		return addDocument(schemaName, indexName, fields, document, null);
	}

	<T> int addDocument(String schemaName, String indexName, Map<String, Field> fields, T document,
			Map<String, String> commitUserData) throws IOException, InterruptedException;

	default <T> int addDocuments(String schemaName, String indexName, Map<String, Field> fields,
			Collection<T> documents) throws IOException, InterruptedException {
		return addDocuments(schemaName, indexName, fields, documents, null);
	}

	<T> int addDocuments(String schemaName, String indexName, Map<String, Field> fields, Collection<T> documents,
			Map<String, String> commitUserData) throws IOException, InterruptedException;

	default <T> int updateDocValues(String schemaName, String indexName, Map<String, Field> fields, T document)
			throws IOException, InterruptedException {
		return updateDocValues(schemaName, indexName, fields, document, null);
	}

	<T> int updateDocValues(String schemaName, String indexName, Map<String, Field> fields, T document,
			Map<String, String> commitUserData) throws IOException, InterruptedException;

	default <T> int updateDocsValues(String schemaName, String indexName, Map<String, Field> fields,
			Collection<T> documents) throws IOException, InterruptedException {
		return updateDocsValues(schemaName, indexName, fields, documents, null);
	}

	<T> int updateDocsValues(String schemaName, String indexName, Map<String, Field> fields, Collection<T> documents,
			Map<String, String> commitUserData) throws IOException, InterruptedException;

	<T> T getDocument(String schemaName, String indexName, Object id, FieldMapWrapper<T> fieldMapWrapper);

	<T> List<T> getDocuments(String schemaName, String indexName, Integer start, Integer rows,
			FieldMapWrapper<T> fieldMapWrapper);

	<T> ResultDefinition.WithObject<T> searchQuery(String schemaName, String indexName, QueryDefinition query,
			FieldMapWrapper<T> fieldMapWrapper);

	ResultDefinition.Empty searchQuery(String schemaName, String indexName, QueryDefinition query,
			ResultDocumentsInterface resultDocuments);
}
