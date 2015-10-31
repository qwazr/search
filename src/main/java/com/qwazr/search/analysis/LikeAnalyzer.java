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
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.io.IOException;
import java.io.Reader;

final public class LikeAnalyzer extends Analyzer {

	private final static int MAX_TOKEN_LENGTH = 1024;

	protected TokenStreamComponents createComponents(String fieldName) {
		final StandardTokenizer tok = new StandardTokenizer();
		tok.setMaxTokenLength(MAX_TOKEN_LENGTH);
		TokenStream result = new StandardFilter((TokenStream) tok);
		result = new LowerCaseFilter(result);
		result = new ASCIIFoldingFilter(result, false);
		return new TokenStreamComponents(tok, result) {
			protected void setReader(Reader reader) throws IOException {
				tok.setMaxTokenLength(MAX_TOKEN_LENGTH);
				super.setReader(reader);
			}
		};
	}
}
