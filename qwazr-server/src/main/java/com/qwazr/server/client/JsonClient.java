/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.server.client;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.smile.JacksonSmileProvider;
import com.qwazr.server.RemoteService;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.Closeable;
import java.util.Objects;

public class JsonClient implements Closeable {

    private final static int DEFAULT_TIMEOUT;

    static {
        String s = System.getProperty("com.qwazr.server.client.default_timeout");
        DEFAULT_TIMEOUT = s == null ? 60000 : Integer.parseInt(s);
    }

    protected final RemoteService remote;

    final protected Client client;

    protected JsonClient(final RemoteService remote) {
        this.remote = Objects.requireNonNull(remote, "The remote parameter is null");

        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(JacksonJsonProvider.class).register(JacksonSmileProvider.class);

        if (remote.isCredential()) {
            final HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder()
                    .nonPreemptive()
                    .credentials(remote.username, remote.password)
                    .build();
            clientConfig.register(feature);
        }

        final int timeout = remote.timeout != null ? remote.timeout : DEFAULT_TIMEOUT;

        client = ClientBuilder.newClient(clientConfig);
        client.property(ClientProperties.CONNECT_TIMEOUT, timeout);
        client.property(ClientProperties.READ_TIMEOUT, timeout);

    }

    public void close() {
        client.close();
    }

    @Override
    public String toString() {
        return remote.toString();
    }

}
