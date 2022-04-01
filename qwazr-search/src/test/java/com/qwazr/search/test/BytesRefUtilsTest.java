/*
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
package com.qwazr.search.test;

import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.utils.RandomUtils;
import org.apache.lucene.util.BytesRef;
import org.junit.Assert;
import org.junit.Test;

public class BytesRefUtilsTest {

	private <T> void checkConverter(T value, BytesRefUtils.Converter<T> converter, boolean fromAny) {
		if (fromAny)
			Assert.assertEquals(value, converter.to(BytesRefUtils.fromAny(value)));
		Assert.assertEquals(value, converter.to(converter.from(value)));
		Assert.assertEquals(new BytesRef(), converter.from(null));
		Assert.assertNull(converter.to(null));
		Assert.assertNull(converter.to(new BytesRef()));
	}

	@Test
	public void doubleConverter() {
		checkConverter(RandomUtils.nextDouble(), new BytesRefUtils.DoubleConverter(), true);
		checkConverter(RandomUtils.nextDouble(), new BytesRefUtils.DoublePointConverter(), false);
	}

	@Test
	public void integerConverter() {
		checkConverter(RandomUtils.nextInt(), new BytesRefUtils.IntegerConverter(), true);
		checkConverter(RandomUtils.nextInt(), new BytesRefUtils.IntFacetConverter(), false);
		checkConverter(RandomUtils.nextInt(), new BytesRefUtils.IntPointConverter(), false);
	}

	@Test
	public void longConverter() {
		checkConverter(RandomUtils.nextLong(), new BytesRefUtils.LongConverter(), true);
		checkConverter(RandomUtils.nextLong(), new BytesRefUtils.LongPointConverter(), false);
	}

	@Test
	public void floatConverter() {
		checkConverter(RandomUtils.nextFloat(), new BytesRefUtils.FloatConverter(), true);
		checkConverter(RandomUtils.nextFloat(), new BytesRefUtils.FloatFacetConverter(), false);
		checkConverter(RandomUtils.nextFloat(), new BytesRefUtils.FloatPointConverter(), false);
	}

	@Test
	public void stringConverter() {
		checkConverter(RandomUtils.alphanumeric(10), new BytesRefUtils.StringConverter(), true);
	}

	@Test
	public void byteRefConverter() {
		checkConverter(new BytesRef(RandomUtils.alphanumeric(10)), new BytesRefUtils.BytesRefConverter(), true);
	}
}
