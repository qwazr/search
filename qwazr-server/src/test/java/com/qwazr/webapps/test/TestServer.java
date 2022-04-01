/**
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
package com.qwazr.webapps.test;

import com.qwazr.server.BaseServer;
import com.qwazr.server.GenericServer;
import com.qwazr.server.GenericServerBuilder;
import com.qwazr.server.configuration.ServerConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;

public class TestServer implements BaseServer {

    public static final String BASE_SERVLET_URL = "http://localhost:9090";

    private final GenericServer server;

    public TestServer() throws IOException {
        final Path dataDir = Files.createTempDirectory("webapp-date");
        System.setProperty("QWAZR_DATA", dataDir.toAbsolutePath().toString());
        System.setProperty("PUBLIC_ADDR", "localhost");
        System.setProperty("LISTEN_ADDR", "localhost");
        FileUtils.copyDirectoryToDirectory(Paths.get("src", "test", "css").toFile(), dataDir.toFile());
        FileUtils.copyDirectoryToDirectory(Paths.get("src", "test", "html").toFile(), dataDir.toFile());
        System.setProperty("WEBAPP_AUTH", "BASIC");
        System.setProperty("WEBAPP_REALM", "testRealm");

        final GenericServerBuilder serverBuilder = GenericServer.of(ServerConfiguration.of().applySystemProperties().build())
                .identityManagerProvider(new TestIdentityProvider());

        // Clear the properties when the test ends
        serverBuilder.shutdownListener(server -> {
            System.clearProperty("QWAZR_DATA");
            System.clearProperty("PUBLIC_ADDR");
            System.clearProperty("LISTEN_ADDR");
            System.clearProperty("WEBAPP_AUTH");
            System.clearProperty("WEBAPP_REALM");
        });


        serverBuilder.getWebAppContext()
                .getWebappBuilder()
                .registerDefaultFaviconServlet()
                .registerServlet("/java", TestServlet.class)
                .registerServlet("/java-bis", TestServlet.class)
                .registerJaxRsAppServlet("/jarx-app/*", TestJaxRsSimpleApp.class)
                .registerJaxRsAppServlet("/jaxrs-app-auth/*", TestJaxRsAppAuth.class)
                .registerJaxRsClassServlet("/jaxrs-auth/*", TestJaxRsResources.ServiceAuth.class, TestJaxRsResources.ServiceAuthConfig.class)
                .registerJaxRsClassServlet("/jaxrs-class-json/*", TestJaxRsResources.ServiceJson.class)
                .registerJaxRsClassServlet("/jaxrs-class-xml/*", TestJaxRsResources.ServiceXml.class)
                .registerJaxRsClassServlet("/jaxrs-class-both/*", TestJaxRsResources.ServiceJson.class, TestJaxRsResources.ServiceXml.class, TestJaxRsResources.ServiceBothConfig.class)
                .registerFilter("/*", TestFilter.class)
                .registerStaticServlet("/css/*", dataDir.resolve("css"))
                .registerStaticServlet("/img/*", "/com/qwazr/server/test/img")
                .registerStaticServlet("/index", dataDir.resolve("html").resolve("index.html"))
                .registerStaticServlet("/html/*", dataDir.resolve("html"))
                .registerListener(TestListener.class)
                .registerSecurePaths("/jaxrs-app-auth/*", "/jaxrs-auth/*");

        server = serverBuilder.build();
    }

    @Override
    public GenericServer getServer() {
        return server;
    }
}
