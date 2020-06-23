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
import com.qwazr.search.query.QueryInterface;
import com.qwazr.search.replication.ReplicationSession;
import com.qwazr.server.PATCH;
import com.qwazr.server.ServiceInterface;
import com.qwazr.utils.concurrent.FunctionEx;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.NotImplementedException;
import org.glassfish.jersey.server.ManagedAsync;

@RolesAllowed(IndexServiceInterface.SERVICE_NAME)
@Path("/" + IndexServiceInterface.PATH)
public interface IndexServiceInterface extends ServiceInterface {

    String SERVICE_NAME = "search";
    String PATH = "indexes";

    String MEDIATYPE_TEXT_GRAPHVIZ = "text/vnd.graphviz";

    @GET
    @Path("/")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Map<String, UUID> getIndexes();

    @POST
    @Path("/{index_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    IndexStatus createUpdateIndex(@PathParam("index_name") String indexName);

    @POST
    @Path("/{index_name}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    IndexStatus createUpdateIndex(@PathParam("index_name") String indexName, IndexSettingsDefinition settings);

    @GET
    @Path("/{index_name}/fields")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Map<String, FieldDefinition> getFields(@PathParam("index_name") String indexName);

    @POST
    @Path("/{index_name}/fields")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Map<String, FieldDefinition> setFields(@PathParam("index_name") String indexName,
                                           Map<String, FieldDefinition> fields);

    @GET
    @Path("/{index_name}/fields/{field_name}/analyzer/query")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    List<TermDefinition> doAnalyzeQuery(@PathParam("index_name") String indexName,
                                        @PathParam("field_name") String fieldName,
                                        @QueryParam("text") String text);

    @GET
    @Path("/{index_name}/fields/{field_name}/analyzer/index")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    List<TermDefinition> doAnalyzeIndex(@PathParam("index_name") String indexName,
                                        @PathParam("field_name") String fieldName,
                                        @QueryParam("text") String text);

    @GET
    @Path("/{index_name}/fields/{field_name}/stats")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    FieldStats getFieldStats(@PathParam("index_name") String indexName,
                             @PathParam("field_name") String fieldName);

    @GET
    @Path("/{index_name}/fields/{field_name}/terms")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    List<TermEnumDefinition> doExtractTerms(@PathParam("index_name") String indexName,
                                            @PathParam("field_name") String fieldName,
                                            @QueryParam("start") Integer start,
                                            @QueryParam("rows") Integer rows);

    @GET
    @Path("/{index_name}/fields/{field_name}/terms/{prefix}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    List<TermEnumDefinition> doExtractTerms(@PathParam("index_name") String indexName,
                                            @PathParam("field_name") String fieldName,
                                            @PathParam("prefix") String prefix,
                                            @QueryParam("start") Integer start,
                                            @QueryParam("rows") Integer rows);

    @GET
    @Path("/{index_name}/fields/{field_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    FieldDefinition getField(@PathParam("index_name") String indexName,
                             @PathParam("field_name") String fieldName);

    @POST
    @Path("/{index_name}/fields/{field_name}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    FieldDefinition setField(@PathParam("index_name") String indexName,
                             @PathParam("field_name") String fieldName,
                             FieldDefinition field);

    @DELETE
    @Path("/{index_name}/fields/{field_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean deleteField(@PathParam("index_name") String indexName,
                        @PathParam("field_name") String fieldName);

    @GET
    @Path("/{index_name}/analyzers")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Map<String, AnalyzerDefinition> getAnalyzers(@PathParam("index_name") String indexName);

    @GET
    @Path("/{index_name}/analyzers/{analyzer_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    AnalyzerDefinition getAnalyzer(@PathParam("index_name") String indexName,
                                   @PathParam("analyzer_name") String analyzerName);

    @POST
    @Path("/{index_name}/analyzers/{analyzer_name}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    AnalyzerDefinition setAnalyzer(@PathParam("index_name") String indexName,
                                   @PathParam("analyzer_name") String analyzerName,
                                   AnalyzerDefinition analyzer);

    @POST
    @Path("/{index_name}/analyzers")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Map<String, AnalyzerDefinition> setAnalyzers(@PathParam("index_name") String indexName,
                                                 Map<String, AnalyzerDefinition> analyzers);

    @DELETE
    @Path("/{index_name}/analyzers/{analyzer_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean deleteAnalyzer(@PathParam("index_name") String indexName,
                           @PathParam("analyzer_name") String analyzerName);

    @PATCH
    @Path("/{index_name}/analyzers")
    void refreshAnalyzers(@PathParam("index_name") String indexName);

    @POST
    @Path("/{index_name}/analyzers/{analyzer_name}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    List<TermDefinition> testAnalyzer(@PathParam("index_name") String indexName,
                                      @PathParam("analyzer_name") String analyzerName,
                                      String text);

    @GET
    @Path("/{index_name}/analyzers/{analyzer_name}/dot")
    @Produces(MediaType.TEXT_PLAIN)
    String testAnalyzerDot(@PathParam("index_name") String indexName,
                           @PathParam("analyzer_name") String analyzerName,
                           @QueryParam("text") String text);

    @GET
    @Path("/{index_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    IndexStatus getIndex(@PathParam("index_name") String indexName);

    @GET
    @Path("/{index_name}/settings")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    IndexSettingsDefinition getIndexSettings(@PathParam("index_name") String indexName);


    @POST
    @Path("/{index_name}/merge/{merged_index}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    IndexStatus mergeIndex(@PathParam("index_name") String indexName,
                           @PathParam("merged_index") String mergedIndex,
                           final Map<String, String> commitUserData);

    @POST
    @Path("/{index_name}/check")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    IndexCheckStatus checkIndex(@PathParam("index_name") String indexName);

    @DELETE
    @Path("/{index_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean deleteIndex(@PathParam("index_name") String indexName);

    @DELETE
    @Path("/{index_name}/docs")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean deleteAll(@PathParam("index_name") String indexName);

    @GET
    @Path("/{index_name}/doc")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    List<Map<String, Object>> getDocuments(@PathParam("index_name") String indexName,
                                           @QueryParam("start") Integer start,
                                           @QueryParam("rows") Integer rows);

    @GET
    @Path("/{index_name}/doc/{id}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Map<String, Object> getDocument(@PathParam("index_name") String indexName,
                                    @PathParam("id") String docId);

    @POST
    @Path("/{index_name}/doc")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Integer postMappedDocument(@PathParam("index_name") String indexName,
                               PostDefinition.Document document);

    @POST
    @Path("/{index_name}/json")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Integer postJson(@PathParam("index_name") String indexName,
                     JsonNode jsonNode);

    @GET
    @Path("/{index_name}/json/samples")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    List<Map<String, Object>> getJsonSamples(@PathParam("index_name") String indexName,
                                             @QueryParam("count") Integer count);

    @GET
    @Path("/{index_name}/json/sample")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Map<String, Object> getJsonSample(@PathParam("index_name") String indexName);

    @POST
    @Path("/{index_name}/docs")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Integer postMappedDocuments(@PathParam("index_name") String indexName,
                                PostDefinition.Documents documents);

    @POST
    @Path("/{index_name}/doc/values")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Integer updateMappedDocValues(@PathParam("index_name") String indexName,
                                  PostDefinition.Document document);

    @POST
    @Path("/{index_name}/docs/values")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Integer updateMappedDocsValues(@PathParam("index_name") String indexName,
                                   PostDefinition.Documents documents);

    @POST
    @ManagedAsync
    @Path("/{index_name}/backup/{backup_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    SortedMap<String, BackupStatus> doBackup(@PathParam("index_name") String indexName,
                                             @PathParam("backup_name") String backup_name);

    @GET
    @Path("/{index_name}/reindex")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    ReindexDefinition getReindexStatus(@PathParam("index_name") String indexName);

    @POST
    @Path("/{index_name}/reindex")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    ReindexDefinition startReindex(@PathParam("index_name") String indexName,
                                   @QueryParam("buffer_size") Integer bufferSize);

    @DELETE
    @Path("/{index_name}/reindex")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    ReindexDefinition stopReindex(@PathParam("index_name") String indexName);


    @GET
    @Path("/{index_name}/backup/{backup_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    SortedMap<String, SortedMap<String, BackupStatus>> getBackups(@PathParam("index_name") String indexName,
                                                                  @PathParam("backup_name") String backupName,
                                                                  @QueryParam("extractVersion") Boolean extractVersion);

    @DELETE
    @Path("/{index_name}/backup/{backup_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Integer deleteBackups(@PathParam("index_name") String indexName,
                          @PathParam("backup_name") String backupName);

    @GET
    @Path("/{index_name}/replication/{session_id}/{source}/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    InputStream replicationObtain(@PathParam("index_name") String indexName,
                                  @PathParam("session_id") String sessionID,
                                  @PathParam("source") String source,
                                  @PathParam("filename") String fileName);

    @DELETE
    @Path("/{index_name}/replication/{session_id}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean replicationRelease(@PathParam("index_name") String indexName,
                               @PathParam("session_id") String sessionID);

    @POST
    @Path("/{index_name}/replication")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    ReplicationSession replicationUpdate(@PathParam("index_name") String indexName,
                                         String current_version);

    @GET
    @ManagedAsync
    @Path("/{index_name}/replication")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    ReplicationStatus replicationCheck(@PathParam("index_name") String indexName);

    @GET
    @Path("/{index_name}/resources")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Map<String, IndexInstance.ResourceInfo> getResources(@PathParam("index_name") String indexName);

    @GET
    @Path("/{index_name}/resources/{resource_name}")
    InputStream getResource(@PathParam("index_name") String indexName,
                            @PathParam("resource_name") String resourceName);

    @POST
    @Path("/{index_name}/resources/{resource_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean postResource(@PathParam("index_name") String indexName,
                         @PathParam("resource_name") String resourceName,
                         @QueryParam("lastModified") Long lastModified,
                         InputStream inputStream);

    @DELETE
    @Path("/{index_name}/resources/{resource_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean deleteResource(@PathParam("index_name") String indexName,
                           @PathParam("resource_name") String resourceName);

    @POST
    @Path("/{index_name}/search")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    ResultDefinition.WithMap searchQuery(@PathParam("index_name") String indexName,
                                         QueryDefinition query,
                                         @QueryParam("delete") Boolean delete);

    @POST
    @Path("/{index_name}/search/explain/{doc}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    ExplainDefinition explainQuery(@PathParam("index_name") String indexName,
                                   QueryDefinition query,
                                   @PathParam("doc") String docId);

    @POST
    @Path("/{index_name}/search/explain/{doc}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces(MediaType.TEXT_PLAIN)
    String explainQueryText(@PathParam("index_name") String indexName,
                            QueryDefinition query,
                            @PathParam("doc") String docId);

    @POST
    @Path("/{index_name}/search/explain/{doc}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces(MEDIATYPE_TEXT_GRAPHVIZ)
    String explainQueryDot(@PathParam("index_name") String indexName,
                           QueryDefinition query,
                           @PathParam("doc") String docId,
                           @QueryParam("wrap") final Integer descriptionWrapSize);

    GenericType<Set<String>> setStringType = new GenericType<>() {
    };

    GenericType<Map<String, UUID>> mapStringUuidType = new GenericType<>() {
    };

    GenericType<SortedMap<String, BackupStatus>> mapStringBackupStatusType =
        new GenericType<>() {
        };

    GenericType<SortedMap<String, SortedMap<String, BackupStatus>>> mapStringMapStringBackupStatusType =
        new GenericType<>() {
        };

    GenericType<Map<String, IndexInstance.ResourceInfo>> mapStringResourceInfoType =
        new GenericType<>() {
        };

    GenericType<ArrayList<Map<String, Object>>> listMapStringObjectType =
        new GenericType<>() {
        };

    GenericType<Map<String, Object>> mapStringObjectType = new GenericType<>() {
    };

    GenericType<Map<String, FieldDefinition>> mapStringFieldType =
        new GenericType<>() {
        };

    GenericType<List<TermDefinition>> listTermDefinitionType = new GenericType<>() {
    };

    GenericType<List<TermEnumDefinition>> listTermEnumDefinitionType = new GenericType<>() {
    };

    GenericType<Map<String, AnalyzerDefinition>> mapStringAnalyzerType =
        new GenericType<>() {
        };

    @FunctionalInterface
    interface QueryActions<T> extends FunctionEx<QueryContext, T, IOException> {
    }

    default <T> T query(final String indexName,
                        final QueryActions<T> actions) throws IOException {
        throw new NotImplementedException("Method not available");
    }

    @FunctionalInterface
    interface WriteActions<T> extends FunctionEx<WriteContext, T, IOException> {
    }

    default <T> T write(final String indexName,
                        final WriteActions<T> actions)
        throws IOException {
        throw new NotImplementedException("Method not available");
    }

    @GET
    @Path("/{index_name}/search/queries/types")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Set<String> getQueryTypes(@PathParam("index_name") String indexName);

    @GET
    @Path("/{index_name}/search/queries/types/{query_type}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    QueryInterface getQuerySample(@PathParam("index_name") String indexName,
                                  @PathParam("query_type") String queryType);
}
