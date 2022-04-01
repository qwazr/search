package com.qwazr.server;

import com.qwazr.server.configuration.ServerConfiguration;
import com.qwazr.server.logs.AccessLogger;
import com.qwazr.server.logs.LogParam;
import com.qwazr.utils.reflection.ConstructorParameters;
import io.undertow.server.session.SessionListener;
import io.undertow.servlet.api.SessionPersistenceManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericServerBuilder {

    final ServerConfiguration configuration;
    final ExecutorService executorService;
    final ClassLoader classLoader;
    final ConstructorParameters constructorParameters;

    final ServletContextBuilder webAppContext;
    final ServletContextBuilder webServiceContext;

    Map<String, Object> contextAttributes;
    Collection<UdpServerThread.PacketListener> packetListeners;

    AccessLogger webAppAccessLogger;
    AccessLogger webServiceAccessLogger;

    GenericServer.IdentityManagerProvider identityManagerProvider;
    HostnameAuthenticationMechanism.PrincipalResolver hostnamePrincipalResolver;

    Collection<GenericServer.Listener> startedListeners;
    Collection<GenericServer.Listener> shutdownListeners;

    GenericServerBuilder(final ServerConfiguration configuration, final ExecutorService executorService,
                         final ClassLoader classLoader, final ConstructorParameters constructorParameters) {
        this.configuration = configuration;
        this.executorService = executorService;
        this.classLoader = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
        this.constructorParameters =
                constructorParameters == null ? ConstructorParameters.withConcurrentMap() : constructorParameters;
        this.webAppContext = new ServletContextBuilder(this.classLoader, this.constructorParameters, "/", "UTF-8", "ROOT", "WEBAPP");
        this.webServiceContext = new ServletContextBuilder(this.classLoader, this.constructorParameters, "/", "UTF-8", "ROOT", "WEBSERVICE");
    }

    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    public ConstructorParameters getConstructorParameters() {
        return constructorParameters;
    }

    public GenericServer build() throws IOException {
        return new GenericServer(this);
    }

    public ServletContextBuilder getWebAppContext() {
        return webAppContext;
    }

    public ServletContextBuilder getWebServiceContext() {
        return webServiceContext;
    }

    public GenericServerBuilder packetListener(final UdpServerThread.PacketListener packetListener) {
        if (packetListeners == null)
            packetListeners = new LinkedHashSet<>();
        this.packetListeners.add(packetListener);
        return this;
    }

    public GenericServerBuilder contextAttribute(final String name, final Object object) {
        Objects.requireNonNull(name, "The name of the context attribute is null");
        Objects.requireNonNull(object, "The context attribute " + name + " is null");
        if (contextAttributes == null)
            contextAttributes = new LinkedHashMap<>();
        contextAttributes.put(name, object);
        return this;
    }

    public GenericServerBuilder contextAttribute(final Object object) {
        Objects.requireNonNull(object, "The context attribute object is null");
        return contextAttribute(object.getClass().getName(), object);
    }

    public GenericServerBuilder startedListener(final GenericServer.Listener listener) {
        Objects.requireNonNull(listener, "The GenericServer.Listener object is null");
        if (startedListeners == null)
            startedListeners = new LinkedHashSet<>();
        startedListeners.add(listener);
        return this;
    }

    public GenericServerBuilder shutdownListener(final GenericServer.Listener listener) {
        Objects.requireNonNull(listener, "The GenericServer.Listener object is null");
        if (shutdownListeners == null)
            shutdownListeners = new LinkedHashSet<>();
        shutdownListeners.add(listener);
        return this;
    }

    public GenericServerBuilder sessionPersistenceManager(final SessionPersistenceManager manager) {
        webAppContext.setSessionPersistenceManager(manager);
        return this;
    }

    public GenericServerBuilder persistSessions(final Path persistenceDirectory) throws IOException {
        if (!Files.exists(persistenceDirectory))
            Files.createDirectory(persistenceDirectory);
        sessionPersistenceManager(new InFileSessionPersistenceManager(persistenceDirectory));
        return this;
    }

    public GenericServerBuilder identityManagerProvider(final GenericServer.IdentityManagerProvider provider) {
        identityManagerProvider = provider;
        return this;
    }

    public GenericServerBuilder hostnamePrincipalResolver(
            final HostnameAuthenticationMechanism.PrincipalResolver hostnamePrincipalResolver) {
        this.hostnamePrincipalResolver = hostnamePrincipalResolver;
        return this;
    }

    public GenericServerBuilder sessionListener(final SessionListener listener) {
        webAppContext.addSessionListener(listener);
        return this;
    }

    public GenericServerBuilder webAppAccessLogger(AccessLogger accessLogger) {
        webAppAccessLogger = accessLogger;
        return this;
    }

    public GenericServerBuilder webAppAccessLogger(Logger logger, Level level, String logMessage,
                                                   LogParam... logParams) {
        return webAppAccessLogger(new AccessLogger.Jul(logger, level, logMessage, logParams));
    }

    public GenericServerBuilder webAppAccessLogger(Logger logger) {
        return webAppAccessLogger(logger, Level.INFO, LogParam.DEFAULT_MESSAGE,
                LogParam.DEFAULT_PARAMS.toArray(new LogParam[0]));
    }

    public GenericServerBuilder webServiceAccessLogger(AccessLogger accessLogger) {
        webServiceAccessLogger = accessLogger;
        return this;
    }

    public GenericServerBuilder webServiceAccessLogger(Logger logger, Level level, String logMessage,
                                                       LogParam... logParams) {
        return webServiceAccessLogger(new AccessLogger.Jul(logger, level, logMessage, logParams));
    }

    public GenericServerBuilder webServiceAccessLogger(Logger logger) {
        return webServiceAccessLogger(logger, Level.INFO, LogParam.DEFAULT_MESSAGE,
                LogParam.DEFAULT_PARAMS.toArray(new LogParam[0]));
    }

    public GenericServerBuilder defaultMultipartConfig(String location, long maxFileSize, long maxRequestSize,
                                                       int fileSizeThreshold) {
        webAppContext.setDefaultMultipartConfig(location, maxFileSize, maxRequestSize, fileSizeThreshold);
        return this;
    }
}
