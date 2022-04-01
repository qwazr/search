/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.server;

import com.fasterxml.jackson.jaxrs.smile.SmileMediaTypes;
import com.qwazr.utils.RandomUtils;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/" + LoadedService.SERVICE_NAME)
@Singleton
public class LoadedService extends AbstractServiceImpl {

    public final static String SERVICE_NAME = "loaded";

    public final static String TEXT = RandomUtils.alphanumeric(10);

    @GET
    public String load(@QueryParam("properties") Boolean properties, @QueryParam("env") Boolean env) {
        return TEXT;
    }

    @Path("/map")
    @GET
    @Produces({MediaType.APPLICATION_JSON, SmileMediaTypes.APPLICATION_JACKSON_SMILE})
    public Map<String, String> getMap() {
        final HashMap<String, String> map = new HashMap<>();
        map.put(SERVICE_NAME, TEXT);
        return map;
    }

    final static GenericType<Map<String, String>> mapType = new GenericType<>() {
    };

}
