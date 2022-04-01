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

import org.junit.Assert;

public class ShortPrimitiveArraySetterTest extends AbstractMultipleSetterTest {

	short[] value;

	public ShortPrimitiveArraySetterTest() throws NoSuchFieldException {
		super(new ShortPrimitiveArraySetterImpl(ShortPrimitiveArraySetterTest.class.getDeclaredField("value")));
	}

	@Override
	final protected int size() {
		return value.length;
	}

	@Override
	protected String nextString() {
		return Short.toString(nextShort());
	}

	@Override
	protected Short nextObject() {
		return nextShort();
	}

	@Override
	protected void checkValueString(String... next) {
		Assert.assertEquals(next.length, value.length);
		int i = 0;
		for (String v : next)
			Assert.assertEquals(Short.parseShort(v), value[i++], 0);
	}

	@Override
	protected void checkValueNumber(Number... next) {
		Assert.assertEquals(next.length, value.length);
		int i = 0;
		for (Number v : next)
			Assert.assertEquals(v.shortValue(), value[i++], 0);
	}

	@Override
	protected void checkValueChar(Character... next) {
		Assert.assertEquals(next.length, value.length);
		int i = 0;
		for (Character v : next)
			Assert.assertEquals((short) v.charValue(), value[i++], 0);
	}

	@Override
	protected void checkValueBoolean(Boolean... next) {
		int i = 0;
		for (Boolean v : next)
			Assert.assertEquals(v ? 1 : 0, value[i++], 0);
	}

	@Override
	protected void checkValueNull() {
		Assert.assertNull(value);
	}

	@Override
	protected void checkValueObject(Object... values) {
		Assert.assertEquals(values.length, value.length);
		int i = 0;
		for (short v : value)
			Assert.assertEquals(v, (short) values[i++]);
	}

}
