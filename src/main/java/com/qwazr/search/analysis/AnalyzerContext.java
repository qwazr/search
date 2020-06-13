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
package com.qwazr.search.analysis;

import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.index.FieldMap;
import com.qwazr.server.ServerException;
import com.qwazr.utils.ClassLoaderUtils;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.reflection.ConstructorParametersImpl;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.ResourceLoader;

public class AnalyzerContext {

    private final static Logger LOGGER = LoggerUtils.getLogger(AnalyzerContext.class);

    public final Map<String, Analyzer> indexAnalyzerMap;
    public final Map<String, Analyzer> queryAnalyzerMap;

    public AnalyzerContext(final ConstructorParametersImpl instanceFactory, final ResourceLoader resourceLoader,
                           final FieldMap fieldMap, final boolean failOnException,
                           final Map<String, AnalyzerFactory> globalAnalyzerFactoryMap,
                           final Map<String, CustomAnalyzer.Factory> localAnalyzerFactoryMap) throws ServerException {

        if (fieldMap == null || fieldMap.isEmpty()) {
            this.indexAnalyzerMap = Collections.emptyMap();
            this.queryAnalyzerMap = Collections.emptyMap();
            return;
        }

        this.indexAnalyzerMap = new HashMap<>();
        this.queryAnalyzerMap = new HashMap<>();

        final AnalyzerMapBuilder builder =
            new AnalyzerMapBuilder(instanceFactory, resourceLoader, globalAnalyzerFactoryMap,
                localAnalyzerFactoryMap);

        fieldMap.forEach((fieldName, fieldType) -> {
            try {
                final String queryFieldName = fieldType.resolveFieldName(fieldName, FieldTypeInterface.FieldType.textField, FieldTypeInterface.ValueType.textType);
                if (queryFieldName == null)
                    return;
                final FieldDefinition fieldDefinition = fieldType.getDefinition();

                // Load the index analyzer if any specific
                final String indexAnalyzer = StringUtils.isEmpty(fieldDefinition.indexAnalyzer) ? fieldDefinition.analyzer : fieldDefinition.indexAnalyzer;
                if (!StringUtils.isEmpty(indexAnalyzer)) {
                    final Analyzer analyzer = builder.findAnalyzer(indexAnalyzer, SmartAnalyzerSet::forIndex);
                    if (analyzer != null)
                        indexAnalyzerMap.put(queryFieldName, analyzer);
                }

                // Load the query analyzer if any specific
                final String queryAnalyzer = StringUtils.isEmpty(fieldDefinition.queryAnalyzer) ? fieldDefinition.analyzer : fieldDefinition.queryAnalyzer;
                if (!StringUtils.isEmpty(queryAnalyzer)) {
                    final Analyzer analyzer = builder.findAnalyzer(queryAnalyzer, SmartAnalyzerSet::forQuery);
                    if (analyzer != null)
                        queryAnalyzerMap.put(queryFieldName, analyzer);
                }

            } catch (ReflectiveOperationException | IOException e) {
                final String msg = "Analyzer class not known for the field " + fieldName;
                if (failOnException)
                    throw new ServerException(Response.Status.NOT_ACCEPTABLE, msg, e);
                LOGGER.log(Level.WARNING, msg, e);
            }
        });
    }

    @FunctionalInterface
    public interface Builder {

        void add(String fieldName, String analyzerName);
    }

    private final static String[] analyzerClassPrefixes = {StringUtils.EMPTY, "org.apache.lucene.analysis."};

    public final static class AnalyzerMapBuilder {

        private final ConstructorParametersImpl instanceFactory;
        private final ResourceLoader resourceLoader;
        private final Map<String, AnalyzerFactory> globalAnalyzerFactoryMap;
        private final Map<String, CustomAnalyzer.Factory> localAnalyzerFactoryMap;
        private final Map<String, Analyzer> analyzerSingletonMap;

        AnalyzerMapBuilder(final ConstructorParametersImpl instanceFactory, final ResourceLoader resourceLoader,
                           final Map<String, AnalyzerFactory> globalAnalyzerFactoryMap,
                           final Map<String, CustomAnalyzer.Factory> localAnalyzerFactoryMap) {
            this.instanceFactory = instanceFactory;
            this.resourceLoader = resourceLoader;
            this.globalAnalyzerFactoryMap = globalAnalyzerFactoryMap;
            this.localAnalyzerFactoryMap = localAnalyzerFactoryMap;
            this.analyzerSingletonMap = new HashMap<>();
        }

        public Analyzer findAnalyzer(final String analyzerName,
                                     final Function<SmartAnalyzerSet, Class<? extends Analyzer>> smartProvider) throws ReflectiveOperationException, IOException {
            // Get from existing definition
            Analyzer analyzer = analyzerSingletonMap.get(analyzerName);
            if (analyzer != null)
                return analyzer;
            // Get from user
            analyzer = getFromFactory(analyzerName);
            // Get from class
            if (analyzer == null) {
                final SmartAnalyzerSet smartAnalyzerSet = SmartAnalyzerSet.of(analyzerName);
                final Class<? extends Analyzer> analyzerClass;
                if (smartAnalyzerSet != null)
                    analyzerClass = smartProvider.apply(smartAnalyzerSet);
                else
                    analyzerClass = ClassLoaderUtils.findClass(analyzerName, analyzerClassPrefixes);
                analyzer = instanceFactory.findBestMatchingConstructor(analyzerClass).newInstance();
            }
            analyzerSingletonMap.put(analyzerName, analyzer);
            return analyzer;
        }

        private Analyzer getFromFactory(final String analyzerName,
                                        final Map<String, ? extends AnalyzerFactory> analyzerFactoryMap)
            throws IOException, ReflectiveOperationException {
            if (analyzerFactoryMap == null)
                return null;
            final AnalyzerFactory factory = analyzerFactoryMap.get(analyzerName);
            return factory == null ? null : factory.createAnalyzer(resourceLoader);
        }

        Analyzer getFromFactory(final String analyzerName) throws IOException, ReflectiveOperationException {
            if (localAnalyzerFactoryMap != null) {
                final Analyzer analyzer = getFromFactory(analyzerName, localAnalyzerFactoryMap);
                if (analyzer != null)
                    return analyzer;
            }
            if (globalAnalyzerFactoryMap != null) {
                final Analyzer analyzer = getFromFactory(analyzerName, globalAnalyzerFactoryMap);
                if (analyzer != null)
                    return analyzer;
            }
            return null;
        }
    }
}
