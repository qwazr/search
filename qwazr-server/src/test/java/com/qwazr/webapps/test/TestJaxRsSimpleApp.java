/**
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
 **/
package com.qwazr.webapps.test;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider;
import com.qwazr.utils.json.JacksonConfig;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Example of JAX-RS
 */
@OpenAPIDefinition
@ApplicationPath("/jaxrs-app/*")
public class TestJaxRsSimpleApp extends Application {

	public Set<Class<?>> getClasses() {
		return new HashSet<>(Arrays.asList(TestJaxRsResources.ServiceJson.class, TestJaxRsResources.ServiceXml.class,
				TestJaxRsResources.ServiceAuth.class, JacksonConfig.class, JacksonJsonProvider.class,
				JacksonXMLProvider.class));
	}

}
