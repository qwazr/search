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

import com.qwazr.binder.FieldMapWrapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AnnotatedServiceInterface {

    default <T> int postDocument(String indexName, Map<String, Field> fields, T document)
        throws IOException {
        return postDocument(indexName, fields, document, null);
    }

    <T> int postDocument(String indexName,
                         Map<String, Field> fields, T document,
                         Map<String, String> commitUserData) throws IOException;

    default <T> int postDocuments(String indexName, Map<String, Field> fields,
                                  Collection<T> documents) throws IOException {
        return postDocuments(indexName, fields, documents, null);
    }

    <T> int postDocuments(String indexName,
                          Map<String, Field> fields, Collection<T> documents,
                          Map<String, String> commitUserData) throws IOException;

    default <T> int addDocument(String indexName,
                                Map<String, Field> fields, T document)
        throws IOException {
        return addDocument(indexName, fields, document, null);
    }

    <T> int addDocument(String indexName,
                        Map<String, Field> fields, T document,
                        Map<String, String> commitUserData) throws IOException;

    default <T> int addDocuments(String indexName,
                                 Map<String, Field> fields,
                                 Collection<T> documents) throws IOException {
        return addDocuments(indexName, fields, documents, null);
    }

    <T> int addDocuments(String indexName,
                         Map<String, Field> fields, Collection<T> documents,
                         Map<String, String> commitUserData) throws IOException;

    default <T> int updateDocValues(String indexName,
                                    Map<String, Field> fields, T document)
        throws IOException {
        return updateDocValues(indexName, fields, document, null);
    }

    <T> int updateDocValues(String indexName, Map<String, Field> fields,
                            T document,
                            Map<String, String> commitUserData) throws IOException;

    default <T> int updateDocsValues(String indexName,
                                     Map<String, Field> fields,
                                     Collection<T> documents) throws IOException {
        return updateDocsValues(indexName, fields, documents, null);
    }

    <T> int updateDocsValues(String indexName,
                             Map<String, Field> fields,
                             Collection<T> documents,
                             Map<String, String> commitUserData) throws IOException;

    <T> T getDocument(String indexName,
                      Object id,
                      FieldMapWrapper<T> fieldMapWrapper);

    <T> List<T> getDocuments(String indexName,
                             Integer start,
                             Integer rows,
                             FieldMapWrapper<T> fieldMapWrapper);

    <T> ResultDefinition.WithObject<T> searchQuery(String indexName,
                                                   QueryDefinition query,
                                                   FieldMapWrapper<T> fieldMapWrapper);

    ResultDefinition.Empty searchQuery(String indexName,
                                       QueryDefinition query,
                                       ResultDocumentsInterface resultDocuments);
}
