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

import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.QueryDefinition;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;

import java.util.LinkedHashMap;
import java.util.Map;

public class SortUtils {

    static boolean sortReverse(QueryDefinition.SortEnum sortEnum) {
        if (sortEnum == null)
            return false;
        switch (sortEnum) {
            case ascending:
            case ascending_missing_last:
            case ascending_missing_first:
                return false;
        }
        return true;
    }

    static void sortStringMissingValue(QueryDefinition.SortEnum sortEnum, SortField sortField) {
        if (sortEnum == null)
            return;
        switch (sortEnum) {
            case ascending:
            case descending:
                return;
            case ascending_missing_last:
            case descending_missing_first:
                sortField.setMissingValue(SortField.STRING_LAST);
                return;
            case ascending_missing_first:
            case descending_missing_last:
                sortField.setMissingValue(SortField.STRING_FIRST);
                return;
            default:
                break;
        }
    }

    static void sortDoubleMissingValue(QueryDefinition.SortEnum sortEnum, SortField sortField) {
        if (sortEnum == null)
            return;
        switch (sortEnum) {
            case ascending:
            case descending:
                return;
            case ascending_missing_last:
            case descending_missing_first:
                sortField.setMissingValue(Double.MAX_VALUE);
                return;
            case ascending_missing_first:
            case descending_missing_last:
                sortField.setMissingValue(Double.MIN_VALUE);
                return;
            default:
                break;
        }
    }

    static void sortLongMissingValue(QueryDefinition.SortEnum sortEnum, SortField sortField) {
        if (sortEnum == null)
            return;
        switch (sortEnum) {
            case ascending:
            case descending:
                return;
            case ascending_missing_last:
            case descending_missing_first:
                sortField.setMissingValue(Long.MAX_VALUE);
                return;
            case ascending_missing_first:
            case descending_missing_last:
                sortField.setMissingValue(Long.MIN_VALUE);
                return;
            default:
                break;
        }
    }

    static void sortFloatMissingValue(QueryDefinition.SortEnum sortEnum, SortField sortField) {
        if (sortEnum == null)
            return;
        switch (sortEnum) {
            case ascending:
            case descending:
                return;
            case ascending_missing_last:
            case descending_missing_first:
                sortField.setMissingValue(Float.MAX_VALUE);
                return;
            case ascending_missing_first:
            case descending_missing_last:
                sortField.setMissingValue(Float.MIN_VALUE);
                return;
            default:
                break;
        }
    }

    static void sortIntMissingValue(QueryDefinition.SortEnum sortEnum, SortField sortField) {
        if (sortEnum == null)
            return;
        switch (sortEnum) {
            case ascending:
            case descending:
                return;
            case ascending_missing_last:
            case descending_missing_first:
                sortField.setMissingValue(Integer.MAX_VALUE);
                return;
            case ascending_missing_first:
            case descending_missing_last:
                sortField.setMissingValue(Integer.MIN_VALUE);
                return;
            default:
                break;
        }
    }

    static SortField doubleSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
        final SortField sortField =
            new SortedNumericSortField(fieldName, SortField.Type.DOUBLE, SortUtils.sortReverse(sortEnum));
        SortUtils.sortDoubleMissingValue(sortEnum, sortField);
        return sortField;
    }

    static SortField floatSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
        final SortField sortField =
            new SortedNumericSortField(fieldName, SortField.Type.FLOAT, SortUtils.sortReverse(sortEnum));
        SortUtils.sortFloatMissingValue(sortEnum, sortField);
        return sortField;
    }

    static SortField integerSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
        final SortField sortField =
            new SortedNumericSortField(fieldName, SortField.Type.INT, SortUtils.sortReverse(sortEnum));
        SortUtils.sortIntMissingValue(sortEnum, sortField);
        return sortField;
    }

    static SortField longSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
        final SortField sortField =
            new SortedNumericSortField(fieldName, SortField.Type.LONG, SortUtils.sortReverse(sortEnum));
        SortUtils.sortLongMissingValue(sortEnum, sortField);
        return sortField;
    }

    static SortField stringSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum) {
        final SortField sortField = new SortField(fieldName, SortField.Type.STRING, SortUtils.sortReverse(sortEnum));
        SortUtils.sortStringMissingValue(sortEnum, sortField);
        return sortField;
    }

    static SortField buildSortField(final FieldMap fieldMap, final String fieldName,
                                    final QueryDefinition.SortEnum sortEnum) {

        // First check the by score and by doc_id sorting
        if (FieldDefinition.SCORE_FIELD.equals(fieldName))
            return new SortField(null, SortField.Type.SCORE, !sortReverse(sortEnum));
        if (FieldDefinition.DOC_FIELD.equals(fieldName))
            return new SortField(null, SortField.Type.DOC, sortReverse(sortEnum));

        // Let's check if the field exists and supports sorting
        final FieldTypeInterface fieldType = fieldMap.getFieldType(null, fieldName);
        if (fieldType == null)
            throw new IllegalArgumentException("Unknown sort field: " + fieldName);
        final SortField sortField = fieldType.getSortField(fieldName, sortEnum);
        if (sortField == null)
            throw new IllegalArgumentException("The field does not support sorting: " + fieldName);
        return sortField;
    }

    public static Sort buildSort(final FieldMap fieldMap, final LinkedHashMap<String, QueryDefinition.SortEnum> sorts) {
        if (sorts.isEmpty())
            return null;
        final SortField[] sortFields = new SortField[sorts.size()];
        int i = 0;
        for (Map.Entry<String, QueryDefinition.SortEnum> sort : sorts.entrySet())
            sortFields[i++] = buildSortField(fieldMap, sort.getKey(), sort.getValue());
        if (sortFields.length == 1)
            return new Sort(sortFields[0]);
        return new Sort(sortFields);
    }
}
