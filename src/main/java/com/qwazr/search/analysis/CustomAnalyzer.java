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
import org.apache.lucene.analysis.util.AbstractAnalysisFactory;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.analysis.util.TokenizerFactory;

import java.util.*;

final public class CustomAnalyzer extends Analyzer {

	private final TokenizerFactory tokenizerFactory;
	private final List<TokenFilterFactory> tokenFilterFactories;

	public CustomAnalyzer(AnalyzerDefinition analyzerDefinition) throws ReflectiveOperationException {
		super(GLOBAL_REUSE_STRATEGY);
		tokenizerFactory = getFactory(analyzerDefinition.tokenizer, KeywordTokenizerFactory.class);
		if (analyzerDefinition.filters != null && !analyzerDefinition.filters.isEmpty()) {
			tokenFilterFactories = new ArrayList<TokenFilterFactory>(analyzerDefinition.filters.size());
			for (LinkedHashMap<String, String> filterDef : analyzerDefinition.filters)
				tokenFilterFactories.add(getFactory(filterDef, null));
		} else
			tokenFilterFactories = null;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		final Tokenizer source = tokenizerFactory.create();
		if (tokenFilterFactories == null)
			return new TokenStreamComponents(source);
		TokenStream result = source;
		if (tokenFilterFactories != null)
			for (TokenFilterFactory tokenFilterFactory : tokenFilterFactories)
				result = tokenFilterFactory.create(result);
		return new TokenStreamComponents(source, result);
	}

	private final static <T extends AbstractAnalysisFactory> T getFactory(LinkedHashMap<String, String> args,
			Class<T> defaultClass) throws ReflectiveOperationException {
		final String clazz;
		if (args != null) {
			args = (LinkedHashMap<String, String>) args.clone();
			clazz = args.remove("class");
		} else
			clazz = null;
		final Class<T> factoryClass = clazz == null ? defaultClass : getFactoryClass(clazz);
		if (factoryClass == null)
			throw new ClassNotFoundException("No class found for: " + clazz);
		return factoryClass.getConstructor(Map.class).newInstance(args == null ? Collections.emptyMap() : args);
	}

	private final static <T extends AbstractAnalysisFactory> Class<T> getFactoryClass(String clazz)
			throws ClassNotFoundException {
		if (!clazz.endsWith("Factory"))
			clazz += "Factory";
		try {
			return ClassLoaderManager.findClass(clazz);
		} catch (ClassNotFoundException e) {
			clazz = "org.apache.lucene.analysis." + clazz;
			return ClassLoaderManager.findClass(clazz);
		}
	}
}
