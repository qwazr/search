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

import com.qwazr.binder.setter.FieldSetter;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractMapSetterTest<K, V> extends AbstractTest {

	protected AbstractMapSetterTest(FieldSetter setter) {
		super(setter);
	}

	@Test
	public void testNull() {
		setter.setValue(this, null);
		Assert.assertNull(getValue());
	}

	@Test
	public void testSetMap() {
		final Map map = getRandomMap();
		setter.setValue(this, map);
		Assert.assertTrue(CollectionsUtils.equals(map, getValue()));
	}

	protected Map<K, V> getRandomMap() {
		final Map<K, V> map = getNewMap();
		for (int i = 2; i < RandomUtils.nextInt(3, 10); i++)
			map.put(getRandomKey(), getRandomValue());
		return map;
	}

	<T> void testSetArray(Map<K, V> map, T[] array) {
		final AtomicInteger i = new AtomicInteger();
		map.forEach((k, v) -> {
			array[i.getAndIncrement()] = (T) k;
			array[i.getAndIncrement()] = (T) v;
		});
		setter.setValue(this, array);
		Assert.assertTrue(CollectionsUtils.equals(map, getValue()));
	}

	@Test
	public void testSetObjectArray() {
		final Map<K, V> map = getRandomMap();
		final Object[] objects = new Object[map.size() * 2];
		testSetArray(map, objects);
	}

	<T> void testSetCollection(Map<K, V> map, Collection<T> collection) {
		map.forEach((k, v) -> {
			collection.add((T) k);
			collection.add((T) v);
		});
		setter.setValue(this, collection);
		Assert.assertTrue(CollectionsUtils.equals(map, getValue()));
	}

	@Test
	public void testSetObjectCollection() {
		testSetCollection(getRandomMap(), new ArrayList<>());
	}

	protected abstract Map<K, V> getNewMap();

	protected abstract K getRandomKey();

	protected abstract V getRandomValue();

	protected abstract Map<K, V> getValue();

	@Test
	public void checkIsMapSetter() {
		Assert.assertEquals(MapSetterImpl.class, setter.getClass());
	}

	public static class StringString extends AbstractMapSetterTest<String, String> {

		final Map<String, String> value = null;

		public StringString() throws NoSuchFieldException {
			super(FieldSetter.of(StringString.class.getDeclaredField("value")));
		}

		@Override
		protected Map<String, String> getNewMap() {
			return new HashMap<>();
		}

		@Override
		protected String getRandomKey() {
			return RandomUtils.alphanumeric(5);
		}

		@Override
		protected String getRandomValue() {
			return RandomUtils.alphanumeric(5);
		}

		@Override
		protected Map<String, String> getValue() {
			return value;
		}

		@Test
		public void testSetStringArray() {
			final Map<String, String> map = getRandomMap();
			final String[] array = new String[map.size() * 2];
			testSetArray(map, array);
		}

		@Test
		public void testSetStringCollection() {
			testSetCollection(getRandomMap(), new ArrayList<String>());
		}
	}

	public static class StringInteger extends AbstractMapSetterTest<String, Integer> {

		final Map<String, Integer> value = null;

		public StringInteger() throws NoSuchFieldException {
			super(FieldSetter.of(StringInteger.class.getDeclaredField("value")));
		}

		@Override
		protected Map<String, Integer> getNewMap() {
			return new HashMap<>();
		}

		@Override
		protected String getRandomKey() {
			return RandomUtils.alphanumeric(5);
		}

		@Override
		protected Integer getRandomValue() {
			return nextInt();
		}

		@Override
		protected Map<String, Integer> getValue() {
			return value;
		}
		
	}
}
