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
package com.qwazr.search.function;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.Collection;

abstract class AbstractValueSourceArray<T extends AbstractValueSourceArray> extends AbstractValueSource<T> {

	public final AbstractValueSource[] sources;

	protected AbstractValueSourceArray(Class<T> ownClass, AbstractValueSource... sources) {
		super(ownClass);
		this.sources = sources;
	}

	protected AbstractValueSourceArray(Class<T> ownClass, Collection<AbstractValueSource> sources) {
		this(ownClass, sources == null ? null : sources.toArray(new AbstractValueSource[sources.size()]));
	}

	@Override
	@JsonIgnore
	protected boolean isEqual(T q) {
		return Arrays.equals(sources, q.sources);
	}

}
