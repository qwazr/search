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
package com.qwazr.search.index.osse;

import java.io.Closeable;

public class OsseErrorHandler implements Closeable {

	private long errPtr;

	public OsseErrorHandler() throws OsseException {
		errPtr = OsseIndex.LIB.OSSCLib_ExtErrInfo_Create();
		if (errPtr == 0)
			throw new OsseException(
					"Internal error: OSSCLib_ExtErrInfo_Create", 0);
	}

	final public String getError() {
		String error = OsseIndex.LIB.OSSCLib_ExtErrInfo_GetText(errPtr);
		return error != null ? error.toString() : null;
	}

	final public int getErrorCode() {
		return OsseIndex.LIB.OSSCLib_ExtErrInfo_GetErrorCode(errPtr);
	}

	final public void checkNoError() throws OsseException {
		int errorCode = getErrorCode();
		if (errorCode != 0)
			throw new OsseException(getError(), errorCode);
	}

	final public void throwError() throws OsseException {
		throw new OsseException(getError(), getErrorCode());
	}

	final public long getPointer() {
		return errPtr;
	}

	@Override
	final public void close() {
		if (errPtr == 0)
			return;
		OsseIndex.LIB.OSSCLib_ExtErrInfo_Delete(errPtr);
		errPtr = 0;
	}
}
