/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.search.test.queries;

import com.qwazr.search.index.FacetDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ResultDefinition;
import com.qwazr.search.query.MatchAllDocsQuery;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class AssociatedFacetTest extends AbstractIndexTest {

	@BeforeClass
	public static void setup() throws IOException, InterruptedException {
		indexService.postDocument(new IndexRecord("1").intAssociatedFacet(111, "int1"));
		indexService.postDocument(new IndexRecord("2").intAssociatedFacet(222, "int2"));
		indexService.postDocument(new IndexRecord("3").intAssociatedFacet(333, "int3"));
		indexService.postDocument(new IndexRecord("4").floatAssociatedFacet(444f, "float4"));
		indexService.postDocument(new IndexRecord("5").floatAssociatedFacet(555f, "float5"));
		indexService.postDocument(new IndexRecord("6").floatAssociatedFacet(666f, "float6"));
	}

	private void checkIntFacets(ResultDefinition result) {
		Assert.assertNotNull(result.facets);
		Map<String, Number> facet = (Map<String, Number>) result.facets.get("intAssociatedFacet");
		Assert.assertNotNull(facet);
		Assert.assertTrue(facet.containsKey("int1"));
		Assert.assertTrue(facet.containsKey("int2"));
		Assert.assertTrue(facet.containsKey("int3"));
	}

	@Test
	public void intFacets() {
		ResultDefinition result = indexService.searchQuery(
				QueryDefinition.of(new MatchAllDocsQuery()).facet("intAssociatedFacet", new FacetDefinition()).build());
		Assert.assertNotNull(result);
		Assert.assertEquals(Long.valueOf(6), result.total_hits);
		checkIntFacets(result);
	}

	private void checkFloatFacets(ResultDefinition result) {
		Assert.assertNotNull(result.facets);
		Map<String, Number> facet = (Map<String, Number>) result.facets.get("floatAssociatedFacet");
		Assert.assertNotNull(facet);
		Assert.assertTrue(facet.containsKey("float4"));
		Assert.assertTrue(facet.containsKey("float5"));
		Assert.assertTrue(facet.containsKey("float6"));
	}

	@Test
	public void floatFacets() {
		ResultDefinition result = indexService.searchQuery(QueryDefinition.of(new MatchAllDocsQuery())
				.facet("floatAssociatedFacet", new FacetDefinition())
				.build());
		Assert.assertNotNull(result);
		Assert.assertEquals(Long.valueOf(6), result.total_hits);
		checkFloatFacets(result);
	}

	@Test
	public void AllFacets() {
		ResultDefinition result = indexService.searchQuery(QueryDefinition.of(new MatchAllDocsQuery())
				.facet("floatAssociatedFacet", new FacetDefinition())
				.facet("intAssociatedFacet", new FacetDefinition())
				.build());
		Assert.assertNotNull(result);
		Assert.assertEquals(Long.valueOf(6), result.total_hits);
		checkIntFacets(result);
		checkFloatFacets(result);
	}
}
