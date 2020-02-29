/**
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.search.ScoreMode;

import java.io.IOException;

public abstract class DocValuesCollector<CollectorResult, LeafCollector extends org.apache.lucene.search.LeafCollector, ThisCollector, DocValues>
        extends BaseCollector.Parallel<CollectorResult, LeafCollector, ThisCollector> {

    protected final String fieldName;

    private DocValuesCollector(final String fieldName) {
        super(ScoreMode.COMPLETE_NO_SCORES);
        this.fieldName = fieldName;
    }

    protected abstract LeafCollector newLeafCollector(final LeafReader leafReader, final DocValues docValues)
            throws IOException;

    protected abstract DocValues getDocValues(final LeafReader leafReader) throws IOException;

    @Override
    final public LeafCollector newLeafCollector(final LeafReaderContext context) throws IOException {
        final LeafReader leafReader = context.reader();
        final FieldInfo fieldInfo = leafReader.getFieldInfos().fieldInfo(fieldName);
        if (fieldInfo == null)
            return null;
        final DocValuesType type = fieldInfo.getDocValuesType();
        if (type == null)
            return null;
        final DocValues docValues = getDocValues(leafReader);
        if (docValues == null)
            return null;
        return newLeafCollector(leafReader, docValues);
    }

    public static abstract class Binary<CollectorResult, LeafCollector extends org.apache.lucene.search.LeafCollector>
            extends DocValuesCollector<CollectorResult, LeafCollector, Binary<CollectorResult, LeafCollector>, BinaryDocValues> {

        protected Binary(final String fieldName) {
            super(fieldName);
        }

        final protected BinaryDocValues getDocValues(final LeafReader leafReader) throws IOException {
            return leafReader.getBinaryDocValues(fieldName);
        }
    }

    public static abstract class Sorted<CollectorResult, LeafCollector extends org.apache.lucene.search.LeafCollector>
            extends DocValuesCollector<CollectorResult, LeafCollector, Sorted<CollectorResult, LeafCollector>, SortedDocValues> {

        protected Sorted(final String fieldName) {
            super(fieldName);
        }

        final protected SortedDocValues getDocValues(final LeafReader leafReader) throws IOException {
            return leafReader.getSortedDocValues(fieldName);
        }
    }

    public static abstract class SortedSet<CollectorResult, LeafCollector extends org.apache.lucene.search.LeafCollector>
            extends DocValuesCollector<CollectorResult, LeafCollector, SortedSet<CollectorResult, LeafCollector>, SortedSetDocValues> {

        protected SortedSet(final String fieldName) {
            super(fieldName);
        }

        final protected SortedSetDocValues getDocValues(final LeafReader leafReader) throws IOException {
            return leafReader.getSortedSetDocValues(fieldName);
        }
    }

    public static abstract class Numeric<CollectorResult extends Comparable<CollectorResult>, LeafCollector extends org.apache.lucene.search.LeafCollector>
            extends DocValuesCollector<CollectorResult, LeafCollector, Numeric<CollectorResult, LeafCollector>, NumericDocValues> {

        protected Numeric(final String fieldName) {
            super(fieldName);
        }

        final protected NumericDocValues getDocValues(final LeafReader leafReader) throws IOException {
            return leafReader.getNumericDocValues(fieldName);
        }
    }

    public static abstract class SortedNumeric<CollectorResult, LeafCollector extends org.apache.lucene.search.LeafCollector>
            extends DocValuesCollector<CollectorResult, LeafCollector, SortedNumeric<CollectorResult, LeafCollector>, SortedNumericDocValues> {

        protected SortedNumeric(final String fieldName) {
            super(fieldName);
        }

        final protected SortedNumericDocValues getDocValues(final LeafReader leafReader) throws IOException {
            return leafReader.getSortedNumericDocValues(fieldName);
        }
    }
}
