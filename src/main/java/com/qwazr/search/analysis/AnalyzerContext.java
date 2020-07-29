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
import com.qwazr.utils.Equalizer;
import com.qwazr.utils.LoggerUtils;
import com.qwazr.utils.StringUtils;
import com.qwazr.utils.concurrent.ReferenceCounter;
import com.qwazr.utils.reflection.ConstructorParametersImpl;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotAcceptableException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.util.ResourceLoader;

final public class AnalyzerContext extends Equalizer.Immutable<AnalyzerContext> implements Closeable {

    public static KeywordAnalyzer defaultKeywordAnalyzer = new KeywordAnalyzer();

    private final static Logger LOGGER = LoggerUtils.getLogger(AnalyzerContext.class);

    private final ConstructorParametersImpl instanceFactory;
    private final ReferenceCounter refCounter;

    private final Set<Analyzer> disposableAnalyzers;
    private final Object onTheFlyLock;

    private final Set<AnalyzerContext> activeAnalyzerContext;
    private final Map<String, Analyzer> perNameAnalyzers;
    private final Map<Class<? extends Analyzer>, Analyzer> perClassAnalyzers;
    private final Map<String, Analyzer> smartSetIndexAnalyzer;
    private final Map<String, Analyzer> smartSetQueryAnalyzer;
    private final Map<String, Analyzer> perFieldIndexAnalyzers;
    private final Analyzer perFieldQueryAnalyzers;

    public AnalyzerContext(final Set<AnalyzerContext> activeAnalyzerContext,
                           final ConstructorParametersImpl instanceFactory,
                           final ResourceLoader resourceLoader,
                           @NotNull final FieldMap fieldMap,
                           final Map<String, AnalyzerFactory> globalAnalyzerFactoryMap,
                           final Map<String, CustomAnalyzer.Factory> localAnalyzerFactoryMap,
                           @NotNull final Collection<String> errors) throws ServerException {
        super(AnalyzerContext.class);
        this.instanceFactory = instanceFactory;
        refCounter = new ReferenceCounter.Impl().acquire();

        disposableAnalyzers = new HashSet<>();

        onTheFlyLock = new Object();

        perClassAnalyzers = new HashMap<>();
        perNameAnalyzers = new HashMap<>();

        // Build named analyzers
        localAnalyzerFactoryMap.forEach(
            (name, factory) -> createFromFactory(name, resourceLoader, factory, errors,
                analyzer -> perNameAnalyzers.put(name, analyzer)));
        globalAnalyzerFactoryMap.forEach((name, factory) -> {
            if (!perNameAnalyzers.containsKey(name))
                createFromFactory(name, resourceLoader, factory, errors,
                    analyzer -> perNameAnalyzers.put(name, analyzer));
        });

        this.activeAnalyzerContext = activeAnalyzerContext;
        activeAnalyzerContext.add(this);

        // Build smart sets
        smartSetIndexAnalyzer = new HashMap<>();
        smartSetQueryAnalyzer = new HashMap<>();
        for (final SmartAnalyzerSet smartAnalyzer : SmartAnalyzerSet.values()) {
            createFromClass(smartAnalyzer.forIndex(), errors,
                analyzer -> smartSetIndexAnalyzer.put(smartAnalyzer.name(), analyzer));
            createFromClass(smartAnalyzer.forQuery(), errors,
                analyzer -> smartSetQueryAnalyzer.put(smartAnalyzer.name(), analyzer));
        }
        Map<String, Analyzer> perFieldIndexAnalyzers = new HashMap<>();
        final Map<String, Analyzer> perFieldQueryAnalyzers = new HashMap<>();

        fieldMap.forEach((fieldName, fieldType) -> {

            final String resolvedFieldName = fieldType.resolveFieldName(fieldName,
                FieldTypeInterface.FieldType.textField, FieldTypeInterface.ValueType.textType);
            if (resolvedFieldName == null)
                return;
            final FieldDefinition fieldDefinition = fieldType.getDefinition();


            // Load the index analyzer if any specific
            final String indexAnalyzer = fieldDefinition == null ? null : fieldDefinition.resolvedIndexAnalyzer();
            if (indexAnalyzer != null)
                resolveAnalyzer(indexAnalyzer, List.of(perNameAnalyzers, smartSetIndexAnalyzer), errors,
                    analyzer -> perFieldIndexAnalyzers.put(resolvedFieldName, analyzer));

            // Load the query analyzer if any specific
            final String queryAnalyzer = fieldDefinition == null ? null : fieldDefinition.resolvedQueryAnalyzer();
            if (queryAnalyzer != null)
                resolveAnalyzer(queryAnalyzer, List.of(perNameAnalyzers, smartSetQueryAnalyzer), errors,
                    analyzer -> perFieldQueryAnalyzers.put(resolvedFieldName, analyzer));
        });

        this.perFieldIndexAnalyzers = Map.copyOf(perFieldIndexAnalyzers);
        this.perFieldQueryAnalyzers = new PerFieldAnalyzerWrapper(defaultKeywordAnalyzer, perFieldQueryAnalyzers);
    }

    public AnalyzerContext acquire() {
        refCounter.acquire();
        return this;
    }

    final public Map<String, Analyzer> getIndexAnalyzers() {
        return perFieldIndexAnalyzers;
    }

    public Analyzer resolveQueryAnalyzer(final String analyzerName) {
        if (analyzerName == null || analyzerName.isEmpty())
            return perFieldQueryAnalyzers;
        Analyzer analyzer;

        analyzer = perNameAnalyzers.get(analyzerName);
        if (analyzer != null)
            return analyzer;
        analyzer = smartSetIndexAnalyzer.get(analyzerName);
        if (analyzer != null)
            return analyzer;

        synchronized (onTheFlyLock) {
            return fromClassName(analyzerName);
        }
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(perFieldIndexAnalyzers, perFieldQueryAnalyzers, perNameAnalyzers, smartSetIndexAnalyzer, smartSetQueryAnalyzer);
    }

    @Override
    protected boolean isEqual(final AnalyzerContext o) {
        return Objects.equals(perFieldIndexAnalyzers, o.perFieldIndexAnalyzers)
            && Objects.equals(perFieldQueryAnalyzers, o.perFieldQueryAnalyzers)
            && Objects.equals(perNameAnalyzers, o.perNameAnalyzers)
            && Objects.equals(smartSetIndexAnalyzer, o.smartSetIndexAnalyzer)
            && Objects.equals(smartSetQueryAnalyzer, o.smartSetQueryAnalyzer);
    }

    private final static String[] analyzerClassPrefixes = {StringUtils.EMPTY, "org.apache.lucene.analysis."};

    @NotNull
    private Analyzer fromClass(final Class<? extends Analyzer> analyzerClass) {
        Analyzer analyzer = perClassAnalyzers.get(analyzerClass);
        if (analyzer != null)
            return analyzer;
        try {
            analyzer = instanceFactory.findBestMatchingConstructor(analyzerClass).newInstance();
        } catch (ReflectiveOperationException e) {
            throw new NotAcceptableException("Cannot create an analyzer instance for : " + analyzerClass + " : " + e.getMessage(), e);
        }
        disposableAnalyzers.add(analyzer);
        perClassAnalyzers.put(analyzerClass, analyzer);
        return analyzer;
    }

    @NotNull
    private Analyzer fromClassName(final String analyzerName) {
        final Class<? extends Analyzer> analyzerClass;
        try {
            analyzerClass = ClassLoaderUtils.findClass(analyzerName, analyzerClassPrefixes);
        } catch (ClassNotFoundException e) {
            throw new NotAcceptableException("Cannot build the analyzer for: " + analyzerName + " : " + e.getMessage(), e);
        }
        return fromClass(analyzerClass);
    }

    private void createFromClass(final Class<? extends Analyzer> analyzerClass,
                                 final Collection<String> errors,
                                 final Consumer<Analyzer> consumer) {
        try {
            consumer.accept(fromClass(analyzerClass));
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            errors.add(e.getMessage());
        }
    }

    private void resolveAnalyzer(final String analyzerName,
                                 final List<Map<String, Analyzer>> analyzerMaps,
                                 final Collection<String> errors,
                                 final Consumer<Analyzer> consumer) {
        for (final Map<String, Analyzer> analyzerMap : analyzerMaps) {
            final Analyzer analyzer = analyzerMap.get(analyzerName);
            if (analyzer != null) {
                consumer.accept(analyzer);
                return;
            }
        }
        // Last chance, resolve class
        try {
            consumer.accept(fromClassName(analyzerName));
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            errors.add(e.getMessage());
        }
    }

    private void createFromFactory(final String name,
                                   final ResourceLoader resourceLoader,
                                   final AnalyzerFactory analyzerFactory,
                                   final Collection<String> errors,
                                   final Consumer<Analyzer> consumer) {
        try {
            final Analyzer analyzer = analyzerFactory.createAnalyzer(resourceLoader);
            disposableAnalyzers.add(analyzer);
            consumer.accept(analyzer);
        } catch (IOException | ReflectiveOperationException e) {
            final String msg = "Error on analyzer " + name + ": " + e.getMessage();
            LOGGER.log(Level.WARNING, msg, e);
            errors.add(msg);
        }
    }

    @Override
    public void close() {
        if (refCounter.release() > 0)
            return;
        disposableAnalyzers.forEach(Analyzer::close);
        disposableAnalyzers.clear();
        perFieldQueryAnalyzers.close();
        smartSetIndexAnalyzer.clear();
        smartSetQueryAnalyzer.clear();
        perNameAnalyzers.clear();
        activeAnalyzerContext.remove(this);
    }

}
