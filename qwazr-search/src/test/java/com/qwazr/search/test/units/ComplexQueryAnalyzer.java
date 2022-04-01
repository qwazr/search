/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.test.units;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class ComplexQueryAnalyzer extends Analyzer {

    public ComplexQueryAnalyzer() {
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final StandardTokenizer tokenizer = new StandardTokenizer();
        TokenStream result = new WordDelimiterGraphFilter(tokenizer,
                WordDelimiterGraphFilter.GENERATE_WORD_PARTS | WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS |
                        WordDelimiterGraphFilter.SPLIT_ON_NUMERICS | WordDelimiterGraphFilter.SPLIT_ON_CASE_CHANGE |
                        WordDelimiterGraphFilter.CATENATE_ALL | WordDelimiterGraphFilter.CATENATE_NUMBERS |
                        WordDelimiterGraphFilter.CATENATE_WORDS | WordDelimiterGraphFilter.PRESERVE_ORIGINAL,
                CharArraySet.EMPTY_SET);
        result = new EnglishPossessiveFilter(result);
        result = new LowerCaseFilter(result);
        result = new PorterStemFilter(result);
        final ShingleFilter shingleFilter = new ShingleFilter(result, 2, 3);
        shingleFilter.setTokenSeparator("");
        shingleFilter.setOutputUnigrams(true);
        result = shingleFilter;

        return new TokenStreamComponents(tokenizer, result);
    }
}
