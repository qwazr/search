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
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.ga.IrishLowerCaseFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ElisionFilter;
import org.tartarus.snowball.ext.IrishStemmer;

final public class IrishAnalyzer extends Analyzer {

    private static final CharArraySet DEFAULT_ARTICLES = CharArraySet
	    .unmodifiableSet(new CharArraySet(Arrays.asList("d", "m", "b"), true));

    /**
     * When StandardTokenizer splits t‑athair into {t, athair}, we don't want to
     * cause a position increment, otherwise there will be problems with phrase
     * queries versus tAthair (which would not have a gap).
     */
    private static final CharArraySet HYPHENATIONS = CharArraySet
	    .unmodifiableSet(new CharArraySet(Arrays.asList("h", "n", "t"), true));

    public IrishAnalyzer() {
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
	final Tokenizer source = new StandardTokenizer();
	TokenStream result = new StandardFilter(source);
	result = new StopFilter(result, HYPHENATIONS);
	result = new ElisionFilter(result, DEFAULT_ARTICLES);
	result = new IrishLowerCaseFilter(result);
	result = new SnowballFilter(result, new IrishStemmer());
	return new TokenStreamComponents(source, result);
    }
}
