Embarqué dans une application JAVA
==================================

Cet example est également un test unitaire visible dans le code source:
[ClusterServerExample](https://github.com/qwazr/cluster/blob/master/src/test/java/com/qwazr/cluster/ClusterServerExample.java)

Dans ce test, on construit un micro-service avec une application JAX/RS contenant une resource qui expose des
informations au format JSON. Le service est accessible sur l'adresse: http://127.0.0.1:9091.

TODO: Décrire ClusterServiceInterface

```java
package com.qwazr.cluster;

import com.qwazr.server.ApplicationBuilder;
import com.qwazr.server.GenericServer;
import com.qwazr.server.GenericServerBuilder;
import com.qwazr.server.RemoteService;
import com.qwazr.server.RestApplication;
import com.qwazr.server.WelcomeShutdownService;
import com.qwazr.server.configuration.ServerConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.management.JMException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClusterServerExample {

    private ExecutorService executorService;
    private GenericServer genericServer;
    private ClusterServiceInterface clusterService;

    @Before
    public void setup() throws IOException, ServletException, JMException {

        // We create a dedicated ExecutorService.
        executorService = Executors.newCachedThreadPool();

        // Let's build our server configuration
        final ServerConfiguration serverConfiguration = ServerConfiguration.of()
                .publicAddress("127.0.0.1") // This is the address which can be used by other nodes to contact me
                .listenAddress("localhost") // This address defines which TCP/UDP address this cluster node is listening to
                .master("127.0.0.1:9091") // This make my node be a master node
                .build();

        // This let us build our server with servlet and/or JAX-RS resources.
        final GenericServerBuilder builder = GenericServer.of(serverConfiguration, executorService);

        // This will be the list of services announced by this cluster node
        final Set<String> services = new HashSet<>();
        services.add(ClusterServiceInterface.SERVICE_NAME);

        // Creation of the cluster manager
        final ClusterManager clusterManager =
                new ClusterManager(executorService, serverConfiguration)
                        .registerProtocolListener(builder, services);

        // Let's build a simple JAX-RS Application
        final ApplicationBuilder webServices = ApplicationBuilder.of("/*")
                .classes(RestApplication.JSON_CLASSES)
                .singletons(new WelcomeShutdownService());

        // The cluster manager provides a service singleton
        clusterService = clusterManager.getService();
        webServices.singletons(clusterService);
        builder.getWebServiceContext().jaxrs(webServices);

        // We can now build the server and start it
        genericServer = builder.build();
        genericServer.start(true);

        // Few assertions
        Assert.assertNotNull(genericServer);
        Assert.assertNotNull(clusterService);
    }

    @Test
    public void testMyPublicEndpoint() {
        // This is how other nodes can contact me over the network
        Assert.assertEquals("http://127.0.0.1:9091", clusterService.getStatus().me);
        // This is my UUID on the network
        Assert.assertNotNull(clusterService.getStatus().uuid);
    }

    @Test
    public void testIamMaster() {
        Assert.assertTrue(clusterService.getStatus().masters.contains("http://127.0.0.1:9091"));
    }

    @Test
    public void testVisibleAsAClusterNode() {
        Assert.assertTrue(clusterService.getStatus().activeNodes.containsKey("http://127.0.0.1:9091"));
    }

    @Test
    /*
     * This test uses the client library to request the WEB/JSON cluster service.
     */
    public void testRemoteAccess() throws URISyntaxException {
        final RemoteService remoteService = RemoteService.of("http://127.0.0.1:9091").build();
        final ClusterSingleClient client = new ClusterSingleClient(remoteService);
        final TreeSet<String> nodes = client.getActiveNodesByService("cluster", null);

        Assert.assertNotNull(nodes);
        Assert.assertTrue(nodes.contains("http://127.0.0.1:9091"));
    }

    @After
    public void cleanup() {
        genericServer.close();
        executorService.shutdown();
    }
}
```
