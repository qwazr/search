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
package com.qwazr.search.test.units;

import com.qwazr.search.index.TermEnumDefinition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class TermEnumTest extends AbstractIndexTest {

	@BeforeClass
	public static void setup() throws IOException, InterruptedException {
		indexService.postDocument(new IndexRecord("1").textField("v"));
	}

	@Test
	public void testAll() throws IOException, InterruptedException {
		List<TermEnumDefinition> terms = indexService.doExtractTerms("textField", 0, 10);
		Assert.assertNotNull(terms);
		Assert.assertEquals(1, terms.size());
		Assert.assertEquals("v", terms.get(0).term);
	}

	@Test
	public void testExact() throws IOException, InterruptedException {
		List<TermEnumDefinition> terms = indexService.doExtractTerms("textField", "v", 0, 10);
		Assert.assertNotNull(terms);
		Assert.assertEquals(1, terms.size());
		Assert.assertEquals("v", terms.get(0).term);
	}

	@Test
	public void testCeil() throws IOException, InterruptedException {
		List<TermEnumDefinition> terms = indexService.doExtractTerms("textField", "a", 0, 10);
		Assert.assertNotNull(terms);
		Assert.assertEquals(1, terms.size());
		Assert.assertEquals("v", terms.get(0).term);
	}

	@Test
	public void testAfter() throws IOException, InterruptedException {
		List<TermEnumDefinition> terms = indexService.doExtractTerms("textField", "a", 1, 10);
		Assert.assertNotNull(terms);
		Assert.assertEquals(0, terms.size());
	}
}
