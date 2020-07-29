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

import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.DocumentBuilder;
import com.qwazr.server.ServerException;
import com.qwazr.utils.WildcardMatcher;
import java.util.Objects;
import javax.ws.rs.core.Response;
import org.apache.lucene.facet.taxonomy.FloatAssociationFacetField;

final class FloatAssociationFacetType extends CustomFieldTypeAbstract {

    private FloatAssociationFacetType(final Builder<CustomFieldDefinition> builder) {
        super(builder);
    }

    static FloatAssociationFacetType of(final String genericFieldName,
                                        final WildcardMatcher wildcardMatcher,
                                        final CustomFieldDefinition definition) {
        return new FloatAssociationFacetType(CustomFieldTypeAbstract
            .of(genericFieldName, wildcardMatcher, definition)
            .bytesRefConverter(BytesRefUtils.Converter.FLOAT_FACET)
            .facetsConfigSupplier(buildFacetsConfigSupplier(definition))
            .valueType(ValueType.textType)
            .fieldType(FieldType.facetField));
    }

    @Override
    protected void fillArray(final String fieldName, final Object[] values, final DocumentBuilder<?> documentBuilder) {
        Objects.requireNonNull(values, "The value array is empty");
        if (values.length < 2)
            throw new ServerException(Response.Status.NOT_ACCEPTABLE,
                "Expected at least 2 values - Field: " + fieldName);
        final float assoc = TypeUtils.getFloatNumber(fieldName, values[0]);
        final String[] path = TypeUtils.getStringArray(fieldName, values, 1);
        documentBuilder.acceptFacetField(
            new FloatAssociationFacetField(assoc, fieldName, path),
            fieldName, facetsConfigSupplier);
    }

    private static FacetsConfigSupplier buildFacetsConfigSupplier(final CustomFieldDefinition definition) {
        return CustomFieldTypeAbstract.buildFacetsConfigSuppliers(definition,
            (dimensionName, context, config)
                -> config.setIndexFieldName(dimensionName, FieldDefinition.TAXONOMY_FLOAT_ASSOC_FACET_FIELD));
    }
}
