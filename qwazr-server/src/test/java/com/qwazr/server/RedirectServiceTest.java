/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

import org.glassfish.jersey.client.ClientProperties;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.management.JMException;
import javax.servlet.ServletException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.net.URI;

public class RedirectServiceTest {

    private static RedirectJaxRsServer server;

    @BeforeClass
    public static void setup() throws ServletException, IOException, JMException {
        server = new RedirectJaxRsServer();
        server.start();
    }

    @Test
    public void testRedirect() {
        final Client client = ClientBuilder.newClient().property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);
        try {
            client.target("http://localhost:9091").request().get(String.class);
            Assert.fail("RedirectionException not thrown");
        } catch (RedirectionException e) {
            Assert.assertEquals(e.getLocation(), URI.create("http://localhost:9091/redirect"));
        } finally {
            client.close();
        }
    }

    @AfterClass
    public static void cleanup() {
        server.stop();
    }
}
