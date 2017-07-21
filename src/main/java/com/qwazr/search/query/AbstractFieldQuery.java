/*
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

package com.qwazr.search.query;

import com.qwazr.search.index.BytesRefUtils;
import com.qwazr.search.index.FieldMap;
import org.apache.lucene.index.Term;

import java.util.Objects;

public abstract class AbstractFieldQuery extends AbstractQuery {

	final public String field;

	protected AbstractFieldQuery(final String field) {
		this.field = Objects.requireNonNull(field, "The field is null");
	}

	protected AbstractFieldQuery(final AbstractFieldBuilder builder) {
		this(builder.field);
	}

	static String resolveField(final FieldMap fieldMap, final String field) {
		return fieldMap == null ? field : fieldMap.resolveQueryFieldName(field);
	}

	final protected String resolveField(final FieldMap fieldMap) {
		return resolveField(fieldMap, field);
	}

	static Term getResolvedTerm(final FieldMap fieldMap, final String field, final Object value) {
		return fieldMap == null ? new Term(field, BytesRefUtils.fromAny(value)) : Objects.requireNonNull(
				fieldMap.getFieldType(field), "Unknown field: " + field).term(field, value);
	}

	final protected Term getResolvedTerm(final FieldMap fieldMap, final Object value) {
		return getResolvedTerm(fieldMap, field, value);
	}

	public static abstract class AbstractFieldBuilder {

		final public String field;

		protected AbstractFieldBuilder(final String field) {
			this.field = field;
		}
	}
}
