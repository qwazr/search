/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.search.index;

import com.qwazr.search.analysis.TermConsumer;
import com.qwazr.utils.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.CharsRefBuilder;

import java.io.IOException;

public class SynonymMapBuilder {

    private final SynonymMap.Builder builder;
    private final Analyzer analyzer;
    private final Boolean bidirectional;

    public SynonymMapBuilder(final Analyzer analyzer, final boolean bidirectional, final boolean dedup) {
        this.analyzer = analyzer;
        this.builder = new SynonymMap.Builder(dedup);
        this.bidirectional = bidirectional;
    }

    public void add(final boolean keepOrig, final String... synonyms) throws IOException {
        if (synonyms == null || synonyms.length == 1)
            return;
        final CharsRef input = getCharsRef(synonyms[0]);
        for (int i = 1; i < synonyms.length; i++) {
            final CharsRef output = getCharsRef(synonyms[i]);
            builder.add(input, output, keepOrig);
            if (bidirectional)
                builder.add(output, input, keepOrig);
        }
    }

    private CharsRef getCharsRef(final String text) throws IOException {
        try (final TokenStream tokenStream = analyzer.tokenStream(StringUtils.EMPTY, text)) {
            final CharsRefTokensBuilder tokensBuilder = new CharsRefTokensBuilder(tokenStream);
            tokensBuilder.forEachToken();
            return tokensBuilder.charsRefBuilder.get();
        }
    }

    public SynonymMap build() throws IOException {
        return builder.build();
    }

    private static class CharsRefTokensBuilder extends TermConsumer.WithChar {

        private final CharsRefBuilder charsRefBuilder;
        private int upto;

        CharsRefTokensBuilder(final TokenStream tokenStream) {
            super(tokenStream);
            charsRefBuilder = new CharsRefBuilder();
            upto = 0;
        }

        @Override
        final public boolean token() {
            if (charTermAttr == null)
                return false;
            final int termLength = charTermAttr.length();
            final int needed = upto == 0 ? termLength : 1 + termLength;
            final int nextto = upto + needed;
            if (nextto > charsRefBuilder.length())
                charsRefBuilder.grow(nextto);
            if (upto > 0)
                charsRefBuilder.append(SynonymMap.WORD_SEPARATOR);
            charsRefBuilder.append(charTermAttr.buffer(), 0, termLength);
            upto = nextto;
            return true;
        }
    }

}
