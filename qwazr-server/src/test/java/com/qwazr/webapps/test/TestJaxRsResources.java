/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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
 **/
package com.qwazr.webapps.test;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.ServerVariable;
import java.security.Principal;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

/**
 * Example of JAX-RS
 */

public class TestJaxRsResources {

    public final static String TEST_STRING = "JAX_RS_TEST_STRING";

    @Path("/json")
    @OpenAPIDefinition(servers = {@Server(variables = {@ServerVariable(name = "basepath",
            defaultValue = "/jaxrs-class-json")})}, info = @Info(title = "ServiceJson", version = "v1.2.3"))
    public static class ServiceJson {

        @Path("/test/{path-param}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Data getTestJson(@PathParam("path-param") String pathParam) {
            return new Data(TEST_STRING, pathParam);
        }
    }

    @OpenAPIDefinition
    @Path("/xml")
    public static class ServiceBothXml {

        @Path("/test/{path-param}")
        @POST
        @Produces(MediaType.APPLICATION_XML)
        public Data getTestJson(@PathParam("path-param") String pathParam) {
            return new Data(TEST_STRING, pathParam);
        }
    }

    @OpenAPIDefinition(servers = {@Server(variables = {@ServerVariable(name = "basepath",
            defaultValue = "/jaxrs-class-both")})})
    @Path("/json")
    public static class ServiceBothJson {

        @Path("/test/{path-param}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Data getTestJson(@PathParam("path-param") String pathParam) {
            return new Data(TEST_STRING, pathParam);
        }
    }

    @OpenAPIDefinition(info = @Info(title = "ServiceBoth", version = "v1.2.3"))
    public interface ServiceBothConfig {
    }

    @Path("/xml")
    @OpenAPIDefinition(servers = {@Server(variables = {@ServerVariable(name = "basepath",
            defaultValue = "/jaxrs-class-xml")})}, info = @Info(title = "ServiceXml", version = "v1.2.3"))
    public static class ServiceXml {

        @Path("/test/{path-param}")
        @POST
        @Produces(MediaType.APPLICATION_XML)
        public Data getTestJson(@PathParam("path-param") String pathParam) {
            return new Data(TEST_STRING, pathParam);
        }
    }

    @OpenAPIDefinition(servers = {@Server(variables = {@ServerVariable(name = "basepath",
            defaultValue = "/jaxrs-auth")})}, info = @Info(title = "ServiceAuth", version = "v1.2.3"))
    public interface ServiceAuthConfig {
    }

    @OpenAPIDefinition(info = @Info(title = "ServiceAuth", version = "v1.2.3"))
    @Path("/auth")
    @PermitAll
    public static class ServiceAuth {

        @Context
        private HttpServletResponse response;

        @Context
        private SecurityContext securityContext;

        public final static String xAuthUser = "X-AUTH-USER";

        @Path("/test")
        @Produces(MediaType.TEXT_PLAIN)
        @RolesAllowed(TestIdentityProvider.VALID_ROLE)
        @HEAD
        public void testAuth() {
            final Principal principal = securityContext.getUserPrincipal();
            if (principal == null)
                return;
            response.setHeader(xAuthUser, principal.getName());
        }

        @Path("/wrong-role")
        @Produces(MediaType.TEXT_PLAIN)
        @RolesAllowed("dummy")
        @HEAD
        public void testWrongRole() {
            final Principal principal = securityContext.getUserPrincipal();
            if (principal == null)
                return;
            response.setHeader(xAuthUser, principal.getName());
        }
    }

    public static class Data {

        public final String param1;
        public final String param2;

        public Data() {
            param1 = null;
            param2 = null;
        }

        private Data(String param1, String param2) {
            this.param1 = param1;
            this.param2 = param2;
        }
    }

}
