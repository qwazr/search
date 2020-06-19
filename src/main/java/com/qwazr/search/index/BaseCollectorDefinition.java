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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.utils.Equalizer;
import java.util.Arrays;
import java.util.Objects;

public class BaseCollectorDefinition extends Equalizer.Immutable<BaseCollectorDefinition>
    implements QueryDefinition.CollectorDefinition {

    final private String classname;

    final private Object[] arguments;

    @JsonCreator
    public BaseCollectorDefinition(@JsonProperty("class") final String classname,
                                   @JsonProperty("arguments") final Object... arguments) {
        super(BaseCollectorDefinition.class);
        this.classname = classname;
        this.arguments = arguments == null || arguments.length == 0 ? null : arguments;
    }

    @Override
    protected int computeHashCode() {
        return Objects.hashCode(classname);
    }

    @Override
    protected boolean isEqual(final BaseCollectorDefinition other) {
        return Objects.equals(classname, other.classname) && Arrays.equals(arguments, other.arguments);
    }

    @Override
    public String getClassname() {
        return classname;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }
}
