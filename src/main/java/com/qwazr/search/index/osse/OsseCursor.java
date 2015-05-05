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
package com.qwazr.search.index.osse;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qwazr.search.memory.DisposableMemory;
import com.qwazr.search.memory.FastStringArray;
import com.qwazr.search.memory.MemoryBuffer;
import com.qwazr.search.memory.PointerArray;
import com.qwazr.search.memory.PointerProvider;
import com.qwazr.utils.IOUtils;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class OsseCursor implements PointerProvider, Closeable {

	private static final Logger logger = LoggerFactory
			.getLogger(OsseCursor.class);

	private final OsseErrorHandler error;

	private long cursorPtr;

	public OsseCursor(OsseIndex index, OsseErrorHandler error, int fieldId,
			FastStringArray terms, int length, int booleanOperator)
			throws OsseException {
		if (logger.isInfoEnabled())
			logger.info("Create cursor on " + fieldId + " with " + length
					+ " terms");
		this.error = error;
		this.cursorPtr = OsseIndex.LIB.OSSCLib_MsQCursor_Create(
				index.getPointer(), fieldId, terms.getPointer(), length,
				booleanOperator, error.getPointer());
		if (cursorPtr == 0)
			error.checkNoError();
	}

	public OsseCursor(OsseIndex index, OsseErrorHandler error,
			int booleanOperator, Collection<OsseCursor> cursors)
			throws OsseException {
		if (logger.isInfoEnabled())
			logger.info("Create combined cursor on " + cursors.size()
					+ " cursor(s) with operator " + booleanOperator);
		this.error = error;
		PointerArray pointerArray = null;
		try {
			pointerArray = new PointerArray(index.getMemoryBuffer(), cursors);
			this.cursorPtr = OsseIndex.LIB
					.OSSCLib_MsQCursor_CreateCombinedCursor(index.getPointer(),
							pointerArray.getPointer(), cursors.size(),
							booleanOperator, error.getPointer());
			if (cursorPtr == 0)
				error.checkNoError();
		} finally {
			IOUtils.close(pointerArray);
		}
	}

	@Override
	final public void close() {
		if (cursorPtr == 0)
			return;
		OsseIndex.LIB.OSSCLib_MsQCursor_Delete(cursorPtr);
		cursorPtr = 0;
	}

	final public int getDocIds(final long longArrayPtr,
			final long longArraySize, final long startPos) throws OsseException {
		if (logger.isInfoEnabled())
			logger.info("getDocIds with startPos to " + startPos);
		IntByReference success = new IntByReference();

		int count = OsseIndex.LIB.OSSCLib_MsQCursor_GetDocIds(cursorPtr,
				longArrayPtr, startPos, false, longArraySize,
				Pointer.nativeValue(success.getPointer()), error.getPointer());
		if (success.getValue() == 0)
			error.checkNoError();
		return count;
	}

	final public long getNumberOfDocs() throws OsseException {
		IntByReference success = new IntByReference();
		long number = OsseIndex.LIB.OSSCLib_MsQCursor_GetNumberOfDocs(
				cursorPtr, Pointer.nativeValue(success.getPointer()),
				error.getPointer());
		if (success.getValue() == 0)
			error.checkNoError();
		return number;
	}

	final public void collect(final MemoryBuffer memoryBuffer,
			final Collection<Long> collector) throws IOException, OsseException {
		long bufferSize = getNumberOfDocs();
		if (bufferSize > 10000)
			bufferSize = 10000;
		DisposableMemory longBufferArray = memoryBuffer
				.getNewBufferItem((int) (bufferSize * Native.LONG_SIZE));
		try {
			long docPosition = 0;
			for (;;) {
				long length = getDocIds(longBufferArray.getPeer(), bufferSize,
						docPosition);
				if (length == 0)
					break;
				long offset = 0;
				for (int i = 0; i < length; i++) {
					collector.add(longBufferArray.getLong(offset));
					offset += Native.LONG_SIZE;
				}
				docPosition += length;
			}
		} finally {
			longBufferArray.close();
		}
	}

	@Override
	public long getPointer() {
		return cursorPtr;
	}

}
