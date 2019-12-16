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
package com.qwazr.search.query.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.payloads.PayloadDecoder;
import org.apache.lucene.queries.payloads.PayloadFunction;
import org.apache.lucene.queries.payloads.PayloadScoreQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.QueryBuilder;

public class PayloadQueryBuilder extends QueryBuilder {

    private final PayloadFunction payloadFunction;
    private final boolean includeSpanScore;

    public PayloadQueryBuilder(Analyzer analyzer, PayloadFunction payloadFunction, boolean includeSpanScore) {
        super(analyzer);
        this.payloadFunction = payloadFunction;
        this.includeSpanScore = includeSpanScore;
    }

    @Override
    protected Query newTermQuery(final Term term) {
        return new PayloadScoreQuery(new SpanTermQuery(term), payloadFunction, PayloadDecoder.FLOAT_DECODER, includeSpanScore);
    }
}
