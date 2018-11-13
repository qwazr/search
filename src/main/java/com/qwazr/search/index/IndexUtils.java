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
package com.qwazr.search.index;

import com.qwazr.search.field.FieldDefinition;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;

class IndexUtils {

	static SortedSetDocValuesReaderState getNewFacetsState(final IndexReader indexReader, final String stateFacetField)
			throws IOException {
		try {
			return new DefaultSortedSetDocValuesReaderState(indexReader,
					stateFacetField == null ? FieldDefinition.DEFAULT_SORTEDSET_FACET_FIELD : stateFacetField);
		} catch (IllegalArgumentException e) {
			if (e.getMessage().contains("was not indexed with SortedSetDocValues"))
				return null;
			throw e;
		}
	}

}
