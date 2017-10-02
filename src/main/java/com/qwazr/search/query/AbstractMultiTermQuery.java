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
package com.qwazr.search.query;

import com.qwazr.search.index.BytesRefUtils;
import org.apache.lucene.util.BytesRef;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMultiTermQuery<T extends AbstractMultiTermQuery> extends AbstractFieldQuery<T> {

	protected AbstractMultiTermQuery(final Class<T> queryClass, final String genericField, final String field) {
		super(queryClass, genericField, field);
	}

	protected AbstractMultiTermQuery(final Class<T> queryClass, final MultiTermBuilder builder) {
		super(queryClass, builder);
	}

	abstract static public class MultiTermBuilder<T extends AbstractMultiTermQuery> extends AbstractFieldBuilder {

		final List<BytesRef> bytesRefs = new ArrayList<>();
		final List<Object> objects = new ArrayList<>();

		protected MultiTermBuilder(final String genericField, final String field) {
			super(genericField, field);
		}

		final public MultiTermBuilder<T> add(final BytesRef... bytes) {
			if (bytes != null)
				for (BytesRef b : bytes)
					if (b != null)
						bytesRefs.add(b);
			return this;
		}

		final public MultiTermBuilder<T> add(final String... term) {
			if (term != null)
				for (String t : term)
					if (t != null) {
						bytesRefs.add(BytesRefUtils.Converter.STRING.from(t));
						objects.add(t);
					}
			return this;
		}

		final public MultiTermBuilder<T> add(final Integer... value) {
			if (value != null)
				for (Integer v : value)
					if (v != null) {
						bytesRefs.add(BytesRefUtils.Converter.INT.from(v));
						objects.add(v);
					}
			return this;
		}

		final public MultiTermBuilder<T> add(final Float... value) {
			if (value != null)
				for (Float v : value)
					if (v != null) {
						bytesRefs.add(BytesRefUtils.Converter.FLOAT.from(v));
						objects.add(v);
					}
			return this;
		}

		final public MultiTermBuilder<T> add(final Long... value) {
			if (value != null)
				for (Long v : value)
					if (v != null) {
						bytesRefs.add(BytesRefUtils.Converter.LONG.from(v));
						objects.add(v);
					}
			return this;
		}

		final public MultiTermBuilder<T> add(final Double... value) {
			if (value != null)
				for (Double v : value)
					if (v != null) {
						bytesRefs.add(BytesRefUtils.Converter.DOUBLE.from(v));
						objects.add(v);
					}
			return this;
		}

		abstract public T build();
	}
}
