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

import java.util.Collection;

public abstract class AbstractCollectionSetterTest<T> extends AbstractMultipleSetterTest {

	Collection<T> value;

	protected AbstractCollectionSetterTest(FieldSetterAbstract setter) {
		super(setter);
	}

	@Override
	final protected int size() {
		return value.size();
	}

	@Override
	final protected void checkValueNull() {
		Assert.assertNull(value);
	}

	@Override
	final protected void checkValueObject(Object... values) {
		Assert.assertEquals(values.length, value.size());
		int i = 0;
		for (Object v : value)
			Assert.assertEquals(v, values[i++]);
	}
	
}
