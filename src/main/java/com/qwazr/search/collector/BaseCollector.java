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
package com.qwazr.search.collector;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;

import java.io.IOException;

public class BaseCollector<T> implements Collector {

	public final String name;

	public BaseCollector(String name) {
		this.name = name;
	}

	public T getResult() {
		return null;
	}

	@Override
	public LeafCollector getLeafCollector(final LeafReaderContext context) throws IOException {
		return DoNothingCollector.INSTANCE;
	}

	@Override
	public boolean needsScores() {
		return false;
	}

}
