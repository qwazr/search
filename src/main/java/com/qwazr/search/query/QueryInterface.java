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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.qwazr.search.index.QueryContext;
import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BlendedTerm.class),
    @JsonSubTypes.Type(value = Bool.class),
    @JsonSubTypes.Type(value = Boost.class),
    @JsonSubTypes.Type(value = CommonTerms.class),
    @JsonSubTypes.Type(value = ConstantScore.class),
    @JsonSubTypes.Type(value = DisjunctionMax.class),
    @JsonSubTypes.Type(value = DocValuesFieldExistsQuery.class),
    @JsonSubTypes.Type(value = DoubleDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = DoubleDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = DoubleMultiRange.class),
    @JsonSubTypes.Type(value = DoubleRange.class),
    @JsonSubTypes.Type(value = DoubleSet.class),
    @JsonSubTypes.Type(value = DrillDown.class),
    @JsonSubTypes.Type(value = ExactDouble.class),
    @JsonSubTypes.Type(value = ExactFloat.class),
    @JsonSubTypes.Type(value = ExactInteger.class),
    @JsonSubTypes.Type(value = ExactLong.class),
    @JsonSubTypes.Type(value = FacetPath.class),
    @JsonSubTypes.Type(value = FloatDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = FloatDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = FloatMultiRange.class),
    @JsonSubTypes.Type(value = FloatRange.class),
    @JsonSubTypes.Type(value = FloatSet.class),
    @JsonSubTypes.Type(value = Function.class),
    @JsonSubTypes.Type(value = FunctionScore.class),
    @JsonSubTypes.Type(value = Fuzzy.class),
    @JsonSubTypes.Type(value = Geo3DDistanceQuery.class),
    @JsonSubTypes.Type(value = Geo3DBoxQuery.class),
    @JsonSubTypes.Type(value = HasTerm.class),
    @JsonSubTypes.Type(value = IntDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = IntDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = IntegerMultiRange.class),
    @JsonSubTypes.Type(value = IntegerRange.class),
    @JsonSubTypes.Type(value = IntegerSet.class),
    @JsonSubTypes.Type(value = Join.class),
    @JsonSubTypes.Type(value = LatLonPointBBoxQuery.class),
    @JsonSubTypes.Type(value = LatLonPointDistanceQuery.class),
    @JsonSubTypes.Type(value = LatLonPointPolygonQuery.class),
    @JsonSubTypes.Type(value = LongDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = LongDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = LongMultiRange.class),
    @JsonSubTypes.Type(value = LongRange.class),
    @JsonSubTypes.Type(value = LongSet.class),
    @JsonSubTypes.Type(value = MatchAllDocs.class),
    @JsonSubTypes.Type(value = MatchNoDocs.class),
    @JsonSubTypes.Type(value = MultiPhrase.class),
    @JsonSubTypes.Type(value = MoreLikeThis.class),
    @JsonSubTypes.Type(value = MultiFieldQuery.class),
    @JsonSubTypes.Type(value = MultiFieldQueryParser.class),
    @JsonSubTypes.Type(value = NGramPhrase.class),
    @JsonSubTypes.Type(value = PayloadScoreQuery.class),
    @JsonSubTypes.Type(value = Phrase.class),
    @JsonSubTypes.Type(value = Prefix.class),
    @JsonSubTypes.Type(value = QueryParser.class),
    @JsonSubTypes.Type(value = Regexp.class),
    @JsonSubTypes.Type(value = SimpleQueryParser.class),
    @JsonSubTypes.Type(value = SortedDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = SortedDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = SortedDoubleDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = SortedDoubleDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = SortedFloatDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = SortedFloatDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = SortedIntDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = SortedIntDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = SortedLongDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = SortedLongDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = SortedSetDocValuesExactQuery.class),
    @JsonSubTypes.Type(value = SortedSetDocValuesRangeQuery.class),
    @JsonSubTypes.Type(value = SpanContainingQuery.class),
    @JsonSubTypes.Type(value = SpanFirstQuery.class),
    @JsonSubTypes.Type(value = SpanNearQuery.class),
    @JsonSubTypes.Type(value = SpanNotQuery.class),
    @JsonSubTypes.Type(value = SpanOrQuery.class),
    @JsonSubTypes.Type(value = SpanPositionsQuery.class),
    @JsonSubTypes.Type(value = SpanQueryWrapper.class),
    @JsonSubTypes.Type(value = SpanTermQuery.class),
    @JsonSubTypes.Type(value = SpanWithinQuery.class),
    @JsonSubTypes.Type(value = SynonymQuery.class),
    @JsonSubTypes.Type(value = TermQuery.class),
    @JsonSubTypes.Type(value = TermRange.class),
    @JsonSubTypes.Type(value = TermsQuery.class),
    @JsonSubTypes.Type(value = Wildcard.class)})
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    creatorVisibility = JsonAutoDetect.Visibility.NONE,
    fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public interface QueryInterface {

    @JsonIgnore
    Query getQuery(final QueryContext queryContext)
        throws IOException, ParseException, QueryNodeException, ReflectiveOperationException;

    String CORE_BASE_DOC_URI = "https://lucene.apache.org/core/8_5_2/";

}

