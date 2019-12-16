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
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;

public class SynonymsResourceAnalyzer extends Analyzer {

    private final SynonymMap synonymMap;

    public SynonymsResourceAnalyzer() {
        //TODO add support of synonyms loaded by resources
        synonymMap = null;
    }

    public SynonymsResourceAnalyzer(final SynonymMap synonymMap) {
        this.synonymMap = synonymMap;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer tokenizer = new WhitespaceTokenizer();
        final TokenFilter input = new SynonymGraphFilter(tokenizer, synonymMap, true);

        return new TokenStreamComponents(tokenizer, input);
    }
}
