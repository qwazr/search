/*
 * Copyright 2017-2018 Emmanuel Keller / QWAZR
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

import com.qwazr.server.GenericServer;
import com.qwazr.server.GenericServerBuilder;
import com.qwazr.server.configuration.ServerConfiguration;
import com.qwazr.utils.RandomUtils;
import java.io.IOException;
import java.nio.file.Files;
import javax.management.JMException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ServletTest implements TestChecker {

    public static GenericServer server;
    public static Client client;
    public static WebTarget target;
    public static String randomString1;
    public static String randomString2;

    @BeforeClass
    public static void before() throws IOException, JMException, ServletException {
        randomString1 = RandomUtils.alphanumeric(10);
        randomString2 = RandomUtils.alphanumeric(10);
        final ServerConfiguration configuration = ServerConfiguration.of()
                .data(Files.createTempDirectory("servletTest"))
                .publicAddress("localhost")
                .listenAddress("localhost")
                .build();

        final GenericServerBuilder serverBuilder = GenericServer.of(configuration);
        serverBuilder.getConstructorParameters().registerConstructorParameter(randomString1);
        serverBuilder.getWebAppContext().getWebappBuilder()
                .registerServlet("/", TestServletConstructorParameter.class)
                .registerServlet(TestServletAnnotation1.class)
                .registerServlet(TestServletAnnotation2.class, () -> new TestServletAnnotation2(randomString2))
                .registerWebjars();
        server = serverBuilder.build();
        server.start(true);
        client = ClientBuilder.newClient();
        target = client.target(TestServer.BASE_SERVLET_URL);
    }

    @Test
    public void testConstructorParameter() {
        try (Response response = checkResponse(target.request().get(), 200)) {
            final String content = checkEntity(response, MediaType.TEXT_HTML_TYPE);
            checkContains(content, randomString1 + "CONSTRUCTOR");
        }
    }

    @Test
    public void testAnnotatedServlet1() {
        try (Response response = checkResponse(target.path("test1").request().get(), 200)) {
            final String content = checkEntity(response, MediaType.TEXT_HTML_TYPE);
            checkContains(content, randomString1 + "ANNOTATION");
        }
    }

    @Test
    public void testAnnotatedServlet2() {
        try (final Response response = checkResponse(target.path("test2").request().get(), 200)) {
            final String content = checkEntity(response, MediaType.TEXT_HTML_TYPE);
            checkContains(content, randomString2 + "ANNOTATION");
        }
    }

    @Test
    public void testWebjars() {
        try (final Response response = checkResponse(
                target.path("webjars/bootstrap/5.1.3/css/bootstrap.css").request().get(), 200)) {
            final String content = checkEntity(response, MediaType.valueOf("text/css"));
            checkContains(content, "bootstrap");
            assertThat(response.getHeaderString("ETag"), is("bootstrap.css_5.1.3"));
        }
    }

    @AfterClass
    public static void after() {
        if (server != null) {
            server.close();
            server = null;
        }
        if (client != null) {
            client.close();
            client = null;
        }
    }

    public static class TestServletConstructorParameter extends HttpServlet implements Cloneable {

        public final String testString;

        public TestServletConstructorParameter(String testString) {
            this.testString = testString;
        }

        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType(MediaType.TEXT_HTML);
            resp.getWriter().write("<html><body>" + testString + "CONSTRUCTOR</body></html>");
        }

    }

    @WebServlet("/test1")
    public static class TestServletAnnotation1 extends HttpServlet {

        public final String testString;

        public TestServletAnnotation1(String testString) {
            this.testString = testString;
        }

        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType(MediaType.TEXT_HTML);
            resp.getWriter().write("<html><body>" + testString + "ANNOTATION</body></html>");
        }

    }

    @WebServlet("/test2")
    public static class TestServletAnnotation2 extends TestServletAnnotation1 {

        public TestServletAnnotation2(String testString) {
            super(testString);
        }
    }
}
