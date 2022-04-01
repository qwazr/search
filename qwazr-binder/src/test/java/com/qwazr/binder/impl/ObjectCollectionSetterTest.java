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

import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;

public class ObjectCollectionSetterTest extends AbstractCollectionSetterTest<Object> {

	public ObjectCollectionSetterTest() throws NoSuchFieldException {
		super(new ObjectCollectionSetterImpl(AbstractCollectionSetterTest.class.getDeclaredField("value")));
	}

	@Override
	protected String nextString() {
		return RandomUtils.alphanumeric(10);
	}

	@Override
	protected Object nextObject() {
		return new Object();
	}

	@Override
	protected void checkValueString(String... v) {
		Assert.assertTrue(CollectionsUtils.equals(value, v));
	}

	@Override
	protected void checkValueNumber(Number... v) {
		Assert.assertArrayEquals(v, value.toArray());
	}

	@Override
	protected void checkValueChar(Character... v) {
		Assert.assertArrayEquals(v, value.toArray());
	}

	@Override
	protected void checkValueBoolean(Boolean... v) {
		Assert.assertArrayEquals(v, value.toArray());
	}
}
