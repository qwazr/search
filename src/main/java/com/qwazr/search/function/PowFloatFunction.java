/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
package com.qwazr.search.function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;

import java.io.IOException;
import java.util.Objects;

public class PowFloatFunction extends AbstractValueSource<PowFloatFunction> {

	public final AbstractValueSource a;
	public final AbstractValueSource b;

	@JsonCreator
	public PowFloatFunction(@JsonProperty("a") AbstractValueSource a, @JsonProperty("b") AbstractValueSource b) {
		super(PowFloatFunction.class);
		this.a = Objects.requireNonNull(a, "a value source is missing");
		this.b = Objects.requireNonNull(b, "b value source is missing");
	}

	@Override
	public ValueSource getValueSource(final QueryContext queryContext)
			throws ReflectiveOperationException, IOException, ParseException, QueryNodeException {
		return new org.apache.lucene.queries.function.valuesource.PowFloatFunction(b.getValueSource(queryContext),
				b.getValueSource(queryContext));
	}

	@Override
	protected boolean isEqual(final PowFloatFunction query) {
		return Objects.equals(a, query.a) && Objects.equals(b, query.b);
	}
}
