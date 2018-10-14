/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

package com.qwazr.search.analyzer;

import com.qwazr.search.analysis.FirstTokenPayloadFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FirstTokenPayloadFilterTest {

    public static class Token {

        private String term;
        private int startOffset;
        private int endOffset;
        private int posIncrement;
        private int posLength;
        private BytesRef payload;

        Token(String term, int startOffset, int endOffset, int posI, int posL, BytesRef payload) {
            this.term = term;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            posIncrement = posI;
            posLength = posL;
            this.payload = payload;
        }

        @Override
        public String toString() {
            return "AnalyzedResult{" + "term='" + term + '\'' + ", startOffset=" + startOffset + ", endOffset=" +
                    endOffset + ", posIncrement=" + posIncrement + ", posLength=" + posLength + ", payload=" + payload +
                    '}';
        }

        public String term() {
            return term;
        }

        public int startOffset() {
            return startOffset;
        }

        public int endOffset() {
            return endOffset;
        }

        public BytesRef payload() {
            return payload;
        }

        public int payloadAsInt() {
            return NumericUtils.sortableBytesToInt(payload.bytes, 0);
        }

        public int positionIncrement() {
            return posIncrement;
        }

        public int positionLength() {
            return posLength;
        }
    }

    static List<Token> analyze(final String input, final Analyzer analyzer) throws IOException {
        final TokenStream ts = analyzer.tokenStream("dummy", new StringReader(input));

        final CharTermAttribute termAttr = ts.addAttribute(CharTermAttribute.class);
        final OffsetAttribute offsetAttr = ts.addAttribute(OffsetAttribute.class);
        final PositionIncrementAttribute posIncAttr = ts.addAttribute(PositionIncrementAttribute.class);
        final PositionLengthAttribute posLenAttr = ts.addAttribute(PositionLengthAttribute.class);
        final PayloadAttribute payloadAttr = ts.addAttribute(PayloadAttribute.class);

        final List<Token> results = new ArrayList<>();
        try {
            ts.reset();
            while (ts.incrementToken()) {
                results.add(new Token(termAttr.toString(), offsetAttr.startOffset(), offsetAttr.endOffset(),
                        posIncAttr.getPositionIncrement(), posLenAttr.getPositionLength(), payloadAttr.getPayload()));
            }
            ts.end();
        } finally {
            ts.close();
        }
        return results;
    }

    static class MyAnalyzer extends Analyzer {

        @Override
        protected TokenStreamComponents createComponents(final String s) {
            final StandardTokenizer tok = new StandardTokenizer();
            tok.setMaxTokenLength(1024);
            final FirstTokenPayloadFilter firstTokenPayloadFilter = new FirstTokenPayloadFilter(tok);
            TokenStream result = new StandardFilter(firstTokenPayloadFilter);
            result = firstTokenPayloadFilter.newSetterFilter(result);
            return new TokenStreamComponents(tok, result);
        }

    }

    @Test
    public void testOk() throws IOException {
        Analyzer analyzer = new MyAnalyzer();
        // 0 sony 4k
        // 0123456789
        List<Token> tokens = analyze("9 sony 4k", analyzer);
        assertThat(tokens.size()).isEqualTo(2);
        Token token = tokens.get(0);
        assertThat(token.term()).isEqualTo("sony");
        assertThat(token.startOffset()).isEqualTo(2);
        assertThat(token.payload().toString()).isEqualTo("[9]");
        //assertThat(token.payloadAsInt()).isEqualTo(9);
        token = tokens.get(1);
        assertThat(token.term()).isEqualTo("4k");
        assertThat(token.startOffset()).isEqualTo(7);
        assertThat(token.payload().toString()).isEqualTo("[9]");
    }

    @Test
    @Ignore
    public void testKo() throws IOException {
        Analyzer analyzer = new MyAnalyzer();
        // 0 sony 4k
        // 0123456789
        List<Token> tokens = analyze("42 sony 4k", analyzer);
        assertThat(tokens.size()).isEqualTo(2);
        Token token = tokens.get(0);
        assertThat(token.term()).isEqualTo("sony");
        assertThat(token.startOffset()).isEqualTo(2);
        assertThat(token.payload().toString()).isEqualTo("[9]");
        //assertThat(token.payloadAsInt()).isEqualTo(9);
        token = tokens.get(1);
        assertThat(token.term()).isEqualTo("4k");
        assertThat(token.startOffset()).isEqualTo(7);
        assertThat(token.payload().toString()).isEqualTo("[9]");
    }

}
