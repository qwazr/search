/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qwazr.search.field;

import javax.validation.constraints.NotNull;
import org.apache.lucene.index.Term;

class FieldUtils {

    private FieldUtils() {
    }

    static float getFloatValue(Object value) {
        return value instanceof Number ? ((Number) value).floatValue() : Float.parseFloat(value.toString());
    }

    static double getDoubleValue(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(value.toString());
    }

    static int getIntValue(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString());
    }

    static long getLongValue(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString());
    }

    static Term newStringTerm(String fieldName, Object value) {
        return new Term(fieldName, value.toString());
    }

    static String getStringValue(@NotNull final Object value) {
        final String stringValue = value.toString();
        return stringValue == null || stringValue.isEmpty() ? null : stringValue;
    }

}
