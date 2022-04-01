/**
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class InstancesSupplierTest {

	private InstancesSupplier instancesSupplier;

	@Before
	public void setup() {
		instancesSupplier = InstancesSupplier.withConcurrentMap();
	}

	@Test
	public void registerInstance() {
		final String instance = "test";
		Assert.assertEquals(0, instancesSupplier.size());
		Assert.assertNull(instancesSupplier.registerInstance(instance));
		Assert.assertEquals(1, instancesSupplier.size());
		Assert.assertEquals(instance, instancesSupplier.getInstance(String.class));
		Assert.assertEquals(instance, instancesSupplier.registerInstance(instance));
		Assert.assertEquals(1, instancesSupplier.size());
	}

	@Test
	public void unregisterInstance() {
		final String instance = "test";
		Assert.assertEquals(0, instancesSupplier.size());
		Assert.assertNull(instancesSupplier.unregisterInstance(String.class));
		Assert.assertEquals(0, instancesSupplier.size());
		Assert.assertEquals(null, instancesSupplier.registerInstance(instance));
		Assert.assertEquals(1, instancesSupplier.size());
		Assert.assertEquals(instance, instancesSupplier.unregisterInstance(String.class));
		Assert.assertEquals(0, instancesSupplier.size());
	}

	@Test(expected = NullPointerException.class)
	public void checkRegisterNull() {
		instancesSupplier.registerInstance(null);
	}

	@Test(expected = NullPointerException.class)
	public void checkUnregisterNull() {
		instancesSupplier.unregisterInstance(null);
	}

	@Test(expected = NullPointerException.class)
	public void checkgetInstanceNull() {
		instancesSupplier.getInstance(null);
	}
}
