/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.server.AbstractStreamingOutput;
import com.qwazr.server.ServiceInterface;
import com.qwazr.server.ServiceName;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

@RolesAllowed(IndexServiceInterface.SERVICE_NAME)
@Path("/" + IndexServiceInterface.PATH)
@ServiceName(IndexServiceInterface.SERVICE_NAME)
public interface IndexServiceInterface extends ServiceInterface {

	String SERVICE_NAME = "search";
	String PATH = "indexes";

	String MEDIATYPE_TEXT_GRAPHVIZ = "text/vnd.graphviz";

	@POST
	@Path("/{schema_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	SchemaSettingsDefinition createUpdateSchema(@PathParam("schema_name") String schema_name);

	@POST
	@Path("/{schema_name}")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	SchemaSettingsDefinition createUpdateSchema(@PathParam("schema_name") String schema_name,
			SchemaSettingsDefinition settings);

	@GET
	@Path("/")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Set<String> getSchemas();

	@DELETE
	@Path("/{schema_name}")
	Response deleteSchema(@PathParam("schema_name") String schema_name);

	@GET
	@Path("/{schema_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Set<String> getIndexes(@PathParam("schema_name") String schema_name);

	@POST
	@Path("/{schema_name}/{index_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	IndexStatus createUpdateIndex(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name);

	@POST
	@Path("/{schema_name}/{index_name}")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	IndexStatus createUpdateIndex(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, IndexSettingsDefinition settings);

	@GET
	@Path("/{schema_name}/{index_name}/fields")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	LinkedHashMap<String, FieldDefinition> getFields(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name);

	@POST
	@Path("/{schema_name}/{index_name}/fields")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	LinkedHashMap<String, FieldDefinition> setFields(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, LinkedHashMap<String, FieldDefinition> fields);

	@GET
	@Path("/{schema_name}/{index_name}/fields/{field_name}/analyzer/query")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	List<TermDefinition> doAnalyzeQuery(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @PathParam("field_name") String field_name,
			@QueryParam("text") String text);

	@GET
	@Path("/{schema_name}/{index_name}/fields/{field_name}/analyzer/index")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	List<TermDefinition> doAnalyzeIndex(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @PathParam("field_name") String field_name,
			@QueryParam("text") String text);

	@GET
	@Path("/{schema_name}/{index_name}/fields/{field_name}/stats")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	FieldStats getFieldStats(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			@PathParam("field_name") String field_name);

	@GET
	@Path("/{schema_name}/{index_name}/fields/{field_name}/terms")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	List<TermEnumDefinition> doExtractTerms(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @PathParam("field_name") String field_name,
			@QueryParam("start") Integer start, @QueryParam("rows") Integer rows);

	@GET
	@Path("/{schema_name}/{index_name}/fields/{field_name}/terms/{prefix}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	List<TermEnumDefinition> doExtractTerms(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @PathParam("field_name") String field_name,
			@PathParam("prefix") String prefix, @QueryParam("start") Integer start, @QueryParam("rows") Integer rows);

	@GET
	@Path("/{schema_name}/{index_name}/fields/{field_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	FieldDefinition getField(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			@PathParam("field_name") String field_name);

	@POST
	@Path("/{schema_name}/{index_name}/fields/{field_name}")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	FieldDefinition setField(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			@PathParam("field_name") String field_name, FieldDefinition fields);

	@DELETE
	@Path("/{schema_name}/{index_name}/fields/{field_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Response deleteField(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			@PathParam("field_name") String field_name);

	@GET
	@Path("/{schema_name}/{index_name}/analyzers")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	LinkedHashMap<String, AnalyzerDefinition> getAnalyzers(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name);

	@GET
	@Path("/{schema_name}/{index_name}/analyzers/{analyzer_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	AnalyzerDefinition getAnalyzer(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @PathParam("analyzer_name") String analyzer_name);

	@POST
	@Path("/{schema_name}/{index_name}/analyzers/{analyzer_name}")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	AnalyzerDefinition setAnalyzer(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @PathParam("analyzer_name") String analyzer_name,
			AnalyzerDefinition analyzer);

	@POST
	@Path("/{schema_name}/{index_name}/analyzers")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	LinkedHashMap<String, AnalyzerDefinition> setAnalyzers(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, LinkedHashMap<String, AnalyzerDefinition> analyzers);

	@DELETE
	@Path("/{schema_name}/{index_name}/analyzers/{analyzer_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Response deleteAnalyzer(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			@PathParam("analyzer_name") String analyzer_name);

	@POST
	@Path("/{schema_name}/{index_name}/analyzers/{analyzer_name}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	List<TermDefinition> testAnalyzer(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @PathParam("analyzer_name") String analyzer_name, String text);

	@GET
	@Path("/{schema_name}/{index_name}/analyzers/{analyzer_name}/dot")
	@Produces(MediaType.TEXT_PLAIN)
	String testAnalyzerDot(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			@PathParam("analyzer_name") String analyzer_name, @QueryParam("text") String text);

	@GET
	@Path("/{schema_name}/{index_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	IndexStatus getIndex(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name);

	@DELETE
	@Path("/{schema_name}/{index_name}")
	Response deleteIndex(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name);

	@DELETE
	@Path("/{schema_name}/{index_name}/docs")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Response deleteAll(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name);

	@GET
	@Path("/{schema_name}/{index_name}/doc")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	List<Map<String, Object>> getDocuments(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @QueryParam("start") Integer start,
			@QueryParam("rows") Integer rows);

	@GET
	@Path("/{schema_name}/{index_name}/doc/{id}")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Map<String, Object> getDocument(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @PathParam("id") String doc_id);

	@POST
	@Path("/{schema_name}/{index_name}/doc")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Integer postMappedDocument(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			Map<String, Object> document);

	@POST
	@Path("/{schema_name}/{index_name}/docs")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Integer postMappedDocuments(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, Collection<Map<String, Object>> documents);

	@POST
	@Path("/{schema_name}/{index_name}/doc/values")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Integer updateMappedDocValues(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, Map<String, Object> document);

	@POST
	@Path("/{schema_name}/{index_name}/docs/values")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Integer updateMappedDocsValues(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, Collection<Map<String, Object>> documents);

	@POST
	@Path("/{schema_name}/{index_name}/backup/{backup_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	SortedMap<String, SortedMap<String, BackupStatus>> doBackup(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @PathParam("backup_name") String backup_name);

	@GET
	@Path("/{schema_name}/{index_name}/backup/{backup_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>> getBackups(
			@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			@PathParam("backup_name") String backup_name);

	@DELETE
	@Path("/{schema_name}/{index_name}/backup/{backup_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Integer deleteBackups(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			@PathParam("backup_name") String backup_name);

	@GET
	@Path("/{schema_name}/{index_name}/replication/{master_uuid}/{session_id}/{source}/{filename}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	AbstractStreamingOutput replicationObtain(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @PathParam("master_uuid") String masterUuid,
			@PathParam("session_id") String sessionID, @PathParam("source") String source,
			@PathParam("filename") String fileName);

	@DELETE
	@Path("/{schema_name}/{index_name}/replication/{master_uuid}/{session_id}")
	Response replicationRelease(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @PathParam("master_uuid") String masterUuid,
			@PathParam("session_id") String sessionID);

	@GET
	@Path("/{schema_name}/{index_name}/replication/{master_uuid}/{current_version}")
	AbstractStreamingOutput replicationUpdate(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @PathParam("master_uuid") String masterUuid,
			@PathParam("current_version") String current_version);

	@GET
	@Path("/{schema_name}/{index_name}/replication")
	Response replicationCheck(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name);

	@GET
	@Path("/{schema_name}/{index_name}/resources")
	LinkedHashMap<String, IndexInstance.ResourceInfo> getResources(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name);

	@GET
	@Path("/{schema_name}/{index_name}/resources/{resource_name}")
	AbstractStreamingOutput getResource(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @PathParam("resource_name") String resourceName);

	@POST
	@Path("/{schema_name}/{index_name}/resources/{resource_name}")
	Response postResource(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			@PathParam("resource_name") String resourceName, @QueryParam("lastModified") long lastModified,
			InputStream inputStream);

	@DELETE
	@Path("/{schema_name}/{index_name}/resources/{resource_name}")
	Response deleteResource(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			@PathParam("resource_name") String resourceName);

	@POST
	@Path("/{schema_name}/{index_name}/search")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	ResultDefinition.WithMap searchQuery(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, QueryDefinition query, @QueryParam("delete") Boolean delete);

	@POST
	@Path("/{schema_name}/{index_name}/search/{doc}")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	ExplainDefinition explainQuery(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, QueryDefinition query, @PathParam("doc") int docId);

	@POST
	@Path("/{schema_name}/{index_name}/search/{doc}")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(MediaType.TEXT_PLAIN)
	String explainQueryText(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			QueryDefinition query, @PathParam("doc") int docId);

	@POST
	@Path("/{schema_name}/{index_name}/search/{doc}")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(MEDIATYPE_TEXT_GRAPHVIZ)
	String explainQueryDot(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			QueryDefinition query, @PathParam("doc") int docId, @QueryParam("wrap") final Integer descriptionWrapSize);

	TypeReference<Set<String>> SetStringTypeRef = new TypeReference<Set<String>>() {
	};

	TypeReference<SortedMap<String, SortedMap<String, BackupStatus>>> MapStringMapStringBackupStatusTypeRef =
			new TypeReference<SortedMap<String, SortedMap<String, BackupStatus>>>() {
			};

	TypeReference<SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>>>
			MapStringMapStringMapStringBackupStatusTypeRef =
			new TypeReference<SortedMap<String, SortedMap<String, SortedMap<String, BackupStatus>>>>() {
			};

	TypeReference<LinkedHashMap<String, IndexInstance.ResourceInfo>> MapStringResourceInfoTypeRef =
			new TypeReference<LinkedHashMap<String, IndexInstance.ResourceInfo>>() {
			};

	TypeReference<ArrayList<Map<String, Object>>> ListMapStringObjectTypeRef =
			new TypeReference<ArrayList<Map<String, Object>>>() {
			};

	TypeReference<LinkedHashMap<String, Object>> MapStringObjectTypeRef =
			new TypeReference<LinkedHashMap<String, Object>>() {
			};

	TypeReference<Collection<Map<String, Object>>> CollectionMapStringObjectTypeRef =
			new TypeReference<Collection<Map<String, Object>>>() {
			};

}
