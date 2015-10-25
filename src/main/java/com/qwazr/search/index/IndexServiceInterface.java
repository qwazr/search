/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import com.qwazr.search.SearchServer;
import com.qwazr.utils.server.RestApplication;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RolesAllowed(SearchServer.SERVICE_NAME_SEARCH)
@Path("/indexes")
public interface IndexServiceInterface {

	@POST
	@Path("/{schema_name}")
	@Consumes(RestApplication.APPLICATION_JSON_UTF8)
	public Response createUpdateSchema(@PathParam("schema_name") String schema_name,
					@QueryParam("local") Boolean local);

	@GET
	@Path("/")
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public Set<String> getSchemas(@QueryParam("local") Boolean local);

	@DELETE
	@Path("/{schema_name}")
	public Response deleteSchema(@PathParam("schema_name") String schema_name, @QueryParam("local") Boolean local);

	@OPTIONS
	@Path("/{schema_name}")
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public SettingsDefinition getSettings(@PathParam("schema_name") String schema_name);

	@OPTIONS
	@Path("/{schema_name}")
	@Consumes(RestApplication.APPLICATION_JSON_UTF8)
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public SettingsDefinition setSettings(@PathParam("schema_name") String schema_name, SettingsDefinition settings);

	@GET
	@Path("/{schema_name}")
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public Set<String> getIndexes(@PathParam("schema_name") String schema_name, @QueryParam("local") Boolean local);

	@POST
	@Path("/{schema_name}/{index_name}")
	@Consumes(RestApplication.APPLICATION_JSON_UTF8)
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public IndexStatus createUpdateIndex(@PathParam("schema_name") String schema_name,
					@PathParam("index_name") String index_name, @QueryParam("local") Boolean local,
					Map<String, FieldDefinition> fields);

	@GET
	@Path("/{schema_name}/{index_name}")
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public IndexStatus getIndex(@PathParam("schema_name") String schema_name,
					@PathParam("index_name") String index_name);

	@DELETE
	@Path("/{schema_name}/{index_name}")
	public Response deleteIndex(@PathParam("schema_name") String schema_name,
					@PathParam("index_name") String index_name, @QueryParam("local") Boolean local);

	@POST
	@Path("/{schema_name}/{index_name}/docs")
	@Consumes(RestApplication.APPLICATION_JSON_UTF8)
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public Response postDocuments(@PathParam("schema_name") String schema_name,
					@PathParam("index_name") String index_name, List<Map<String, Object>> documents);

	@DELETE
	@Path("/{schema_name}/{index_name}/docs")
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public Response deleteAll(@PathParam("schema_name") String schema_name, @PathParam("index_name") String index_name,
					@QueryParam("local") Boolean local);

	@POST
	@Path("/{schema_name}/{index_name}/doc")
	@Consumes(RestApplication.APPLICATION_JSON_UTF8)
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public Response postDocument(@PathParam("schema_name") String schema_name,
					@PathParam("index_name") String index_name, Map<String, Object> document);

	@POST
	@Path("/{schema_name}/{index_name}/backup")
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public BackupStatus doBackup(@PathParam("schema_name") String schema_name,
					@PathParam("index_name") String index_name, @QueryParam("keep_last") Integer keep_last_count);

	@GET
	@Path("/{schema_name}/{index_name}/backup")
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public List<BackupStatus> getBackups(@PathParam("schema_name") String schema_name,
					@PathParam("index_name") String index_name);

	@POST
	@Path("/{schema_name}/{index_name}/search")
	@Consumes(RestApplication.APPLICATION_JSON_UTF8)
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public ResultDefinition searchQuery(@PathParam("schema_name") String schema_name,
					@PathParam("index_name") String index_name, QueryDefinition query,
					@QueryParam("delete") Boolean delete);

	@POST
	@Path("/{schema_name}/{index_name}/mlt")
	@Consumes(RestApplication.APPLICATION_JSON_UTF8)
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public ResultDefinition mltQuery(@PathParam("schema_name") String schema_name,
					@PathParam("index_name") String index_name, MltQueryDefinition query);

}
