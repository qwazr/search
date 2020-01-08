/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

package com.qwazr.search.field.Converters;

import com.qwazr.search.index.BytesRefUtils;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiReader {

    final int[] docBases;
    final LeafReader[] leafReaders;

    public MultiReader(final IndexReader reader) {
        docBases = new int[reader.leaves().size()];
        leafReaders = new LeafReader[docBases.length];
        int i = 0;
        for (LeafReaderContext leafReaderContext : reader.leaves()) {
            docBases[i] = leafReaderContext.docBase;
            leafReaders[i++] = leafReaderContext.reader();
        }
    }

    final static int NOT_FOUND = -1;

    int getLeafReader(final int docId) {
        int i = NOT_FOUND;
        for (final int docBase : docBases) {
            if (docBase > docId)
                break;
            i++;
        }
        return i <= docBases.length ? i : NOT_FOUND;
    }

    long getNumericDocValues(final int docId, final String field) throws IOException {
        final int pos = getLeafReader(docId);
        if (pos == NOT_FOUND)
            return 0;
        final NumericDocValues docValues = leafReaders[pos].getNumericDocValues(field);
        if (docValues == null)
            return 0;
        synchronized (docValues) {
            final int target = docId - docBases[pos];
            if (docValues.advance(target) != target)
                return 0;
            return docValues.longValue();
        }
    }

    BytesRef getSortedDocValues(final int docId, final String field) throws IOException {
        final int pos = getLeafReader(docId);
        if (pos == NOT_FOUND)
            return BytesRefUtils.EMPTY;
        final SortedDocValues docValues = leafReaders[pos].getSortedDocValues(field);
        if (docValues == null)
            return BytesRefUtils.EMPTY;
        synchronized (docValues) {
            final int target = docId - docBases[pos];
            if (docValues.advance(target) != target)
                return BytesRefUtils.EMPTY;
            return BytesRef.deepCopyOf(docValues.binaryValue());
        }
    }

    BytesRef getBinaryDocValues(final int docId, final String field) throws IOException {
        final int pos = getLeafReader(docId);
        if (pos == NOT_FOUND)
            return BytesRefUtils.EMPTY;
        final BinaryDocValues docValues = leafReaders[pos].getBinaryDocValues(field);
        if (docValues == null)
            return BytesRefUtils.EMPTY;
        synchronized (docValues) {
            final int target = docId - docBases[pos];
            if (docValues.advance(target) != target)
                return BytesRefUtils.EMPTY;
            return docValues.binaryValue();
        }
    }

    final static long[] empty = new long[0];

    long[] getSortedNumericDocValues(final int docId, final String field) throws IOException {
        final int pos = getLeafReader(docId);
        if (pos == NOT_FOUND)
            return empty;
        final SortedNumericDocValues docValues = leafReaders[pos].getSortedNumericDocValues(field);
        if (docValues == null)
            return empty;
        synchronized (docValues) {
            final int target = docId - docBases[pos];
            if (docValues.advance(target) != target)
                return empty;
            final int count = docValues.docValueCount();
            if (count == 0)
                return empty;
            final long[] values = new long[count];
            for (int i = 0; i < count; i++)
                values[i] = docValues.nextValue();
            return values;
        }
    }

    List<String> getSortedSetDocValues(final int docId, final String field) throws IOException {
        final int pos = getLeafReader(docId);
        if (pos == NOT_FOUND)
            return Collections.emptyList();
        final SortedSetDocValues docValues = leafReaders[pos].getSortedSetDocValues(field);
        if (docValues == null)
            return Collections.emptyList();
        synchronized (docValues) {
            final int target = docId - docBases[pos];
            if (docValues.advance(target) != target)
                return Collections.emptyList();
            final List<String> values = new ArrayList<>();
            long ord = -1;
            try {
                while ((ord = docValues.nextOrd()) != SortedSetDocValues.NO_MORE_ORDS) {
                    values.add(docValues.lookupOrd(ord).utf8ToString());
                    System.out.println("Value: " + ord + " - " + values.size());
                }
                return values;
            }
            catch (IndexOutOfBoundsException e) {
                throw new IOException("Ord: " + ord, e);
            }
        }
    }

}
