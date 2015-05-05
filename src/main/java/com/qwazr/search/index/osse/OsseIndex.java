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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.jaeksoft.searchlib.index.osse.api.OsseJNILibrary;
import com.qwazr.search.index.FieldDefinition;
import com.qwazr.search.memory.MemoryBuffer;
import com.qwazr.search.memory.PointerProvider;
import com.qwazr.utils.StringUtils;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;

public class OsseIndex implements PointerProvider {

	public static OsseJNILibrary LIB = null;

	private final MemoryBuffer memoryBuffer;

	private long indexPtr;

	public static synchronized void initOsseJNILibrary() {
		if (LIB != null)
			return;
		LIB = new OsseJNILibrary();
	}

	public OsseIndex(MemoryBuffer memoryBuffer, File indexDirectory,
			OsseErrorHandler err, boolean bCreate) throws OsseException {
		this.memoryBuffer = memoryBuffer;
		String path = indexDirectory.getPath();
		if (bCreate) {
			indexPtr = LIB.OSSCLib_MsIndex_Create(path, null, err.getPointer());
		} else {
			indexPtr = LIB.OSSCLib_MsIndex_Open(path, null, err.getPointer());
		}
		if (indexPtr == 0)
			err.throwError();
	}

	public MemoryBuffer getMemoryBuffer() {
		return memoryBuffer;
	}

	Pair<String, FieldInfo> getFieldNameAndProperties(OsseErrorHandler error,
			int ui32MsFieldId) throws OsseException {
		IntByReference fieldType = new IntByReference();
		IntByReference fieldFlags = new IntByReference();
		String hFieldName = LIB
				.OSSCLib_MsIndex_GetFieldNameAndProperties(indexPtr,
						ui32MsFieldId,
						Memory.nativeValue(fieldType.getPointer()),
						Memory.nativeValue(fieldFlags.getPointer()),
						error.getPointer());
		if (StringUtils.isEmpty(hFieldName))
			error.throwError();
		return Pair.of(hFieldName,
				new FieldInfo(ui32MsFieldId, fieldFlags.getValue()));
	}

	public Map<String, FieldInfo> getFieldMap(OsseErrorHandler error)
			throws OsseException {
		int nField = LIB.OSSCLib_MsIndex_GetListOfFields(indexPtr, null, 0,
				false, error.getPointer());
		error.checkNoError();
		if (nField == 0)
			return Collections.emptyMap();
		Map<String, FieldInfo> fieldMap = new HashMap<String, FieldInfo>();
		int[] hFieldArray = new int[nField];
		LIB.OSSCLib_MsIndex_GetListOfFields(indexPtr, hFieldArray, nField,
				false, error.getPointer());
		error.checkNoError();
		for (int fieldId : hFieldArray) {
			Pair<String, FieldInfo> fieldInfo = getFieldNameAndProperties(
					error, fieldId);
			fieldMap.put(fieldInfo.getKey(), fieldInfo.getValue());
		}
		return fieldMap;
	}

	public void close(OsseErrorHandler err) throws OsseException {
		if (indexPtr == 0)
			return;
		if (!LIB.OSSCLib_MsIndex_Close(indexPtr, err.getPointer()))
			err.throwError();
	}

	public void deleteAll(OsseErrorHandler err) throws OsseException {
		if (!LIB.OSSCLib_MsIndex_DeleteAllDocs(indexPtr, 0, err.getPointer()))
			err.throwError();
	}

	public long getNumberOfDocs(OsseErrorHandler err) throws OsseException {
		long res = LIB.OSSCLib_MsIndex_GetNumberOfDocs(indexPtr,
				err.getPointer());
		if (res == -1)
			err.throwError();
		return res;
	}

	@Override
	public String toString() {
		return Long.toString(indexPtr);
	}

	@Override
	public long getPointer() {
		return indexPtr;
	}

	public class FieldInfo {

		public final int id;
		public final FieldDefinition fieldDefinition;

		private FieldInfo(int id, int flags) {
			this.id = id;
			fieldDefinition = new FieldDefinition(flags);
		}

	}

}
