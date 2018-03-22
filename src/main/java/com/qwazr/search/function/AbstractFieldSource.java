/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
package com.qwazr.search.function;

import java.util.Objects;

public abstract class AbstractFieldSource<T extends AbstractFieldSource> extends AbstractValueSource<T> {

	public final String field;

	protected AbstractFieldSource(final Class<T> ownClass, final String field) {
		super(ownClass);
		this.field = Objects.requireNonNull(field, "The field is missing");
	}

	@Override
	public boolean isEqual(AbstractFieldSource source) {
		return Objects.equals(field, source.field);
	}
}
