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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.Equalizer;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;

import java.io.IOException;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "source")
@JsonSubTypes({ @JsonSubTypes.Type(value = ConstValueSource.class),
		@JsonSubTypes.Type(value = DefFunction.class),
		@JsonSubTypes.Type(value = DivFloatFunction.class),
		@JsonSubTypes.Type(value = DoubleConstValueSource.class),
		@JsonSubTypes.Type(value = DoubleFieldSource.class),
		@JsonSubTypes.Type(value = FloatFieldSource.class),
		@JsonSubTypes.Type(value = IfFunction.class),
		@JsonSubTypes.Type(value = IntFieldSource.class),
		@JsonSubTypes.Type(value = LongFieldSource.class),
		@JsonSubTypes.Type(value = MaxDocValueSource.class),
		@JsonSubTypes.Type(value = MaxFloatFunction.class),
		@JsonSubTypes.Type(value = MinFloatFunction.class),
		@JsonSubTypes.Type(value = MultiValuedDoubleFieldSource.class),
		@JsonSubTypes.Type(value = MultiValuedFloatFieldSource.class),
		@JsonSubTypes.Type(value = MultiValuedIntFieldSource.class),
		@JsonSubTypes.Type(value = MultiValuedLongFieldSource.class),
		@JsonSubTypes.Type(value = NumDocsValueSource.class),
		@JsonSubTypes.Type(value = PowFloatFunction.class),
		@JsonSubTypes.Type(value = ProductFloatFunction.class),
		@JsonSubTypes.Type(value = QueryValueSource.class),
		@JsonSubTypes.Type(value = SortedSetFieldSource.class),
		@JsonSubTypes.Type(value = SumFloatFunction.class) })
public abstract class AbstractValueSource<T extends AbstractValueSource> extends Equalizer<T> {

	protected AbstractValueSource(final Class<T> ownClass) {
		super(ownClass);
	}

	@JsonIgnore
	abstract public ValueSource getValueSource(final QueryContext queryContext)
			throws QueryNodeException, ReflectiveOperationException, ParseException, IOException;

}
