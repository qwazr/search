/**
 * Copyright 2015 OpenSearchServer Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import java.util.ArrayList;
import java.util.List;

class FieldContentBuilder {

	private List<String> termList = null;
	private List<Integer> incrementList = null;
	private List<Integer> offsetStartList = null;
	private List<Integer> offsetEndList = null;

	public FieldContentBuilder addTerm(String term) {
		if (termList == null)
			termList = new ArrayList<String>();
		termList.add(term);
		return this;
	}

	public FieldContentBuilder addTerm(String term, int increment) {
		addTerm(term);
		addIncrement(increment);
		return this;
	}

	public FieldContentBuilder addTerm(String term, int increment,
			int offset_start, int offset_end) {
		addTerm(term);
		addIncrement(increment);
		addOffset(offset_start, offset_end);
		return this;
	}

	public FieldContentBuilder addTerm(String term, int offset_start,
			int offset_end) {
		addTerm(term);
		addOffset(offset_start, offset_end);
		return this;
	}

	public FieldContentBuilder addIncrement(int increment) {
		if (incrementList == null)
			incrementList = new ArrayList<Integer>();
		incrementList.add(increment);
		return this;
	}

	public FieldContentBuilder addOffset(int offset_start, int offset_end) {
		if (offsetStartList == null)
			offsetStartList = new ArrayList<Integer>();
		offsetStartList.add(offset_start);
		if (offsetEndList == null)
			offsetEndList = new ArrayList<Integer>();
		offsetEndList.add(offset_end);
		return this;
	}

	/**
	 * Create a new FieldContent object
	 * 
	 * @return a new FieldContent instance
	 */
	public FieldContent build() {
		String[] terms = termList == null || termList.isEmpty() ? null
				: termList.toArray(new String[termList.size()]);
		Integer[] increments = incrementList == null || incrementList.isEmpty() ? null
				: incrementList.toArray(new Integer[incrementList.size()]);
		Integer[] offsets_start = offsetStartList == null
				|| offsetStartList.isEmpty() ? null : offsetStartList
				.toArray(new Integer[offsetStartList.size()]);
		Integer[] offsets_end = offsetEndList == null
				|| offsetEndList.isEmpty() ? null : offsetEndList
				.toArray(new Integer[offsetEndList.size()]);
		return new FieldContent(terms, increments, offsets_start, offsets_end);
	}
}
