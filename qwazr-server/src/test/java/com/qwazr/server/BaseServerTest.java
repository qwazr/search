/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.smile.JacksonSmileProvider;
import org.junit.After;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.ArrayList;
import java.util.List;

public class BaseServerTest {

    final List<Client> clients = new ArrayList<>();

    public Client getClient(Object... components) {
        final Client client =
                ClientBuilder.newClient().register(JacksonJsonProvider.class).register(JacksonSmileProvider.class);
        for (Object component : components)
            client.register(component);
        clients.add(client);
        return client;
    }

    @After
    public void cleanup() {
        clients.forEach(Client::close);
    }

}
