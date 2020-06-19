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

import com.qwazr.utils.ClassLoaderUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.util.AbstractAnalysisFactory;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.analysis.util.TokenizerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

final public class CustomAnalyzer extends Analyzer {

    private final Map<String, Integer> positionIncrementGap;
    private final Map<String, Integer> offsetGap;
    private final TokenizerFactory tokenizerFactory;
    private final List<TokenFilterFactory> tokenFilterFactories;

    public CustomAnalyzer(final ResourceLoader resourceLoader, final AnalyzerDefinition analyzerDefinition)
        throws IOException, ReflectiveOperationException {
        super(GLOBAL_REUSE_STRATEGY);

        final Map<String, Integer> defPositionIncrementGap = analyzerDefinition.getPositionIncrementGap();
        positionIncrementGap = defPositionIncrementGap == null || defPositionIncrementGap.isEmpty() ? null : new HashMap<>(defPositionIncrementGap);

        final Map<String, Integer> defOffsetGap = analyzerDefinition.getOffsetGap();
        offsetGap = defOffsetGap == null || defOffsetGap.isEmpty() ? null : new HashMap<>(defOffsetGap);

        tokenizerFactory = getFactory(resourceLoader, analyzerDefinition.getTokenizer(), KeywordTokenizerFactory.class);

        final List<LinkedHashMap<String, String>> defFilters = analyzerDefinition.getFilters();

        if (defFilters != null && !defFilters.isEmpty()) {
            tokenFilterFactories = new ArrayList<>(defFilters.size());
            for (final LinkedHashMap<String, String> filterDef : defFilters)
                tokenFilterFactories.add(getFactory(resourceLoader, filterDef, null));
        } else
            tokenFilterFactories = null;
    }

    @Override
    public int getPositionIncrementGap(final String fieldName) {
        if (positionIncrementGap == null)
            return 0;
        return positionIncrementGap.getOrDefault(fieldName, 0);
    }

    @Override
    public int getOffsetGap(final String fieldName) {
        if (offsetGap == null)
            return 1;
        return offsetGap.getOrDefault(fieldName, 1);
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        final Tokenizer source = tokenizerFactory.create();
        if (tokenFilterFactories == null)
            return new TokenStreamComponents(source);
        TokenStream result = source;
        for (TokenFilterFactory tokenFilterFactory : tokenFilterFactories)
            result = tokenFilterFactory.create(result);
        return new TokenStreamComponents(source, result);
    }

    private static <T extends AbstractAnalysisFactory> T getFactory(final ResourceLoader resourceLoader,
                                                                    Map<String, String> args,
                                                                    final Class<T> defaultClass)
        throws ReflectiveOperationException, IOException {
        final String clazz;
        if (args != null) {
            args = new LinkedHashMap<>(args);
            clazz = args.remove("class");
        } else
            clazz = null;
        final Class<T> factoryClass = clazz == null ? defaultClass : getFactoryClass(clazz);
        if (factoryClass == null)
            throw new ClassNotFoundException("No class found for: " + clazz);
        final T factory =
            factoryClass.getConstructor(Map.class).newInstance(args == null ? Collections.emptyMap() : args);
        if (factory instanceof ResourceLoaderAware)
            ((ResourceLoaderAware) factory).inform(resourceLoader);
        return factory;
    }

    private static <T extends AbstractAnalysisFactory> Class<T> getFactoryClass(String clazz)
        throws ClassNotFoundException {
        if (!clazz.endsWith("Factory"))
            clazz += "Factory";
        try {
            return ClassLoaderUtils.findClass(clazz);
        } catch (ClassNotFoundException e) {
            clazz = "org.apache.lucene.analysis." + clazz;
            return ClassLoaderUtils.findClass(clazz);
        }
    }

    final public static class Factory implements AnalyzerFactory {

        final public AnalyzerDefinition definition;

        public Factory(AnalyzerDefinition definition) {
            this.definition = definition;
        }

        @Override
        public Analyzer createAnalyzer(ResourceLoader resourceLoader) throws IOException, ReflectiveOperationException {
            return new CustomAnalyzer(resourceLoader, definition);
        }
    }

    public static LinkedHashMap<String, CustomAnalyzer.Factory> createFactoryMap(final Map<String, AnalyzerDefinition> definitionMap,
                                                                                 final Supplier<LinkedHashMap<String, Factory>> defaultMap) {
        if (definitionMap == null)
            return defaultMap == null ? null : defaultMap.get();
        final LinkedHashMap<String, CustomAnalyzer.Factory> factoryMap = new LinkedHashMap<>();
        definitionMap.forEach((name, def) -> factoryMap.put(name, new CustomAnalyzer.Factory(def)));
        return factoryMap;
    }

    public static LinkedHashMap<String, AnalyzerDefinition> createDefinitionMap(
        final LinkedHashMap<String, CustomAnalyzer.Factory> factoryMap) {
        if (factoryMap == null)
            return null;
        final LinkedHashMap<String, AnalyzerDefinition> definitionMap = new LinkedHashMap<>();
        factoryMap.forEach((name, factory) -> definitionMap.put(name, factory.definition));
        return definitionMap;
    }

}
