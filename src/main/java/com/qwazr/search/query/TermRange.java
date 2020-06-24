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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.annotations.QuerySampleCreator;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.QueryContext;
import com.qwazr.utils.StringUtils;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.TermRangeQuery;

public class TermRange extends AbstractFieldQuery<TermRange> {

    @JsonProperty("lower_term")
    final public String lowerTerm;
    @JsonProperty("upper_term")
    final public String upperTerm;
    @JsonProperty("include_lower")
    final public Boolean includeLower;
    @JsonProperty("include_upper")
    final public Boolean includeUpper;

    @JsonCreator
    public TermRange(@JsonProperty("generic_field") final String genericField,
                     @JsonProperty("field") final String field,
                     @JsonProperty("lower_term") final String lowerTerm,
                     @JsonProperty("upper_term") final String upperTerm,
                     @JsonProperty("include_lower") final Boolean includeLower,
                     @JsonProperty("include_upper") final Boolean includeUpper) {
        super(TermRange.class, genericField, field);
        this.lowerTerm = lowerTerm;
        this.upperTerm = upperTerm;
        this.includeLower = includeLower;
        this.includeUpper = includeUpper;
    }

    public TermRange(final String field,
                     final String lowerTerm,
                     final String upperTerm,
                     final Boolean includeLower,
                     final Boolean includeUpper) {
        this(null, field, lowerTerm, upperTerm, includeLower, includeUpper);
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/search/TermRangeQuery.html")
    public TermRange(final IndexSettingsDefinition settings,
                     final Map<String, AnalyzerDefinition> analyzers,
                     final Map<String, FieldDefinition> fields) {
        this(getFullTextField(fields, () -> getTextField(fields, () -> "text")), "A", "Z", true, true);
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(final TermRange q) {
        return super.isEqual(q)
            && Objects.equals(lowerTerm, q.lowerTerm)
            && Objects.equals(upperTerm, q.upperTerm)
            && Objects.equals(includeLower, q.includeLower)
            && Objects.equals(includeUpper, q.includeUpper);
    }

    @Override
    final public MultiTermQuery getQuery(final QueryContext queryContext) {
        final String fieldName = resolveIndexTextField(queryContext.getFieldMap(), StringUtils.EMPTY);
        return TermRangeQuery.newStringRange(fieldName,
            lowerTerm, upperTerm, includeLower == null ? true : includeLower,
            includeUpper == null ? true : includeUpper
        );
    }
}
