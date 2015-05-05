/**
 * Copyright 2015 OpenSearchServer Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.qwazr.utils.server.RestApplication;

@Path("/indexes")
public interface IndexServiceInterface {

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<String> getIndexes(@QueryParam("local") Boolean local);

	@GET
	@Path("/version")
	@Produces(MediaType.TEXT_PLAIN)
	public String getVersion();

	@POST
	@Path("/{index_name}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public IndexStatus createIndex(@PathParam("index_name") String index_name,
			@QueryParam("local") Boolean local,
			Map<String, FieldDefinition> fields);

	@GET
	@Path("/{index_name}")
	@Produces(MediaType.APPLICATION_JSON)
	public IndexStatus getIndex(@PathParam("index_name") String index_name);

	@DELETE
	@Path("/{index_name}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteIndex(@PathParam("index_name") String index_name,
			@QueryParam("local") Boolean local);

	@POST
	@Path("/{index_name}/fields")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, FieldDefinition> createFields(
			@PathParam("index_name") String index_name,
			Map<String, FieldDefinition> fields);

	@POST
	@Path("/{index_name}/fields/{field_name}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public FieldDefinition createField(
			@PathParam("index_name") String index_name,
			@PathParam("field_name") String field_name, FieldDefinition field);

	@DELETE
	@Path("/{index_name}/fields/{field_name}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteField(@PathParam("index_name") String index_name,
			@PathParam("field_name") String field_name);

	@POST
	@Path("/{index_name}/documents")
	@Consumes(RestApplication.APPLICATION_JSON_UTF8)
	@Produces(MediaType.TEXT_PLAIN)
	public Response postDocuments(@PathParam("index_name") String index_name,
			List<Map<String, FieldContent>> documents);

	@POST
	@Path("/{index_name}/queries")
	@Consumes(RestApplication.APPLICATION_JSON_UTF8)
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public List<ResultDefinition> findDocuments(
			@PathParam("index_name") String index_name,
			List<QueryDefinition> queries, @QueryParam("delete") Boolean delete);

	@POST
	@Path("/{index_name}/query")
	@Consumes(RestApplication.APPLICATION_JSON_UTF8)
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public ResultDefinition findDocuments(
			@PathParam("index_name") String index_name, QueryDefinition query,
			@QueryParam("delete") Boolean delete);

}
