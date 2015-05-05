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
import java.util.List;

import com.qwazr.search.memory.EncodedTermBuffer.EncodedTerm;
import com.qwazr.utils.StringUtils;
import com.sun.jna.Pointer;

/**
 * This class implements a fast UTF-8 String array *
 */
public class FastStringArray implements PointerProvider, Closeable {

	/**
	 * Optimized write only StringArray
	 * 
	 * @param strings
	 */

	private final DisposableMemory termPointers;

	public FastStringArray(final MemoryBuffer memoryBuffer,
			final EncodedTermBuffer termBuffer) {

		// First we reserve memory for the list of pointers
		final int termCount = termBuffer.getTermCount();
		termPointers = memoryBuffer.getNewBufferItem(termCount * Pointer.SIZE);

		// Filling the pointer array memory
		List<EncodedTerm> terms = termBuffer.getTerms();
		Pointer[] pointers = new Pointer[termCount];
		int i = 0;
		for (EncodedTerm term : terms)
			pointers[i++] = new Pointer(term.memory.getPeer() + term.offset);
		termPointers.write(0, pointers, 0, termCount);
	}

	@Override
	final public long getPointer() {
		return termPointers == null ? 0 : termPointers.getPeer();
	}

	@Override
	final public void close() {
		termPointers.close();
	}

	@Override
	public String toString() {
		return StringUtils.fastConcat("[", super.toString(), " ", "]");
	}
}
