/**
 * Copyright 2017 Emmanuel Keller / QWAZR
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
package com.qwazr.search.test.units;

import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.query.PhraseQuery;
import com.qwazr.utils.CharsetUtils;
import com.qwazr.utils.IOUtils;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.synonym.SolrSynonymParser;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.text.ParseException;

@FixMethodOrder
public class RealTimeSynonymsResourcesTest extends AbstractIndexTest {

	private final static String EN_FR_SYNONYMS = "hello world, bonjour le monde";
	private final static String EN_FR_DE_SYNONYMS = "hello world, bonjour le monde, hallo welt";

	@BeforeClass
	public static void setup() throws IOException, ParseException, InterruptedException, URISyntaxException {
		initIndexManager();
		indexManager.registerConstructorParameter(SynonymMap.class, getSynonymMap(EN_FR_SYNONYMS));
		initIndexService();
		indexService.postDocument(new IndexRecord("1").textSynonymsField("hello world"));
		IOUtils.toInputStream("test", CharsetUtils.CharsetUTF8);
		indexService.postTextResource("synonyms", EN_FR_SYNONYMS);
	}

	static SynonymMap getSynonymMap(String synonyms) throws IOException, ParseException {
		SolrSynonymParser parser = new SolrSynonymParser(true, true, new WhitespaceAnalyzer());
		parser.parse(new StringReader(synonyms));
		return parser.build();
	}

	@Test
	public void test001_check_en_fr() {
		Assert.assertEquals(Long.valueOf(1), indexService.searchQuery(
				QueryDefinition.of(new PhraseQuery("textSynonymsField", 1, "hello", "world")).build()).total_hits);
		Assert.assertEquals(Long.valueOf(0), indexService.searchQuery(
				QueryDefinition.of(new PhraseQuery("textSynonymsField", 1, "hallo", "welt")).build()).total_hits);
	}
}
