/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.jaxrs.smile.SmileMediaTypes;
import com.qwazr.binder.FieldMapWrapper;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.replication.ReplicationSession;
import com.qwazr.server.PATCH;
import com.qwazr.server.ServiceInterface;
import com.qwazr.utils.concurrent.FunctionEx;
import org.apache.commons.lang3.NotImplementedException;
import org.glassfish.jersey.server.ManagedAsync;

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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;

@RolesAllowed(IndexServiceInterface.SERVICE_NAME)
@Path("/" + IndexServiceInterface.PATH)
public interface IndexServiceInterface extends ServiceInterface {

    String SERVICE_NAME = "search";
    String PATH = "indexes";

    String MEDIATYPE_TEXT_GRAPHVIZ = "text/vnd.graphviz";

    @POST
    @Path("/{schema_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    SchemaSettingsDefinition createUpdateSchema(@PathParam("schema_name") String schema_name);

    @POST
    @Path("/{schema_name}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    SchemaSettingsDefinition createUpdateSchema(@PathParam("schema_name") String schema_name,
                                                SchemaSettingsDefinition settings);

    @GET
    @Path("/")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Set<String> getSchemas();

    @DELETE
    @Path("/{schema_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean deleteSchema(@PathParam("schema_name") String schema_name);

    @GET
    @Path("/{schema_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Map<String, UUID> getIndexes(@PathParam("schema_name") String schema_name);

    @POST
    @Path("/{schema_name}/{index_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    IndexStatus createUpdateIndex(@PathParam("schema_name") String schema_name,
                                  @PathParam("index_name") String index_name);

    @POST
    @Path("/{schema_name}/{index_name}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    IndexStatus createUpdateIndex(@PathParam("schema_name") String schema_name,
                                  @PathParam("index_name") String index_name, IndexSettingsDefinition settings);

    @GET
    @Path("/{schema_name}/{index_name}/fields")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    LinkedHashMap<String, FieldDefinition> getFields(@PathParam("schema_name") String schema_name,
                                                     @PathParam("index_name") String index_name);

    @POST
    @Path("/{schema_name}/{index_name}/fields")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    LinkedHashMap<String, FieldDefinition> setFields(@PathParam("schema_name") String schema_name,
                                                     @PathParam("index_name") String index_name, LinkedHashMap<String, FieldDefinition> fields);

    @GET
    @Path("/{schema_name}/{index_name}/fields/{field_name}/analyzer/query")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    List<TermDefinition> doAnalyzeQuery(@PathParam("schema_name") String schema_name,
                                        @PathParam("index_name") String index_name, @PathParam("field_name") String field_name,
                                        @QueryParam("text") String text);

    @GET
    @Path("/{schema_name}/{index_name}/fields/{field_name}/analyzer/index")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    List<TermDefinition> doAnalyzeIndex(@PathParam("schema_name") String schema_name,
                                        @PathParam("index_name") String index_name, @PathParam("field_name") String field_name,
                                        @QueryParam("text") String text);

    @GET
    @Path("/{schema_name}/{index_name}/fields/{field_name}/stats")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    FieldStats getFieldStats(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                             @PathParam("field_name") String field_name);

    @GET
    @Path("/{schema_name}/{index_name}/fields/{field_name}/terms")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    List<TermEnumDefinition> doExtractTerms(@PathParam("schema_name") String schema_name,
                                            @PathParam("index_name") String index_name, @PathParam("field_name") String field_name,
                                            @QueryParam("start") Integer start, @QueryParam("rows") Integer rows);

    @GET
    @Path("/{schema_name}/{index_name}/fields/{field_name}/terms/{prefix}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    List<TermEnumDefinition> doExtractTerms(@PathParam("schema_name") String schema_name,
                                            @PathParam("index_name") String index_name, @PathParam("field_name") String field_name,
                                            @PathParam("prefix") String prefix, @QueryParam("start") Integer start, @QueryParam("rows") Integer rows);

    @GET
    @Path("/{schema_name}/{index_name}/fields/{field_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    FieldDefinition getField(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                             @PathParam("field_name") String field_name);

    @POST
    @Path("/{schema_name}/{index_name}/fields/{field_name}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    FieldDefinition setField(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                             @PathParam("field_name") String field_name, FieldDefinition fields);

    @DELETE
    @Path("/{schema_name}/{index_name}/fields/{field_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean deleteField(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                        @PathParam("field_name") String field_name);

    @GET
    @Path("/{schema_name}/{index_name}/analyzers")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    LinkedHashMap<String, AnalyzerDefinition> getAnalyzers(@PathParam("schema_name") String schema_name,
                                                           @PathParam("index_name") String index_name);

    @GET
    @Path("/{schema_name}/{index_name}/analyzers/{analyzer_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    AnalyzerDefinition getAnalyzer(@PathParam("schema_name") String schema_name,
                                   @PathParam("index_name") String index_name, @PathParam("analyzer_name") String analyzer_name);

    @POST
    @Path("/{schema_name}/{index_name}/analyzers/{analyzer_name}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    AnalyzerDefinition setAnalyzer(@PathParam("schema_name") String schema_name,
                                   @PathParam("index_name") String index_name, @PathParam("analyzer_name") String analyzer_name,
                                   AnalyzerDefinition analyzer);

    @POST
    @Path("/{schema_name}/{index_name}/analyzers")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    LinkedHashMap<String, AnalyzerDefinition> setAnalyzers(@PathParam("schema_name") String schema_name,
                                                           @PathParam("index_name") String index_name, LinkedHashMap<String, AnalyzerDefinition> analyzers);

    @DELETE
    @Path("/{schema_name}/{index_name}/analyzers/{analyzer_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean deleteAnalyzer(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                           @PathParam("analyzer_name") String analyzer_name);

    @PATCH
    @Path("/{schema_name}/{index_name}/analyzers")
    void refreshAnalyzers(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name);

    @POST
    @Path("/{schema_name}/{index_name}/analyzers/{analyzer_name}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    List<TermDefinition> testAnalyzer(@PathParam("schema_name") String schema_name,
                                      @PathParam("index_name") String index_name, @PathParam("analyzer_name") String analyzer_name, String text);

    @GET
    @Path("/{schema_name}/{index_name}/analyzers/{analyzer_name}/dot")
    @Produces(MediaType.TEXT_PLAIN)
    String testAnalyzerDot(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                           @PathParam("analyzer_name") String analyzer_name, @QueryParam("text") String text);

    @GET
    @Path("/{schema_name}/{index_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    IndexStatus getIndex(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name);

    @POST
    @Path("/{schema_name}/{index_name}/merge/{merged_index}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    IndexStatus mergeIndex(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                           @PathParam("merged_index") String merged_index, final Map<String, String> commitUserData);

    @POST
    @Path("/{schema_name}/{index_name}/check")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    IndexCheckStatus checkIndex(@PathParam("schema_name") String schema_name,
                                @PathParam("index_name") String index_name);

    @DELETE
    @Path("/{schema_name}/{index_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean deleteIndex(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name);

    @DELETE
    @Path("/{schema_name}/{index_name}/docs")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean deleteAll(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name);

    @GET
    @Path("/{schema_name}/{index_name}/doc")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    List<Map<String, Object>> getDocuments(@PathParam("schema_name") String schema_name,
                                           @PathParam("index_name") String index_name, @QueryParam("start") Integer start,
                                           @QueryParam("rows") Integer rows);

    @GET
    @Path("/{schema_name}/{index_name}/doc/{id}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Map<String, Object> getDocument(@PathParam("schema_name") String schema_name,
                                    @PathParam("index_name") String index_name, @PathParam("id") String doc_id);

    @POST
    @Path("/{schema_name}/{index_name}/doc")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Integer postMappedDocument(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                               PostDefinition.Document document);

    @POST
    @Path("/{schema_name}/{index_name}/docs")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Integer postMappedDocuments(@PathParam("schema_name") String schema_name,
                                @PathParam("index_name") String index_name, PostDefinition.Documents documents);

    @POST
    @Path("/{schema_name}/{index_name}/doc/values")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Integer updateMappedDocValues(@PathParam("schema_name") String schema_name,
                                  @PathParam("index_name") String index_name, PostDefinition.Document document);

    @POST
    @Path("/{schema_name}/{index_name}/docs/values")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Integer updateMappedDocsValues(@PathParam("schema_name") String schema_name,
                                   @PathParam("index_name") String index_name, PostDefinition.Documents documents);

    @POST
    @ManagedAsync
    @Path("/{schema_name}/{index_name}/backup/{backup_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    SortedMap<String, SortedMap<String, BackupStatus>> doBackup(
            @PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
            @PathParam("backup_name") String backup_name);

    @GET
    @Path("/{schema_name}/{index_name}/backup/{backup_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>> getBackups(
            @PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
            @PathParam("backup_name") String backup_name, @QueryParam("extractVersion") Boolean extractVersion);

    @DELETE
    @Path("/{schema_name}/{index_name}/backup/{backup_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Integer deleteBackups(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                          @PathParam("backup_name") String backup_name);

    @GET
    @Path("/{schema_name}/{index_name}/replication/{session_id}/{source}/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    InputStream replicationObtain(@PathParam("schema_name") String schema_name,
                                  @PathParam("index_name") String index_name, @PathParam("session_id") String sessionID,
                                  @PathParam("source") String source, @PathParam("filename") String fileName);

    @DELETE
    @Path("/{schema_name}/{index_name}/replication/{session_id}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean replicationRelease(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                               @PathParam("session_id") String sessionID);

    @POST
    @Path("/{schema_name}/{index_name}/replication")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    ReplicationSession replicationUpdate(@PathParam("schema_name") String schema_name,
                                         @PathParam("index_name") String index_name, String current_version);

    @GET
    @ManagedAsync
    @Path("/{schema_name}/{index_name}/replication")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    ReplicationStatus replicationCheck(@PathParam("schema_name") String schema_name,
                                       @PathParam("index_name") String index_name);

    @GET
    @Path("/{schema_name}/{index_name}/resources")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    Map<String, IndexInstance.ResourceInfo> getResources(@PathParam("schema_name") String schema_name,
                                                         @PathParam("index_name") String index_name);

    @GET
    @Path("/{schema_name}/{index_name}/resources/{resource_name}")
    InputStream getResource(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                            @PathParam("resource_name") String resourceName);

    @POST
    @Path("/{schema_name}/{index_name}/resources/{resource_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean postResource(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                         @PathParam("resource_name") String resourceName, @QueryParam("lastModified") Long lastModified,
                         InputStream inputStream);

    @DELETE
    @Path("/{schema_name}/{index_name}/resources/{resource_name}")
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN})
    boolean deleteResource(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                           @PathParam("resource_name") String resourceName);

    @POST
    @Path("/{schema_name}/{index_name}/search")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    ResultDefinition.WithMap searchQuery(@PathParam("schema_name") String schema_name,
                                         @PathParam("index_name") String index_name, QueryDefinition query, @QueryParam("delete") Boolean delete);

    @POST
    @Path("/{schema_name}/{index_name}/search/explain/{doc}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    ExplainDefinition explainQuery(@PathParam("schema_name") String schema_name,
                                   @PathParam("index_name") String index_name, QueryDefinition query, @PathParam("doc") int docId);

    @POST
    @Path("/{schema_name}/{index_name}/search/explain/{doc}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces(MediaType.TEXT_PLAIN)
    String explainQueryText(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                            QueryDefinition query, @PathParam("doc") int docId);

    @POST
    @Path("/{schema_name}/{index_name}/search/explain/{doc}")
    @Consumes({ServiceInterface.APPLICATION_JSON_UTF8, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    @Produces(MEDIATYPE_TEXT_GRAPHVIZ)
    String explainQueryDot(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
                           QueryDefinition query, @PathParam("doc") int docId, @QueryParam("wrap") final Integer descriptionWrapSize);

    GenericType<Set<String>> setStringType = new GenericType<Set<String>>() {
    };

    GenericType<Map<String, UUID>> mapStringUuidType = new GenericType<Map<String, UUID>>() {
    };

    GenericType<SortedMap<String, SortedMap<String, BackupStatus>>> mapStringMapStringBackupStatusType =
            new GenericType<SortedMap<String, SortedMap<String, BackupStatus>>>() {
            };

    GenericType<SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>>>
            mapStringMapStringMapStringBackupStatusType =
            new GenericType<SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>>>() {
            };

    GenericType<LinkedHashMap<String, IndexInstance.ResourceInfo>> mapStringResourceInfoType =
            new GenericType<LinkedHashMap<String, IndexInstance.ResourceInfo>>() {
            };

    GenericType<ArrayList<Map<String, Object>>> listMapStringObjectType =
            new GenericType<ArrayList<Map<String, Object>>>() {
            };

    GenericType<LinkedHashMap<String, Object>> mapStringObjectType = new GenericType<LinkedHashMap<String, Object>>() {
    };

    GenericType<LinkedHashMap<String, FieldDefinition>> mapStringFieldType =
            new GenericType<LinkedHashMap<String, FieldDefinition>>() {
            };

    GenericType<List<TermDefinition>> listTermDefinitionType = new GenericType<List<TermDefinition>>() {
    };

    GenericType<List<TermEnumDefinition>> listTermEnumDefinitionType = new GenericType<List<TermEnumDefinition>>() {
    };

    GenericType<LinkedHashMap<String, AnalyzerDefinition>> mapStringAnalyzerType =
            new GenericType<LinkedHashMap<String, AnalyzerDefinition>>() {
            };

    @FunctionalInterface
    interface QueryActions<T> extends FunctionEx<QueryContext, T, IOException> {
    }

    default <T> T query(final String schemaName, final String indexName, final FieldMapWrapper.Cache fieldMapWrappers,
                        final QueryActions<T> actions) throws IOException {
        throw new NotImplementedException("Method not available");
    }

    @FunctionalInterface
    interface WriteActions<T> extends FunctionEx<WriteContext, T, IOException> {
    }

    default <T> T write(final String schemaName, final String indexName, final WriteActions<T> actions)
            throws IOException {
        throw new NotImplementedException("Method not available");
    }

}
