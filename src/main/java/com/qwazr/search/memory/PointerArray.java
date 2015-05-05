/**
 * Copyright 2014-2015 OpenSearchServer Inc.
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
import java.util.Collection;

import com.sun.jna.Pointer;

/**
 * This class implements a fast Pointer array *
 */
public class PointerArray implements PointerProvider, Closeable {

	private final DisposableMemory memory;

	public PointerArray(final MemoryBuffer memoryBuffer,
			final Collection<? extends PointerProvider> pointers) {
		memory = memoryBuffer.getNewBufferItem((pointers.size() + 1)
				* Pointer.SIZE);
		int i = 0;
		for (PointerProvider pointer : pointers) {
			memory.setLong(Pointer.SIZE * i, pointer.getPointer());
			i++;
		}
		memory.setPointer(Pointer.SIZE * i, null);
	}

	public PointerArray(final MemoryBuffer memoryBuffer, final long pointer) {
		memory = memoryBuffer.getNewBufferItem(2 * Pointer.SIZE);
		memory.setLong(0, pointer);
		memory.setPointer(Pointer.SIZE, null);
	}

	@Override
	public void close() {
		memory.close();
	}

	@Override
	public long getPointer() {
		return memory == null ? 0 : memory.getPeer();
	}
}
