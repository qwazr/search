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
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.Equalizer;
import java.util.Date;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(
    creatorVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class ReindexDefinition extends Equalizer.Immutable<ReindexDefinition> {

    public enum Status {
        initialized, running, aborting, done, aborted, error
    }

    public final Date start;

    public final Date end;

    public final Float completion;

    public final Status status;

    public final String error;

    @JsonCreator
    public ReindexDefinition(@JsonProperty("start") final Date start,
                             @JsonProperty("end") final Date end,
                             @JsonProperty("completion") final Float completion,
                             @JsonProperty("status") final Status status,
                             @JsonProperty("error") final String error) {
        super(ReindexDefinition.class);
        this.start = start;
        this.end = end;
        this.completion = completion;
        this.status = status;
        this.error = error;
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(start, end, completion, status, error);
    }

    @Override
    protected boolean isEqual(final ReindexDefinition other) {
        return Objects.equals(start, other.start)
            && Objects.equals(end, other.end)
            && Objects.equals(completion, other.completion)
            && Objects.equals(status, other.status)
            && Objects.equals(error, other.error);
    }

    public static final ReindexDefinition EMPTY = new ReindexDefinition(null, null, null, null, null);
}
