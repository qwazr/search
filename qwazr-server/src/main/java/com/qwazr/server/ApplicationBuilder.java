/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.AnnotationsUtils;
import com.qwazr.utils.StringUtils;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;

public class ApplicationBuilder {

    final Set<String> applicationPaths = new LinkedHashSet<>();

    final Set<Class<?>> classes = new LinkedHashSet<>();

    final Set<Object> singletons = new LinkedHashSet<>();

    final Map<String, Object> properties = new LinkedHashMap<>();

    private volatile ResourceConfig cache;

    public static ApplicationBuilder of(String... applicationPaths) {
        return new ApplicationBuilder(applicationPaths);
    }

    public ApplicationBuilder(String... applicationPaths) {
        if (applicationPaths != null)
            Collections.addAll(this.applicationPaths, applicationPaths);
    }

    public ApplicationBuilder(Collection<String> applicationPaths) {
        if (applicationPaths != null)
            this.applicationPaths.addAll(applicationPaths);
    }

    public Collection<String> getApplicationPaths() {
        return Collections.unmodifiableCollection(applicationPaths);
    }

    public ApplicationBuilder classes(Class<?>... classes) {
        if (classes != null)
            Collections.addAll(this.classes, classes);
        cache = null;
        return this;
    }

    public ApplicationBuilder classes(Collection<Class<?>> classes) {
        if (classes != null)
            this.classes.addAll(classes);
        cache = null;
        return this;
    }

    public ApplicationBuilder singletons(Object... singletons) {
        if (singletons != null)
            Collections.addAll(this.singletons, singletons);
        cache = null;
        return this;
    }

    public ApplicationBuilder singletons(Collection<?> singletons) {
        if (singletons != null)
            this.singletons.addAll(singletons);
        cache = null;
        return this;
    }

    public ApplicationBuilder properties(Map<String, ?> properties) {
        if (properties != null)
            this.properties.putAll(properties);
        cache = null;
        return this;
    }

    public ApplicationBuilder load(Class<?> resourcesType) {
        ServiceLoader.load(resourcesType).forEach(this::singletons);
        return this;
    }

    public ApplicationBuilder loadServices() {
        return load(ServiceInterface.class);
    }

    void apply(ResourceConfig resourceConfig) {
        resourceConfig.registerClasses(classes);
        resourceConfig.registerInstances(singletons);
        resourceConfig.setProperties(properties);
    }

    ResourceConfig build() {
        if (cache != null)
            return cache;
        final ResourceConfig resourceConfig = new ResourceConfig();
        apply(resourceConfig);
        return cache = resourceConfig;
    }

    public void forEachEndPoint(final Consumer<String> consumer) {
        Objects.requireNonNull(consumer, "The consumer is null");
        final Set<Class<?>> cls = new LinkedHashSet<>(classes);
        singletons.forEach(s -> cls.add(s.getClass()));
        for (String applicationPath : applicationPaths) {
            final String appPathPrefix = StringUtils.removeEnd(applicationPath, "*");
            for (Class<?> cl : cls) {
                final Path path = AnnotationsUtils.getFirstAnnotation(cl, Path.class);
                if (path != null && !path.value().isEmpty())
                    consumer.accept('/' + StringUtils.join(StringUtils.split(appPathPrefix + path.value(), '/'), '/'));
            }
        }
    }

}
