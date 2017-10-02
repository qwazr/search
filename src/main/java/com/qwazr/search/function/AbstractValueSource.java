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
import java.util.ArrayList;
import java.util.List;

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
		@JsonSubTypes.Type(value = MaxFloatFunction.class),
		@JsonSubTypes.Type(value = MinFloatFunction.class),
		@JsonSubTypes.Type(value = NumDocsValueSource.class),
		@JsonSubTypes.Type(value = PowFloatFunction.class),
		@JsonSubTypes.Type(value = ProductFloatFunction.class),
		@JsonSubTypes.Type(value = QueryValueSource.class),
		@JsonSubTypes.Type(value = SortedSetFieldSource.class),
		@JsonSubTypes.Type(value = SumFloatFunction.class) })
public abstract class AbstractValueSource<T extends AbstractValueSource> extends Equalizer<T> {

	protected AbstractValueSource(Class<T> ownClass) {
		super(ownClass);
	}

	@JsonIgnore
	public abstract ValueSource getValueSource(QueryContext queryContext)
			throws ParseException, IOException, QueryNodeException, ReflectiveOperationException;

	@JsonIgnore
	public static final ValueSource[] getValueSourceArray(QueryContext queryContext, AbstractValueSource[] sources)
			throws ParseException, IOException, QueryNodeException, ReflectiveOperationException {
		if (sources == null)
			return null;
		ValueSource[] valueSources = new ValueSource[sources.length];
		int i = 0;
		for (AbstractValueSource source : sources)
			valueSources[i++] = source.getValueSource(queryContext);
		return valueSources;
	}

	@JsonIgnore
	public static List<ValueSource> getValueSourceList(QueryContext queryContext, AbstractValueSource[] sources)
			throws ParseException, IOException, QueryNodeException, ReflectiveOperationException {
		if (sources == null)
			return null;
		final List<ValueSource> valueSources = new ArrayList<>(sources.length);
		for (AbstractValueSource source : sources)
			valueSources.add(source.getValueSource(queryContext));
		return valueSources;
	}

}
