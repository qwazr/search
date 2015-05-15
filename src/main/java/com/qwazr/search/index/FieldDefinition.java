/**
 * Copyright 2015 Emmanuel Keller / QWAZR
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
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.jaeksoft.searchlib.index.osse.api.OsseJNILibrary;

@JsonInclude(Include.NON_EMPTY)
public class FieldDefinition {

	final public Boolean term_freq;
	final public Boolean offset;
	final public Boolean position;
	final public Boolean stored;
	final public Boolean vector;

	public FieldDefinition(Boolean term_freq, Boolean offset, Boolean position,
			Boolean stored, Boolean vector) {
		this.term_freq = term_freq;
		this.offset = offset;
		this.position = position;
		this.stored = stored;
		this.vector = vector;
	}

	public FieldDefinition() {
		this(null, null, null, null, null);
	}

	public FieldDefinition(int fieldFlags) {
		offset = checkFlag(fieldFlags,
				OsseJNILibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_OFFSET);
		position = checkFlag(fieldFlags,
				OsseJNILibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_POSITION);
		vector = checkFlag(fieldFlags,
				OsseJNILibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_VSM1);
		term_freq = checkFlag(fieldFlags,
				OsseJNILibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_TERMFREQ);
		stored = checkFlag(fieldFlags,
				OsseJNILibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_STORED);
	}

	private boolean checkFlag(int flags, int flag) {
		return (flags & flag) == flag;
	}

	int getFlags() {
		int flags = 0;
		if (offset != null && offset)
			flags = flags | OsseJNILibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_OFFSET;
		if (position != null && position)
			flags = flags
					| OsseJNILibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_POSITION;
		if (vector != null && vector)
			flags = flags | OsseJNILibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_VSM1;
		if (term_freq != null && term_freq)
			flags = flags
					| OsseJNILibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_TERMFREQ;
		if (stored != null && stored)
			flags = flags | OsseJNILibrary.OSSCLIB_FIELD_UI32FIELDFLAGS_STORED;
		return flags;
	}
}
