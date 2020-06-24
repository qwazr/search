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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.util.automaton.Operations;
import org.apache.lucene.util.automaton.RegExp;

public class Regexp extends AbstractFieldQuery<Regexp> {

    @JsonProperty("text")
    final public String text;
    @JsonProperty("flags")
    final public List<Flag> flags;
    @JsonProperty("max_determinized_states")
    final public Integer maxDeterminizedStates;

    enum Flag {
        intersection(RegExp.INTERSECTION),
        complement(RegExp.COMPLEMENT),
        empty(RegExp.EMPTY),
        anystring(RegExp.ANYSTRING),
        automaton(RegExp.AUTOMATON),
        interval(RegExp.INTERVAL),
        all(RegExp.ALL),
        none(RegExp.NONE);

        private int value;

        Flag(int value) {
            this.value = value;
        }
    }

    @JsonIgnore
    private final int effectiveFlag;
    @JsonIgnore
    private final int effectiveMaxDeterminizedStates;

    @JsonCreator
    public Regexp(@JsonProperty("generic_field") final String genericField,
                  @JsonProperty("field") final String field,
                  @JsonProperty("text") final String text,
                  @JsonProperty("max_determinized_states") final Integer maxDeterminizedStates,
                  @JsonProperty("flags") final List<Flag> flags) {
        super(Regexp.class, genericField, field);
        this.text = text;
        this.maxDeterminizedStates = maxDeterminizedStates;
        this.flags = flags;
        if (flags == null || flags.isEmpty())
            effectiveFlag = RegExp.ALL;
        else {
            int flag = 0;
            for (Flag f : flags)
                flag = flag | f.value;
            effectiveFlag = flag;
        }
        effectiveMaxDeterminizedStates = maxDeterminizedStates == null ? Operations.DEFAULT_MAX_DETERMINIZED_STATES : maxDeterminizedStates;
    }

    public Regexp(final String field, final String text, final Integer maxDeterminizedStates, final List<Flag> flags) {
        this(null, field, text, maxDeterminizedStates, flags);
    }

    public Regexp(final String field, final String text, final Integer maxDeterminizedStates, final Flag... flags) {
        this(field, text, maxDeterminizedStates, Arrays.asList(flags));
    }

    public Regexp(final String field, final String text, final Flag... flags) {
        this(field, text, null, Arrays.asList(flags));
    }

    @QuerySampleCreator(docUri = CORE_BASE_DOC_URI + "core/org/apache/lucene/search/RegexpQuery.html")
    public Regexp(final IndexSettingsDefinition settings,
                  final Map<String, AnalyzerDefinition> analyzers,
                  final Map<String, FieldDefinition> fields) {
        this(getFullTextField(fields, () -> getTextField(fields, () -> "text")),
            "Hel.*o",
            Operations.DEFAULT_MAX_DETERMINIZED_STATES, Flag.all);
    }

    @Override
    final public Query getQuery(final QueryContext queryContext) {
        return new RegexpQuery(resolveIndexTextTerm(queryContext.getFieldMap(), text), effectiveFlag, effectiveMaxDeterminizedStates);
    }

    @Override
    @JsonIgnore
    protected boolean isEqual(final Regexp q) {
        return super.isEqual(q)
            && Objects.equals(text, q.text)
            && Objects.equals(flags, q.flags)
            && Objects.equals(maxDeterminizedStates, q.maxDeterminizedStates);
    }
}
