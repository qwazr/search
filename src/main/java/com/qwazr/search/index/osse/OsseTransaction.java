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
 **/
package com.qwazr.search.index.osse;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jaeksoft.searchlib.index.osse.api.OsseJNILibrary;
import com.qwazr.search.index.FieldContent;
import com.qwazr.search.index.osse.OsseIndex.FieldInfo;
import com.qwazr.search.memory.EncodedTermBuffer;
import com.qwazr.search.memory.FastStringArray;
import com.qwazr.search.memory.MemoryBuffer;
import com.qwazr.search.memory.PointerArray;
import com.qwazr.utils.IOUtils;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;

public class OsseTransaction implements Closeable {

	private static final Logger logger = LoggerFactory
			.getLogger(OsseTransaction.class);

	private long transactPtr;

	private OsseErrorHandler err;

	public final MemoryBuffer internalMemoryBuffer;
	public final MemoryBuffer memoryBuffer;

	private final Map<String, Long> fieldPointerMap;

	public OsseTransaction(OsseIndex index, MemoryBuffer memoryBuffer,
			Map<String, FieldInfo> fieldMap, int maxBufferSize)
			throws OsseException {
		if (memoryBuffer == null) {
			internalMemoryBuffer = new MemoryBuffer();
			this.memoryBuffer = internalMemoryBuffer;
		} else {
			internalMemoryBuffer = null;
			this.memoryBuffer = memoryBuffer;
		}
		err = new OsseErrorHandler();
		transactPtr = OsseIndex.LIB.OSSCLib_MsTransact_Begin(
				index.getPointer(), null, maxBufferSize, err.getPointer());
		if (transactPtr == 0)
			err.throwError();
		if (fieldMap != null) {
			fieldPointerMap = new TreeMap<String, Long>();
			for (Map.Entry<String, FieldInfo> entry : fieldMap.entrySet())
				fieldPointerMap.put(entry.getKey(),
						getExistingField(entry.getValue().id));
		} else
			fieldPointerMap = null;
	}

	final public void commit() throws OsseException {
		if (!OsseIndex.LIB.OSSCLib_MsTransact_Commit(transactPtr, 0, 0, 0,
				err.getPointer()))
			err.throwError();
		transactPtr = 0;
	}

	final public int newDocumentId() throws OsseException {
		int documentId = OsseIndex.LIB.OSSCLib_MsTransact_Document_GetNewDocId(
				transactPtr, err.getPointer());
		err.checkNoError();
		if (documentId < 0)
			err.throwError();
		return documentId;
	}

	final private long checkFieldPointer(String fieldName) throws OsseException {
		Long fieldPtr = fieldPointerMap.get(fieldName);
		if (fieldPtr == null)
			throw new OsseException("Unknown field : " + fieldName, null);
		return fieldPtr;
	}

	final public void addTerms(int documentId, String fieldName,
			FieldContent fieldContent) throws OsseException, IOException {
		FastStringArray fastStringArray = null;
		try {
			EncodedTermBuffer encodedTermBuffer = new EncodedTermBuffer(
					memoryBuffer, fieldContent);
			if (encodedTermBuffer.getTermCount() == 0)
				return;
			fastStringArray = new FastStringArray(memoryBuffer,
					encodedTermBuffer);
			long transactFieldPtr = checkFieldPointer(fieldName);
			int res = OsseIndex.LIB.OSSCLib_MsTransact_Document_AddStringTerms(
					transactFieldPtr, documentId, fastStringArray.getPointer(),
					encodedTermBuffer.getTermCount(), err.getPointer());
			if (res <= 0)
				err.throwError();
		} finally {
			if (fastStringArray != null)
				fastStringArray.close();
		}
	}

	final public int createField(String fieldName, int flag)
			throws OsseException {
		IntByReference fieldId = new IntByReference();
		long fieldPtr = OsseIndex.LIB.OSSCLib_MsTransact_CreateFieldW(
				transactPtr, fieldName,
				OsseJNILibrary.OSSCLIB_FIELD_UI32FIELDTYPE_STRING, flag,
				(long) 0, true, Memory.nativeValue(fieldId.getPointer()),
				err.getPointer());
		err.checkNoError();
		if (fieldPtr == 0)
			err.throwError();
		return fieldId.getValue();
	}

	final public void deleteField(String fieldName) throws OsseException {
		long transactFieldPtr = checkFieldPointer(fieldName);
		PointerArray pointerArray = null;
		try {
			pointerArray = new PointerArray(memoryBuffer, transactFieldPtr);
			int i = OsseIndex.LIB.OSSCLib_MsTransact_DeleteFields(transactPtr,
					pointerArray.getPointer(), 1, err.getPointer());
			if (i != 1)
				err.throwError();
		} finally {
			IOUtils.close(pointerArray);
		}
	}

	final private long getExistingField(int fieldId) throws OsseException {
		long transactFieldPtr = OsseIndex.LIB
				.OSSCLib_MsTransact_GetExistingField(transactPtr, fieldId,
						err.getPointer());
		if (transactFieldPtr == 0)
			err.throwError();
		return transactFieldPtr;
	}

	final public void deleteDocumentsByCursor(OsseCursor cursor)
			throws OsseException {
		boolean res = OsseIndex.LIB
				.OSSCLib_MsTransact_DeleteDocumentsByMsQCursor(transactPtr,
						cursor.getPointer(), err.getPointer());
		if (!res)
			err.throwError();
	}

	final public void rollback() throws OsseException {
		if (!OsseIndex.LIB.OSSCLib_MsTransact_RollBack(transactPtr,
				err.getPointer()))
			err.throwError();
		transactPtr = 0;
	}

	@Override
	final public void close() {
		try {
			if (transactPtr != 0)
				rollback();
		} catch (Throwable e) {
			logger.warn(e.getMessage(), e);
		}
		IOUtils.close(internalMemoryBuffer);
		if (err != null) {
			IOUtils.closeQuietly(err);
			err = null;
		}
	}
}
