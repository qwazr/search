/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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
package com.qwazr.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ReadOnlyMap<K, V> implements Map<K, V> {

	private volatile Map<K, V> map;

	protected ReadOnlyMap() {
		this.map = Collections.emptyMap();
	}

	protected Map<K, V> setMap(Map<K, V> newMap) {
		Map<K, V> oldMap = this.map;
		this.map = newMap;
		return oldMap;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return map.get(key);
	}

	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException("Cannot modify a read only map");
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException("Cannot modify a read only map");
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException("Cannot modify a read only map");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Cannot modify an immutable map");
	}

	@Override
	public Set<K> keySet() {
		return Collections.unmodifiableSet(map.keySet());
	}

	@Override
	public Collection<V> values() {
		return Collections.unmodifiableCollection(map.values());
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return Collections.unmodifiableSet(map.entrySet());
	}
}
