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

import com.qwazr.utils.RandomUtils;
import org.junit.Assert;

public class ShortCollectionSetterTest extends AbstractCollectionSetterTest<Short> {

	public ShortCollectionSetterTest() throws NoSuchFieldException {
		super(new ShortCollectionSetterImpl(AbstractCollectionSetterTest.class.getDeclaredField("value")));
	}

	@Override
	protected String nextString() {
		return Short.toString(RandomUtils.nextShort());
	}

	@Override
	protected Short nextObject() {
		return nextShort();
	}

	@Override
	protected void checkValueString(String... values) {
		Assert.assertEquals(values.length, value.size());
		int i = 0;
		for (Short v : value)
			Assert.assertEquals(v, Short.parseShort(values[i++]), 0);
	}

	@Override
	protected void checkValueNumber(Number... values) {
		Assert.assertEquals(values.length, value.size());
		int i = 0;
		for (Short v : value)
			Assert.assertEquals(v, values[i++].shortValue(), 0);
	}

	@Override
	protected void checkValueChar(Character... values) {
		Assert.assertEquals(values.length, value.size());
		int i = 0;
		for (Short v : value)
			Assert.assertEquals(v, values[i++], 0);
	}

	@Override
	protected void checkValueBoolean(Boolean... values) {
		Assert.assertEquals(values.length, value.size());
		int i = 0;
		for (Short v : value)
			Assert.assertEquals(v, values[i++] ? 1 : 0, 0);
	}
}
