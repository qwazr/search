/*
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
 */
package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.automaton.Operations;
import org.apache.lucene.util.automaton.RegExp;

import java.io.IOException;

public class RegexpQuery extends AbstractFieldQuery {

	final public String text;
	final public Integer flags;
	final public Integer max_determinized_states;

	public RegexpQuery(final String field, final String text, final Integer flags) {
		this(field, text, flags, null);
	}

	@JsonCreator
	public RegexpQuery(@JsonProperty("field") final String field, @JsonProperty("text")  final String text,
			@JsonProperty("flags") final Integer flags,
			@JsonProperty("max_determinized_states") final Integer maxDeterminizedStates) {
		super(field);
		this.text = text;
		this.flags = flags;
		this.max_determinized_states = maxDeterminizedStates;
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) throws IOException {
		return new org.apache.lucene.search.RegexpQuery(getResolvedTerm(queryContext.getFieldMap(), text),
				flags == null ? RegExp.ALL : flags,
				max_determinized_states == null ? Operations.DEFAULT_MAX_DETERMINIZED_STATES : max_determinized_states);
	}
}
