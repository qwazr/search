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

import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;
import java.util.List;

public abstract class MaxNumericCollector<CollectorResult extends Comparable<CollectorResult>, LeafCollector extends DocValuesLeafCollector.Numeric<CollectorResult>>
        extends DocValuesCollector.Numeric<CollectorResult, LeafCollector> {

    public MaxNumericCollector(final String fieldName) {
        super(fieldName);
    }

    @Override
    public CollectorResult reduce(List<DocValuesCollector.Numeric<CollectorResult, LeafCollector>> collectors) {
        CollectorResult currentResult = null;
        for (final DocValuesCollector.Numeric<CollectorResult, LeafCollector> collector : collectors) {
            for (final LeafCollector leaf : collector.getLeaves()) {
                final CollectorResult newResult = leaf.getResult();
                if (newResult != null) {
                    if (currentResult != null) {
                        if (newResult.compareTo(currentResult) > 0)
                            currentResult = newResult;
                    } else
                        currentResult = newResult;
                }
            }
        }
        return currentResult;
    }


    public static class MaxLong extends MaxNumericCollector<Long, MaxLong.Leaf> {

        public MaxLong(final String fieldName) {
            super(fieldName);
        }

        @Override
        protected Leaf newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues) {
            return new Leaf(docValues);
        }

        static class Leaf extends DocValuesLeafCollector.Numeric<Long> {

            private long result;

            private Leaf(NumericDocValues docValues) {
                super(docValues);
                result = Long.MIN_VALUE;
            }

            @Override
            final public void collect(final int doc) throws IOException {
                count++;
                docValues.advance(doc);
                final long value = docValues.longValue();
                if (value > result)
                    result = value;
            }

            @Override
            protected Long getResult() {
                return count == 0 ? null : result;
            }
        }
    }

    public static class MaxInteger extends MaxNumericCollector<Integer, MaxInteger.Leaf> {

        public MaxInteger(final String fieldName) {
            super(fieldName);
        }

        @Override
        protected Leaf newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues) {
            return new Leaf(docValues);
        }

        private static class Leaf extends DocValuesLeafCollector.Numeric<Integer> {

            private int result;

            private Leaf(final NumericDocValues docValues) {
                super(docValues);
                result = Integer.MIN_VALUE;
            }

            @Override
            final public void collect(final int doc) throws IOException {
                count++;
                docValues.advance(doc);
                final int value = (int) docValues.longValue();
                if (value > result)
                    result = value;
            }

            @Override
            protected Integer getResult() {
                return count == 0 ? null : result;
            }

        }
    }

    public static class MaxDouble extends MaxNumericCollector<Double, MaxDouble.Leaf> {

        public MaxDouble(final String fieldName) {
            super(fieldName);
        }

        @Override
        protected Leaf newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues) {
            return new Leaf(docValues);
        }

        private static class Leaf extends DocValuesLeafCollector.Numeric<Double> {

            private double result;

            private Leaf(NumericDocValues docValues) {
                super(docValues);
                result = Double.MIN_VALUE;
            }

            @Override
            final public void collect(final int doc) throws IOException {
                count++;
                docValues.advance(doc);
                final double value = NumericUtils.sortableLongToDouble(docValues.longValue());
                if (value > result)
                    result = value;
            }

            @Override
            protected Double getResult() {
                return count == 0 ? null : result;
            }
        }
    }

    public static class MaxFloat extends MaxNumericCollector<Float, MaxFloat.Leaf> {

        public MaxFloat(final String fieldName) {
            super(fieldName);
        }

        @Override
        protected Leaf newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues) {
            return new Leaf(docValues);
        }

        private static class Leaf extends DocValuesLeafCollector.Numeric<Float> {

            private float result;

            private Leaf(NumericDocValues docValues) {
                super(docValues);
                result = Float.MIN_VALUE;
            }

            @Override
            final public void collect(final int doc) throws IOException {
                count++;
                docValues.advance(doc);
                final float value = NumericUtils.sortableIntToFloat((int) docValues.longValue());
                if (value > result)
                    result = value;
            }

            @Override
            protected Float getResult() {
                return count == 0 ? null : result;
            }
        }
    }

}
