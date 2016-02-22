/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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

import com.qwazr.search.index.QueryContext;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DefFunction extends AbstractValueSource {

	public final List<AbstractValueSource> sources;

	public DefFunction() {
		sources = null;
	}

	public DefFunction(AbstractValueSource... sources) {
		this.sources = sources == null ? null : Arrays.asList(sources);
	}

	public DefFunction(List<AbstractValueSource> sources) {
		this.sources = sources;
	}

	@Override
	public ValueSource getValueSource(QueryContext queryContext)
					throws ParseException, IOException, QueryNodeException, ReflectiveOperationException {
		Objects.requireNonNull(sources, "The sources list is missing (sources)");
		return new org.apache.lucene.queries.function.valuesource.DefFunction(
						AbstractValueSource.getValueSourceList(queryContext, sources));
	}
}
