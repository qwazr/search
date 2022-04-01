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

import com.fasterxml.jackson.databind.JsonNode;
import com.qwazr.utils.ObjectMappers;
import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FullTest implements TestChecker {

    static Client client;
    static WebTarget target;
    static TestServer server;


    @BeforeClass
    public static void setup() throws IOException {
        client = ClientBuilder.newClient();
        target = client.target(TestServer.BASE_SERVLET_URL);
        server = new TestServer();
    }

    @AfterClass
    public static void cleanup() {
        if (client != null) {
            client.close();
            client = null;
        }
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void test000StartServer() throws Exception {
        server.start();
        Assert.assertNotNull(server.getServer());
    }

    @Test
    public void test50Listener() {
        Assert.assertEquals(1, TestListener.initializedListeners.size());
        TestListener listener = TestListener.initializedListeners.iterator().next();
        Assert.assertEquals(TestListener.class, listener.getClass());
    }

    @Test
    public void test100javaServlet() {
        final Response response = checkResponse(target.path("java").request().get(), 200);
        try {
            final String content = checkEntity(response, MediaType.TEXT_HTML_TYPE);
            checkContains(content, TestServlet.TEST_STRING);
        } finally {
            response.close();
        }
    }

    @Test
    public void test102javaServletBis() {
        final Response response = checkResponse(target.path("java-bis").request().get(), 200);
        try {
            final String content = checkEntity(response, MediaType.TEXT_HTML_TYPE);
            checkContains(content, TestServlet.TEST_STRING);
        } finally {
            response.close();
        }
    }

    @Test
    public void test150JaxRsAppJson() {
        final String pathParam = "sub-path-app-json";
        final Response response = checkResponse(target.path("/jaxrs-app/json/test/" + pathParam).request().get(), 200);
        try {
            final String content = checkEntity(response, MediaType.APPLICATION_JSON_TYPE);
            checkContains(content, TestJaxRsResources.TEST_STRING, pathParam);
            checkResponse(target.path("/jaxrs-app/openapi.json").request().get(), 404).close();
        } finally {
            response.close();
        }
    }

    @Test
    public void test151JaxRsAppXml() throws IOException {
        final String pathParam = "sub-path-app-xml";
        try (final Response response = target.path("/jaxrs-app/xml/test/" + pathParam).request().post(null)) {
            checkResponse(response, 200);
            final String content = checkEntity(response, MediaType.APPLICATION_XML_TYPE);
            checkContains(content, TestJaxRsResources.TEST_STRING, pathParam);
        }
        try (final Response response = target.path("/jaxrs-app/openapi.json").request().get()) {
            checkResponse(response, 404);
        }
    }

    @Test
    public void test160JaxRsClassJson() throws IOException {
        final String pathParam = "sub-path-class-json";
        try (final Response response = target.path("/jaxrs-class-json/json/test/" + pathParam).request().get()) {
            checkResponse(response, 200);
            final String content = checkEntity(response, MediaType.APPLICATION_JSON_TYPE);
            checkContains(content, TestJaxRsResources.TEST_STRING, pathParam);
        }
        try (final Response response = target.path("/jaxrs-class-json/openapi.json").request().get()) {
            checkSwagger(checkResponse(response, 200), "ServiceJson", "/jaxrs-class-json");
        }
    }

    @Test
    public void test161JaxRsClassXml() throws IOException {
        final String pathParam = "sub-path-class-xml";
        try (final Response response = target.path("/jaxrs-class-xml/xml/test/" + pathParam).request().post(null)) {
            checkResponse(response, 200);
            final String content = checkEntity(response, MediaType.APPLICATION_XML_TYPE);
            checkContains(content, TestJaxRsResources.TEST_STRING, pathParam);
        }
        try (final Response response = target.path("/jaxrs-class-xml/openapi.json").request().get()) {
            checkSwagger(checkResponse(response, 200), "ServiceXml", "/jaxrs-class-xml");
        }
    }

    @Test
    public void test170JaxRsClassBoth() throws IOException {
        final String pathParam = "sub-path-class-both";
        try (Response response = target.path("/jaxrs-class-both/xml/test/" + pathParam).request().post(null)) {
            checkResponse(response, 200);
            String content = checkEntity(response, MediaType.APPLICATION_XML_TYPE);
            checkContains(content, TestJaxRsResources.TEST_STRING, pathParam);
        }
        try (Response response = target.path("/jaxrs-class-both/json/test/" + pathParam).request().get()) {
            checkResponse(response, 200);
            String content = checkEntity(response, MediaType.APPLICATION_JSON_TYPE);
            checkContains(content, TestJaxRsResources.TEST_STRING, pathParam);
        }
        try (Response response = target.path("/jaxrs-class-both/openapi.json").request().get()) {
            checkSwagger(checkResponse(response, 200), "ServiceBoth", "/jaxrs-class-both");
        }
    }

    Client getAuthClient(String user, String pass) {
        return ClientBuilder.newClient().register(HttpAuthenticationFeature.basic(user, pass));
    }

    private Client getValidAuth() {
        return getAuthClient(TestIdentityProvider.TEST_USER, TestIdentityProvider.TEST_PASSWORD);
    }

    private Client getWrongAuth() {
        return getAuthClient("dummy", "dummy");
    }

    private void checkSwagger(final Response response, final String title, final String basePath) throws IOException {
        final JsonNode root = ObjectMappers.JSON.readTree(response.readEntity(String.class));
        Assert.assertNotNull(root);
        Assert.assertTrue(root.has("openapi"));
        Assert.assertEquals("3.0.1", root.get("openapi").asText());
        //TODO check basePath
        //Assert.assertTrue(root.has("server"));
        //Assert.assertEquals(basePath, root.get("server").asText());
        Assert.assertTrue(root.has("info"));
        JsonNode info = root.get("info");
        Assert.assertEquals("v1.2.3", info.get("version").asText());
        // TODO check title
        //  Assert.assertEquals(title, info.get("title").asText());
    }

    private void testAuth(final String appPath, final String appTitle) throws IOException {
        checkResponse(target.path(appPath).path("/auth/test").request().head(), 401).close();
        Client wrongAuthClient = getWrongAuth();
        checkResponse(
                wrongAuthClient.target(TestServer.BASE_SERVLET_URL).path(appPath).path("/auth/test").request().head(),
                401).close();
        wrongAuthClient.close();
        Client validAuthClient = getValidAuth();
        checkResponse(validAuthClient.target(TestServer.BASE_SERVLET_URL)
                .path(appPath)
                .path("/auth/wrong-role")
                .request()
                .head(), 403).close();
        final Response response = checkResponse(
                validAuthClient.target(TestServer.BASE_SERVLET_URL).path(appPath).path("/auth/test").request().head(),
                204);
        Assert.assertNotNull(response.getHeaderString(TestJaxRsResources.ServiceAuth.xAuthUser));
        response.close();
        checkSwagger(checkResponse(
                validAuthClient.target(TestServer.BASE_SERVLET_URL).path(appPath).path("/openapi.json").request().get(),
                200), appTitle, appPath);
    }

    @Test
    public void test180JaxRsAuth() throws IOException {
        testAuth("/jaxrs-auth", "ServiceAuth");
        Assert.assertEquals(4, TestIdentityProvider.authCount.get());
        Assert.assertEquals(3, TestIdentityProvider.authSuccessCount.get());
        testAuth("/jaxrs-app-auth", "TestJaxRsAppAuth");
        Assert.assertEquals(8, TestIdentityProvider.authCount.get());
        Assert.assertEquals(6, TestIdentityProvider.authSuccessCount.get());

    }

    @Test
    public void test300staticFile() {
        final String badUrl = "/css/dummy.css";
        checkResponse(target.path(badUrl).request().head(), 404).close();
        checkResponse(target.path(badUrl).request().get(), 404).close();
        final String goodUrl = "/css/test.css";
        checkContentType(checkResponse(target.path(goodUrl).request().head(), 200), MIME_TEXT_CSS).close();
        try (final Response response = target.path(goodUrl).request().get()) {
            checkResponse(response, 200);
            checkContains(checkEntity(response, MIME_TEXT_CSS), ".qwazr {");
            assertThat(response.getHeaderString("Etag"), notNullValue());
            assertThat(response.getHeaderString("Last-Modified"), notNullValue());
        }
    }

    @Test
    public void test301staticResource() {
        final String badUrl = "/img/dummy.png";
        checkResponse(target.path(badUrl).request().head(), 404).close();
        checkResponse(target.path(badUrl).request().get(), 404).close();
        final String goodUrl = "/img/logo.png";
        checkContentType(checkResponse(target.path(goodUrl).request().head(), 200), MIME_IMAGE_X_PNG).close();
        checkEntity(checkResponse(target.path(goodUrl).request().get(), 200), MIME_IMAGE_X_PNG);
    }

    @Test
    public void test302favicon() {
        final String url = "/favicon.ico";
        checkResponse(target.path(url).request().head(), 200).close();
        checkEntity(checkResponse(target.path(url).request().get(), 200), MIME_FAVICON);
    }

    @Test
    public void test400staticHtml() {
        final String url = "/index";
        checkResponse(target.path(url).request().head(), 200).close();
        checkContains(checkEntity(checkResponse(target.path(url).request().get(), 200), MediaType.TEXT_HTML_TYPE),
                "QWAZR - Hello World!");
    }

    @Test
    public void test402staticIndexHtml() {
        final String url = "/html";
        checkResponse(target.path(url).request().head(), 200).close();
        checkContains(checkEntity(checkResponse(target.path(url).request().get(), 200), MediaType.TEXT_HTML_TYPE),
                "QWAZR - Hello World!");
    }

    @Test
    public void test404staticIndexHtml() {
        final String url = "/html/index.html";
        checkResponse(target.path(url).request().head(), 200).close();
        checkContains(checkEntity(checkResponse(target.path(url).request().get(), 200), MediaType.TEXT_HTML_TYPE),
                "QWAZR - Hello World!");
    }

    @Test
    public void test800Filters() {
        Assert.assertEquals(1, TestFilter.initializedFilters.size());
        Assert.assertEquals(TestFilter.class, TestFilter.initializedFilters.iterator().next().getClass());
        Assert.assertEquals(1, TestFilter.calledFilters.size());
        Assert.assertEquals(TestFilter.class, TestFilter.calledFilters.iterator().next().getClass());
    }
}
