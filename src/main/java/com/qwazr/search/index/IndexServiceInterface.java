/**
 * Copyright 2015 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.server.RestApplication;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/indexes")
public interface IndexServiceInterface {

	@GET
	@Path("/")
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public Set<String> getIndexes(@QueryParam("local") Boolean local);

	@POST
	@Path("/{index_name}")
	@Consumes(RestApplication.APPLICATION_JSON_UTF8)
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public IndexStatus createUpdateIndex(@PathParam("index_name") String index_name,
										 @QueryParam("local") Boolean local,
										 Map<String, FieldDefinition> fields);

	@GET
	@Path("/{index_name}")
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public IndexStatus getIndex(@PathParam("index_name") String index_name);

	@DELETE
	@Path("/{index_name}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteIndex(@PathParam("index_name") String index_name,
								@QueryParam("local") Boolean local);

	@POST
	@Path("/{index_name}/docs")
	@Consumes(RestApplication.APPLICATION_JSON_UTF8)
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public Response postDocuments(@PathParam("index_name") String index_name,
								  List<Map<String, Object>> documents);

	@POST
	@Path("/{index_name}/doc")
	@Consumes(RestApplication.APPLICATION_JSON_UTF8)
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public Response postDocument(@PathParam("index_name") String index_name,
								 Map<String, Object> document);

	@POST
	@Path("/{index_name}/search")
	@Consumes(RestApplication.APPLICATION_JSON_UTF8)
	@Produces(RestApplication.APPLICATION_JSON_UTF8)
	public ResultDefinition searchQuery(@PathParam("index_name") String index_name,
										QueryDefinition query);

}
