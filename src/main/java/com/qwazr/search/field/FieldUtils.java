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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qwazr.search.index.QueryDefinition;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FieldUtils {

	private static Number checkNumberType(String fieldName, Object value) {
		if (!(value instanceof Number))
			throw new IllegalArgumentException(
					"Wrong value type for the field: " + fieldName + " - " + value.getClass().getSimpleName());
		return (Number) value;
	}

	private static BytesRef checkStringBytesRef(Object value) {
		return new BytesRef(value.toString());
	}

	public final static Object getValue(IndexableField field) {
		if (field == null)
			return null;
		String s = field.stringValue();
		if (s != null)
			return s;
		Number n = field.numericValue();
		if (n != null)
			return n;
		return null;
	}

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
}
