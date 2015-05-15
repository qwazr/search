/**
 * Copyright 2014-2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.memory;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractBuffer<T extends BufferItemInterface> implements
		Closeable {

	private TreeMap<Integer, ArrayDeque<T>> available;

	public AbstractBuffer() {
		available = new TreeMap<Integer, ArrayDeque<T>>();
	}

	@Override
	final public void close() {
		available.clear();
	}

	protected abstract T newBufferItem(final int size);

	@SuppressWarnings("unchecked")
	final public T getNewBufferItem(final int size) {
		Map.Entry<Integer, ArrayDeque<T>> entry = available.ceilingEntry(size);
		if (entry == null)
			return newBufferItem(size);
		ArrayDeque<T> memoryQue = entry.getValue();
		if (memoryQue.isEmpty())
			return newBufferItem(size);
		return (T) memoryQue.poll().reset();
	}

	final void closed(T bufferItem) {
		final int size = bufferItem.getSize();
		ArrayDeque<T> memoryQue = available.get(size);
		if (memoryQue == null) {
			memoryQue = new ArrayDeque<T>();
			available.put(size, memoryQue);
		}
		memoryQue.offer(bufferItem);
	}

}
