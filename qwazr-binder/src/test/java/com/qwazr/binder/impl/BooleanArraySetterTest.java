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

public class BooleanArraySetterTest extends AbstractArraySetterTest<Boolean> {

	public BooleanArraySetterTest() throws NoSuchFieldException {
		super(new BooleanArraySetterImpl(AbstractArraySetterTest.class.getDeclaredField("value")));
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
	protected void checkValueNull() {
		Assert.assertNull(value);
	}

	@Override
	protected void checkValueString(String... values) {
		Assert.assertEquals(values.length, size());
		int i = 0;
		for (String v : values)
			Assert.assertEquals(Boolean.parseBoolean(v), get(i++));
	}

	@Override
	protected void checkValueNumber(Number... values) {
		Assert.assertEquals(values.length, size());
		int i = 0;
		for (Number v : values)
			Assert.assertEquals(v.doubleValue() != 0, get(i++));
	}

	@Override
	protected void checkValueChar(Character... values) {
		Assert.assertEquals(values.length, size());
		int i = 0;
		for (Character v : values)
			Assert.assertEquals(v != 0, get(i++));
	}

	@Override
	protected void checkValueBoolean(Boolean... v) {
		Assert.assertArrayEquals(v, value);
	}
}
