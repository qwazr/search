/**
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

public abstract class AbstractMultiTermQuery extends AbstractFieldQuery {

	protected AbstractMultiTermQuery(final String field) {
		super(field);
	}

	protected AbstractMultiTermQuery(final MultiTermBuilder builder) {
		super(builder);
	}

	abstract static public class MultiTermBuilder<T extends AbstractMultiTermQuery> extends AbstractFieldBuilder {

		final List<BytesRef> terms = new ArrayList<>();

		protected MultiTermBuilder(final String field) {
			super(field);
		}

		final public MultiTermBuilder<T> add(final BytesRef... bytes) {
			if (bytes != null)
				for (BytesRef b : bytes)
					if (b != null)
						terms.add(b);
			return this;
		}

		final public MultiTermBuilder<T> add(final String... term) {
			if (term != null)
				for (String t : term)
					if (t != null)
						terms.add(BytesRefUtils.Converter.STRING.from(t));
			return this;
		}

		final public MultiTermBuilder<T> add(final Integer... value) {
			if (value != null)
				for (Integer v : value)
					if (v != null)
						terms.add(BytesRefUtils.Converter.INT.from(v));
			return this;
		}

		final public MultiTermBuilder<T> add(final Float... value) {
			if (value != null)
				for (Float v : value)
					if (v != null)
						terms.add(BytesRefUtils.Converter.FLOAT.from(v));
			return this;
		}

		final public MultiTermBuilder<T> add(final Long... value) {
			if (value != null)
				for (Long v : value)
					if (v != null)
						terms.add(BytesRefUtils.Converter.LONG.from(v));
			return this;
		}

		final public MultiTermBuilder<T> add(final Double... value) {
			if (value != null)
				for (Double v : value)
					if (v != null)
						terms.add(BytesRefUtils.Converter.DOUBLE.from(v));
			return this;
		}

		abstract public T build();
	}
}
