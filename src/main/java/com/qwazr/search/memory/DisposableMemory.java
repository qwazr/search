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

import java.nio.ByteBuffer;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

final public class DisposableMemory extends Pointer implements
		BufferItemInterface {

	final int size;
	protected final MemoryBuffer buffer;
	private ByteBuffer byteBuffer;

	DisposableMemory(final MemoryBuffer buffer, final int size) {
		super(Native.malloc(size));
		if (peer == 0)
			throw new OutOfMemoryError("Cannot allocate " + size + " bytes");
		this.size = size;
		this.buffer = buffer;
		this.byteBuffer = null;
	}

	final public ByteBuffer getByteBuffer() {
		if (byteBuffer != null)
			return byteBuffer;
		byteBuffer = getByteBuffer(0, size);
		return byteBuffer;
	}

	@Override
	final public void finalize() {
		if (peer != 0)
			Native.free(peer);
		peer = 0;
	}

	final public long getPeer() {
		return peer;
	}

	@Override
	final public void close() {
		if (buffer != null)
			buffer.closed(this);
	}

	@Override
	final public int getSize() {
		return size;
	}

	@Override
	final public DisposableMemory reset() {
		if (byteBuffer != null)
			byteBuffer.clear();
		return this;
	}
}
