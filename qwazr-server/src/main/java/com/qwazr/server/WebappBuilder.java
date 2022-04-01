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
package com.qwazr.server;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JacksonConfig;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.SecurityInfo;
import io.undertow.servlet.api.ServletInfo;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.webjars.servlet.WebjarsServlet;

public class WebappBuilder {

    public final static int DEFAULT_EXPIRATION_TIME = 86400;

    public final static List<Class<?>> SWAGGER_CLASSES = List.of(OpenApiResource.class);

    public final static List<Class<?>> JACKSON_CLASSES =
            List.of(JacksonConfig.class, JacksonXMLProvider.class, JacksonJsonProvider.class);

    public final static String SESSIONS_PERSISTENCE_DIR = "webapp-sessions";

    public final static String DEFAULT_FAVICON_RESOURCE_PATH = "/com/qwazr/server/favicon.ico";
    public final static String DEFAULT_FAVICON_PATH = "/favicon.ico";

    private final ServletContextBuilder context;
    private MimetypesFileTypeMap mimeTypeMap;

    WebappBuilder(final ServletContextBuilder context) {
        this.context = context;
    }

    public WebappBuilder registerCustomFaviconServlet(final String faviconResourcePath) {
        return registerStaticServlet(DEFAULT_FAVICON_PATH, faviconResourcePath, DEFAULT_EXPIRATION_TIME);
    }

    /**
     * Set the default favicon
     *
     * @return the current builder
     */
    public WebappBuilder registerDefaultFaviconServlet() {
        return registerCustomFaviconServlet(DEFAULT_FAVICON_RESOURCE_PATH);
    }


    public WebappBuilder registerWebjars(final boolean disableCache, final String... urlMappings) {
        ServletInfo servletInfo = new ServletInfo("WebjarsServlet", WebjarsServlet.class).setLoadOnStartup(2)
                .addMappings(urlMappings);
        if (disableCache)
            servletInfo = servletInfo.addInitParam("disableCache", Boolean.toString(disableCache));
        context.addServlet(servletInfo);
        return this;
    }

    public WebappBuilder registerWebjars(final boolean disableCache) {
        return registerWebjars(disableCache, "/webjars/*");
    }

    public WebappBuilder registerWebjars() {
        return registerWebjars(false);
    }

    private synchronized MimetypesFileTypeMap getMimeTypeMap() {
        if (mimeTypeMap == null)
            mimeTypeMap = new MimetypesFileTypeMap(getClass().getResourceAsStream("/com/qwazr/server/mime.types"));
        return mimeTypeMap;
    }

    public WebappBuilder registerStaticServlet(final String urlPath,
                                               final String resourcePath,
                                               final int expirationSecTime) {
        final ServletInfo servletInfo = new ServletInfo(
                StaticResourceServlet.class.getName() + '@' + urlPath,
                StaticResourceServlet.class, GenericFactory.fromInstance(
                new StaticResourceServlet(resourcePath,
                        getMimeTypeMap(), expirationSecTime))).addMapping(urlPath);
        context.servlet(servletInfo);
        return this;
    }

    public WebappBuilder registerStaticServlet(final String urlPath, final String resourcePath) {
        return registerStaticServlet(urlPath, resourcePath, DEFAULT_EXPIRATION_TIME);
    }

    public WebappBuilder registerStaticServlet(final String urlPath,
                                               final java.nio.file.Path staticsPath,
                                               final int expirationSecTime) {
        final ServletInfo servletInfo =
                new ServletInfo(StaticFileServlet.class.getName() + '@' + urlPath, StaticFileServlet.class,
                        GenericFactory.fromInstance(new StaticFileServlet(getMimeTypeMap(), staticsPath,
                                expirationSecTime))).addMapping(urlPath);
        context.servlet(servletInfo);
        return this;
    }

    public WebappBuilder registerStaticServlet(final String urlPath,
                                               final java.nio.file.Path staticsPath) {
        return registerStaticServlet(urlPath, staticsPath, DEFAULT_EXPIRATION_TIME);
    }

    public WebappBuilder registerListener(final Class<? extends EventListener> listenerClass) {
        context.listener(Servlets.listener(listenerClass));
        return this;
    }

    public <T extends Servlet> WebappBuilder registerServlet(final String urlPath,
                                                             final Class<T> servletClass,
                                                             GenericFactory<T> servletFactory) {
        if (servletFactory == null && context.getConstructorParameters() != null)
            servletFactory = GenericFactory.fromConstructor(context.getConstructorParameters(), servletClass);
        context.servlet(servletClass.getName() + '@' + urlPath, servletClass, servletFactory, urlPath == null ? null : StringUtils.split(urlPath));
        return this;
    }

    public <T extends Servlet> WebappBuilder registerServlet(final String urlPath,
                                                             final Class<T> servletClass) {
        return registerServlet(urlPath, servletClass, null);
    }

    public <T extends Servlet> WebappBuilder registerServlet(final Class<T> servletClass,
                                                             final GenericFactory<T> servletFactory) {
        return registerServlet(null, servletClass, servletFactory);
    }

    public <T extends Servlet> WebappBuilder registerServlet(final Class<T> servletClass,
                                                             final Supplier<T> servletSupplier) {
        return registerServlet(null, servletClass, GenericFactory.fromSupplier(servletSupplier));
    }

    public <T extends Servlet> WebappBuilder registerServlet(final Class<T> servletClass) {
        return registerServlet(servletClass, (GenericFactory<T>) null);
    }

    public <T extends Filter> WebappBuilder registerFilter(final String urlPathes,
                                                           final Class<T> filterClass,
                                                           final GenericFactory<T> filterFactory,
                                                           final Map<String, String> initParams) {
        final String filterName = filterClass.getName() + '@' + urlPathes;
        context.filter(filterName, filterClass, filterFactory, initParams);
        if (urlPathes != null) {
            String[] urlPaths = StringUtils.split(urlPathes);
            for (String urlPath : urlPaths)
                context.urlFilterMapping(filterName, urlPath, DispatcherType.REQUEST);
        }
        return this;
    }

    public <T extends Filter> WebappBuilder registerFilter(final String urlPath,
                                                           final Class<T> filterClass,
                                                           final Map<String, String> initParams) {
        registerFilter(urlPath, filterClass, null, initParams);
        return this;
    }

    public <T extends Filter> WebappBuilder registerFilter(final String urlPath, final Class<T> filterClass) {
        registerFilter(urlPath, filterClass, null);
        return this;
    }

    public WebappBuilder registerSecurePaths(final String... securePaths) {
        context.addSecurityConstraint(Servlets.securityConstraint()
                .setEmptyRoleSemantic(SecurityInfo.EmptyRoleSemantic.AUTHENTICATE)
                .addWebResourceCollection(Servlets.webResourceCollection().addUrlPatterns(securePaths)));
        return this;
    }


    private ServletInfo addSwaggerContext(String urlPath, final ServletInfo servletInfo) {
        urlPath = StringUtils.removeEnd(urlPath, "*");
        urlPath = StringUtils.removeEnd(urlPath, "/");
        return servletInfo.addInitParam("swagger.api.basepath", urlPath);
    }

    public WebappBuilder registerJaxRsAppServlet(final String urlPath, final Class<? extends Application> appClass) {
        context.jaxrs(ServletContainer.class.getName() + '@' + urlPath, appClass,
                servletInfo -> {
                    servletInfo.addMapping(urlPath).setLoadOnStartup(1);
                    addSwaggerContext(urlPath, servletInfo);
                });
        return this;
    }

    public WebappBuilder registerJaxRsClassServlet(final String urlPath, final Class<?>... classes) {
        final ApplicationBuilder appBuilder = new ApplicationBuilder(urlPath);
        appBuilder.classes(classes);
        registerJaxRsResources(appBuilder, true, true);
        return this;
    }

    public WebappBuilder registerJaxRsResources(final ApplicationBuilder applicationBuilder,
                                                boolean withSwagger,
                                                boolean withRoleFeature) {
        applicationBuilder.classes(JACKSON_CLASSES);
        if (withRoleFeature)
            applicationBuilder.classes(RolesAllowedDynamicFeature.class);
        if (withSwagger)
            applicationBuilder.classes(SWAGGER_CLASSES);
        context.jaxrs(null, applicationBuilder, servletInfo -> {
            final Collection<String> paths = applicationBuilder.getApplicationPaths();
            if (paths != null && !paths.isEmpty() && withSwagger)
                addSwaggerContext(paths.iterator().next(), servletInfo);
        });
        return this;
    }

    public WebappBuilder registerJaxRsResources(final ApplicationBuilder applicationBuilder) {
        return registerJaxRsResources(applicationBuilder, false, false);
    }

}
