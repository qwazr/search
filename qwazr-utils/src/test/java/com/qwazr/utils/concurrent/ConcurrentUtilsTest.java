/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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

package com.qwazr.utils.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ConcurrentUtilsTest {

	private Integer functionEx(FunctionEx<String, Integer, NumberFormatException> funct, String value)
			throws NumberFormatException {
		return funct.apply(value);
	}

	@Test
	public void testFunction() {
		Assert.assertEquals(Integer.valueOf(10), functionEx(Integer::valueOf, "10"));
		Assert.assertEquals(Integer.valueOf(10), functionEx(Integer::valueOf, "10"));
	}

	@Test
	public void testFunctionError() {
		try {
			functionEx(Integer::valueOf, "sdflksdfkj");
			Assert.fail("No exception thrown: NumberFormatException");
		} catch (NumberFormatException e) {
			Assert.assertTrue(true);
		}
	}

	private void intConsumerEx(IntConsumerEx<NullPointerException> funct, int value) throws NullPointerException {
		funct.accept(value);
	}

	@Test
	public void testIntConsumer() {
		intConsumerEx(Integer::valueOf, 10);
		Assert.assertTrue(true);
	}

	private void consumerEx(ConsumerEx<String, NumberFormatException> funct, String value) throws NullPointerException {
		funct.accept(value);
	}

	@Test
	public void testConsumer() {
		consumerEx(Integer::valueOf, "10");
	}

	@Test
	public void testConsumerError() {
		try {
			consumerEx(Integer::valueOf, "sdflksdfkj");
			Assert.fail("No exception thrown: NumberFormatException");
		} catch (NumberFormatException e) {
			Assert.assertTrue(true);
		}
	}

	private void biConsumerEx(BiConsumerEx<String, Integer, NumberFormatException> funct, String value1, Integer value2)
			throws NumberFormatException {
		funct.accept(value1, value2);
	}

	@Test
	public void testBiConsumer() throws IllegalAccessException {
		biConsumerEx((v1, v2) -> {
			if (!Integer.toString(v2).equals(v1))
				throw new NumberFormatException("Parameters are not equal");
		}, "10", 10);
	}

	@Test
	public void testBiConsumerError() throws IllegalAccessException {
		try {
			biConsumerEx((v1, v2) -> {
				if (!Integer.toString(v2).equals(v1))
					throw new NumberFormatException("Parameters are not equal");
			}, "10", 9);
			Assert.fail("No exception thrown: NumberFormatException");
		} catch (NumberFormatException e) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testMapForEach() {
		Map<String, Integer> map = new HashMap<>();
		map.put("10", 10);
		map.put("9", 9);
		ConcurrentUtils.forEachEx(map, (v1, v2) -> {
			if (!Integer.toString(v2).equals(v1))
				throw new NumberFormatException("Parameters are not equal");
		});
	}

}
