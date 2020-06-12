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

import com.qwazr.search.index.DocumentBuilder;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.document.LatLonPoint;

import java.util.Map;

final class LatLonPointType extends CustomFieldTypeAbstract {

    LatLonPointType(final String genericFieldName,
                    final WildcardMatcher wildcardMatcher,
                    final CustomFieldDefinition definition) {
        super(genericFieldName, wildcardMatcher, null, null, null, definition);
    }

    @Override
    protected void fillArray(final String fieldName, final double[] values, final DocumentBuilder documentBuilder) {
        if ((values.length & 1) != 0)
            throw new RuntimeException("Expect even double values, but got: " + values.length);
        for (int i = 0; i < values.length; )
            documentBuilder.accept(genericFieldName, fieldName, new LatLonPoint(fieldName, values[i++], values[i++]));
    }

    @Override
    protected void fillArray(final String fieldName, final float[] values, final DocumentBuilder documentBuilder) {
        if ((values.length & 1) != 0)
            throw new RuntimeException("Expect even float values, but got: " + values.length);
        for (int i = 0; i < values.length; )
            documentBuilder.accept(genericFieldName, fieldName, new LatLonPoint(fieldName, values[i++], values[i++]));
    }

    @Override
    protected void fillArray(final String fieldName, final Object[] values, final DocumentBuilder documentBuilder) {
        if ((values.length & 1) != 0)
            throw new RuntimeException("Expect even number values, but got: " + values.length);
        for (int i = 0; i < values.length; )
            documentBuilder.accept(genericFieldName, fieldName,
                new LatLonPoint(fieldName, ((Number) values[i++]).doubleValue(),
                    ((Number) values[i++]).doubleValue()));
    }

    @Override
    protected void fillMap(final String fieldName, final Map<Object, Object> values, final DocumentBuilder documentBuilder) {
        final Number latitude = (Number) values.get("lat");
        TypeUtils.notNull(latitude, fieldName, "The latitude parameter (lat) is missing");
        final Number longitude = (Number) values.get("lon");
        TypeUtils.notNull(longitude, fieldName, "The longitude parameter (lon) is missing");
        documentBuilder.accept(genericFieldName, fieldName,
            new LatLonPoint(fieldName, latitude.doubleValue(), longitude.doubleValue()));
    }

}
