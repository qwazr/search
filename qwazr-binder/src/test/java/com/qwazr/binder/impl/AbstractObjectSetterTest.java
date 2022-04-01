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
import org.junit.Test;

import java.lang.reflect.Field;

public abstract class AbstractObjectSetterTest<T> extends AbstractSingleSetterTest {

	final protected T value = null;

	protected AbstractObjectSetterTest(FieldSetterAbstract setter) {
		super(setter);
	}

	static public Field getValueField() {
		try {
			return AbstractObjectSetterTest.class.getDeclaredField("value");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testNull() {
		setter.set(this, null);
		checkValueNull();
	}

	@Override
	final protected void checkValueNull() {
		Assert.assertNull(value);
	}

}
