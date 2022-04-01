/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.management.JMException;
import javax.servlet.ServletException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.NewCookie;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class SessionManagerTest {

    private SimpleServer server1;
    private SimpleServer server2;

    static Path sessionDir;

    @Before
    public void setup() throws IOException {
        sessionDir = Files.createTempDirectory("sessionTest");
    }

    @After
    public void cleanup() {
        if (server1 != null)
            server1.stop();
        if (server2 != null)
            server2.stop();
    }

    private SimpleServer startNewServer() throws IOException, ServletException, JMException {
        final SimpleServer server = new SimpleServer(new InFileSessionPersistenceManager(sessionDir), null);
        Assert.assertNotNull(server.getServer());
        Assert.assertTrue(Files.exists(sessionDir));
        server.start();
        return server;
    }

    @Test
    public void test() throws JMException, ServletException, IOException {

        final WebTarget target = ClientBuilder.newClient().target("http://localhost:9090");

        server1 = startNewServer();
        Map<String, NewCookie> cookies = target.path("/test_bis").request().get().getCookies();

        String firstSessionId = SimpleServlet.sessionId;
        Assert.assertNotNull(firstSessionId);
        server1.stop();
        server1 = null;

        server2 = startNewServer();
        Invocation.Builder builder = target.path("/test_bis").request();
        cookies.forEach((k, c) -> builder.cookie(c.toCookie()));
        Assert.assertEquals(200, builder.get().getStatus());
        Assert.assertEquals(firstSessionId, SimpleServlet.lastSessionAttribute);

        server2.stop();
        server2 = null;
    }

}
