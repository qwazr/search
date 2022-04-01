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

import com.qwazr.utils.ArrayUtils;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;

public class StringArraySetterTest extends AbstractArraySetterTest<String> {

	public StringArraySetterTest() throws NoSuchFieldException {
		super(new StringArraySetterImpl(AbstractArraySetterTest.class.getDeclaredField("value")));
	}

	@Override
	protected String nextString() {
		return RandomUtils.alphanumeric(10);
	}

	@Override
	protected String nextObject() {
		return nextString();
	}

	@Override
	protected void checkValueNull() {
		Assert.assertNull(value);
	}

	@Override
	protected void checkValueString(String... v) {
		Assert.assertArrayEquals(v, value);
	}

	@Override
	protected void checkValueNumber(Number... v) {
		Assert.assertArrayEquals(ArrayUtils.toStringArray(v), value);
	}

	@Override
	protected void checkValueChar(Character... v) {
		Assert.assertArrayEquals(ArrayUtils.toStringArray(v), value);
	}

	@Override
	protected void checkValueBoolean(Boolean... v) {
		Assert.assertArrayEquals(ArrayUtils.toStringArray(v), value);
	}

	@Override
	protected void checkValueObject(Object... values) {
		Assert.assertEquals(values.length, value.length);
		int i = 0;
		for (String v : value)
			Assert.assertEquals(v, values[i++].toString());
	}
}
