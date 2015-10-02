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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ar.ArabicNormalizationFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.fa.PersianNormalizationFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

final public class PersianAnalyzer extends Analyzer {

    public PersianAnalyzer() {
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
	final Tokenizer source = new StandardTokenizer();
	TokenStream result = new LowerCaseFilter(source);
	result = new ArabicNormalizationFilter(result);
	result = new PersianNormalizationFilter(result);
	return new TokenStreamComponents(source, result);
    }
}
