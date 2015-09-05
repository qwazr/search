/**
 * Copyright 2015 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FieldDefinition {

	public final String analyzer;
	public final Boolean stored;
	public final Boolean doc_values;

	public FieldDefinition() {
		this(null, null, null);
	}

	public FieldDefinition(String analyzer, Boolean stored, Boolean doc_values) {
		this.analyzer = analyzer;
		this.stored = stored;
		this.doc_values = doc_values;
	}
}
