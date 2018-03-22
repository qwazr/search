/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

package com.qwazr.search.function;

import com.qwazr.search.index.QueryContext;
import com.qwazr.search.query.MatchAllDocsQuery;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.RandomUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.SortedNumericSelector;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ValueSourceTest {

	protected void checkValueSource(AbstractValueSource valueSource)
			throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {

		final AbstractValueSource valueSource2 =
				ObjectMappers.JSON.readValue(ObjectMappers.JSON.writeValueAsString(valueSource),
						AbstractValueSource.class);
		Assert.assertEquals(valueSource, valueSource2);
		Assert.assertNotNull(valueSource.getValueSource(QueryContext.DEFAULT));
		Assert.assertEquals(valueSource.getValueSource(QueryContext.DEFAULT),
				valueSource2.getValueSource(QueryContext.DEFAULT));
	}

	@Test
	public void test() throws IOException, ParseException, ReflectiveOperationException, QueryNodeException {
		checkValueSource(new ConstValueSource(RandomUtils.nextFloat(1f, 2f)));
		checkValueSource(new DefFunction(new ConstValueSource(1.0f), new NumDocsValueSource()));
		checkValueSource(new DivFloatFunction(new ConstValueSource(1.0f), new ConstValueSource(2.0f)));
		checkValueSource(new DoubleConstValueSource(2.0));
		checkValueSource(new DoubleFieldSource(RandomStringUtils.randomAlphanumeric(5)));
		checkValueSource(new FloatFieldSource(RandomStringUtils.randomAlphanumeric(5)));
		checkValueSource(
				new IfFunction(new ConstValueSource(1.0f), new NumDocsValueSource(), new DoubleConstValueSource(2.0)));
		checkValueSource(new IntFieldSource(RandomStringUtils.randomAlphanumeric(5)));
		checkValueSource(new MaxDocValueSource());
		checkValueSource(new MaxFloatFunction(new ConstValueSource(1.0f), new ConstValueSource(2.0f),
				new ConstValueSource(3.0f)));
		checkValueSource(new MinFloatFunction(new ConstValueSource(1.0f), new ConstValueSource(2.0f),
				new ConstValueSource(3.0f)));
		checkValueSource(new LongFieldSource(RandomStringUtils.randomAlphanumeric(5)));
		checkValueSource(new MultiValuedLongFieldSource("test", SortedNumericSelector.Type.MAX));
		checkValueSource(new MultiValuedDoubleFieldSource("test", SortedNumericSelector.Type.MIN));
		checkValueSource(new MultiValuedIntFieldSource("test", SortedNumericSelector.Type.MAX));
		checkValueSource(new MultiValuedFloatFieldSource("test", SortedNumericSelector.Type.MIN));
		checkValueSource(new NumDocsValueSource());
		checkValueSource(new PowFloatFunction(new ConstValueSource(2f), new ConstValueSource(4f)));
		checkValueSource(
				new ProductFloatFunction(new ConstValueSource(2f), new ConstValueSource(4f), new ConstValueSource(6f)));
		checkValueSource(new QueryValueSource(new MatchAllDocsQuery(), 3f));
		checkValueSource(new SortedSetFieldSource("test"));
		checkValueSource(
				new SumFloatFunction(new ConstValueSource(2f), new ConstValueSource(4f), new ConstValueSource(6f)));
	}
}
