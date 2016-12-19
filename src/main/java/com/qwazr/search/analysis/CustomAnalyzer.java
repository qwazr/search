/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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

import com.qwazr.classloader.ClassLoaderManager;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.util.*;

import java.io.IOException;
import java.util.*;

final public class CustomAnalyzer extends Analyzer {

	private final Map<String, Integer> positionIncrementGap;
	private final Map<String, Integer> offsetGap;
	private final TokenizerFactory tokenizerFactory;
	private final List<TokenFilterFactory> tokenFilterFactories;

	public CustomAnalyzer(final ClassLoaderManager classLoaderManager, final ResourceLoader resourceLoader,
			final AnalyzerDefinition analyzerDefinition) throws ReflectiveOperationException, IOException {
		super(GLOBAL_REUSE_STRATEGY);
		positionIncrementGap = analyzerDefinition.position_increment_gap == null ?
				null :
				new HashMap<>(analyzerDefinition.position_increment_gap);
		offsetGap = analyzerDefinition.offset_gap == null ? null : new HashMap<>(analyzerDefinition.offset_gap);
		tokenizerFactory = getFactory(classLoaderManager, resourceLoader, analyzerDefinition.tokenizer,
				KeywordTokenizerFactory.class);
		if (analyzerDefinition.filters != null && !analyzerDefinition.filters.isEmpty()) {
			tokenFilterFactories = new ArrayList<>(analyzerDefinition.filters.size());
			for (LinkedHashMap<String, String> filterDef : analyzerDefinition.filters)
				tokenFilterFactories.add(getFactory(classLoaderManager, resourceLoader, filterDef, null));
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

	private static <T extends AbstractAnalysisFactory> T getFactory(final ClassLoaderManager classLoaderManager,
			final ResourceLoader resourceLoader, LinkedHashMap<String, String> args, final Class<T> defaultClass)
			throws ReflectiveOperationException, IOException {
		final String clazz;
		if (args != null) {
			args = (LinkedHashMap<String, String>) args.clone();
			clazz = args.remove("class");
		} else
			clazz = null;
		final Class<T> factoryClass = clazz == null ? defaultClass : getFactoryClass(classLoaderManager, clazz);
		if (factoryClass == null)
			throw new ClassNotFoundException("No class found for: " + clazz);
		final T factory =
				factoryClass.getConstructor(Map.class).newInstance(args == null ? Collections.emptyMap() : args);
		if (factory instanceof ResourceLoaderAware)
			((ResourceLoaderAware) factory).inform(resourceLoader);
		return factory;
	}

	private static <T extends AbstractAnalysisFactory> Class<T> getFactoryClass(
			final ClassLoaderManager classLoaderManager, String clazz) throws ClassNotFoundException {
		if (!clazz.endsWith("Factory"))
			clazz += "Factory";
		try {
			return classLoaderManager.findClass(clazz);
		} catch (ClassNotFoundException e) {
			clazz = "org.apache.lucene.analysis." + clazz;
			return classLoaderManager.findClass(clazz);
		}
	}
}
