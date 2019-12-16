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
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;

import java.io.IOException;

public abstract class DocValuesLeafCollector implements LeafCollector {

    @Override
    public void setScorer(Scorable scorable) {
    }

    public static abstract class Numeric extends DocValuesLeafCollector {

        protected final NumericDocValues docValues;

        protected Numeric(NumericDocValues docValues) {
            this.docValues = docValues;
        }
    }

    public static abstract class SortedNumeric extends DocValuesLeafCollector {

        protected final SortedNumericDocValues docValues;

        protected SortedNumeric(SortedNumericDocValues docValues) {
            this.docValues = docValues;
        }
    }

    public static abstract class Binary extends DocValuesLeafCollector {

        protected final BinaryDocValues docValues;

        protected Binary(BinaryDocValues docValues) throws IOException {
            this.docValues = docValues;
        }
    }

    public static abstract class Sorted extends DocValuesLeafCollector {

        protected final SortedDocValues docValues;

        protected Sorted(SortedDocValues docValues) throws IOException {
            this.docValues = docValues;
        }
    }

    public static abstract class SortedSet extends DocValuesLeafCollector {

        protected final SortedSetDocValues docValues;

        protected SortedSet(SortedSetDocValues docValues) throws IOException {
            this.docValues = docValues;
        }
    }
}
