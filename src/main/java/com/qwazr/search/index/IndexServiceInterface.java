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

import com.qwazr.cluster.manager.ClusterManager;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.utils.server.ServiceInterface;
import com.qwazr.utils.server.ServiceName;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RolesAllowed(IndexManager.SERVICE_NAME_SEARCH)
@Path("/indexes")
@ServiceName(IndexManager.SERVICE_NAME_SEARCH)
public interface IndexServiceInterface extends ServiceInterface {

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
	@Path("/{schema_name}/{index_name}/doc/{id}")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Map<String, Object> getDocument(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, @PathParam("id") String doc_id);

	@POST
	@Path("/{schema_name}/{index_name}/doc")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Response postDocument(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			Map<String, Object> document);

	@POST
	@Path("/{schema_name}/{index_name}/docs")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Response postDocuments(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			List<Map<String, Object>> documents);

	@POST
	@Path("/{schema_name}/{index_name}/doc/values")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Response updateDocumentValues(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, Map<String, Object> document);

	@POST
	@Path("/{schema_name}/{index_name}/docs/values")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	Response updateDocumentsValues(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, List<Map<String, Object>> documents);

	@POST
	@Path("/{schema_name}/{index_name}/backup")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	BackupStatus doBackup(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
			@QueryParam("keep_last") Integer keep_last_count);

	@GET
	@Path("/{schema_name}/{index_name}/backup")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	List<BackupStatus> getBackups(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name);

	@POST
	@Path("/{schema_name}/{index_name}/search")
	@Consumes(ServiceInterface.APPLICATION_JSON_UTF8)
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	ResultDefinition searchQuery(@PathParam("schema_name") String schema_name,
			@PathParam("index_name") String index_name, QueryDefinition query, @QueryParam("delete") Boolean delete);

	public static IndexServiceInterface getClient(Boolean local, String group, Integer msTimeout)
			throws URISyntaxException {
		if (local != null && local)
			return new IndexServiceImpl();
		if (!ClusterManager.INSTANCE.isCluster())
			return new IndexServiceImpl();
		String[] nodes = ClusterManager.INSTANCE.getClusterClient()
				.getActiveNodesByService(IndexManager.SERVICE_NAME_SEARCH, group);
		if (nodes == null)
			throw new RuntimeException("Index service not available");
		if (nodes.length == 1)
			return new IndexSingleClient(nodes[0], msTimeout);
		return new IndexMultiClient(ClusterManager.INSTANCE.executor, nodes, msTimeout);
	}
}
