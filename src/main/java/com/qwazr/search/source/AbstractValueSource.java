/**
 * Copyright 2015 Emmanuel Keller / QWAZR
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
package com.qwazr.search.source;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.lucene.queries.function.ValueSource;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "source")
@JsonSubTypes({ @JsonSubTypes.Type(value = DoubleFieldSource.class), @JsonSubTypes.Type(value = FloatFieldSource.class),
		@JsonSubTypes.Type(value = IntFieldSource.class), @JsonSubTypes.Type(value = LongFieldSource.class),
		@JsonSubTypes.Type(value = SortedSetFieldSource.class) })
public abstract class AbstractValueSource {

	@JsonIgnore
	public abstract ValueSource getValueSource();
}
