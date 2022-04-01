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

public class BooleanSetterTest extends AbstractObjectSetterTest<Boolean> {

	public BooleanSetterTest() {
		super(new BooleanSetterImpl(getValueField()));

	}

	@Override
	protected String nextString() {
		return Boolean.toString(nextBoolean());
	}

	@Override
	protected void checkValueString(String next) {
		Assert.assertEquals(Boolean.parseBoolean(next), value);
	}

	@Override
	protected void checkValueNumber(Number next) {
		Assert.assertEquals(next.doubleValue() != 0, value);
	}

	@Override
	protected void checkValueChar(Character next) {
		Assert.assertEquals(next != 0, value);
	}

	@Override
	protected void checkValueBoolean(Boolean next) {
		Assert.assertEquals(next, value);
	}
}
