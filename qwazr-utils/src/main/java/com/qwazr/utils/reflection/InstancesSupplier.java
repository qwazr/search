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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public interface InstancesSupplier {

	default <T> T registerInstance(T object) {
		return registerInstance((Class<T>) object.getClass(), object);
	}

	<T> T registerInstance(Class<? extends T> type, T object);

	<T> T unregisterInstance(Class<? extends T> type);

	<T> T getInstance(Class<T> type);

	int size();

	class Impl implements InstancesSupplier {

		private final Map<Class<?>, Object> instances;

		protected Impl(Map<Class<?>, Object> instances) {
			this.instances = instances;
		}

		private <T> Class<T> checkType(Class<T> type) {
			return Objects.requireNonNull(type, "The type is missing");
		}

		@Override
		public <T> T registerInstance(Class<? extends T> type, T object) {
			return type.cast(instances.put(checkType(type), Objects.requireNonNull(object, "The object is missing")));
		}

		@Override
		public <T> T unregisterInstance(Class<? extends T> type) {
			return type.cast(instances.remove(checkType(type)));
		}

		@Override
		public <T> T getInstance(final Class<T> type) {
			return type.cast(instances.get(checkType(type)));
		}

		@Override
		public int size() {
			return instances.size();
		}

	}

	static InstancesSupplier withMap(Map<Class<?>, Object> map) {
		return new Impl(map);
	}

	static InstancesSupplier withConcurrentMap() {
		return withMap(new ConcurrentHashMap<>());
	}
}
