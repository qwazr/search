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
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qwazr.search.field.FieldTypeInterface;
import org.apache.lucene.index.Terms;

import java.io.IOException;

@JsonInclude(Include.NON_EMPTY)
public class FieldStats {

	@JsonProperty("doc_count")
	final public Integer docCount;

	@JsonProperty("sum_doc_freq")
	final public Long sumDocFreq;

	@JsonProperty("sum_total_term_freq")
	final public Long sumTotalTermFreq;

	final public Object min;

	final public Object max;

	@JsonProperty("number_of_terms")
	final public Long numberOfTerms;

	public FieldStats() {
		docCount = null;
		sumDocFreq = null;
		sumTotalTermFreq = null;
		min = null;
		max = null;
		numberOfTerms = null;
	}

	FieldStats(final Terms terms, final FieldTypeInterface fieldType) throws IOException {
		docCount = terms.getDocCount();
		sumDocFreq = terms.getSumDocFreq();
		sumTotalTermFreq = terms.getSumTotalTermFreq();
		if (fieldType != null) {
			min = fieldType.toTerm(terms.getMin());
			max = fieldType.toTerm(terms.getMax());
		} else {
			min = null;
			max = null;
		}
		numberOfTerms = terms.size();
	}

}