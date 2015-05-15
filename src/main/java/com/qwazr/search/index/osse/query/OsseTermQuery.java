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
package com.qwazr.search.index.osse.query;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.jaeksoft.searchlib.index.osse.api.OsseJNILibrary;
import com.qwazr.search.index.QueryDefinition.TermQuery;
import com.qwazr.search.index.osse.OsseCursor;
import com.qwazr.search.index.osse.OsseErrorHandler;
import com.qwazr.search.index.osse.OsseException;
import com.qwazr.search.index.osse.OsseIndex;
import com.qwazr.search.index.osse.OsseIndex.FieldInfo;
import com.qwazr.search.memory.EncodedTermBuffer;
import com.qwazr.search.memory.FastStringArray;
import com.qwazr.search.memory.MemoryBuffer;

public class OsseTermQuery extends OsseAbstractQuery {

	final private TermQuery termQuery;

	OsseTermQuery(TermQuery termQuery) {
		this.termQuery = termQuery;
	}

	@Override
	public void execute(OsseIndex index, Map<String, FieldInfo> fieldMap,
			OsseErrorHandler error) throws OsseException {
		if (fieldMap == null)
			throw new OsseException("Unknown field: " + termQuery.field, null);
		FieldInfo fieldInfo = fieldMap.get(termQuery.field);
		if (fieldInfo == null)
			throw new OsseException("Unknown field: " + termQuery.field, null);
		FastStringArray fastStringArray = null;
		try {
			MemoryBuffer memoryBuffer = index.getMemoryBuffer();
			fastStringArray = new FastStringArray(memoryBuffer,
					new EncodedTermBuffer(memoryBuffer, termQuery.value));
			cursor = new OsseCursor(index, error, fieldInfo.id,
					fastStringArray, 1,
					OsseJNILibrary.OSSCLIB_QCURSOR_UI32BOP_AND);
		} catch (UnsupportedEncodingException e) {
			throw new OsseException(e.getMessage(), null);
		} catch (IOException e) {
			throw new OsseException(e.getMessage(), null);
		} finally {
			if (fastStringArray != null)
				fastStringArray.close();
		}
	}
}
