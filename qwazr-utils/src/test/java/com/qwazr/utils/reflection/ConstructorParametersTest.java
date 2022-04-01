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
package com.qwazr.utils.reflection;

import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConstructorParametersTest {

	@Test
	public void parameterMapTest() {
		final Collection<?> parameters = Arrays.asList("Test", Integer.valueOf(1));
		final Map<Class<?>, Object> givenMap = new HashMap<>();

		ConstructorParametersImpl cpi = new ConstructorParametersImpl(givenMap);
		cpi.registerConstructorParameters(parameters);
		Assert.assertEquals(parameters.size(), givenMap.size());
		parameters.forEach(parameter -> Assert.assertEquals(parameter, givenMap.get(parameter.getClass())));
	}

	public static class NoPublicConstructor {

		private NoPublicConstructor() {
		}
	}

	@Test
	public void checkNoPublicConstructor() throws ReflectiveOperationException {
		final ConstructorParameters cpi = ConstructorParameters.withHashMap();
		final InstanceFactory<NoPublicConstructor> result = cpi.findBestMatchingConstructor(NoPublicConstructor.class);
		Assert.assertNotNull(result);
		try {
			result.newInstance();
			Assert.fail("IllegalAccessException should be thrown");
		} catch (IllegalAccessException e) {
			Assert.assertNull(result.parameters);
		}
	}

	public static class EmptyConstructor {

		public EmptyConstructor() {
		}
	}

	@Test
	public void checkPublicConstructor() throws ReflectiveOperationException {
		final ConstructorParameters cpi = ConstructorParameters.withHashMap();
		final InstanceFactory<EmptyConstructor> result = cpi.findBestMatchingConstructor(EmptyConstructor.class);
		Assert.assertNotNull(result);
		Assert.assertEquals(EmptyConstructor.class, result.newInstance().getClass());
	}

	@Test
	public void checkPublicConstructorWithParameters() throws ReflectiveOperationException {
		final ConstructorParameters cpi = ConstructorParameters.withConcurrentMap();
		cpi.registerConstructorParameters("Test", Integer.valueOf(1));
		final InstanceFactory<EmptyConstructor> result = cpi.findBestMatchingConstructor(EmptyConstructor.class);
		Assert.assertNotNull(result);
		Assert.assertEquals(EmptyConstructor.class, result.newInstance().getClass());
	}

	public static class ManyNoEmptyConstructor {

		public final String string;
		public final Integer integer;
		public final EmptyConstructor empty;

		public ManyNoEmptyConstructor(String string) {
			this.string = string;
			this.integer = null;
			this.empty = null;
		}

		public ManyNoEmptyConstructor(Integer integer) {
			this.string = null;
			this.integer = integer;
			this.empty = null;
		}

		public ManyNoEmptyConstructor(String string, Integer integer) {
			this.string = string;
			this.integer = integer;
			this.empty = null;
		}

		public ManyNoEmptyConstructor(String string, Integer integer, EmptyConstructor empty) {
			this.string = string;
			this.integer = integer;
			this.empty = empty;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof ManyNoEmptyConstructor))
				return false;
			final ManyNoEmptyConstructor e = (ManyNoEmptyConstructor) o;
			return Objects.equals(string, e.string) && Objects.equals(integer, e.integer) && Objects.equals(empty,
					e.empty);
		}
	}

	@Test
	public void checkManyNoEmptyConstructor() throws ReflectiveOperationException {
		final ConstructorParameters cpi = ConstructorParameters.withConcurrentMap();
		final ManyNoEmptyConstructor exFull = new ManyNoEmptyConstructor(RandomUtils.alphanumeric(5),
				RandomUtils.nextInt());
		cpi.registerConstructorParameters(exFull.integer, exFull.string);
		final InstanceFactory<ManyNoEmptyConstructor> result = cpi.findBestMatchingConstructor(
				ManyNoEmptyConstructor.class);
		Assert.assertNotNull(result);
		Assert.assertFalse(exFull == result.newInstance());
		Assert.assertEquals(exFull, result.newInstance());
	}

	public static class ManyAndEmptyConstructor extends ManyNoEmptyConstructor {

		public ManyAndEmptyConstructor() {
			super((String) null);
		}

		public ManyAndEmptyConstructor(String string, EmptyConstructor empty) {
			super(string, null, empty);
		}

		public ManyAndEmptyConstructor(String string, Integer integer, EmptyConstructor empty) {
			super(string, integer, empty);
		}
	}

	@Test
	public void checkManyAndEmptyConstructorThreeParameters() throws ReflectiveOperationException {
		final ConstructorParameters cpi = ConstructorParameters.withConcurrentMap();
		final ManyAndEmptyConstructor exFull = new ManyAndEmptyConstructor(RandomUtils.alphanumeric(5),
				RandomUtils.nextInt(), new EmptyConstructor());
		cpi.registerConstructorParameters(exFull.integer, exFull.empty, exFull.string);
		final InstanceFactory<ManyNoEmptyConstructor> result = cpi.findBestMatchingConstructor(
				ManyNoEmptyConstructor.class);
		Assert.assertNotNull(result);
		Assert.assertFalse(exFull == result.newInstance());
		Assert.assertEquals(exFull, result.newInstance());
	}

	@Test
	public void checkManyAndEmptyConstructorTwoParameters() throws ReflectiveOperationException {
		final ConstructorParameters cpi = ConstructorParameters.withConcurrentMap();
		final ManyAndEmptyConstructor exFull = new ManyAndEmptyConstructor(RandomUtils.alphanumeric(5),
				new EmptyConstructor());
		cpi.registerConstructorParameters(exFull.empty, exFull.string);
		final InstanceFactory<ManyAndEmptyConstructor> result = cpi.findBestMatchingConstructor(
				ManyAndEmptyConstructor.class);
		Assert.assertNotNull(result);
		Assert.assertFalse(exFull == result.newInstance());
		Assert.assertEquals(exFull, result.newInstance());
	}

	@Test
	public void checkManyAndEmptyConstructorNoParameters() throws ReflectiveOperationException {
		final ConstructorParameters cpi = ConstructorParameters.withHashMap();
		final InstanceFactory<ManyAndEmptyConstructor> result = cpi.findBestMatchingConstructor(
				ManyAndEmptyConstructor.class);
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.newInstance());
	}

	@Test
	public void testParameterMap() {

		final Map<Class<?>, Object> map = new HashMap<>();
		final ConstructorParameters cpi = ConstructorParameters.withMap(map);

		cpi.registerConstructorParameter(1);
		cpi.registerConstructorParameter(Long.class, 2L);

		Assert.assertEquals(1, map.get(Integer.class));
		Assert.assertEquals(2L, map.get(Long.class));

		Assert.assertNotNull(cpi.unregisterConstructorParameter(1));
		Assert.assertNull(map.get(Integer.class));

		cpi.unregisterConstructorParameter(Long.class);
		Assert.assertNull(map.get(Long.class));

	}

}
