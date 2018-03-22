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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.automaton.Operations;
import org.apache.lucene.util.automaton.RegExp;

import java.util.Objects;

public class RegexpQuery extends AbstractFieldQuery<RegexpQuery> {

	final public String text;
	final public Integer flags;
	final public Integer max_determinized_states;

	@JsonCreator
	public RegexpQuery(@JsonProperty("generic_field") final String genericField,
			@JsonProperty("field") final String field, @JsonProperty("text") final String text,
			@JsonProperty("flags") final Integer flags,
			@JsonProperty("max_determinized_states") final Integer maxDeterminizedStates) {
		super(RegexpQuery.class, genericField, field);
		this.text = text;
		this.flags = flags;
		this.max_determinized_states = maxDeterminizedStates;
	}

	public RegexpQuery(final String field, final String text, final Integer flags,
			final Integer maxDeterminizedStates) {
		this(null, field, text, flags, maxDeterminizedStates);
	}

	public RegexpQuery(final String field, final String text, final Integer flags) {
		this(field, text, flags, null);
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) {
		return new org.apache.lucene.search.RegexpQuery(getResolvedTerm(queryContext.getFieldMap(), text),
				flags == null ? RegExp.ALL : flags,
				max_determinized_states == null ? Operations.DEFAULT_MAX_DETERMINIZED_STATES : max_determinized_states);
	}

	@Override
	@JsonIgnore
	protected boolean isEqual(RegexpQuery q) {
		return super.isEqual(q) && Objects.equals(text, q.text) && Objects.equals(flags, q.flags) &&
				Objects.equals(max_determinized_states, q.max_determinized_states);
	}
}
