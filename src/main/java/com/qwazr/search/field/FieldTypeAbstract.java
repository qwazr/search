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
import com.qwazr.server.ServerException;
import com.qwazr.utils.ArrayUtils;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.util.BytesRef;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

abstract class FieldTypeAbstract<T extends FieldDefinition> implements FieldTypeInterface {

    final protected String genericFieldName;
    final private WildcardMatcher wildcardMatcher;
    final protected T definition;
    final protected BytesRefUtils.Converter<?> bytesRefConverter;
    final private FieldTypeInterface.Facet[] facetConfig;
    final private Map<FieldTypeInterface, String> copyToFields;

    protected FieldTypeAbstract(final Builder<T> builder) {
        this.genericFieldName = builder.genericFieldName;
        this.wildcardMatcher = builder.wildcardMatcher;
        this.definition = builder.definition;
        this.bytesRefConverter = builder.bytesRefConverter;
        prepareFacet(builder);
        this.facetConfig = builder.facetConfig == null || builder.facetConfig.isEmpty() ?
            null :
            builder.facetConfig.toArray(new Facet[0]);
        this.copyToFields = new LinkedHashMap<>();
    }

    protected abstract void prepareFacet(final Builder<T> builder);

    public final void setFacetsConfig(final String fieldName, final FieldMap fieldMap,
                                      final FacetsConfig facetsConfig) {
        if (facetConfig != null)
            for (FieldTypeInterface.Facet config : facetConfig)
                config.config(fieldName, fieldMap, facetsConfig);
    }

    @Override
    public ValueConverter<?> getConverter(String fieldName, MultiReader reader) {
        return null;
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
            newField(fieldName, ArrayUtils.toPrimitive((Byte[]) value), documentBuilder);
        else if (value instanceof Object[])
            fillArray(fieldName, (Object[]) value, documentBuilder);
        else if (value instanceof Collection)
            fillCollection(fieldName, (Collection) value, documentBuilder);
        else if (value instanceof Map)
            fillMap(fieldName, (Map) value, documentBuilder);
        else
            newField(fieldName, value, documentBuilder);
    }

    abstract protected void newField(final String fieldName,
                                     final Object value,
                                     final DocumentBuilder documentBuilder);

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
    final public Object toTerm(final BytesRef bytesRef) {
        return bytesRef == null ? null : bytesRefConverter == null ? null : bytesRefConverter.to(bytesRef);
    }

    static <T extends FieldDefinition> Builder<T> of(String genericFieldName, WildcardMatcher wildcardMatcher,
                                                     T definition) {
        return new Builder<>(genericFieldName, wildcardMatcher, definition);
    }

    static class Builder<T extends FieldDefinition> {

        final T definition;
        private final String genericFieldName;
        private final WildcardMatcher wildcardMatcher;
        private BytesRefUtils.Converter<?> bytesRefConverter;
        private LinkedHashSet<Facet> facetConfig;

        Builder(String genericFieldName, WildcardMatcher wildcardMatcher, T definition) {
            this.genericFieldName = genericFieldName;
            this.wildcardMatcher = wildcardMatcher;
            this.definition = definition;
        }

        Builder<T> bytesRefConverter(BytesRefUtils.Converter<?> bytesRefConverter) {
            this.bytesRefConverter = bytesRefConverter;
            return this;
        }

        Builder<T> facetConfig(FieldTypeInterface.Facet facetConfig) {
            if (this.facetConfig == null)
                this.facetConfig = new LinkedHashSet<>();
            this.facetConfig.add(facetConfig);
            return this;
        }

    }
}
