/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Objects;

public class SortedDocValuesRangeQuery extends AbstractRangeQuery<String, SortedDocValuesRangeQuery> {

	@JsonProperty("lower_inclusive")
	public final Boolean lowerInclusive;

	@JsonProperty("upper_inclusive")
	public final Boolean upperInclusive;

	@JsonCreator
	public SortedDocValuesRangeQuery(@JsonProperty("generic_field") final String genericField,
			@JsonProperty("field") final String field, @JsonProperty("lower_value") final String lowerValue,
			@JsonProperty("upper_value") final String upperValue,
			@JsonProperty("lower_inclusive") final Boolean lowerInclusive,
			@JsonProperty("upper_inclusive") final Boolean upperInclusive) {
		super(SortedDocValuesRangeQuery.class, genericField, field,
				Objects.requireNonNull(lowerValue, "The lower value is null"),
				Objects.requireNonNull(upperValue, "The upper value is null"));
		this.lowerInclusive = lowerInclusive == null ? true : lowerInclusive;
		this.upperInclusive = upperInclusive == null ? false : upperInclusive;
	}

	public SortedDocValuesRangeQuery(final String field, final String lowerValue, final String upperValue,
			final Boolean lowerInclusive, final Boolean upperInclusive) {
		this(null, field, lowerValue, upperValue, lowerInclusive, upperInclusive);
	}

	@Override
	public Query getQuery(final QueryContext queryContext) throws IOException {
		return SortedDocValuesField.newRangeQuery(resolveField(queryContext.getFieldMap()), new BytesRef(lower_value),
				new BytesRef(upper_value), lowerInclusive, upperInclusive);
	}
}
