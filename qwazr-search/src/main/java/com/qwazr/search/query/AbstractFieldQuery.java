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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.field.FieldTypeInterface;
import com.qwazr.search.index.FieldMap;
import java.util.Objects;
import org.apache.lucene.index.Term;

public abstract class AbstractFieldQuery<T extends AbstractFieldQuery<T>> extends AbstractQuery<T> {

    @JsonProperty("generic_field")
    final public String genericField;

    final public String field;

    protected AbstractFieldQuery(final Class<T> queryClass,
                                 final String field) {
        this(queryClass, null, field);
    }

    protected AbstractFieldQuery(final Class<T> queryClass,
                                 final String genericField,
                                 final String field) {
        super(queryClass);
        this.genericField = genericField;
        this.field = Objects.requireNonNull(field, "The field is null");
    }

    protected AbstractFieldQuery(final Class<T> queryClass, final AbstractFieldBuilder<?> builder) {
        this(queryClass, builder.genericField, builder.field);
    }

    final protected String resolveFieldName(final FieldMap fieldMap,
                                            final Object value,
                                            final FieldTypeInterface.ValueType valueType,
                                            final FieldTypeInterface.FieldType... fieldTypes) {
        return FieldResolver.resolveFieldName(fieldMap, genericField, field, value, valueType, fieldTypes);
    }

    final protected String resolveDocValueField(final FieldMap fieldMap,
                                                final Object value,
                                                FieldTypeInterface.ValueType valueType) {
        return FieldResolver.resolveDocValueField(fieldMap, genericField, field, value, valueType);
    }

    final protected String resolvePointField(final FieldMap fieldMap,
                                             final Object value,
                                             FieldTypeInterface.ValueType valueType) {
        return FieldResolver.resolvePointField(fieldMap, genericField, field, value, valueType);
    }


    final protected String resolveIndexTextField(final FieldMap fieldMap,
                                                 final Object value) {
        return FieldResolver.resolveIndexTextField(fieldMap, genericField, field, value);
    }

    final protected String resolveFullTextField(final FieldMap fieldMap,
                                                final Object value) {
        return FieldResolver.resolveFullTextField(fieldMap, genericField, field, value);
    }

    final protected Term resolveIndexTextTerm(final FieldMap fieldMap,
                                              final Object value) {
        return FieldResolver.resolveIndexTextTerm(fieldMap, genericField, field, value);
    }

    final protected Term resolveFullTextTerm(final FieldMap fieldMap,
                                             final Object value) {
        return FieldResolver.resolveFullTextTerm(fieldMap, genericField, field, value);
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(T q) {
        return Objects.equals(genericField, q.genericField) && Objects.equals(field, q.field);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(genericField, field);
    }

    public static abstract class AbstractFieldBuilder<Builder extends AbstractFieldBuilder<Builder>> {

        final public String genericField;
        final public String field;

        protected AbstractFieldBuilder(final String genericField, final String field) {
            this.genericField = genericField;
            this.field = field;
        }

        protected abstract Builder me();
    }
}
