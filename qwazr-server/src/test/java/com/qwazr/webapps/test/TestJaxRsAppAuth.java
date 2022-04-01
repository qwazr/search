/*
 * Copyright 2016-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.webapps.test;

import com.qwazr.server.WebappBuilder;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.ServerVariable;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.security.PermitAll;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

/**
 * Example of JAX-RS with authentication
 */
@PermitAll
@OpenAPIDefinition(servers = {@Server(variables = {@ServerVariable(name = "basepath", defaultValue = "/jaxrs-app-auth")})})
@ApplicationPath("/jaxrs-app-auth")
public class TestJaxRsAppAuth extends Application {

    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new LinkedHashSet<>();
        classes.add(TestJaxRsResources.ServiceAuth.class);
        classes.addAll(WebappBuilder.JACKSON_CLASSES);
        classes.addAll(WebappBuilder.SWAGGER_CLASSES);
        classes.add(RolesAllowedDynamicFeature.class);
        classes.add(AppConfig.class);
        return classes;
    }

    @OpenAPIDefinition(info = @Info(title = "TestJaxRsAppAuth", version = "v1.2.3"))
    public interface AppConfig {
    }
}
