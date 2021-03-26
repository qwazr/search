/*
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

import java.io.IOException;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;

public abstract class DocValuesLeafCollector<DocValuesType extends DocIdSetIterator> implements LeafCollector {

    protected long count = 0;

    protected final DocValuesType docValues;

    private int lastDocId = -1;

    protected DocValuesLeafCollector(DocValuesType docValues) {
        this.docValues = docValues;
    }

    protected boolean advance(int doc) throws IOException {
        if (doc <= lastDocId)
            return false;
        if (docValues.advance(doc) != doc)
            return false;
        lastDocId = doc;
        return true;
    }


    @Override
    public void setScorer(Scorable scorable) {
    }

    public static abstract class Numeric<CollectorResult extends Comparable<CollectorResult>>
        extends DocValuesLeafCollector<NumericDocValues> {

        protected Numeric(NumericDocValues docValues) {
            super(docValues);
        }

        protected abstract CollectorResult getResult();

    }

    public static abstract class SortedNumeric extends DocValuesLeafCollector<SortedNumericDocValues> {

        protected SortedNumeric(SortedNumericDocValues docValues) {
            super(docValues);
        }
    }

    public static abstract class Binary extends DocValuesLeafCollector<BinaryDocValues> {

        protected Binary(BinaryDocValues docValues) {
            super(docValues);
        }
    }

    public static abstract class Sorted extends DocValuesLeafCollector<SortedDocValues> {

        protected Sorted(SortedDocValues docValues) {
            super(docValues);
        }
    }

    public static abstract class SortedSet extends DocValuesLeafCollector<SortedSetDocValues> {

        protected SortedSet(SortedSetDocValues docValues) {
            super(docValues);
        }
    }
}
