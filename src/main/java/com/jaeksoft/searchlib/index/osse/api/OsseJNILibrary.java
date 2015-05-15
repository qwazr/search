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
package com.jaeksoft.searchlib.index.osse.api;

import com.qwazr.utils.NativeUtils;

public class OsseJNILibrary {

	final public static int OSSCLIB_FIELD_UI32FIELDTYPE_STRING = 1;
	final public static int OSSCLIB_FIELD_UI32FIELDFLAGS_OFFSET = 0x00000002;
	final public static int OSSCLIB_FIELD_UI32FIELDFLAGS_POSITION = 0x00000004;
	final public static int OSSCLIB_FIELD_UI32FIELDFLAGS_TERMFREQ = 0x00000008;
	final public static int OSSCLIB_FIELD_UI32FIELDFLAGS_VSM1 = 0x00000040;
	final public static int OSSCLIB_FIELD_UI32FIELDFLAGS_STORED = 0x00000100;
	final public static int OSSCLIB_FIELD_UI32FIELDFLAGS_STORED_COMPRESSED = 0x00000200;

	final public static int OSSCLIB_QCURSOR_UI32BOP_OR = 0x00000000;
	final public static int OSSCLIB_QCURSOR_UI32BOP_AND = 0x00000001;
	final public static int OSSCLIB_QCURSOR_UI32BOP_INVERTED_OR = 0x00000002;
	final public static int OSSCLIB_QCURSOR_UI32BOP_INVERTED_AND = 0x00000003;

	static {
		try {
			NativeUtils.loadLibrary("libOpenSearchServer_CLib");
		} catch (Throwable t) {
			NativeUtils.loadLibrary("OpenSearchServer_CLib");
		}
	}

	public native String OSSCLib_GetVersionInfoText();

	public native long OSSCLib_ExtErrInfo_Create();

	public native int OSSCLib_ExtErrInfo_GetErrorCode(long hExtErrInfo);

	public native String OSSCLib_ExtErrInfo_GetText(long lpErr);

	public native void OSSCLib_ExtErrInfo_Delete(long hExtErrInfo);

	public native long OSSCLib_MsIndex_Create(String lpwszIndexDirectoryName,
			String lpwszFileName_MsRoot, long hExtErrInfo);

	public native long OSSCLib_MsIndex_Open(String lpwszIndexDirectoryName,
			String lpwszFileName_MsRoot, long hExtErrInfo);

	public native boolean OSSCLib_MsIndex_Close(long hMsIndex, long hExtErrInfo);

	public native long OSSCLib_MsIndex_GetNumberOfDocs(long hMsIndex,
			long hExtErrInfo);

	public native boolean OSSCLib_MsIndex_DeleteAllDocs(long hMsIndex,
			int ui32IndexSignature, long hExtErrInfo);

	public native long OSSCLib_MsTransact_Begin(long hMsIndex,
			String lpwszNewSegmentDirectoryName, int ui32MaxNumberOfNewDocs,
			long hExtErrInfo);

	public native int OSSCLib_MsTransact_Document_GetNewDocId(long hMsTransact,
			long hExtErrInfo);

	public native int OSSCLib_MsTransact_AddEntireNewDocument(long hMsTransact,
			int ui32NumberOfTransactFields, long hMsTransactFieldArray,
			long ui32NumberOfTermsArray, long lpsu8zTermArray, long hExtErrInfo);

	public native int OSSCLib_MsTransact_Document_AddStringTerms(
			long hMsTransactField, int ui32DocId, long lplpsu8zTermArray,
			int ui32NumberOfTerms, long hExtErrInfo);

	public native int OSSCLib_MsTransact_Document_AddStringTermsJ(
			long hMsTransactField, int ui32DocId, String[] lplpsu8zTermArray,
			int ui32NumberOfTerms, long hExtErrInfo);

	public native int OSSCLib_MsTransact_Document_AddStringTermsW(
			long hMsTransactField, int ui32DocId, long lplpsu8zTermArray,
			int ui32NumberOfTerms, long hExtErrInfo);

	public native boolean OSSCLib_MsTransact_Document_AddStringTerms_Offsets32(
			long hMsTransactField, int ui32MsTransactDocId,
			int ui32NumberOfTerms, long lplpsu8zTermArray,
			long lpTermOffset32Array, long lpui32TermPosIncrArray,
			long hExtErrInfo);

	public native boolean OSSCLib_MsTransact_Document_AddStringTerms_Offsets64(
			long hMsTransactField, int ui32MsTransactDocId,
			int ui32NumberOfTerms, long jl_lplpsu8zTermArray,
			long jl_lpTermOffset64Array, long jl_lpui32TermPosIncrArray,
			long jl_hExtErrInfo);

	public native boolean OSSCLib_MsTransact_RollBack(long hMsTransact,
			long hExtErrInfo);

	public native boolean OSSCLib_MsTransact_Commit(long hMsTransact,
			int ui32IndexSignature, long lpui64NewDocIdBase,
			long lpbSomeDocIdsChanged, long hExtErrInfo);

	public native long OSSCLib_MsTransact_CreateFieldW(long hMsTransact,
			String lpwszFieldName, int ui32FieldType, int ui32FieldFlags,
			long lpFieldParams, boolean bFailIfAlreadyExists,
			long lpui32MsFieldId, long hExtErrInfo);

	public native long OSSCLib_MsTransact_FindFieldW(long hMsTransact,
			String lpwszFieldName, long hExtErrInfo);

	public native long OSSCLib_MsTransact_GetExistingField(long hMsTransact,
			int ui32MsFieldId, long hExtErrInfo);

	public native int OSSCLib_MsTransact_DeleteFields(long hMsTransact,
			long lphMsTransactFieldArray, int ui32NumberOfFields,
			long hExtErrInfo);

	public native boolean OSSCLib_MsTransact_DeleteDocumentsByMsQCursor(
			long hMsTransact, long hMsQCursor, long hExtErrInfo);

	public native int OSSCLib_MsIndex_GetListOfFields(long hMsIndex,
			int[] lpui32FieldIdArray, int ui32FieldIdArraySize,
			boolean bSortArrayByFieldId, long hExtErrInfo);

	public native String OSSCLib_MsIndex_GetFieldNameAndProperties(
			long hMsIndex, int ui32MsFieldId, long lpui32FieldType,
			long lpui32FieldFlags, long hExtErrInfo);

	public native long OSSCLib_MsQCursor_Create(long hMsIndex,
			int ui32MsFieldId, long lplpsu8zTerm, int ui32NumberOfTerms,
			int ui32Bop, long hExtErrInfo);

	public native void OSSCLib_MsQCursor_Delete(long hMsQCursor);

	public native long OSSCLib_MsQCursor_GetNumberOfDocs(long hMsQCursor,
			long lpbSuccess, long hExtErrInfo);

	public native int OSSCLib_MsQCursor_GetDocIds(
			long hMsQCursor, // Cursor handle
			long lpui64DocIdArray, long ui64DocPosition,
			boolean bPosMeasuredFromEnd, long ui32NumberOfDocsToRetrieve,
			long lpbSuccess, long hExtErrInfo);

	public native long OSSCLib_MsQCursor_CreateCombinedCursor(
			long hMsIndex, // Index handle
			long lphMsQCursor, int ui32NumberOfCursors, int ui32Bop,
			long hExtErrInfo);

	public native long OSSCLib_MsDocTCursor_Create(long hMsIndex,
			long hExtErrInfo);

	public native void OSSCLib_MsDocTCursor_Delete(long hMsDocTCursor);

	public native int OSSCLib_MsDocTCursor_FindFirstTerm(long hMsDocTCursor,
			int ui32MsFieldId, long ui64DocId, long lplpsu8zTerm,
			long lpbError, long hExtErrInfo);

	public native int OSSCLib_MsDocTCursor_FindNextTerm(long hMsDocTCursor,
			long lplpsu8zTerm, long lpbError, long hExtErrInfo);

	public native String OSSCLib_MsDocTCursor_GetCurrentTerm(
			long hMsDocTCursor, long hExtErrInfo);

}