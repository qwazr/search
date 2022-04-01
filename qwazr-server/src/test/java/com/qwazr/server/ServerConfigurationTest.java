/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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

import com.qwazr.server.configuration.ServerConfiguration;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServerConfigurationTest {

    private void checkConnector(ServerConfiguration.WebConnector connector, String address, Integer port) {
        Assert.assertEquals(address, connector.address);
        Assert.assertEquals(port == null ? null : address + ":" + port, connector.addressPort);
        if (port != null)
            Assert.assertEquals((int) port, connector.port);
    }

    private void checkConnectors(ServerConfiguration config, Integer webAppPort, Integer webServicePort,
                                 Integer multicastPort) {
        checkConnector(config.webAppConnector, webAppPort == null ? null : config.publicAddress, webAppPort);
        checkConnector(config.webServiceConnector, webServicePort == null ? null : config.publicAddress,
                webServicePort);
        checkConnector(config.multicastConnector, multicastPort == null ? null : config.publicAddress, multicastPort);
    }

    private static final Path dataDir;

    static {
        try {
            dataDir = Files.createTempDirectory("ServerConfigurationTest");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Path tempDir = dataDir.resolve("temp").toAbsolutePath();

    @Before
    public void before() {
        System.setProperty("QWAZR_DATA", dataDir.toAbsolutePath().toString());
    }

    @Test
    public void empty() throws IOException {
        ServerConfiguration config = ServerConfiguration.of()
                .applyEnvironmentVariables()
                .applySystemProperties()
                .applyCommandLineArgs()
                .build();
        Assert.assertEquals(dataDir, config.dataDirectory);
        Assert.assertEquals("0.0.0.0", config.listenAddress);
        Assert.assertNotNull(config.publicAddress);
        checkConnectors(config, 9090, 9091, null);
    }

    @Test
    public void ipArgs() throws IOException {
        ServerConfiguration config = ServerConfiguration.of()
                .applyCommandLineArgs(
                        "--LISTEN_ADDR=PUBLIC_ADDR=MULTICAST_ADDR=192.168.0.1",
                        "--WEBAPP_PORT=9190",
                        "--WEBSERVICE_PORT=9191", "--MULTICAST_PORT=9192")
                .build();
        Assert.assertEquals("192.168.0.1", config.listenAddress);
        Assert.assertEquals("192.168.0.1", config.publicAddress);
        checkConnectors(config, 9190, 9191, 9192);
    }

    @Test
    public void ipRange() throws IOException {
        ServerConfiguration config = ServerConfiguration.of()
                .applyCommandLineArgs("--LISTEN_ADDR=PUBLIC_ADDR=127.0.0.0/24")
                .build();
        Assert.assertEquals("127.0.0.1", config.listenAddress);
        Assert.assertEquals("127.0.0.1", config.publicAddress);
        checkConnectors(config, 9090, 9091, null);
    }

    @Test
    public void dataDirectory() throws IOException {
        ServerConfiguration config = ServerConfiguration.of()
                .applyEnvironmentVariables()
                .applySystemProperties()
                .applyCommandLineArgs()
                .build();
        Assert.assertEquals(dataDir, config.dataDirectory);
    }

    @Test
    public void groups() throws IOException {
        // First with properties
        System.setProperty("QWAZR_GROUPS", "groupProp");
        ServerConfiguration config = ServerConfiguration.of()
                .applyEnvironmentVariables()
                .applySystemProperties()
                .applyCommandLineArgs()
                .build();
        Assert.assertEquals(1, config.groups.size());
        Assert.assertTrue(config.groups.contains("groupProp"));

        // Then override by arguments
        config = ServerConfiguration.of().applyCommandLineArgs("--QWAZR_GROUPS=group1, group2, group3").build();
        Assert.assertEquals(3, config.groups.size());
        Assert.assertTrue(config.groups.contains("group1"));
        Assert.assertTrue(config.groups.contains("group2"));
        Assert.assertTrue(config.groups.contains("group3"));
        Assert.assertFalse(config.groups.contains("groupProp"));
    }

    @Test
    public void masters() throws IOException {
        // First with properties
        System.setProperty("QWAZR_MASTERS", "master5:9591");
        ServerConfiguration config = ServerConfiguration.of()
                .applyEnvironmentVariables()
                .applySystemProperties()
                .applyCommandLineArgs()
                .build();
        Assert.assertEquals(1, config.masters.size());
        Assert.assertTrue(config.masters.contains("master5:9591"));

        // Then overrided by arguments
        config = ServerConfiguration.of()
                .applyCommandLineArgs("--QWAZR_MASTERS=master6:9691 , master7:9791 ; master8:9891")
                .build();
        Assert.assertEquals(3, config.masters.size());
        Assert.assertTrue(config.masters.contains("master6:9691"));
        Assert.assertTrue(config.masters.contains("master7:9791"));
        Assert.assertTrue(config.masters.contains("master8:9891"));
        Assert.assertFalse(config.masters.contains("master5:9591"));
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        properties.put("QWAZR_MASTERS", "localhost:9191,localhost:9291");
        properties.put("WEBAPP_PORT", Integer.toString(9190));
        properties.put("WEBSERVICE_PORT", Integer.toString(9191));
        properties.put("QWAZR_DATA", dataDir.toAbsolutePath().toString());
        return properties;
    }

    public ServerConfiguration getConfig(Properties properties) throws IOException {
        Path propFile = Files.createTempFile("qwazr-test", ".properties");
        try (final BufferedWriter writer = Files.newBufferedWriter(propFile)) {
            properties.store(writer, null);
        }
        return ServerConfiguration.of()
                .applyCommandLineArgs("--QWAZR_PROPERTIES=" + propFile.toAbsolutePath())
                .build();
    }

    @Test
    public void webPort() throws IOException {
        final ServerConfiguration configuration = getConfig(getProperties());
        Assert.assertEquals(configuration.webAppConnector.port, 9190);
        Assert.assertEquals(configuration.webServiceConnector.port, 9191);
        Assert.assertTrue(configuration.masters.contains("localhost:9191"));
        Assert.assertTrue(configuration.masters.contains("localhost:9291"));
    }

    @Test
    public void builder() throws IOException {
        ServerConfiguration config = ServerConfiguration.of()
                .data(dataDir)
                .temp(tempDir)
                .publicAddress("localhost")
                .listenAddress("0.0.0.0")
                .webAppRealm("webapprealm")
                .webAppPort(9390)
                .webServicePort(9391)
                .webServiceRealm("webservicerealm")
                .multicastPort(9392)
                .multicastAddress("224.0.0.1")
                .group("group1", "group2")
                .group(Arrays.asList("group1", "group2"))
                .master("localhost:9090", "localhost:9091")
                .master(Arrays.asList("localhost:9090", "localhost:9091"))
                .build();
        Assert.assertEquals(dataDir, config.dataDirectory);
        Assert.assertEquals(tempDir, config.tempDirectory);
        Assert.assertEquals("0.0.0.0", config.listenAddress);
        Assert.assertEquals("localhost", config.publicAddress);
        Assert.assertEquals("webapprealm", config.webAppConnector.realm);
        Assert.assertEquals("webservicerealm", config.webServiceConnector.realm);
        Assert.assertEquals(9390, config.webAppConnector.port);
        Assert.assertEquals(9391, config.webServiceConnector.port);
        Assert.assertEquals(9392, config.multicastConnector.port);
        Assert.assertEquals("224.0.0.1", config.multicastConnector.address);
        Assert.assertTrue(config.groups.contains("group1"));
        Assert.assertTrue(config.groups.contains("group2"));
        Assert.assertTrue(config.masters.contains("localhost:9090"));
        Assert.assertTrue(config.masters.contains("localhost:9091"));
        Assert.assertEquals(2, config.groups.size());
    }

    @Test(expected = SocketException.class)
    public void checkNoPublicAddressMaskMatching() throws IOException {
        ServerConfiguration.of().publicAddress("123.123.123.123/24").build();
    }

    @Test
    public void checkValidPublicAddressMaskMatching() throws IOException {
        final ServerConfiguration config = ServerConfiguration.of().publicAddress("127.0.0.1/24").build();
        Assert.assertEquals("127.0.0.1", config.publicAddress);
    }

    @Test
    public void checkLocalhostAsPublicAddress() throws IOException {
        final ServerConfiguration config = ServerConfiguration.of().publicAddress("localhost").build();
        Assert.assertEquals("localhost", config.publicAddress);
        Assert.assertEquals("0.0.0.0", config.listenAddress);
    }
}
