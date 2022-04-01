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
package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qwazr.search.index.BytesRefUtils;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.util.BytesRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractMultiTermQuery<T extends AbstractMultiTermQuery<T>> extends AbstractFieldQuery<T> {

    final protected MultiTermQuery.RewriteMethod rewriteMethod;

    protected AbstractMultiTermQuery(final Class<T> queryClass, final String genericField, final String field,
                                     final MultiTermQuery.RewriteMethod rewriteMethod) {
        super(queryClass, genericField, field);
        this.rewriteMethod = rewriteMethod;
    }

    protected AbstractMultiTermQuery(final Class<T> queryClass, final MultiTermBuilder<?,?> builder) {
        super(queryClass, builder);
        this.rewriteMethod = builder.rewriteMethod;
    }

    final protected MultiTermQuery applyRewriteMethod(final MultiTermQuery query) {
        if (rewriteMethod != null && query != null)
            query.setRewriteMethod(rewriteMethod);
        return query;
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(T q) {
        return super.isEqual(q) && Objects.equals(rewriteMethod, q.rewriteMethod);
    }

    abstract static public class MultiTermBuilder<Query extends AbstractMultiTermQuery<Query>, Builder extends MultiTermBuilder<Query, Builder>>
        extends AbstractFieldBuilder<Builder> {

        MultiTermQuery.RewriteMethod rewriteMethod;
        final List<BytesRef> bytesRefs = new ArrayList<>();
        final List<Object> objects = new ArrayList<>();

        protected MultiTermBuilder(final String genericField, final String field) {
            super(genericField, field);
        }

        final public Builder add(final BytesRef... bytes) {
            if (bytes != null)
                for (BytesRef b : bytes)
                    if (b != null)
                        bytesRefs.add(b);
            return me();
        }

        final public Builder add(final String... term) {
            if (term != null)
                for (String t : term)
                    if (t != null) {
                        bytesRefs.add(BytesRefUtils.Converter.STRING.from(t));
                        objects.add(t);
                    }
            return me();
        }

        final public Builder add(final Integer... value) {
            if (value != null)
                for (Integer v : value)
                    if (v != null) {
                        bytesRefs.add(BytesRefUtils.Converter.INT.from(v));
                        objects.add(v);
                    }
            return me();
        }

        final public Builder add(final Float... value) {
            if (value != null)
                for (Float v : value)
                    if (v != null) {
                        bytesRefs.add(BytesRefUtils.Converter.FLOAT.from(v));
                        objects.add(v);
                    }
            return me();
        }

        final public Builder add(final Long... value) {
            if (value != null)
                for (Long v : value)
                    if (v != null) {
                        bytesRefs.add(BytesRefUtils.Converter.LONG.from(v));
                        objects.add(v);
                    }
            return me();
        }

        final public Builder add(final Double... value) {
            if (value != null)
                for (Double v : value)
                    if (v != null) {
                        bytesRefs.add(BytesRefUtils.Converter.DOUBLE.from(v));
                        objects.add(v);
                    }
            return me();
        }

        public Builder rewriteMethod(MultiTermQuery.RewriteMethod rewriteMethod) {
            this.rewriteMethod = rewriteMethod;
            return me();
        }

        abstract public Query build();
    }
}
