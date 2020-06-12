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
import org.apache.lucene.facet.taxonomy.IntAssociationFacetField;

import javax.ws.rs.core.Response;
import java.util.Objects;

final class IntAssociationFacetType extends CustomFieldTypeAbstract {

    IntAssociationFacetType(final String genericFieldName,
                            final WildcardMatcher wildcardMatcher,
                            final CustomFieldDefinition definition) {
        super(genericFieldName, wildcardMatcher, BytesRefUtils.Converter.INT_FACET, null, null, definition);
    }

    @Override
    protected void fillArray(final String fieldName, final Object[] values, final DocumentBuilder consumer) {
        Objects.requireNonNull(values, "The value array is empty");
        if (values.length < 2)
            throw new ServerException(Response.Status.NOT_ACCEPTABLE,
                "Expected at least 2 values - Field: " + fieldName);
        final int assoc = TypeUtils.getIntNumber(fieldName, values[0]);
        final String[] path = TypeUtils.getStringArray(fieldName, values, 1);
        consumer.accept(genericFieldName, fieldName, new IntAssociationFacetField(assoc, fieldName, path));
    }

}
