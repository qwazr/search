/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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

public abstract class AbstractFieldQuery extends AbstractQuery {

	final public String field;

	protected AbstractFieldQuery() {
		this.field = null;
	}

	protected AbstractFieldQuery(String field) {
		this.field = field;
	}

	public abstract class AbstractFieldBuilder {

		final public String field;

		protected AbstractFieldBuilder(String field) {
			this.field = field;
		}
	}
}
