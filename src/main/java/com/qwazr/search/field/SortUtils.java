/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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

import com.qwazr.search.index.QueryDefinition;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

import java.util.LinkedHashMap;
import java.util.Map;

public class SortUtils {

	final static boolean sortReverse(QueryDefinition.SortEnum sortEnum) {
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

	final static void sortStringMissingValue(QueryDefinition.SortEnum sortEnum, SortField sortField) {
		if (sortEnum == null)
			return;
		switch (sortEnum) {
		case ascending:
		case descending:
			return;
		case ascending_missing_last:
		case descending_missing_first:
			sortField.setMissingValue(SortField.STRING_LAST);
		case ascending_missing_first:
		case descending_missing_last:
			sortField.setMissingValue(SortField.STRING_FIRST);
		}
	}

	final static void sortDoubleMissingValue(QueryDefinition.SortEnum sortEnum, SortField sortField) {
		if (sortEnum == null)
			return;
		switch (sortEnum) {
		case ascending:
		case descending:
			return;
		case ascending_missing_last:
		case descending_missing_first:
			sortField.setMissingValue(Double.MAX_VALUE);
		case ascending_missing_first:
		case descending_missing_last:
			sortField.setMissingValue(Double.MIN_VALUE);
		}
	}

	final static void sortLongMissingValue(QueryDefinition.SortEnum sortEnum, SortField sortField) {
		if (sortEnum == null)
			return;
		switch (sortEnum) {
		case ascending:
		case descending:
			return;
		case ascending_missing_last:
		case descending_missing_first:
			sortField.setMissingValue(Long.MAX_VALUE);
		case ascending_missing_first:
		case descending_missing_last:
			sortField.setMissingValue(Long.MIN_VALUE);
		}
	}

	final static void sortFloatMissingValue(QueryDefinition.SortEnum sortEnum, SortField sortField) {
		if (sortEnum == null)
			return;
		switch (sortEnum) {
		case ascending:
		case descending:
			return;
		case ascending_missing_last:
		case descending_missing_first:
			sortField.setMissingValue(Float.MAX_VALUE);
		case ascending_missing_first:
		case descending_missing_last:
			sortField.setMissingValue(Float.MIN_VALUE);
		}
	}

	final static void sortIntMissingValue(QueryDefinition.SortEnum sortEnum, SortField sortField) {
		if (sortEnum == null)
			return;
		switch (sortEnum) {
		case ascending:
		case descending:
			return;
		case ascending_missing_last:
		case descending_missing_first:
			sortField.setMissingValue(Integer.MAX_VALUE);
		case ascending_missing_first:
		case descending_missing_last:
			sortField.setMissingValue(Integer.MIN_VALUE);
		}
	}

	final static SortField buildSortField(final Map<String, FieldTypeInterface> fields, final String fieldName,
			final QueryDefinition.SortEnum sortEnum) {

		// First check the by score and by doc_id sorting
		if (FieldDefinition.SCORE_FIELD.equals(fieldName))
			return new SortField(null, SortField.Type.SCORE, !sortReverse(sortEnum));
		if (FieldDefinition.DOC_FIELD.equals(fieldName))
			return new SortField(null, SortField.Type.DOC, sortReverse(sortEnum));

		// Let's check if the field exists and supports sorting
		FieldTypeInterface fieldType = fields.get(fieldName);
		if (fieldType == null)
			throw new IllegalArgumentException("Unknown sort field: " + fieldName);
		SortField sortField = fieldType.getSortField(fieldName, sortEnum);
		if (sortField == null)
			throw new IllegalArgumentException("The field does not support sorting: " + fieldName);
		return sortField;
	}

	final public static Sort buildSort(Map<String, FieldTypeInterface> fields,
			LinkedHashMap<String, QueryDefinition.SortEnum> sorts) {
		if (sorts.isEmpty())
			return null;
		final SortField[] sortFields = new SortField[sorts.size()];
		int i = 0;
		for (Map.Entry<String, QueryDefinition.SortEnum> sort : sorts.entrySet())
			sortFields[i++] = buildSortField(fields, sort.getKey(), sort.getValue());
		if (sortFields.length == 1)
			return new Sort(sortFields[0]);
		return new Sort(sortFields);
	}
}
