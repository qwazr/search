/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
package com.qwazr.search.function;

import org.apache.lucene.queries.function.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractValueSources<T extends AbstractValueSources> extends AbstractValueSource<T> {

	public final AbstractValueSource[] sources;

	protected AbstractValueSources(final Class<T> ownClass, final ValueSource valueSource,
			final AbstractValueSource... sources) {
		super(ownClass, valueSource);
		this.sources = sources;
	}

	protected static ValueSource[] getValueSourceArray(final AbstractValueSource... sources) {
		Objects.requireNonNull(sources, "The source list is missing (sources)");
		final ValueSource[] valueSources = new ValueSource[sources.length];
		int i = 0;
		for (AbstractValueSource source : sources)
			valueSources[i++] = source.getValueSource();
		return valueSources;
	}

	protected static List<ValueSource> getValueSourceList(final AbstractValueSource... sources) {
		Objects.requireNonNull(sources, "The source list is missing (sources)");
		final List<ValueSource> valueSources = new ArrayList<>(sources.length);
		for (AbstractValueSource source : sources)
			valueSources.add(source.getValueSource());
		return valueSources;
	}

}
