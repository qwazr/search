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
 **/
package com.qwazr.search.index;

import com.qwazr.classloader.ClassLoaderManager;
import com.qwazr.search.field.FieldDefinition;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;

class IndexUtils {

	final static String[] similarityClassPrefixes =
			{ "", "com.qwazr.search.similarity.", "org.apache.lucene.search.similarities." };

	static Similarity findSimilarity(final ClassLoaderManager classLoaderManager, final String similarityClassname)
			throws ReflectiveOperationException, IOException {
		return classLoaderManager.newInstance(similarityClassname, similarityClassPrefixes);
	}

	static SortedSetDocValuesReaderState getNewFacetsState(final IndexReader indexReader) throws IOException {
		return new DefaultSortedSetDocValuesReaderState(indexReader, FieldDefinition.SORTEDSET_FACET_FIELD);
	}

}
