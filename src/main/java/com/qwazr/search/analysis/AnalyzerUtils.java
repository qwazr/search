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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.util.Attribute;

import java.io.IOException;
import java.util.Objects;

public class AnalyzerUtils {

	public interface TermConsumer {

		boolean apply(CharTermAttribute charTermAttr, FlagsAttribute flagsAttr, OffsetAttribute offsetAttr,
						PositionIncrementAttribute posIncAttr, PositionLengthAttribute posLengthAttr,
						TypeAttribute typeAttr, KeywordAttribute keywordAttr);
	}

	final static <T extends Attribute> T getAttribute(TokenStream tokenStream, Class<T> attributeClass) {
		return tokenStream.hasAttribute(attributeClass) ? tokenStream.getAttribute(attributeClass) : null;
	}

	final static public void forEachTerm(Analyzer analyzer, String field, String text, TermConsumer consumer)
					throws IOException {
		Objects.requireNonNull(analyzer, "The analyzer cannot be null");
		Objects.requireNonNull(field, "The field cannot be null");
		Objects.requireNonNull(text, "The text cannot be null");
		final TokenStream tokenStream = analyzer.tokenStream(field, text);
		try {
			final CharTermAttribute charTermAttr = getAttribute(tokenStream, CharTermAttribute.class);
			final FlagsAttribute flagsAttr = getAttribute(tokenStream, FlagsAttribute.class);
			final OffsetAttribute offsetAttr = getAttribute(tokenStream, OffsetAttribute.class);
			final PositionIncrementAttribute posIncAttr = getAttribute(tokenStream, PositionIncrementAttribute.class);
			final PositionLengthAttribute posLengthAttr = getAttribute(tokenStream, PositionLengthAttribute.class);
			final TypeAttribute typeAttr = getAttribute(tokenStream, TypeAttribute.class);
			final KeywordAttribute keywordAttr = getAttribute(tokenStream, KeywordAttribute.class);
			tokenStream.reset();
			while (tokenStream.incrementToken())
				if (!consumer.apply(charTermAttr, flagsAttr, offsetAttr, posIncAttr, posLengthAttr, typeAttr,
								keywordAttr))
					break;

		} finally {
			tokenStream.close();
		}
	}

}
