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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.util.Attribute;

import java.io.IOException;

public interface TermConsumer {

	boolean token() throws IOException;

	void forEachToken() throws IOException;

	static <T extends Attribute> T getAttribute(final TokenStream tokenStream, final Class<T> attributeClass) {
		return tokenStream.hasAttribute(attributeClass) ? tokenStream.getAttribute(attributeClass) : null;
	}

	abstract class TermConsumerAbstract implements TermConsumer {

		protected final TokenStream tokenStream;

		public TermConsumerAbstract(final TokenStream tokenStream) {
			this.tokenStream = tokenStream;
		}

		@Override
		public void forEachToken() throws IOException {
			tokenStream.reset();
			while (tokenStream.incrementToken())
				if (!token())
					break;
		}
	}

	abstract class WithChar extends TermConsumerAbstract {

		protected final CharTermAttribute charTermAttr;

		public WithChar(final TokenStream tokenStream) {
			super(tokenStream);
			charTermAttr = getAttribute(tokenStream, CharTermAttribute.class);
		}
	}

	abstract class WithCharPositionIncrement extends TermConsumerAbstract {

		protected final CharTermAttribute charTermAttr;
		protected final PositionIncrementAttribute posIncAttr;

		public WithCharPositionIncrement(final TokenStream tokenStream) {
			super(tokenStream);
			charTermAttr = getAttribute(tokenStream, CharTermAttribute.class);
			posIncAttr = getAttribute(tokenStream, PositionIncrementAttribute.class);
		}
	}

	abstract class AllAttributes extends WithChar {

		protected final FlagsAttribute flagsAttr;
		protected final OffsetAttribute offsetAttr;
		protected final PositionIncrementAttribute posIncAttr;
		protected final PositionLengthAttribute posLengthAttr;
		protected final TypeAttribute typeAttr;
		protected final KeywordAttribute keywordAttr;

		public AllAttributes(final TokenStream tokenStream) {
			super(tokenStream);
			flagsAttr = getAttribute(tokenStream, FlagsAttribute.class);
			offsetAttr = getAttribute(tokenStream, OffsetAttribute.class);
			posIncAttr = getAttribute(tokenStream, PositionIncrementAttribute.class);
			posLengthAttr = getAttribute(tokenStream, PositionLengthAttribute.class);
			typeAttr = getAttribute(tokenStream, TypeAttribute.class);
			keywordAttr = getAttribute(tokenStream, KeywordAttribute.class);
		}
	}
}
