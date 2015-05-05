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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.List;

import com.qwazr.search.index.FieldContent;
import com.qwazr.utils.CharsetUtils;
import com.qwazr.utils.array.IntBufferedArrayFactory;
import com.qwazr.utils.array.IntBufferedArrayInterface;

public class EncodedTermBuffer implements Closeable {

	public final class EncodedTerm {

		public final DisposableMemory memory;
		public final int offset;

		private EncodedTerm(final int charLength) {
			checkByteBuffer(charLength);
			this.memory = currentByteArray;
			this.offset = currentByteBuffer.position();
		}

		private EncodedTerm(final char[] charArray, final int charLength)
				throws IOException {
			this(charLength);
			if (encoder.encode(CharBuffer.wrap(charArray, 0, charLength),
					currentByteBuffer, false) != CoderResult.UNDERFLOW)
				throw new IOException("Charset Encoder issue");
			currentByteBuffer.put((byte) 0);
		}

		private EncodedTerm(String term) throws IOException {
			this(term.length());
			try {
				currentByteBuffer.put(term.getBytes(CharsetUtils.CharsetUTF8));
				currentByteBuffer.put((byte) 0);
			} catch (java.nio.BufferOverflowException e) {
				throw e;
			}
		}

	}

	final List<EncodedTerm> terms;
	final IntBufferedArrayInterface offsets;
	final IntBufferedArrayInterface positionIncrements;

	private final MemoryBuffer memoryBuffer;
	private final List<DisposableMemory> byteArrays;

	private DisposableMemory currentByteArray;
	private ByteBuffer currentByteBuffer;

	private final CharsetEncoder encoder;
	private final int maxBytesPerChar;

	public EncodedTermBuffer(final MemoryBuffer memoryBuffer) {
		this.memoryBuffer = memoryBuffer;
		this.terms = new ArrayList<EncodedTerm>(1);
		this.offsets = IntBufferedArrayFactory.INSTANCE.newInstance(1000);
		this.positionIncrements = IntBufferedArrayFactory.INSTANCE
				.newInstance(500);
		this.byteArrays = new ArrayList<DisposableMemory>(1);
		this.encoder = CharsetUtils.CharsetUTF8.newEncoder();
		this.maxBytesPerChar = (int) encoder.maxBytesPerChar();
		reset();
	}

	public EncodedTermBuffer(final MemoryBuffer memoryBuffer, final String term)
			throws IOException {
		this(memoryBuffer);
		addTerm(term);
	}

	public EncodedTermBuffer(MemoryBuffer memoryBuffer,
			FieldContent fieldContent) throws IOException {
		this(memoryBuffer);
		if (fieldContent == null)
			return;
		if (fieldContent.terms == null)
			return;
		for (String term : fieldContent.terms)
			if (term != null && !term.isEmpty())
				addTerm(term);
	}

	final public void addTerm(final char[] charArray, final int charLength)
			throws IOException {
		terms.add(new EncodedTerm(charArray, charLength));
	}

	final public void addTerm(final String term) throws IOException {
		terms.add(new EncodedTerm(term));
	}

	final public void addTerm(final String term, final int startOffset,
			final int endOffset, final int posIncr) throws IOException {
		terms.add(new EncodedTerm(term));
		offsets.add(startOffset);
		offsets.add(endOffset);
		positionIncrements.add(posIncr);
	}

	final public void reset() {
		close();
		terms.clear();
		offsets.reset();
		positionIncrements.reset();
		byteArrays.clear();
		currentByteArray = null;
		currentByteBuffer = null;
		newByteBuffer(16384);
	}

	@Override
	final public void close() {
		for (DisposableMemory byteArray : byteArrays)
			byteArray.close();
	}

	final private void checkByteBuffer(final int charLength) {
		final int fullLength = charLength * maxBytesPerChar + 1;
		if (fullLength > currentByteBuffer.remaining())
			newByteBuffer(fullLength);
	}

	final private void newByteBuffer(final int length) {
		currentByteArray = memoryBuffer.getNewBufferItem(length < 16384 ? 16384
				: length);
		currentByteBuffer = currentByteArray.getByteBuffer();
		byteArrays.add(currentByteArray);
	}

	final public List<EncodedTerm> getTerms() {
		return terms;
	}

	final public int getTermCount() {
		return terms.size();
	}

	final public int getByteArrayCount() {
		return byteArrays.size();
	}

	final public int[] getOffsets() {
		return offsets.getFinalArray();
	}

	final public int[] getPositionIncrements() {
		return positionIncrements.getFinalArray();
	}

}