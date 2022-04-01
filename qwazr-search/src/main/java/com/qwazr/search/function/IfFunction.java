/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

public class IfFunction extends AbstractValueSource<IfFunction> {

    public final AbstractValueSource<?> ifSource;
    public final AbstractValueSource<?> trueSource;
    public final AbstractValueSource<?> falseSource;

    @JsonCreator
    public IfFunction(final @JsonProperty("ifSource") AbstractValueSource<?> ifSource,
                      final @JsonProperty("trueSource") AbstractValueSource<?> trueSource,
                      final @JsonProperty("falseSource") AbstractValueSource<?> falseSource) {
        super(IfFunction.class);
        this.ifSource = Objects.requireNonNull(ifSource, "ifSource value source is missing");
        this.trueSource = Objects.requireNonNull(trueSource, "trueSource value source is missing");
        this.falseSource = Objects.requireNonNull(falseSource, "falseSource value source is missing");
    }

    @Override
    public ValueSource getValueSource(final QueryContext queryContext)
        throws ReflectiveOperationException, IOException, ParseException, QueryNodeException {
        return new org.apache.lucene.queries.function.valuesource.IfFunction(ifSource.getValueSource(queryContext),
            trueSource.getValueSource(queryContext), falseSource.getValueSource(queryContext));
    }

    @Override
    protected boolean isEqual(final IfFunction query) {
        return Objects.equals(ifSource, query.ifSource)
            && Objects.equals(trueSource, query.trueSource)
            && Objects.equals(falseSource, query.falseSource);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(ifSource, trueSource, falseSource);
    }
}
