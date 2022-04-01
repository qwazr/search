/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
 *s
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

import com.qwazr.search.index.QueryContext;
import java.util.Arrays;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractValueSources<T extends AbstractValueSources<T>> extends AbstractValueSource<T> {

    public final AbstractValueSource<?>[] sources;

    protected AbstractValueSources(final Class<T> ownClass, final AbstractValueSource<?>... sources) {
        super(ownClass);
        this.sources = Objects.requireNonNull(sources, "The source are mising");
    }

    protected ValueSource[] getValueSourceArray(final QueryContext queryContext)
        throws ReflectiveOperationException, IOException, ParseException, QueryNodeException {
        final ValueSource[] valueSources = new ValueSource[sources.length];
        int i = 0;
        for (AbstractValueSource<?> source : sources)
            valueSources[i++] = source.getValueSource(queryContext);
        return valueSources;
    }

    protected List<ValueSource> getValueSourceList(final QueryContext queryContext)
        throws ReflectiveOperationException, IOException, ParseException, QueryNodeException {
        final List<ValueSource> valueSources = new ArrayList<>(sources.length);
        for (AbstractValueSource<?> source : sources)
            valueSources.add(source.getValueSource(queryContext));
        return valueSources;
    }

    @Override
    protected int computeHashCode() {
        return Arrays.hashCode(sources);
    }

    @Override
    protected boolean isEqual(final T valueSources) {
        int i = 0;
        for (AbstractValueSource<?> source : sources)
            if (!Objects.equals(source, valueSources.sources[i++]))
                return false;
        return true;
    }
}
