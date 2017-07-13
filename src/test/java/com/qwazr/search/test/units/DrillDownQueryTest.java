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

import com.qwazr.search.index.QueryContext;
import com.qwazr.search.query.DrillDownQuery;
import com.qwazr.search.query.MatchAllDocsQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class DrillDownQueryTest extends AbstractIndexTest.WithIndexRecord {

	@BeforeClass
	public static void setup() throws IOException, InterruptedException, URISyntaxException {
		initIndexService();
	}

	@Test
	public void luceneQuery() throws ReflectiveOperationException, QueryNodeException, ParseException, IOException {
		Query luceneQuery = new DrillDownQuery(new MatchAllDocsQuery(), true).add("dim", "value").getQuery(
				QueryContext.DEFAULT);
		Assert.assertNotNull(luceneQuery);
	}

}