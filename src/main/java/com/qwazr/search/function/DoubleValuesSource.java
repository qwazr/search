/*
 *  Copyright 2015-2018 Emmanuel Keller / QWAZR
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.qwazr.search.function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.qwazr.search.index.QueryContext;
import com.qwazr.search.query.AbstractQuery;
import com.qwazr.utils.Equalizer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;

import java.io.IOException;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "from")
@JsonSubTypes({@JsonSubTypes.Type(value = DoubleValuesSource.Constant.class),
        @JsonSubTypes.Type(value = DoubleValuesSource.FloatField.class),
        @JsonSubTypes.Type(value = DoubleValuesSource.DoubleField.class),
        @JsonSubTypes.Type(value = DoubleValuesSource.IntField.class),
        @JsonSubTypes.Type(value = DoubleValuesSource.LongField.class),
        @JsonSubTypes.Type(value = DoubleValuesSource.Query.class),
        @JsonSubTypes.Type(value = DoubleValuesSource.Score.class)

})
public abstract class DoubleValuesSource<T extends DoubleValuesSource<?>> extends Equalizer<T> {

    protected DoubleValuesSource(final Class<T> ownClass) {
        super(ownClass);
    }

    @JsonIgnore
    public abstract org.apache.lucene.search.DoubleValuesSource getValueSource(final QueryContext context) throws QueryNodeException, ReflectiveOperationException, ParseException, IOException;

    public static class Constant extends DoubleValuesSource<Constant> {

        @JsonProperty("value")
        public final double value;

        @JsonCreator
        public Constant(@JsonProperty("value") final double value) {
            super(Constant.class);
            this.value = value;
        }

        @Override
        public org.apache.lucene.search.DoubleValuesSource getValueSource(final QueryContext context) {
            return org.apache.lucene.search.DoubleValuesSource.constant(value);
        }

        @Override
        protected boolean isEqual(final Constant constant) {
            return value == constant.value;
        }
    }

    protected static abstract class AbstractField<T extends AbstractField<T>> extends DoubleValuesSource<T> {

        @JsonProperty("field")
        public final String field;

        public AbstractField(final Class<T> ownClass, final String field) {
            super(ownClass);
            this.field = field;
        }

        @Override
        final protected boolean isEqual(final T other) {
            return Objects.equals(field, other.field);
        }

    }

    public static class DoubleField extends AbstractField<DoubleField> {

        @JsonCreator
        public DoubleField(@JsonProperty("field") final String field) {
            super(DoubleField.class, field);
        }

        @Override
        public org.apache.lucene.search.DoubleValuesSource getValueSource(final QueryContext context) {
            return org.apache.lucene.search.DoubleValuesSource.fromDoubleField(field);
        }
    }

    public static class IntField extends AbstractField<IntField> {

        @JsonCreator
        public IntField(@JsonProperty("field") final String field) {
            super(IntField.class, field);
        }

        @Override
        public org.apache.lucene.search.DoubleValuesSource getValueSource(final QueryContext context) {
            return org.apache.lucene.search.DoubleValuesSource.fromIntField(field);
        }
    }

    public static class FloatField extends AbstractField<FloatField> {

        @JsonCreator
        public FloatField(@JsonProperty("field") final String field) {
            super(FloatField.class, field);
        }

        @Override
        public org.apache.lucene.search.DoubleValuesSource getValueSource(final QueryContext context) {
            return org.apache.lucene.search.DoubleValuesSource.fromFloatField(field);
        }
    }

    public static class LongField extends AbstractField<LongField> {

        @JsonCreator
        public LongField(@JsonProperty("field") final String field) {
            super(LongField.class, field);
        }

        @Override
        public org.apache.lucene.search.DoubleValuesSource getValueSource(final QueryContext context) {
            return org.apache.lucene.search.DoubleValuesSource.fromLongField(field);
        }
    }

    public static class Query extends DoubleValuesSource<Query> {

        @JsonProperty("query")
        public final AbstractQuery<?> query;

        @JsonCreator
        public Query(@JsonProperty("value") final AbstractQuery<?> query) {
            super(Query.class);
            this.query = Objects.requireNonNull(query, "The query is missing");
        }

        @Override
        public org.apache.lucene.search.DoubleValuesSource getValueSource(final QueryContext context) throws QueryNodeException, ReflectiveOperationException, ParseException, IOException {
            return org.apache.lucene.search.DoubleValuesSource.fromQuery(query.getQuery(context));
        }

        @Override
        final protected boolean isEqual(final Query other) {
            return Objects.equals(query, other.query);
        }
    }

    public static class Score extends DoubleValuesSource<Score> {

        public final Score INSTANCE = new Score();

        @JsonCreator
        public Score() {
            super(Score.class);
        }

        @Override
        public org.apache.lucene.search.DoubleValuesSource getValueSource(final QueryContext context) throws QueryNodeException, ReflectiveOperationException, ParseException, IOException {
            return org.apache.lucene.search.DoubleValuesSource.SCORES;
        }

        @Override
        final protected boolean isEqual(final Score other) {
            return true;
        }
    }
}
