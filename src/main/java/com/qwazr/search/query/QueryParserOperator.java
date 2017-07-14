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
package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;

public enum QueryParserOperator {

	and(QueryParser.Operator.AND, StandardQueryConfigHandler.Operator.AND), AND(QueryParser.Operator.AND,
			StandardQueryConfigHandler.Operator.AND), or(QueryParser.Operator.OR,
			StandardQueryConfigHandler.Operator.OR), OR(QueryParser.Operator.OR,
			StandardQueryConfigHandler.Operator.OR);

	@JsonIgnore
	final QueryParser.Operator queryParseroperator;

	@JsonIgnore
	final StandardQueryConfigHandler.Operator queryConfigHandlerOperator;

	QueryParserOperator(QueryParser.Operator queryParseroperator,
			StandardQueryConfigHandler.Operator queryConfigHandlerOperator) {
		this.queryParseroperator = queryParseroperator;
		this.queryConfigHandlerOperator = queryConfigHandlerOperator;
	}
}
