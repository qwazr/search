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

package com.qwazr.search.field;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.annotations.Copy;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.SmartField;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.search.query.MultiFieldQueryParser;
import com.qwazr.search.query.QueryParser;
import com.qwazr.search.query.QueryParserOperator;
import com.qwazr.search.query.StandardQueryParser;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.utils.LoggerUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

public class SmartFieldFullTextTest extends AbstractIndexTest {

	private final static Logger LOGGER = LoggerUtils.getLogger(SmartFieldFullTextTest.class);

	private static AnnotatedIndexService<Record> indexService;

	@BeforeClass
	public static void setup() throws IOException, URISyntaxException, InterruptedException {
		indexService = initIndexService(Record.class);
		indexService.postDocument(new Record(1, "First news", new String[] { "First sentence", "Second sentence" },
				new String[] { "tag1", "tag1and2" }));
		indexService.postDocument(new Record(2, "Second article", new String[] { "Third sentence", "Fourth sentence" },
				new String[] { "tag2", "tag1and2" }));
	}

	ResultDefinition.WithObject<Record> checkResult(AbstractQuery query, String queryExplain, long... expectedIds)
			throws IOException, ReflectiveOperationException {
		final ResultDefinition.WithObject<Record> result = indexService.searchQuery(QueryDefinition.of(query)
				.returnedField("*")
				.queryDebug(true)
				.build(), Record.class);
		if (queryExplain != null)
			Assert.assertEquals(queryExplain, result.query);
		else
			LOGGER.info(result.query);
		if (expectedIds == null)
			Assert.assertEquals(0, result.total_hits, 0);
		else
			Assert.assertEquals(expectedIds.length, result.total_hits, 0);
		return result;
	}

	void fullSearch(String queryString, String queryExplain, long... expectedIds)
			throws IOException, ReflectiveOperationException {
		checkResult(StandardQueryParser.of("full")
				.setDefaultOperator(QueryParserOperator.AND)
				.setQueryString(queryString)
				.build(), queryExplain, expectedIds);
		checkResult(QueryParser.of("full")
				.setDefaultOperator(QueryParserOperator.AND)
				.setQueryString(queryString)
				.build(), queryExplain, expectedIds);
	}

	ResultDefinition.WithObject<Record> multiSearch(String queryString, String queryExplain, long... expectedIds)
			throws IOException, ReflectiveOperationException {
		return checkResult(MultiFieldQueryParser.of().setDefaultOperator(QueryParserOperator.AND).addField("title",
				"content", "tags").setQueryString(queryString).build(), queryExplain, expectedIds);
	}

	@Test
	public void searchTitleTest() throws IOException, ReflectiveOperationException {
		multiSearch("first news",
				"+(tt€title:first tt€content:first tt€tags:first) +(tt€title:news tt€content:news tt€tags:news)", 1);
		fullSearch("first news", "+tt€full:first +tt€full:news", 1);
		multiSearch("second article",
				"+(tt€title:second tt€content:second tt€tags:second) +(tt€title:article tt€content:article tt€tags:article)",
				2);
		fullSearch("second article", "+tt€full:second +tt€full:article", 2);
	}

	@Test
	public void searchContentTest() throws IOException, ReflectiveOperationException {
		multiSearch("first sentence",
				"+(tt€title:first tt€content:first tt€tags:first) +(tt€title:sentence tt€content:sentence tt€tags:sentence)",
				1);
		fullSearch("first sentence", "+tt€full:first +tt€full:sentence", 1);
		multiSearch("third sentence",
				"+(tt€title:third tt€content:third tt€tags:third) +(tt€title:sentence tt€content:sentence tt€tags:sentence)",
				2);
		fullSearch("third sentence", "+tt€full:third +tt€full:sentence", 2);
	}

	@Test
	public void searchManyTest() throws IOException, ReflectiveOperationException {
		multiSearch("sentence", "tt€title:sentence tt€content:sentence tt€tags:sentence", 1, 2);
		fullSearch("sentence", "tt€full:sentence", 1, 2);
	}

	@Test
	public void searchCrossFields() throws IOException, ReflectiveOperationException {
		multiSearch("news sentence",
				"+(tt€title:news tt€content:news tt€tags:news) +(tt€title:sentence tt€content:sentence tt€tags:sentence)",
				1);
		fullSearch("news sentence", "+tt€full:news +tt€full:sentence", 1);
		multiSearch("article sentence",
				"+(tt€title:article tt€content:article tt€tags:article) +(tt€title:sentence tt€content:sentence tt€tags:sentence)",
				2);
		fullSearch("article sentence", "+tt€full:article +tt€full:sentence", 2);
	}

	@Index(name = "SmartFieldSorted", schema = "TestQueries")
	static public class Record {

		@SmartField(name = FieldDefinition.ID_FIELD, type = SmartFieldDefinition.Type.LONG, stored = true)
		final public long id;

		@SmartField(type = SmartFieldDefinition.Type.TEXT, index = true, analyzerClass = StandardAnalyzer.class)
		@Copy(to = { @Copy.To(order = 1, field = "full"), @Copy.To(order = 1, field = "autocomplete") })
		final public String title;

		@SmartField(type = SmartFieldDefinition.Type.TEXT, index = true, analyzerClass = StandardAnalyzer.class)
		@Copy(to = { @Copy.To(order = 2, field = "full") })
		final public String[] content;

		@SmartField(type = SmartFieldDefinition.Type.TEXT, index = true, analyzerClass = StandardAnalyzer.class)
		@Copy(to = { @Copy.To(order = 3, field = "full") })
		final public String[] tags;

		@SmartField(type = SmartFieldDefinition.Type.TEXT, index = true)
		final public String nonFullTextTitle;

		@SmartField(type = SmartFieldDefinition.Type.TEXT, index = true, analyzerClass = StandardAnalyzer.class)
		final public List<String> full;

		Record(long id, String title, String[] content, String[] tags) {
			this.id = id;
			this.title = this.nonFullTextTitle = title;
			this.content = content;
			this.tags = tags;
			this.full = null;
		}

		public Record() {
			this(0, null, null, null);
		}
	}
}
