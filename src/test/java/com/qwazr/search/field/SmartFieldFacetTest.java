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
import com.qwazr.search.index.FacetDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.search.query.DrillDownQuery;
import com.qwazr.search.query.FacetPathQuery;
import com.qwazr.search.query.MatchAllDocsQuery;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.utils.LoggerUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class SmartFieldFacetTest extends AbstractIndexTest {

	private final static Logger LOGGER = LoggerUtils.getLogger(SmartFieldFacetTest.class);

	private static AnnotatedIndexService<Record> indexService;

	@BeforeClass
	public static void setup() throws IOException, URISyntaxException, InterruptedException {
		indexService = initIndexService(Record.class);
		indexService.postDocument(new Record(1, new String[] { "tag1", "tag1and2" }));
		indexService.postDocument(new Record(2, new String[] { "tag2", "tag1and2" }));
	}

	ResultDefinition.WithObject<Record> checkResult(AbstractQuery query, String queryExplain, long... expectedIds)
			throws IOException, ReflectiveOperationException {
		final ResultDefinition.WithObject<Record> result = indexService.searchQuery(QueryDefinition.of(query)
				.returnedField("*")
				.queryDebug(true)
				.facet("tags", FacetDefinition.of().build())
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

	@Test
	public void drillDownQueryTest() throws IOException, ReflectiveOperationException {
		checkResult(new DrillDownQuery(new MatchAllDocsQuery(), false).add("tags", "tag1"),
				"+*:* #($facets$sdv:ft€tags\u001Ftag1)", 1);
		checkResult(new DrillDownQuery(null, false).add("tags", "tag2"), "#($facets$sdv:ft€tags\u001Ftag2)", 2);
		checkResult(new DrillDownQuery(new MatchAllDocsQuery(), false).add("tags", "tag1and2"),
				"+*:* #($facets$sdv:ft€tags\u001Ftag1and2)", 1, 2);
	}

	@Test
	public void facetPathQueryTest() throws IOException, ReflectiveOperationException {
		checkResult(new FacetPathQuery("tags", "tag1"), "$facets$sdv:ft€tags\u001Ftag1", 1);
		checkResult(new FacetPathQuery("tags", "tag1and2"), "$facets$sdv:ft€tags\u001Ftag1and2", 1, 2);
	}

	@Ignore
	@Test
	public void computeFacetsTest() throws IOException, ReflectiveOperationException {
		checkResult(new MatchAllDocsQuery(), "*:*", 1, 2);
	}

	@Index(name = "SmartFieldSorted", schema = "TestQueries")
	static public class Record {

		@SmartField(name = FieldDefinition.ID_FIELD, type = SmartFieldDefinition.Type.LONG, stored = true)
		final public long id;

		@SmartField(type = SmartFieldDefinition.Type.TEXT, facet = true)
		@Copy(to = { @Copy.To(order = 3, field = "full") })
		final public String[] tags;

		Record(long id, String[] tags) {
			this.id = id;
			this.tags = tags;
		}

		public Record() {
			this(0, null);
		}
	}
}
