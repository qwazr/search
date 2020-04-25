/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
package com.qwazr.search.field.converters;

import com.qwazr.binder.setter.FieldSetter;

import java.io.IOException;

public abstract class ValueConverter<T> {

	final MultiReader reader;
	final String field;

	protected ValueConverter(final MultiReader reader, final String field) {
		this.reader = reader;
		this.field = field;
	}

	public abstract T convert(final int docId) throws IOException;

	public abstract void fill(final Object record, final FieldSetter fieldSetter, final int docId) throws IOException;

	@FunctionalInterface
	public interface Supplier<T> {
		ValueConverter<T> getConverter(final MultiReader reader, final String field);
	}

	public final static ValueConverter.Supplier NullSupplier = (reader, field) -> null;

}
