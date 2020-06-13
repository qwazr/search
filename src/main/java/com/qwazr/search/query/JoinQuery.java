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
package com.qwazr.search.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.index.QueryContext;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.join.ScoreMode;

import java.io.IOException;
import java.util.Objects;

public class JoinQuery extends AbstractQuery<JoinQuery> {

    final public String from_index;
    final public String from_field;
    final public String to_field;
    final public Boolean multiple_values_per_document;
    final public ScoreMode score_mode;
    final public AbstractQuery<?> from_query;

    @JsonCreator
    public JoinQuery(@JsonProperty("from_index") final String fromIndex,
                     @JsonProperty("from_field") final String fromField,
                     @JsonProperty("to_field") final String toField,
                     @JsonProperty("multiple_values_per_document") final Boolean multipleValuesPerDocument,
                     @JsonProperty("score_mode") final ScoreMode scoreMode,
                     @JsonProperty("from_query") final AbstractQuery<?> fromQuery) {
        super(JoinQuery.class);
        this.from_index = fromIndex;
        this.from_field = fromField;
        this.to_field = toField;
        this.multiple_values_per_document = multipleValuesPerDocument;
        this.score_mode = scoreMode;
        this.from_query = fromQuery;
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) throws IOException {
        return queryContext.getIndex(from_index).createJoinQuery(this);
    }

    @Override
    protected boolean isEqual(JoinQuery q) {
        return Objects.equals(from_query, q.from_query) && Objects.equals(from_field, q.from_field) &&
            Objects.equals(to_field, q.to_field) &&
            Objects.equals(multiple_values_per_document, q.multiple_values_per_document) &&
            Objects.equals(score_mode, q.score_mode) && Objects.equals(from_query, q.from_query);
    }

}
