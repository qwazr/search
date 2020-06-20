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
import com.fasterxml.jackson.jaxrs.smile.SmileMediaTypes;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.search.replication.ReplicationSession;
import com.qwazr.server.RemoteService;
import com.qwazr.server.ServerException;
import com.qwazr.server.client.JsonClient;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class IndexSingleClient extends JsonClient implements IndexServiceInterface {

    private final String preferedSerializedMediaType;
    private final WebTarget indexTarget;

    public IndexSingleClient(final RemoteService remote, final String preferedSerializedMediaType) {
        super(remote);
        indexTarget = client.target(remote.serviceAddress).path(IndexServiceInterface.PATH);
        this.preferedSerializedMediaType = preferedSerializedMediaType;
    }

    public IndexSingleClient(final RemoteService remote) {
        this(remote, SmileMediaTypes.APPLICATION_JACKSON_SMILE);
    }

    @Override
    public Map<String, UUID> getIndexes() {
        try {
            return indexTarget.request(preferedSerializedMediaType).get(mapStringUuidType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public IndexStatus createUpdateIndex(final String indexName) {
        return createUpdateIndex(indexName, null);
    }

    @Override
    public IndexStatus createUpdateIndex(final String indexName,
                                         final IndexSettingsDefinition settings) {
        try {
            return indexTarget
                .path(indexName)
                .request(preferedSerializedMediaType)
                .post(Entity.entity(settings, preferedSerializedMediaType), IndexStatus.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public Map<String, FieldDefinition> getFields(final String indexName) {
        try {
            return indexTarget
                .path(indexName)
                .path("fields")
                .request(preferedSerializedMediaType)
                .get(mapStringFieldType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public Map<String, FieldDefinition> setFields(final String indexName,
                                                  final Map<String, FieldDefinition> fields) {
        try {
            return indexTarget
                .path(indexName)
                .path("fields")
                .request(preferedSerializedMediaType)
                .post(Entity.entity(fields, preferedSerializedMediaType), mapStringFieldType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public List<TermDefinition> doAnalyzeQuery(final String indexName,
                                               final String fieldName,
                                               final String text) {
        try {
            return indexTarget
                .path(indexName)
                .path("fields")
                .path(fieldName)
                .path("analyzer/query")
                .queryParam("text", text == null ? StringUtils.EMPTY : text)
                .request(preferedSerializedMediaType)
                .get(listTermDefinitionType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public List<TermDefinition> doAnalyzeIndex(final String indexName,
                                               final String fieldName,
                                               final String text) {
        try {
            return indexTarget
                .path(indexName)
                .path("fields")
                .path(fieldName)
                .path("analyzer/index")
                .queryParam("text", text == null ? StringUtils.EMPTY : text)
                .request(preferedSerializedMediaType)
                .get(listTermDefinitionType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public FieldStats getFieldStats(final String indexName, final String fieldName) {
        try {
            return indexTarget
                .path(indexName)
                .path("fields")
                .path(fieldName)
                .path("stats")
                .request(preferedSerializedMediaType)
                .get(FieldStats.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public List<TermEnumDefinition> doExtractTerms(final String indexName,
                                                   final String fieldName,
                                                   final Integer start,
                                                   final Integer rows) {
        return doExtractTerms(indexName, fieldName, null, start, rows);
    }

    @Override
    public List<TermEnumDefinition> doExtractTerms(final String indexName,
                                                   final String fieldName,
                                                   final String prefix,
                                                   final Integer start,
                                                   final Integer rows) {
        try {
            WebTarget target = indexTarget.path(indexName).path("fields").path(fieldName).path("terms");
            if (prefix != null)
                target = target.path(prefix);
            if (start != null)
                target = target.queryParam("start", start);
            if (rows != null)
                target = target.queryParam("rows", rows);
            return target.request(preferedSerializedMediaType).get(listTermEnumDefinitionType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public FieldDefinition getField(final String indexName,
                                    final String fieldName) {
        return indexTarget
            .path(indexName)
            .path("fields")
            .path(fieldName == null ? StringUtils.EMPTY : fieldName)
            .request(preferedSerializedMediaType)
            .get(FieldDefinition.class);
    }

    @Override
    public FieldDefinition setField(final String indexName,
                                    final String fieldName,
                                    final FieldDefinition field) {
        try {
            return indexTarget
                .path(indexName)
                .path("fields")
                .path(fieldName == null ? StringUtils.EMPTY : fieldName)
                .request(preferedSerializedMediaType)
                .post(Entity.entity(field, preferedSerializedMediaType), FieldDefinition.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public boolean deleteField(final String indexName,
                               final String fieldName) {
        try {
            return indexTarget
                .path(indexName)
                .path("fields")
                .path(fieldName == null ? StringUtils.EMPTY : fieldName)
                .request()
                .delete(Boolean.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public Map<String, AnalyzerDefinition> getAnalyzers(final String indexName) {
        try {
            return indexTarget
                .path(indexName)
                .path("analyzers")
                .request(preferedSerializedMediaType)
                .get(mapStringAnalyzerType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public AnalyzerDefinition getAnalyzer(final String indexName,
                                          final String analyzerName) {
        try {
            return indexTarget
                .path(indexName)
                .path("analyzers")
                .path(analyzerName)
                .request(preferedSerializedMediaType)
                .get(AnalyzerDefinition.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public void refreshAnalyzers(final String indexName) {
        try {
            final Response.StatusType statusType = indexTarget
                .path(indexName)
                .path("analyzers")
                .request()
                .method("PATCH")
                .getStatusInfo();
            if (statusType.getFamily() != Response.Status.Family.SUCCESSFUL)
                throw new ServerException("Analyzer refresh failed: " + statusType.getReasonPhrase());
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public AnalyzerDefinition setAnalyzer(final String indexName,
                                          final String analyzerName,
                                          final AnalyzerDefinition analyzer) {
        try {
            return indexTarget
                .path(indexName)
                .path("analyzers")
                .path(analyzerName)
                .request(preferedSerializedMediaType)
                .post(Entity.entity(analyzer, preferedSerializedMediaType), AnalyzerDefinition.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public Map<String, AnalyzerDefinition> setAnalyzers(final String indexName,
                                                        final Map<String, AnalyzerDefinition> analyzers) {
        try {
            return indexTarget
                .path(indexName)
                .path("analyzers")
                .request(preferedSerializedMediaType)
                .post(Entity.entity(analyzers, preferedSerializedMediaType), mapStringAnalyzerType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public boolean deleteAnalyzer(final String indexName,
                                  final String analyzerName) {
        try {
            return indexTarget
                .path(indexName)
                .path("analyzers")
                .path(analyzerName)
                .request()
                .delete(Boolean.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public List<TermDefinition> testAnalyzer(final String indexName,
                                             final String analyzerName,
                                             final String text) {
        try {
            return indexTarget
                .path(indexName)
                .path("analyzers")
                .path(analyzerName)
                .request(preferedSerializedMediaType)
                .post(Entity.text(text == null ? StringUtils.EMPTY : text), listTermDefinitionType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public String testAnalyzerDot(final String indexName,
                                  final String analyzerName,
                                  final String text) {
        try {
            return indexTarget
                .path(indexName)
                .path("analyzers")
                .path(analyzerName)
                .path("dot")
                .queryParam("text", text == null ? StringUtils.EMPTY : text)
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public IndexStatus getIndex(final String indexName) {
        try {
            return indexTarget
                .path(indexName)
                .request(preferedSerializedMediaType)
                .get(IndexStatus.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public IndexSettingsDefinition getIndexSettings(final String indexName) {
        try {
            return indexTarget
                .path(indexName)
                .path("settings")
                .request(preferedSerializedMediaType)
                .get(IndexSettingsDefinition.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public IndexStatus mergeIndex(final String indexName,
                                  final String mergedIndex,
                                  final Map<String, String> commitUserData) {
        try {
            return indexTarget
                .path(indexName)
                .path("merge")
                .path(mergedIndex)
                .request(preferedSerializedMediaType)
                .post(Entity.entity(commitUserData, preferedSerializedMediaType), IndexStatus.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public IndexCheckStatus checkIndex(final String indexName) {
        try {
            return indexTarget
                .path(indexName)
                .path("check")
                .request(preferedSerializedMediaType)
                .post(Entity.entity(null, preferedSerializedMediaType), IndexCheckStatus.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public boolean deleteIndex(final String indexName) {
        try {
            return indexTarget
                .path(indexName)
                .request(MediaType.TEXT_PLAIN)
                .delete(boolean.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public SortedMap<String, BackupStatus> doBackup(final String indexName,
                                                    final String backupName) {
        try {
            return indexTarget
                .path(indexName)
                .path("backup")
                .path(backupName)
                .request(preferedSerializedMediaType)
                .async()
                .post(Entity.entity(null, MediaType.TEXT_PLAIN_TYPE), mapStringBackupStatusType)
                .get();
        } catch (InterruptedException | ExecutionException e) {
            throw ServerException.of(e);
        }
    }

    @Override
    public ReindexDefinition getReindexStatus(final String indexName) {
        try {
            return indexTarget
                .path(indexName)
                .path("reindex")
                .request(preferedSerializedMediaType)
                .get(ReindexDefinition.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public ReindexDefinition startReindex(final String indexName,
                                          final Integer bufferSize) {
        try {
            return indexTarget
                .path(indexName)
                .path("reindex")
                .queryParam("buffer_size", bufferSize)
                .request(preferedSerializedMediaType)
                .post(null, ReindexDefinition.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public ReindexDefinition stopReindex(final String indexName) {
        try {
            return indexTarget
                .path(indexName)
                .path("reindex")
                .request(preferedSerializedMediaType)
                .delete(ReindexDefinition.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public SortedMap<String, SortedMap<String, BackupStatus>> getBackups(final String indexName,
                                                                         final String backupName,
                                                                         final Boolean extractVersion) {
        return indexTarget
            .path(indexName)
            .path("backup")
            .path(backupName)
            .queryParam("extractVersion", extractVersion == null ? Boolean.FALSE : extractVersion)
            .request(preferedSerializedMediaType)
            .get(mapStringMapStringBackupStatusType);
    }

    @Override
    public Integer deleteBackups(final String indexName,
                                 final String backupName) {
        try {
            return indexTarget
                .path(indexName)
                .path("backup")
                .path(backupName)
                .request(preferedSerializedMediaType)
                .delete(Integer.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public InputStream replicationObtain(final String indexName,
                                         final String sessionID,
                                         final String source,
                                         final String fileName) {
        try {
            return new AutoCloseInputStream(indexTarget
                .path(indexName)
                .path("replication")
                .path(sessionID)
                .path(source)
                .path(fileName)
                .request(MediaType.APPLICATION_OCTET_STREAM)
                .get(InputStream.class));
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public boolean replicationRelease(final String indexName,
                                      final String sessionID) {
        try {
            return indexTarget
                .path(indexName)
                .path("replication")
                .path(sessionID)
                .request(MediaType.TEXT_PLAIN)
                .delete(boolean.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public ReplicationSession replicationUpdate(final String indexName,
                                                final String currentVersion) {
        try {
            return indexTarget
                .path(indexName)
                .path("replication")
                .queryParam("current_version", currentVersion)
                .request(preferedSerializedMediaType)
                .post(Entity.entity(currentVersion, preferedSerializedMediaType), ReplicationSession.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public ReplicationStatus replicationCheck(final String indexName) {
        try {
            return indexTarget
                .path(indexName)
                .path("replication")
                .request(preferedSerializedMediaType)
                .async()
                .get(ReplicationStatus.class).get();
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        } catch (InterruptedException | ExecutionException e) {
            throw ServerException.of(e);
        }
    }

    @Override
    public Map<String, IndexInstance.ResourceInfo> getResources(final String indexName) {
        try {
            return indexTarget
                .path(indexName)
                .path("resources")
                .request(preferedSerializedMediaType)
                .get(mapStringResourceInfoType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public InputStream getResource(final String indexName,
                                   final String resourceName) {
        try {
            return new AutoCloseInputStream(
                indexTarget
                    .path(indexName)
                    .path("resources")
                    .path(resourceName)
                    .request(MediaType.APPLICATION_OCTET_STREAM)
                    .get(InputStream.class));
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public boolean postResource(final String indexName,
                                final String resourceName,
                                final Long lastModified,
                                final InputStream inputStream) {
        try {
            WebTarget target = indexTarget.path(indexName).path("resources").path(resourceName);
            if (lastModified != null)
                target = target.queryParam("lastModified", lastModified);
            return target.request(MediaType.TEXT_PLAIN).post(Entity.text(inputStream), boolean.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public boolean deleteResource(final String indexName,
                                  final String resourceName) {
        try {
            return indexTarget
                .path(indexName)
                .path("resources")
                .path(resourceName)
                .request(MediaType.TEXT_PLAIN)
                .delete(boolean.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public Integer postMappedDocument(final String indexName,
                                      final PostDefinition.Document post) {
        try {
            return indexTarget
                .path(indexName)
                .path("doc")
                .request(preferedSerializedMediaType)
                .post(Entity.entity(post, preferedSerializedMediaType), Integer.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public Integer postJson(final String indexName,
                            final JsonNode jsonNode) {
        try {
            return indexTarget
                .path(indexName)
                .path("json")
                .request()
                .post(Entity.entity(jsonNode, preferedSerializedMediaType), Integer.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public List<Map<String, Object>> getJsonSamples(final String indexName,
                                                    final Integer count) {
        try {
            WebTarget target = indexTarget
                .path(indexName)
                .path("json")
                .path("samples")
                .queryParam("count", count);
            return target.request(preferedSerializedMediaType).get(listMapStringObjectType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public Map<String, Object> getJsonSample(final String indexName) {
        try {
            final WebTarget target = indexTarget
                .path(indexName)
                .path("json")
                .path("sample");
            return target.request(preferedSerializedMediaType).get(mapStringObjectType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public Integer postMappedDocuments(final String indexName,
                                       final PostDefinition.Documents post) {
        try {
            return indexTarget
                .path(indexName)
                .path("docs")
                .request(preferedSerializedMediaType)
                .post(Entity.entity(post, preferedSerializedMediaType), Integer.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public Integer updateMappedDocValues(final String indexName,
                                         final PostDefinition.Document post) {
        try {
            return indexTarget
                .path(indexName)
                .path("doc")
                .path("values")
                .request(preferedSerializedMediaType)
                .post(Entity.entity(post, preferedSerializedMediaType), Integer.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public Integer updateMappedDocsValues(final String indexName,
                                          final PostDefinition.Documents post) {
        try {
            return indexTarget
                .path(indexName)
                .path("docs")
                .path("values")
                .request(preferedSerializedMediaType)
                .post(Entity.entity(post, preferedSerializedMediaType), Integer.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public boolean deleteAll(final String indexName) {
        try {
            return indexTarget
                .path(indexName)
                .path("docs")
                .request(MediaType.TEXT_PLAIN)
                .delete(boolean.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public Map<String, Object> getDocument(final String indexName,
                                           final String docId) {
        try {
            return indexTarget
                .path(indexName)
                .path("doc")
                .path(docId)
                .request(preferedSerializedMediaType)
                .get(mapStringObjectType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public List<Map<String, Object>> getDocuments(final String indexName,
                                                  final Integer start,
                                                  final Integer rows) {
        try {
            WebTarget target = indexTarget.path(indexName).path("doc");
            if (start != null)
                target = target.queryParam("start", start);
            if (rows != null)
                target = target.queryParam("rows", rows);
            return target.request(preferedSerializedMediaType).get(listMapStringObjectType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public ResultDefinition.WithMap searchQuery(final String indexName,
                                                final QueryDefinition query,
                                                final Boolean delete) {
        try {
            WebTarget target = indexTarget.path(indexName).path("search");
            if (delete != null)
                target = target.queryParam("delete", delete);
            return target.request(preferedSerializedMediaType)
                .post(Entity.entity(query, preferedSerializedMediaType), ResultDefinition.WithMap.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public ExplainDefinition explainQuery(final String indexName,
                                          final QueryDefinition query,
                                          int docId) {
        try {
            return indexTarget
                .path(indexName)
                .path("search")
                .path("explain")
                .path(Integer.toString(docId))
                .request(preferedSerializedMediaType)
                .post(Entity.entity(query, preferedSerializedMediaType), ExplainDefinition.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public String explainQueryText(final String indexName,
                                   final QueryDefinition query,
                                   final int docId) {
        try {
            return indexTarget
                .path(indexName)
                .path("search")
                .path("explain")
                .path(Integer.toString(docId))
                .request(MediaType.TEXT_PLAIN)
                .post(Entity.entity(query, preferedSerializedMediaType), String.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public String explainQueryDot(final String indexName,
                                  final QueryDefinition query,
                                  final int docId,
                                  final Integer descriptionWrapSize) {
        try {
            WebTarget target = indexTarget
                .path(indexName)
                .path("search")
                .path("explain")
                .path(Integer.toString(docId));
            if (descriptionWrapSize != null)
                target = target.queryParam("wrap", descriptionWrapSize);
            return target.request(MEDIATYPE_TEXT_GRAPHVIZ)
                .post(Entity.entity(query, preferedSerializedMediaType), String.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public Set<String> getQueryTypes(final String indexName) {
        try {
            final WebTarget target = indexTarget
                .path(indexName)
                .path("queries")
                .path("types");
            return target.request(preferedSerializedMediaType).get(setStringType);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

    @Override
    public AbstractQuery<?> getQuerySample(final String indexName,
                                           final String queryType) {
        try {
            final WebTarget target = indexTarget
                .path(indexName)
                .path("queries")
                .path("types")
                .path(queryType);
            return target.request(preferedSerializedMediaType).get(AbstractQuery.class);
        } catch (WebApplicationException e) {
            throw ServerException.from(e);
        }
    }

}
