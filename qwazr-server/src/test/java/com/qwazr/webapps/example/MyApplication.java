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
package com.qwazr.webapps.example;

import com.qwazr.server.GenericServer;
import com.qwazr.server.GenericServerBuilder;
import com.qwazr.server.configuration.ServerConfiguration;
import java.io.IOException;
import javax.management.JMException;
import javax.servlet.ServletException;

public class MyApplication {

    static GenericServer serverInstance;

    public static void main(String[] args)
            throws IOException, ServletException, JMException {

        // Build the configuration of the server
        final ServerConfiguration configuration =
                ServerConfiguration.of().listenAddress("127.0.0.1").webAppPort(8081).build();

        // This is the generic server builder
        final GenericServerBuilder builder = GenericServer.of(configuration);

        // The web application definition
        builder.getWebAppContext().getWebappBuilder()
                .registerDefaultFaviconServlet()
                .registerWebjars() // Automatically mount all the webjars at /webjars/...
                .registerServlet(MyServlet.class, () -> new MyServlet(
                        "Hello World")); // Create a new servlet and inject dependencies thru the constructor

        // Build and start the server
        serverInstance = builder.build();
        serverInstance.start(true);
    }

}
