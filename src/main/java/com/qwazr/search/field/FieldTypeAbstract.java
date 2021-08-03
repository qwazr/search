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
package com.qwazr.search.field;

import com.qwazr.search.field.converters.MultiReader;
import com.qwazr.search.field.converters.ValueConverter;
import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.DocumentBuilder;
import com.qwazr.search.index.FieldsContext;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.server.ServerException;
import com.qwazr.utils.ArrayUtils;
import com.qwazr.utils.WildcardMatcher;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.core.Response;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;

abstract class FieldTypeAbstract<T extends FieldDefinition> implements FieldTypeInterface {

    @NotNull
    final protected String genericFieldName;
    final private WildcardMatcher wildcardMatcher;
    @NotNull
    final protected T definition;
    @NotNull
    final protected BytesRefUtils.Converter<?> bytesRefConverter;
    @NotNull
    final private FieldSupplier fieldSupplier;
    @NotNull
    final protected FacetsConfigSupplier facetsConfigSupplier;
    @NotNull
    final private SortFieldSupplier sortFieldSupplier;
    @NotNull
    final private TermSupplier primaryTermSupplier;
    @NotNull
    final private FieldNameResolver fieldNameResolver;
    @NotNull
    final private Map<FieldTypeInterface, String> copyToFields;

    final private Set<FieldType> fieldTypes;
    final private ValueType valueType;


    protected FieldTypeAbstract(final Builder<T> builder) {
        this.genericFieldName = builder.genericFieldName;
        this.wildcardMatcher = builder.wildcardMatcher;
        this.definition = builder.definition;
        this.bytesRefConverter = Objects.requireNonNull(builder.bytesRefConverter);
        this.fieldSupplier = builder.fieldSupplier;
        this.facetsConfigSupplier = builder.facetsConfigSupplier;
        this.sortFieldSupplier = builder.sortFieldSupplier;
        this.primaryTermSupplier = builder.primaryTermSupplier;
        this.fieldNameResolver = builder.fieldNameResolver;
        this.copyToFields = new LinkedHashMap<>();
        this.fieldTypes = builder.fieldTypes == null ? Collections.emptySet() : Collections.unmodifiableSet(builder.fieldTypes);
        this.valueType = builder.valueType;
    }

    protected static <T> boolean addIfNotNull(final T item, final List<T> itemList) {
        if (item == null)
            return false;
        itemList.add(item);
        return true;
    }

    protected static FieldSupplier reduceFieldSuppliers(@NotNull final List<FieldSupplier> fieldSupplierList) {
        if (fieldSupplierList.isEmpty())
            return null;
        if (fieldSupplierList.size() == 1)
            return fieldSupplierList.get(0);
        final FieldSupplier[] fieldSuppliers = fieldSupplierList.toArray(new FieldSupplier[0]);
        return (fieldName, value, documentBuilder) -> {
            for (FieldSupplier fieldSupplier : fieldSuppliers)
                fieldSupplier.addFields(fieldName, value, documentBuilder);
        };
    }

    @Override
    public final void applyFacetsConfig(final String fieldName,
                                        final FieldsContext fieldsContext,
                                        final FacetsConfig facetsConfig) {
        facetsConfigSupplier.setConfig(fieldName, fieldsContext, facetsConfig);
    }

    @Override
    public ValueConverter<?> getConverter(String fieldName, MultiReader reader) {
        return null;
    }

    final public SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
        final SortField sortField = sortFieldSupplier.newSortField(fieldName, sortEnum);
        if (sortField == null)
            throw new NotAcceptableException("This field does not support sorting: "
                + (genericFieldName == null ? fieldName : genericFieldName));
        return sortField;
    }

    @Override
    final public void copyTo(final String fieldName, final FieldTypeInterface fieldType) {
        copyToFields.put(fieldType, fieldName);
    }

    @Override
    final public FieldDefinition getDefinition() {
        return definition;
    }

    @Override
    final public ValueType getValueType() {
        return valueType;
    }

    @Override
    final public FieldType findFirstOf(final FieldType... expectedTypes) {
        for (final FieldType expectedType : expectedTypes)
            if (fieldTypes.contains(expectedType))
                return expectedType;
        return null;
    }

    protected void fillArray(final String fieldName, final int[] values, final DocumentBuilder<?> documentBuilder) {
        for (int value : values)
            fill(fieldName, value, documentBuilder);
    }

    protected void fillArray(final String fieldName, final long[] values, final DocumentBuilder<?> documentBuilder) {
        for (long value : values)
            fill(fieldName, value, documentBuilder);
    }

    protected void fillArray(final String fieldName, final double[] values, final DocumentBuilder<?> documentBuilder) {
        for (double value : values)
            fill(fieldName, value, documentBuilder);
    }

    protected void fillArray(final String fieldName, final float[] values, final DocumentBuilder<?> documentBuilder) {
        for (float value : values)
            fill(fieldName, value, documentBuilder);
    }

    protected void fillArray(final String fieldName, final Object[] values, final DocumentBuilder<?> documentBuilder) {
        for (Object value : values)
            fill(fieldName, value, documentBuilder);
    }

    protected void fillArray(final String fieldName, final String[] values, final DocumentBuilder<?> documentBuilder) {
        for (String value : values)
            fill(fieldName, value, documentBuilder);
    }

    protected void fillCollection(final String fieldName,
                                  final Collection<Object> values,
                                  final DocumentBuilder<?> documentBuilder) {
        values.forEach(value -> {
            if (value != null)
                fill(fieldName, value, documentBuilder);
        });
    }

    protected void fillMap(final String fieldName,
                           final Map<Object, Object> values,
                           final DocumentBuilder<?> documentBuilder) {
        throw new ServerException(Response.Status.NOT_ACCEPTABLE,
            "Map is not a supported type for the field: " + fieldName);
    }

    protected void fillWildcardMatcher(final String wildcardName, final Object value,
                                       final DocumentBuilder<?> documentBuilder) {
        if (value instanceof Map) {
            ((Map<String, Object>) value).forEach((fieldName, valueObject) -> {
                if (!wildcardMatcher.match(fieldName))
                    throw new ServerException(Response.Status.NOT_ACCEPTABLE,
                        "The field name does not match the field pattern: " + wildcardName);
                fill(fieldName, valueObject, documentBuilder);
            });
        } else
            fill(wildcardName, value, documentBuilder);
    }

    protected void fill(final String fieldName,
                        final Object value,
                        final DocumentBuilder<?> documentBuilder) {
        if (value == null || fieldSupplier == null)
            return;
        if (value instanceof String[])
            fillArray(fieldName, (String[]) value, documentBuilder);
        else if (value instanceof int[])
            fillArray(fieldName, (int[]) value, documentBuilder);
        else if (value instanceof long[])
            fillArray(fieldName, (long[]) value, documentBuilder);
        else if (value instanceof double[])
            fillArray(fieldName, (double[]) value, documentBuilder);
        else if (value instanceof float[])
            fillArray(fieldName, (float[]) value, documentBuilder);
        else if (value instanceof Byte[])
            fieldSupplier.addFields(fieldName, ArrayUtils.toPrimitive((Byte[]) value), documentBuilder);
        else if (value instanceof Object[])
            fillArray(fieldName, (Object[]) value, documentBuilder);
        else if (value instanceof Collection)
            fillCollection(fieldName, (Collection) value, documentBuilder);
        else if (value instanceof Map)
            fillMap(fieldName, (Map) value, documentBuilder);
        else
            fieldSupplier.addFields(fieldName, value, documentBuilder);
    }

    @Override
    final public void dispatch(final String fieldName,
                               final Object value,
                               final DocumentBuilder<?> documentBuilder) {
        if (value == null)
            return;
        if (wildcardMatcher != null)
            fillWildcardMatcher(fieldName, value, documentBuilder);
        else {
            fill(fieldName, value, documentBuilder);
            if (!copyToFields.isEmpty())
                copyToFields.forEach(
                    (fieldType, copyFieldName) -> fieldType.dispatch(copyFieldName, value, documentBuilder));
        }
    }

    @Override
    final public String resolveFieldName(final String fieldName, final FieldType fieldType, final ValueType valueType) {
        return fieldNameResolver.resolve(fieldName, fieldType, valueType);
    }

    @Override
    final public Term newPrimaryTerm(final String fieldName, final Object value) {
        final Term term = primaryTermSupplier.newTerm(fieldName, value);
        if (term == null)
            throw new NotAcceptableException(
                "This field cannot be used as a primary key: " + (genericFieldName == null ? fieldName : genericFieldName));
        return term;
    }

    @Override
    final public Object toTerm(final BytesRef bytesRef) {
        return bytesRef == null ? null : bytesRefConverter.to(bytesRef);
    }

    static <T extends FieldDefinition> Builder<T> of(final String genericFieldName,
                                                     final WildcardMatcher wildcardMatcher,
                                                     final T definition) {
        return new Builder<>(genericFieldName, wildcardMatcher, definition);
    }

    protected static class Builder<T extends FieldDefinition> {

        protected final String genericFieldName;
        protected final WildcardMatcher wildcardMatcher;
        protected final T definition;


        private BytesRefUtils.Converter<?> bytesRefConverter;
        private FieldSupplier fieldSupplier;
        private FacetsConfigSupplier facetsConfigSupplier;
        private SortFieldSupplier sortFieldSupplier;
        private TermSupplier primaryTermSupplier;
        private FieldNameResolver fieldNameResolver;
        private Set<FieldType> fieldTypes;
        private ValueType valueType;

        protected Builder(final String genericFieldName, final WildcardMatcher wildcardMatcher, final T definition) {
            this.genericFieldName = genericFieldName;
            this.wildcardMatcher = wildcardMatcher;
            this.definition = definition;
            this.bytesRefConverter = BytesRefUtils.Converter.NOPE;
            this.fieldSupplier = (fieldName, value, documentBuilder) -> {
            };
            this.facetsConfigSupplier = (fieldName, fieldsContext, facetsConfig) -> {
            };
            this.sortFieldSupplier = (fieldName, sortEnum) -> null;
            this.primaryTermSupplier = (fieldName, value) -> null;
            this.fieldNameResolver = (name, field, value) -> name;
        }

        public Builder<T> bytesRefConverter(BytesRefUtils.Converter<?> bytesRefConverter) {
            this.bytesRefConverter = bytesRefConverter;
            return this;
        }

        public Builder<T> fieldSupplier(FieldSupplier fieldSupplier) {
            this.fieldSupplier = fieldSupplier;
            return this;
        }

        public Builder<T> facetsConfigSupplier(FacetsConfigSupplier facetsConfigSupplier) {
            this.facetsConfigSupplier = facetsConfigSupplier;
            return this;
        }

        public Builder<T> sortFieldSupplier(SortFieldSupplier sortFieldSupplier) {
            this.sortFieldSupplier = sortFieldSupplier;
            return this;
        }

        public Builder<T> primaryTermSupplier(TermSupplier primaryTermSupplier) {
            this.primaryTermSupplier = primaryTermSupplier;
            return this;
        }

        public Builder<T> fieldNameResolver(FieldNameResolver fieldNameResolver) {
            this.fieldNameResolver = fieldNameResolver;
            return this;
        }

        public Builder<T> fieldType(FieldType fieldType) {
            if (fieldTypes == null)
                fieldTypes = new HashSet<>();
            this.fieldTypes.add(fieldType);
            return this;
        }

        public Builder<T> fieldTypes(Collection<FieldType> fieldTypes) {
            for (final FieldType fieldType : fieldTypes)
                fieldType(fieldType);
            return this;
        }

        public Builder<T> valueType(ValueType valueType) {
            this.valueType = valueType;
            return this;
        }

    }
}
