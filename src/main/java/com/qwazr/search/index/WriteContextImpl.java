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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qwazr.search.analysis.UpdatableAnalyzers;
import com.qwazr.server.ServerException;
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.StringUtils;
import java.util.function.Supplier;
import javax.ws.rs.NotAcceptableException;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

final class WriteContextImpl extends IndexContextImpl implements WriteContext {

    final IndexWriter indexWriter;
    final TaxonomyWriter taxonomyWriter;
    private final String primaryKey;
    private final Supplier<String> autoIdProvider;

    WriteContextImpl(final IndexInstance.Provider indexProvider,
                     final ResourceLoader resourceLoader,
                     final ExecutorService executorService,
                     final UpdatableAnalyzers indexAnalyzers,
                     final UpdatableAnalyzers queryAnalyzers,
                     final FieldMap fieldMap,
                     final IndexWriter indexWriter,
                     final TaxonomyWriter taxonomyWriter) {
        super(indexProvider, resourceLoader, executorService, indexAnalyzers, queryAnalyzers, fieldMap);
        this.indexWriter = indexWriter;
        this.taxonomyWriter = taxonomyWriter;
        this.primaryKey = fieldMap.getPrimaryKey();
        if (StringUtils.isBlank(primaryKey))
            autoIdProvider = null;
        else
            autoIdProvider = getAutoIdProvider();
    }

    @Override
    public IndexWriter getIndexWriter() {
        return indexWriter;
    }

    @Override
    public TaxonomyWriter getTaxonomyWriter() {
        return taxonomyWriter;
    }

    private <T> int postObjectDoc(final RecordsPoster.ObjectDocument poster,
                                  final T document,
                                  final Map<String, String> commitUserData) throws IOException {
        poster.accept(document);
        if (commitUserData != null)
            setLiveCommitData(commitUserData, true);
        return poster.getCount();
    }

    private <T> int postObjectDocs(final RecordsPoster.ObjectDocument poster,
                                   final Collection<T> documents,
                                   final Map<String, String> commitUserData) throws IOException {
        for (final Object document : documents)
            poster.accept(document);
        if (commitUserData != null)
            setLiveCommitData(commitUserData, true);
        return poster.getCount();
    }

    public final void setLiveCommitData(Map<String, String> commitUserData, boolean doIncrementVersion) {
        indexWriter.setLiveCommitData(Objects.requireNonNull(commitUserData, "commitUserData is null").entrySet(),
            doIncrementVersion);
    }

    @Override
    public final <T> int postDocument(final Map<String, Field> fields,
                                      final T document,
                                      final Map<String, String> commitUserData) throws IOException {
        if (document == null)
            return 0;
        final RecordsPoster.ObjectDocument poster = RecordsPoster.ObjectDocument.of(
            fields, fieldMap, indexWriter, taxonomyWriter);
        return postObjectDoc(poster, document, commitUserData);
    }

    @Override
    public final <T> int postDocuments(final Map<String, Field> fields,
                                       final Collection<T> documents,
                                       final Map<String, String> commitUserData) throws IOException {
        if (documents == null || documents.isEmpty())
            return 0;
        final RecordsPoster.ObjectDocument poster =
            RecordsPoster.ObjectDocument.of(fields, fieldMap, indexWriter, taxonomyWriter);
        return postObjectDocs(poster, documents, commitUserData);
    }

    @Override
    public int postMappedDoc(final RecordsPoster.MapDocument poster, final PostDefinition.Document post)
        throws IOException {
        poster.accept(post.document);
        if (post.commitUserData != null)
            setLiveCommitData(post.commitUserData, true);
        return poster.getCount();
    }

    @Override
    public int postMappedDocs(final RecordsPoster.MapDocument poster, final PostDefinition.Documents post)
        throws IOException {
        for (final Map<String, ?> doc : post.documents)
            poster.accept(doc);
        if (post.commitUserData != null)
            setLiveCommitData(post.commitUserData, true);
        return poster.getCount();
    }

    @Override
    public final int postMappedDocument(final PostDefinition.Document post) throws IOException {
        if (post == null || post.document == null || post.document.isEmpty())
            return 0;
        final RecordsPoster.MapDocument poster =
            RecordsPoster.MapDocument.of(fieldMap, indexWriter, taxonomyWriter);
        return postMappedDoc(poster, post);
    }

    @Override
    public final int postMappedDocuments(final PostDefinition.Documents post) throws IOException {
        if (post == null || post.documents == null || post.documents.isEmpty())
            return 0;
        final RecordsPoster.MapDocument poster =
            RecordsPoster.MapDocument.of(fieldMap, indexWriter, taxonomyWriter);
        return postMappedDocs(poster, post);
    }

    private void postJsonNode(final RecordsPoster.JsonNodeDocument poster,
                              final JsonNode jsonNode) throws IOException {
        if (!jsonNode.isObject())
            throw new NotAcceptableException("This json type can't be indexed as a document: " + jsonNode.getNodeType());
        final ObjectNode objectNode = (ObjectNode) jsonNode;
        if (autoIdProvider != null) {
            if (!objectNode.has(primaryKey))
                objectNode.put(primaryKey, autoIdProvider.get());
        }
        poster.accept((ObjectNode) jsonNode);
    }

    @Override
    public int postJsonNode(final JsonNode jsonNode) throws IOException {
        if (jsonNode == null)
            return 0;
        final RecordsPoster.JsonNodeDocument poster =
            RecordsPoster.JsonNodeDocument.of(fieldMap, indexWriter, taxonomyWriter);
        if (jsonNode.isArray()) {
            for (final JsonNode element : jsonNode) {
                postJsonNode(poster, element);
            }
        } else if (jsonNode.isObject())
            poster.accept((ObjectNode) jsonNode);
        else
            throw new ServerException("The json should be either an array or an object: " + jsonNode.getNodeType());
        return poster.getCount();
    }

    @Override
    public int postJsonNodes(final Collection<JsonNode> jsonNodes) throws IOException {
        if (jsonNodes == null)
            return 0;
        final RecordsPoster.JsonNodeDocument poster =
            RecordsPoster.JsonNodeDocument.of(fieldMap, indexWriter, taxonomyWriter);
        for (final JsonNode jsonNode : jsonNodes)
            postJsonNode(poster, jsonNode);
        return poster.getCount();
    }

    @Override
    public final <T> int updateDocValues(final Map<String, Field> fields,
                                         final T document,
                                         final Map<String, String> commitUserData) throws IOException {
        if (document == null)
            return 0;
        final RecordsPoster.ObjectDocument poster =
            RecordsPoster.ObjectDocument.forDocValueUpdate(fields, fieldMap, indexWriter, taxonomyWriter);
        return postObjectDoc(poster, document, commitUserData);
    }

    @Override
    public final <T> int updateDocsValues(final Map<String, Field> fields,
                                          final Collection<T> documents,
                                          final Map<String, String> commitUserData) throws IOException {
        if (documents == null || documents.isEmpty())
            return 0;
        final RecordsPoster.ObjectDocument poster =
            RecordsPoster.ObjectDocument.forDocValueUpdate(fields, fieldMap, indexWriter, taxonomyWriter);
        return postObjectDocs(poster, documents, commitUserData);
    }

    @Override
    public final int updateMappedDocValues(final PostDefinition.Document post) throws IOException {
        if (post == null || post.document == null || post.document.isEmpty())
            return 0;
        final RecordsPoster.MapDocument poster =
            RecordsPoster.MapDocument.forDocValueUpdate(fieldMap, indexWriter, taxonomyWriter);
        return postMappedDoc(poster, post);
    }

    @Override
    public final int updateMappedDocsValues(final PostDefinition.Documents post) throws IOException, ServerException {
        if (post == null || post.documents == null || post.documents.isEmpty())
            return 0;
        final RecordsPoster.MapDocument poster =
            RecordsPoster.MapDocument.forDocValueUpdate(fieldMap, indexWriter, taxonomyWriter);
        return postMappedDocs(poster, post);
    }

    static Supplier<String> getAutoIdProvider() {
        final byte[] buffer = HashUtils.getBase58buffer(1);
        return () -> HashUtils.base58encode(HashUtils.newTimeBasedUUID(), buffer);
    }
}
