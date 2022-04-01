/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.cluster;

import com.qwazr.server.ServiceInterface;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.SortedMap;
import java.util.SortedSet;

@RolesAllowed(ClusterServiceInterface.SERVICE_NAME)
@Path("/" + ClusterServiceInterface.SERVICE_NAME)
public interface ClusterServiceInterface extends ServiceInterface {

	String SERVICE_NAME = "cluster";

	@GET
	@Path("/")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	ClusterStatusJson getStatus();

	@GET
	@Path("/nodes")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	SortedSet<String> getNodes();

	@GET
	@Path("/services")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	SortedMap<String, ClusterServiceStatusJson.StatusEnum> getServiceMap(@QueryParam("group") String group);

	@GET
	@Path("/services/{service_name}")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	ClusterServiceStatusJson getServiceStatus(@PathParam("service_name") String service_name,
			@QueryParam("group") String group);

	@GET
	@Path("/services/{service_name}/active")
	@Produces(ServiceInterface.APPLICATION_JSON_UTF8)
	SortedSet<String> getActiveNodesByService(@PathParam("service_name") String service_name,
			@QueryParam("group") String group);

	@GET
	@Path("/services/{service_name}/active/random")
	@Produces(MediaType.TEXT_PLAIN)
	String getActiveNodeRandomByService(@PathParam("service_name") String service_name,
			@QueryParam("group") String group);

	@GET
	@Path("/services/{service_name}/active/leader")
	@Produces(MediaType.TEXT_PLAIN)
	String getActiveNodeLeaderByService(@PathParam("service_name") String service_name,
			@QueryParam("group") String group);
}