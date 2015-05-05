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
import java.util.Collection;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class OsseDocCursor implements Closeable {

	private final OsseErrorHandler error;

	private long docCursorPtr;

	public OsseDocCursor(OsseIndex index, OsseErrorHandler error)
			throws OsseException {
		this.error = error;
		docCursorPtr = OsseIndex.LIB.OSSCLib_MsDocTCursor_Create(
				index.getPointer(), error.getPointer());
		if (docCursorPtr == 0)
			error.throwError();
	}

	private void fillCurrentTerm(Collection<String> collector)
			throws OsseException {
		String term = OsseIndex.LIB.OSSCLib_MsDocTCursor_GetCurrentTerm(
				docCursorPtr, error.getPointer());
		if (term == null)
			error.checkNoError();
		else
			collector.add(term);
	}

	public void fillTerms(int fieldId, long docId, Collection<String> collector)
			throws OsseException {
		IntByReference bError = new IntByReference();
		int res = OsseIndex.LIB.OSSCLib_MsDocTCursor_FindFirstTerm(
				docCursorPtr, fieldId, docId, 0,
				Pointer.nativeValue(bError.getPointer()), error.getPointer());
		if (bError.getValue() != 0)
			error.throwError();
		if (res == 0)
			return;
		fillCurrentTerm(collector);
		for (;;) {
			res = OsseIndex.LIB.OSSCLib_MsDocTCursor_FindNextTerm(docCursorPtr,
					0, Pointer.nativeValue(bError.getPointer()),
					error.getPointer());
			if (res == 0)
				break;
			if (bError.getValue() != 0)
				error.throwError();
			fillCurrentTerm(collector);
		}
	}

	final public long getPointer() {
		return docCursorPtr;
	}

	@Override
	final public void close() {
		if (docCursorPtr == 0)
			return;
		OsseIndex.LIB.OSSCLib_MsDocTCursor_Delete(docCursorPtr);
		docCursorPtr = 0;
	}

}
