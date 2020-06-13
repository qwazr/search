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
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.server.ServerException;
import com.qwazr.utils.ArrayUtils;
import com.qwazr.utils.WildcardMatcher;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
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
    final private FacetSupplier facetSupplier;
    @NotNull
    final private SortFieldSupplier sortFieldSupplier;
    @NotNull
    final private TermSupplier primaryTermSupplier;
    @NotNull
    final private TermSupplier indexTermSupplier;
    @NotNull
    final private FieldNameResolver indexFieldNameResolver;
    @NotNull
    final private FieldNameResolver storedFieldNameResolver;
    @NotNull
    final private Map<FieldTypeInterface, String> copyToFields;

    protected FieldTypeAbstract(@NotNull final String genericFieldName,
                                final WildcardMatcher wildcardMatcher,
                                final BytesRefUtils.Converter<?> bytesRefConverter,
                                final FieldSupplier fieldSupplier,
                                final FacetSupplier facetSupplier,
                                final SortFieldSupplier sortFieldSupplier,
                                final TermSupplier primaryTermSupplier,
                                final TermSupplier indexTermSupplier,
                                final FieldNameResolver indexFieldNameResolver,
                                final FieldNameResolver storedFieldNameResolver,
                                @NotNull final T definition) {
        this.genericFieldName = genericFieldName;
        this.wildcardMatcher = wildcardMatcher;
        this.definition = definition;
        this.bytesRefConverter = bytesRefConverter == null ? BytesRefUtils.Converter.NOPE : bytesRefConverter;
        this.fieldSupplier = fieldSupplier == null ? (fieldName, value, documentBuilder) -> {
        } : fieldSupplier;
        this.facetSupplier = facetSupplier == null ? (fieldName, fieldMap, facetsConfig) -> {
        } : facetSupplier;
        this.sortFieldSupplier = sortFieldSupplier == null ? (fieldName, sortEnum) -> null : sortFieldSupplier;
        this.primaryTermSupplier = primaryTermSupplier == null ? (fieldName, value) -> null : primaryTermSupplier;
        this.indexTermSupplier = indexTermSupplier == null ? (fieldName, value) -> null : indexTermSupplier;
        this.indexFieldNameResolver = indexFieldNameResolver == null ? (fieldName) -> null : indexFieldNameResolver;
        this.storedFieldNameResolver = storedFieldNameResolver == null ? (fieldName) -> null : storedFieldNameResolver;
        this.copyToFields = new LinkedHashMap<>();
    }

    protected static <T> void addIfNotNull(final T item, final List<T> itemList) {
        if (item != null)
            itemList.add(item);
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

    protected static boolean isStored(final CustomFieldDefinition definition) {
        return definition.stored != null && definition.stored;
    }

    @Override
    public final void applyFacetsConfig(final String fieldName,
                                        final FieldMap fieldMap,
                                        final FacetsConfig facetsConfig) {
        facetSupplier.setConfig(fieldName, fieldMap, facetsConfig);
    }

    @Override
    public ValueConverter<?> getConverter(String fieldName, MultiReader reader) {
        return null;
    }

    final public SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
        return sortFieldSupplier.newSortField(fieldName, sortEnum);
    }

    @Override
    final public void copyTo(final String fieldName, final FieldTypeInterface fieldType) {
        copyToFields.put(fieldType, fieldName);
    }

    @Override
    final public FieldDefinition getDefinition() {
        return definition;
    }

    protected void fillArray(final String fieldName, final int[] values, final DocumentBuilder documentBuilder) {
        for (int value : values)
            fill(fieldName, value, documentBuilder);
    }

    protected void fillArray(final String fieldName, final long[] values, final DocumentBuilder documentBuilder) {
        for (long value : values)
            fill(fieldName, value, documentBuilder);
    }

    protected void fillArray(final String fieldName, final double[] values, final DocumentBuilder documentBuilder) {
        for (double value : values)
            fill(fieldName, value, documentBuilder);
    }

    protected void fillArray(final String fieldName, final float[] values, final DocumentBuilder documentBuilder) {
        for (float value : values)
            fill(fieldName, value, documentBuilder);
    }

    protected void fillArray(final String fieldName, final Object[] values, final DocumentBuilder documentBuilder) {
        for (Object value : values)
            fill(fieldName, value, documentBuilder);
    }

    protected void fillArray(final String fieldName, final String[] values, final DocumentBuilder documentBuilder) {
        for (String value : values)
            fill(fieldName, value, documentBuilder);
    }

    protected void fillCollection(final String fieldName,
                                  final Collection<Object> values,
                                  final DocumentBuilder documentBuilder) {
        values.forEach(value -> {
            if (value != null)
                fill(fieldName, value, documentBuilder);
        });
    }

    protected void fillMap(final String fieldName,
                           final Map<Object, Object> values,
                           final DocumentBuilder documentBuilder) {
        throw new ServerException(Response.Status.NOT_ACCEPTABLE,
            "Map is not asupported type for the field: " + fieldName);
    }

    protected void fillWildcardMatcher(final String wildcardName, final Object value,
                                       final DocumentBuilder documentBuilder) {
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
                        final DocumentBuilder documentBuilder) {
        if (value == null)
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
                               final DocumentBuilder documentBuilder) {
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
    final public String getIndexFieldName(@NotNull final String fieldName) {
        return indexFieldNameResolver.resolve(fieldName);
    }

    @Override
    final public String getStoredFieldName(@NotNull final String fieldName) {
        return storedFieldNameResolver.resolve(fieldName);
    }

    @Override
    final public Term newIndexTerm(final String fieldName, final Object value) {
        return indexTermSupplier.newTerm(fieldName, value);
    }

    @Override
    final public Term newPrimaryTerm(final String fieldName, final Object value) {
        return primaryTermSupplier.newTerm(fieldName, value);
    }

    @Override
    final public Object toTerm(final BytesRef bytesRef) {
        return bytesRef == null ? null : bytesRefConverter.to(bytesRef);
    }

}
