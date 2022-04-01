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

public class BooleanPrimitiveArraySetterTest extends AbstractMultipleSetterTest {

	boolean[] value;

	public BooleanPrimitiveArraySetterTest() throws NoSuchFieldException {
		super(new BooleanPrimitiveArraySetterImpl(BooleanPrimitiveArraySetterTest.class.getDeclaredField("value")));
	}

	@Override
	final protected int size() {
		return value.length;
	}

	@Override
	protected String nextString() {
		return Boolean.toString(nextBoolean());
	}

	@Override
	protected Boolean nextObject() {
		return nextBoolean();
	}

	@Override
	protected void checkValueString(String... next) {
		Assert.assertEquals(next.length, value.length);
		int i = 0;
		for (String v : next)
			Assert.assertEquals(Boolean.parseBoolean(v), value[i++]);
	}

	@Override
	protected void checkValueNumber(Number... next) {
		Assert.assertEquals(next.length, value.length);
		int i = 0;
		for (Number v : next)
			Assert.assertEquals(v.doubleValue() != 0, value[i++]);
	}

	@Override
	protected void checkValueChar(Character... next) {
		Assert.assertEquals(next.length, value.length);
		int i = 0;
		for (Character v : next)
			Assert.assertEquals(v != 0, value[i++]);
	}

	@Override
	protected void checkValueBoolean(Boolean... next) {
		int i = 0;
		for (Boolean v : next)
			Assert.assertEquals(v, value[i++]);
	}

	@Override
	protected void checkValueObject(Object... values) {
		Assert.assertEquals(values.length, value.length);
		int i = 0;
		for (boolean v : value)
			Assert.assertEquals(v, (boolean) values[i++]);
	}

	@Override
	protected void checkValueNull() {
		Assert.assertNull(value);
	}

}
