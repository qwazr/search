/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import com.qwazr.search.field.Converters.ValueConverter;
import com.qwazr.search.index.FieldConsumer;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.utils.WildcardMatcher;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public interface FieldTypeInterface {

	void dispatch(final String fieldName, final Object value, final FieldConsumer fieldConsumer);

	SortField getSortField(final String fieldName, final QueryDefinition.SortEnum sortEnum);

	ValueConverter getConverter(final String fieldName, final IndexReader reader) throws IOException;

	Object toTerm(final BytesRef bytesRef);

	String getQueryFieldName(String fieldName);

	String getStoredFieldName(String fieldName);

	FieldDefinition getDefinition();

	void copyTo(final String fieldName, final FieldTypeInterface fieldType);

	void setFacetsConfig(String fieldName, FacetsConfig facetsConfig);

	Term term(String fieldName, Object value);

	@FunctionalInterface
	interface Facet {
		void config(String fieldName, FacetsConfig facetsConfig);
	}

	@FunctionalInterface
	interface FieldProvider {
		void fillValue(final String fieldName, final Object value, final FieldConsumer consumer);
	}

	@FunctionalInterface
	interface TermProvider {
		Term term(final String fieldName, final Object value);
	}

	@FunctionalInterface
	interface FieldNameProvider {
		String fieldName(final String fieldName);
	}

	@FunctionalInterface
	interface SortFieldProvider {
		SortField sortField(final String fieldName, final QueryDefinition.SortEnum sortEnum);
	}

	static FieldTypeInterface build(WildcardMatcher wildcardMatcher, FieldDefinition definition) {
		if (definition instanceof CustomFieldDefinition)
			return CustomFieldType.build(wildcardMatcher, (CustomFieldDefinition) definition);
		if (definition instanceof SmartFieldDefinition)
			return SmartFieldType.build(wildcardMatcher, (SmartFieldDefinition) definition);
		return null;
	}

}
