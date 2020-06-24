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
package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.annotations.QuerySampleCreator;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexSettingsDefinition;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import javax.ws.rs.InternalServerErrorException;

public class QuerySampler {

    @FunctionalInterface
    public interface Factory {
        QueryInterface create(IndexSettingsDefinition settings,
                              Map<String, AnalyzerDefinition> analyzers,
                              Map<String, FieldDefinition> fields);
    }


    public final static SortedMap<String, URI> TYPES_URI_DOC;
    public final static SortedMap<String, Factory> TYPES_FACTORY;

    static {
        final JsonSubTypes types = QueryInterface.class.getAnnotation(JsonSubTypes.class);
        final SortedMap<String, Factory> typeFactoryMap = new TreeMap<>();
        final SortedMap<String, URI> typeUriDocMap = new TreeMap<>();
        for (
            final JsonSubTypes.Type type : types.value()) {
            final String typeName = type.value().getSimpleName();
            final Class<?> typeClass = type.value();
            if (!QueryInterface.class.isAssignableFrom(typeClass))
                continue;
            final Class<QueryInterface> typeQueryClass = (Class<QueryInterface>) typeClass;
            findSampleConstructor(typeQueryClass, u -> typeUriDocMap.put(typeName, u), f -> typeFactoryMap.put(typeName, f));
            findSampleFactory(typeQueryClass, u -> typeUriDocMap.put(typeName, u), f -> typeFactoryMap.put(typeName, f));
        }
        TYPES_URI_DOC = Collections.unmodifiableSortedMap(typeUriDocMap);
        TYPES_FACTORY = Collections.unmodifiableSortedMap(typeFactoryMap);
    }

    static private void findSampleConstructor(final Class<QueryInterface> typeClass,
                                              final Consumer<URI> uriDoc,
                                              final Consumer<Factory> factoryConsumer) {
        try {
            final Constructor<QueryInterface> constructor = typeClass.getConstructor(IndexSettingsDefinition.class, Map.class, Map.class);
            final QuerySampleCreator querySampleCreator = constructor.getAnnotation(QuerySampleCreator.class);
            if (querySampleCreator == null)
                return;
            uriDoc.accept(URI.create(querySampleCreator.docUri()));
            factoryConsumer.accept(((settings, analyzers, fields) -> {
                try {
                    return constructor.newInstance(settings, analyzers, fields);
                } catch (ReflectiveOperationException e) {
                    throw new InternalServerErrorException("Cannot create a sample of the query " + typeClass.getSimpleName(), e);
                }
            }));
        } catch (NoSuchMethodException e) {
            //TODO ok
        }
    }

    static private void findSampleFactory(final Class<QueryInterface> typeClass,
                                          final Consumer<URI> uriDoc,
                                          final Consumer<Factory> factoryConsumer) {
        for (final Method method : typeClass.getMethods()) {
            final QuerySampleCreator querySampleCreator = method.getAnnotation(QuerySampleCreator.class);
            if (querySampleCreator == null)
                continue;
            uriDoc.accept(URI.create(querySampleCreator.docUri()));
            factoryConsumer.accept(((settings, analyzers, fields) -> {
                try {
                    return (QueryInterface) method.invoke(null, settings, analyzers, fields);
                } catch (ReflectiveOperationException e) {
                    throw new InternalServerErrorException("Cannot create a sample of the query " + typeClass.getSimpleName(), e);
                }
            }));
        }
    }
}
