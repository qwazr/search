/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Set;

@JsonInclude(Include.NON_EMPTY)
public class BaseQueryDefinition {

	final public Integer start;
	final public Integer rows;
	final public Set<String> returned_fields;
	final public Boolean query_debug;

	public BaseQueryDefinition() {
		start = null;
		rows = null;
		returned_fields = null;
		query_debug = null;
	}

	public BaseQueryDefinition(final QueryBuilder builder) {
		start = builder.start;
		rows = builder.rows;
		returned_fields = builder.returnedFields;
		query_debug = builder.queryDebug;
	}

	@JsonIgnore
	final public int getStartValue() {
		return start == null ? 0 : start;
	}

	@JsonIgnore
	final public int getEndValue() {
		return getStartValue() + (rows == null ? 10 : rows);
	}

}
