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
import com.qwazr.utils.StringUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    public final static Map<String, String[]> TYPES_CAMEL_KEYWORDS;
    public final static SortedMap<String, URI> TYPES_URI_DOC;
    public final static Map<String, Factory> TYPES_FACTORY;

    static {
        final JsonSubTypes types = QueryInterface.class.getAnnotation(JsonSubTypes.class);
        final Map<String, String[]> typeCamelKeywordsMap = new HashMap<>();
        final Map<String, Factory> typeFactoryMap = new HashMap<>();
        final SortedMap<String, URI> typeUriDocMap = new TreeMap<>();
        for (
            final JsonSubTypes.Type type : types.value()) {
            final String typeName = type.value().getSimpleName();
            final Class<?> typeClass = type.value();
            if (!QueryInterface.class.isAssignableFrom(typeClass))
                continue;
            final Class<QueryInterface> typeQueryClass = (Class<QueryInterface>) typeClass;
            if (findSampleConstructor(typeQueryClass,
                u -> typeUriDocMap.put(typeName, u),
                f -> typeFactoryMap.put(typeName, f))
                || findSampleFactory(typeQueryClass,
                u -> typeUriDocMap.put(typeName, u),
                f -> typeFactoryMap.put(typeName, f))) {
                typeCamelKeywordsMap.put(typeName, lowerCaseCamelCaseArray(typeName));
            }
        }
        TYPES_URI_DOC = Collections.unmodifiableSortedMap(typeUriDocMap);
        TYPES_FACTORY = Collections.unmodifiableMap(typeFactoryMap);
        TYPES_CAMEL_KEYWORDS = Collections.unmodifiableMap(typeCamelKeywordsMap);
    }

    static private String[] lowerCaseCamelCaseArray(final String text) {
        final String[] camelCaseKeywords = StringUtils.splitByCharacterTypeCamelCase(text);
        final List<String> keywords = new ArrayList<>();
        for (String camelCaseKeyword : camelCaseKeywords)
            keywords.add(camelCaseKeyword.toLowerCase());
        return keywords.toArray(new String[0]);
    }

    static private boolean findSampleConstructor(final Class<QueryInterface> typeClass,
                                                 final Consumer<URI> uriDoc,
                                                 final Consumer<Factory> factoryConsumer) {
        try {
            final Constructor<QueryInterface> constructor = typeClass.getConstructor(IndexSettingsDefinition.class, Map.class, Map.class);
            final QuerySampleCreator querySampleCreator = constructor.getAnnotation(QuerySampleCreator.class);
            if (querySampleCreator == null)
                return false;
            uriDoc.accept(URI.create(querySampleCreator.docUri()));
            factoryConsumer.accept(((settings, analyzers, fields) -> {
                try {
                    return constructor.newInstance(settings, analyzers, fields);
                } catch (ReflectiveOperationException e) {
                    throw new InternalServerErrorException("Cannot create a sample of the query " + typeClass.getSimpleName(), e);
                }
            }));
            return true;
        } catch (NoSuchMethodException e) {
            //TODO ok
            return false;
        }
    }

    static private boolean findSampleFactory(final Class<QueryInterface> typeClass,
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
            return true;
        }
        return false;
    }
}
