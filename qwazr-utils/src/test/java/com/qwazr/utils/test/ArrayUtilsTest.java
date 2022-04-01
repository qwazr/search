/*
 * Copyright 2016-2018 Emmanuel Keller / QWAZR
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
package com.qwazr.utils.test;

import com.qwazr.utils.ArrayUtils;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

public class ArrayUtilsTest {

	@Test
	public void toStringArrayTest() {
		Collection<String> stringCollection = Arrays.asList(RandomUtils.alphanumeric(10), RandomUtils.alphanumeric(10));
		String[] stringArray = ArrayUtils.toArray(stringCollection);
		Assert.assertTrue(CollectionsUtils.equals(stringCollection, stringArray));
	}

	@Test
	public void toObjectArrayTest() {
		Collection<Object> objectCollection = Arrays.asList(RandomUtils.alphanumeric(10), RandomUtils.alphanumeric(10));
		String[] stringArray = ArrayUtils.toStringArray(objectCollection);
		Assert.assertTrue(CollectionsUtils.equals(objectCollection, stringArray));
	}

	@Test
	public void toPrimitiveShort() {
		Collection<Short> list = Arrays.asList(RandomUtils.nextShort(), RandomUtils.nextShort());
		short[] array = ArrayUtils.toPrimitiveShort(list);
		int i = 0;
		for (short value : list)
			Assert.assertEquals(value, array[i++], 0);
	}

	@Test
	public void toPrimitiveInteger() {
		Collection<Integer> list = Arrays.asList(RandomUtils.nextInt(), RandomUtils.nextInt());
		int[] array = ArrayUtils.toPrimitiveInt(list);
		int i = 0;
		for (int value : list)
			Assert.assertEquals(value, array[i++], 0);
	}

	@Test
	public void toPrimitiveLong() {
		Collection<Long> list = Arrays.asList(RandomUtils.nextLong(), RandomUtils.nextLong());
		long[] array = ArrayUtils.toPrimitiveLong(list);
		int i = 0;
		for (long value : list)
			Assert.assertEquals(value, array[i++], 0);
	}

	@Test
	public void toPrimitiveDouble() {
		Collection<Double> list = Arrays.asList(RandomUtils.nextDouble(), RandomUtils.nextDouble());
		double[] array = ArrayUtils.toPrimitiveDouble(list);
		int i = 0;
		for (double value : list)
			Assert.assertEquals(value, array[i++], 0);
	}

	@Test
	public void toPrimitiveFloat() {
		Collection<Float> list = Arrays.asList(RandomUtils.nextFloat(), RandomUtils.nextFloat());
		float[] array = ArrayUtils.toPrimitiveFloat(list);
		int i = 0;
		for (float value : list)
			Assert.assertEquals(value, array[i++], 0);
	}

	@Test
	public void toPrimitiveByte() {
		Collection<Byte> list = Arrays.asList(RandomUtils.nextByte(), RandomUtils.nextByte());
		byte[] array = ArrayUtils.toPrimitiveByte(list);
		int i = 0;
		for (byte value : list)
			Assert.assertEquals(value, array[i++], 0);
	}

	@Test
	public void toPrimitiveBoolean() {
		Collection<Boolean> list = Arrays.asList(RandomUtils.nextBoolean(), RandomUtils.nextBoolean());
		boolean[] array = ArrayUtils.toPrimitiveBoolean(list);
		int i = 0;
		for (boolean value : list)
			Assert.assertEquals(value, array[i++]);
	}

	@Test
	public void toPrimitiveChar() {
		Collection<Character> list =
				Arrays.asList(RandomUtils.nextAlphanumericChar(), RandomUtils.nextAlphanumericChar());
		char[] array = ArrayUtils.toPrimitiveChar(list);
		int i = 0;
		for (char value : list)
			Assert.assertEquals(value, array[i++], 0);
	}
}
