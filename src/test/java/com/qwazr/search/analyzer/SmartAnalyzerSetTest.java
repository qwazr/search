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

package com.qwazr.search.analyzer;

import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.utils.RandomUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class SmartAnalyzerSetTest {

	private void checkAnalyzer(Class<? extends Analyzer> analyzerClass)
			throws IllegalAccessException, InstantiationException, IOException {
		Assert.assertNotNull(analyzerClass);
		Analyzer analyzer = analyzerClass.newInstance();
		Assert.assertNotNull(analyzer);
		Assert.assertEquals(100, analyzer.getPositionIncrementGap(""), 0);
		try (final TokenStream tokenStream = analyzer.tokenStream("", RandomUtils.alphanumeric(10))) {
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
			}
		}
	}

	@Test
	public void testAll() throws IllegalAccessException, InstantiationException, IOException {
		for (SmartAnalyzerSet smartAnalyzer : SmartAnalyzerSet.values()) {
			checkAnalyzer(smartAnalyzer.indexAnalyzer);
			checkAnalyzer(smartAnalyzer.queryAnalyzer);
		}

	}
}
