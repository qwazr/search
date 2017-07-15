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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.FieldMap;
import com.qwazr.search.index.QueryContext;
import com.qwazr.search.query.lucene.SimpleQueryParserFix;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class SimpleQueryParser extends AbstractQueryBuilder {

	final public LinkedHashMap<String, Float> weights;

	@JsonProperty("default_operator")
	final public QueryParserOperator defaultOperator;

	final public Boolean and_operator;
	final public Boolean escape_operator;
	final public Boolean fuzzy_operator;
	final public Boolean near_operator;
	final public Boolean not_operator;
	final public Boolean or_operator;
	final public Boolean phrase_operator;
	final public Boolean precedence_operators;
	final public Boolean prefix_operator;
	final public Boolean whitespace_operator;

	@JsonIgnore
	private final int flags;

	@JsonCreator
	private SimpleQueryParser() {
		weights = null;
		defaultOperator = null;
		and_operator = null;
		escape_operator = null;
		fuzzy_operator = null;
		near_operator = null;
		not_operator = null;
		or_operator = null;
		phrase_operator = null;
		precedence_operators = null;
		prefix_operator = null;
		whitespace_operator = null;

		flags = -2;
	}

	private SimpleQueryParser(Builder builder) {
		super(builder);
		this.weights = builder.weights;
		this.defaultOperator = builder.defaultOperator;
		this.flags = builder.flags;

		and_operator = (flags & org.apache.lucene.queryparser.simple.SimpleQueryParser.AND_OPERATOR) != 0;
		escape_operator = (flags & org.apache.lucene.queryparser.simple.SimpleQueryParser.ESCAPE_OPERATOR) != 0;
		fuzzy_operator = (flags & org.apache.lucene.queryparser.simple.SimpleQueryParser.FUZZY_OPERATOR) != 0;
		near_operator = (flags & org.apache.lucene.queryparser.simple.SimpleQueryParser.NEAR_OPERATOR) != 0;
		not_operator = (flags & org.apache.lucene.queryparser.simple.SimpleQueryParser.NOT_OPERATOR) != 0;
		or_operator = (flags & org.apache.lucene.queryparser.simple.SimpleQueryParser.OR_OPERATOR) != 0;
		phrase_operator = (flags & org.apache.lucene.queryparser.simple.SimpleQueryParser.PHRASE_OPERATOR) != 0;
		precedence_operators =
				(flags & org.apache.lucene.queryparser.simple.SimpleQueryParser.PRECEDENCE_OPERATORS) != 0;
		prefix_operator = (flags & org.apache.lucene.queryparser.simple.SimpleQueryParser.PREFIX_OPERATOR) != 0;
		whitespace_operator = (flags & org.apache.lucene.queryparser.simple.SimpleQueryParser.WHITESPACE_OPERATOR) != 0;
	}

	private int computeTag() {
		int flags = 0;
		if (and_operator == null || and_operator)
			flags = flags | org.apache.lucene.queryparser.simple.SimpleQueryParser.AND_OPERATOR;
		if (escape_operator == null || escape_operator)
			flags = flags | org.apache.lucene.queryparser.simple.SimpleQueryParser.ESCAPE_OPERATOR;
		if (fuzzy_operator == null || fuzzy_operator)
			flags = flags | org.apache.lucene.queryparser.simple.SimpleQueryParser.FUZZY_OPERATOR;
		if (near_operator == null || near_operator)
			flags = flags | org.apache.lucene.queryparser.simple.SimpleQueryParser.NEAR_OPERATOR;
		if (not_operator == null || not_operator)
			flags = flags | org.apache.lucene.queryparser.simple.SimpleQueryParser.NOT_OPERATOR;
		if (or_operator == null || or_operator)
			flags = flags | org.apache.lucene.queryparser.simple.SimpleQueryParser.OR_OPERATOR;
		if (phrase_operator == null || phrase_operator)
			flags = flags | org.apache.lucene.queryparser.simple.SimpleQueryParser.PHRASE_OPERATOR;
		if (precedence_operators == null || precedence_operators)
			flags = flags | org.apache.lucene.queryparser.simple.SimpleQueryParser.PRECEDENCE_OPERATORS;
		if (prefix_operator == null || prefix_operator)
			flags = flags | org.apache.lucene.queryparser.simple.SimpleQueryParser.PREFIX_OPERATOR;
		if (whitespace_operator == null || whitespace_operator)
			flags = flags | org.apache.lucene.queryparser.simple.SimpleQueryParser.WHITESPACE_OPERATOR;
		return flags;
	}

	@Override
	final public Query getQuery(final QueryContext queryContext) throws IOException, ParseException {

		final FieldMap fieldMap = queryContext.getFieldMap();

		final Map<String, Float> resolvedBoosts = fieldMap == null ? weights : fieldMap.resolveQueryFieldNames(weights,
				new HashMap<>());

		final int fl = flags == -2 ? computeTag() : flags;

		final org.apache.lucene.queryparser.simple.SimpleQueryParser parser = new SimpleQueryParserFix(
				analyzer == null ? queryContext.getQueryAnalyzer() : analyzer, resolvedBoosts, fl);

		setQueryBuilderParameters(parser);

		if (defaultOperator != null)
			parser.setDefaultOperator(
					defaultOperator == QueryParserOperator.AND ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD);

		return parser.parse(Objects.requireNonNull(queryString, "The query string is missing"));
	}

	public static Builder of() {
		return new Builder().setFlags(-1);
	}

	public static class Builder extends AbstractBuilder<SimpleQueryParser> {

		private LinkedHashMap<String, Float> weights;
		private QueryParserOperator defaultOperator;
		private int flags;

		public Builder addField(String... fieldSet) {
			for (String field : fieldSet)
				addBoost(field, 1.0f);
			return this;
		}

		public Builder addBoost(String field, Float boost) {
			if (weights == null)
				weights = new LinkedHashMap<>();
			weights.put(field, boost);
			return this;
		}

		public Builder setDefaultOperator(QueryParserOperator defaultOperator) {
			this.defaultOperator = defaultOperator;
			return this;
		}

		public Builder setFlags(int flags) {
			this.flags = flags;
			return this;
		}

		@Override
		public SimpleQueryParser build() {
			return new SimpleQueryParser(this);
		}
	}
}
