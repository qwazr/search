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
package com.qwazr.binder.impl;

import com.qwazr.binder.BinderException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class AbstractSingleSetterTest extends AbstractSetterTest {

	protected AbstractSingleSetterTest(FieldSetterAbstract setter) {
		super(setter);
	}

	protected abstract void checkValueString(String next);

	protected abstract void checkValueNumber(Number next);

	protected abstract void checkValueChar(Character next);

	protected abstract void checkValueBoolean(Boolean next);

	@Override
	final protected void checkValueEmpty() {
	}

	final protected void checkValueString(String... next) {
		Assert.assertNotNull(next);
		Assert.assertEquals(1, next.length);
		checkValueString(next[0]);
	}

	final protected void checkValueNumber(Number... next) {
		Assert.assertNotNull(next);
		Assert.assertEquals(1, next.length);
		checkValueNumber(next[0]);
	}

	final protected void checkValueChar(Character... next) {
		Assert.assertNotNull(next);
		Assert.assertEquals(1, next.length);
		checkValueChar(next[0]);
	}

	final protected void checkValueBoolean(Boolean... next) {
		Assert.assertNotNull(next);
		Assert.assertEquals(1, next.length);
		checkValueBoolean(next[0]);
	}

	@Test(expected = BinderException.class)
	final public void testStringCollectionOverflow() {
		final Collection<String> v = Arrays.asList(nextString(), nextString());
		setter.fromString(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testStringListOverflow() {
		final List<String> v = Arrays.asList(nextString(), nextString());
		setter.fromString(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testShortPrimitiveArrayOverflow() {
		final short[] v = new short[] { nextShort(), nextShort() };
		setter.fromShort(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testShortArrayOverflow() {
		final Short[] v = new Short[] { nextShort(), nextShort() };
		setter.fromShort(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testShortCollectionOverflow() {
		final Collection<Short> v = Arrays.asList(nextShort(), nextShort());
		setter.fromShort(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testShortListOverflow() {
		final List<Short> v = Arrays.asList(nextShort(), nextShort());
		setter.fromShort(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testLongPrimitiveArrayOverflow() {
		final long[] v = new long[] { nextLong(), nextLong() };
		setter.fromLong(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testLongArrayOverflow() {
		final Long[] v = new Long[] { nextLong(), nextLong() };
		setter.fromLong(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testLongCollectionOverflow() {
		final Collection<Long> v = Arrays.asList(nextLong(), nextLong());
		setter.fromLong(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testLongListOverflow() {
		final List<Long> v = Arrays.asList(nextLong(), nextLong());
		setter.fromLong(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testIntegerPrimitiveArrayOverflow() {
		final int[] v = new int[] { nextInt(), nextInt() };
		setter.fromInteger(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testIntegerArrayOverflow() {
		final Integer[] v = new Integer[] { nextInt(), nextInt() };
		setter.fromInteger(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testIntegerCollectionOverflow() {
		final Collection<Integer> v = Arrays.asList(nextInt(), nextInt());
		setter.fromInteger(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testIntegerListOverflow() {
		final List<Integer> v = Arrays.asList(nextInt(), nextInt());
		setter.fromInteger(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testFloatPrimitiveArrayOverflow() {
		final float[] v = new float[] { nextFloat(), nextFloat() };
		setter.fromFloat(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testFloatArrayOverflow() {
		final Float[] v = new Float[] { nextFloat(), nextFloat() };
		setter.fromFloat(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testFloatCollectionOverflow() {
		final Collection<Float> v = Arrays.asList(nextFloat(), nextFloat());
		setter.fromFloat(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testFloatListOverflow() {
		final List<Float> v = Arrays.asList(nextFloat(), nextFloat());
		setter.fromFloat(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testDoublePrimitiveArrayOverflow() {
		final double[] v = new double[] { nextDouble(), nextDouble() };
		setter.fromDouble(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testDoubleArrayOverflow() {
		final Double[] v = new Double[] { nextDouble(), nextDouble() };
		setter.fromDouble(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testDoubleCollectionOverflow() {
		final Collection<Double> v = Arrays.asList(nextDouble(), nextDouble());
		setter.fromDouble(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testDoubleListOverflow() {
		final List<Double> v = Arrays.asList(nextDouble(), nextDouble());
		setter.fromDouble(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testCharPrimitiveArrayOverflow() {
		final char[] v = new char[] { nextChar(), nextChar() };
		setter.fromChar(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testCharArrayOverflow() {
		final Character[] v = new Character[] { nextChar(), nextChar() };
		setter.fromChar(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testCharCollectionOverflow() {
		final Collection<Character> v = Arrays.asList(nextChar(), nextChar());
		setter.fromChar(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testCharListOverflow() {
		final List<Character> v = Arrays.asList(nextChar(), nextChar());
		setter.fromChar(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testBytePrimitiveArrayOverflow() {
		final byte[] v = new byte[] { nextByte(), nextByte() };
		setter.fromByte(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testByteArrayOverflow() {
		final Byte[] v = new Byte[] { nextByte(), nextByte() };
		setter.fromByte(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testByteCollectionOverflow() {
		final Collection<Byte> v = Arrays.asList(nextByte(), nextByte());
		setter.fromByte(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testByteListOverflow() {
		final List<Byte> v = Arrays.asList(nextByte(), nextByte());
		setter.fromByte(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testBooleanPrimitiveArrayOverflow() {
		final boolean[] v = new boolean[] { nextBoolean(), nextBoolean() };
		setter.fromBoolean(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testBooleanArrayOverflow() {
		final Boolean[] v = new Boolean[] { nextBoolean(), nextBoolean() };
		setter.fromBoolean(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testBooleanCollectionOverflow() {
		final Collection<Boolean> v = Arrays.asList(nextBoolean(), nextBoolean());
		setter.fromBoolean(v, this);
	}

	@Test(expected = BinderException.class)
	final public void testBooleanListOverflow() {
		final List<Boolean> v = Arrays.asList(nextBoolean(), nextBoolean());
		setter.fromBoolean(v, this);
	}
}
