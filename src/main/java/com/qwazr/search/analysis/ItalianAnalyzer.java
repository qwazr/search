/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.analysis;

import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.it.ItalianLightStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ElisionFilter;

final public class ItalianAnalyzer extends Analyzer {

    private static final CharArraySet DEFAULT_ARTICLES = CharArraySet
	    .unmodifiableSet(new CharArraySet(Arrays.asList("c", "l", "all", "dall", "dell", "nell", "sull", "coll",
		    "pell", "gl", "agl", "dagl", "degl", "negl", "sugl", "un", "m", "t", "s", "v", "d"), true));

    public ItalianAnalyzer() {
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
	final Tokenizer source = new StandardTokenizer();
	TokenStream result = new StandardFilter(source);
	result = new ElisionFilter(result, DEFAULT_ARTICLES);
	result = new LowerCaseFilter(result);
	result = new ItalianLightStemFilter(result);
	return new TokenStreamComponents(source, result);
    }
}
