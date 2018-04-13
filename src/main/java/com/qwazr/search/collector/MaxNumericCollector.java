/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;

public abstract class MaxNumericCollector<R extends Comparable<R>> extends DocValuesCollector.Numeric<R> implements NumericCollector<R> {

    public MaxNumericCollector(final String collectorName, final String fieldName) {
        super(collectorName, fieldName);
    }

    @Override
    public boolean keepNewValue(final R previousValue, final R newValue) {
        return newValue.compareTo(previousValue) > 0;
    }

    @Override
    public boolean needsScores() {
        return false;
    }

    public static class MaxLong extends MaxNumericCollector<Long> {

        private long result;

        public MaxLong(final String collectorName, final String fieldName) {
            super(collectorName, fieldName);
            result = Long.MIN_VALUE;
        }

        @Override
        public Long getResult() {
            return count == 0 ? null : result;
        }

        @Override
        protected LeafCollector newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues)
                throws IOException {
            return new Leaf(docValues);
        }

        private class Leaf extends DocValuesLeafCollector.Numeric {

            private Leaf(NumericDocValues docValues) throws IOException {
                super(docValues);
            }

            @Override
            final public void collect(final int doc) {
                count++;
                final long value = docValues.get(doc);
                if (value > result)
                    result = value;
            }
        }
    }

    public static class MaxInteger extends MaxNumericCollector<Integer> {

        private int result;

        public MaxInteger(final String collectorName, final String fieldName) {
            super(collectorName, fieldName);
            result = Integer.MIN_VALUE;
        }

        @Override
        public Integer getResult() {
            return count == 0 ? null : result;
        }

        @Override
        protected LeafCollector newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues)
                throws IOException {
            return new Leaf(docValues);
        }

        private class Leaf extends DocValuesLeafCollector.Numeric {

            private Leaf(NumericDocValues docValues) throws IOException {
                super(docValues);
            }

            @Override
            final public void collect(final int doc) {
                count++;
                final int value = (int) docValues.get(doc);
                if (value > result)
                    result = value;
            }
        }
    }

    public static class MaxDouble extends MaxNumericCollector<Double> {

        private double result;

        public MaxDouble(final String collectorName, final String fieldName) {
            super(collectorName, fieldName);
            result = Double.MIN_VALUE;
        }

        @Override
        public Double getResult() {
            return count == 0 ? null : result;
        }

        @Override
        protected LeafCollector newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues)
                throws IOException {
            return new Leaf(docValues);
        }

        private class Leaf extends DocValuesLeafCollector.Numeric {

            private Leaf(NumericDocValues docValues) throws IOException {
                super(docValues);
            }

            @Override
            final public void collect(final int doc) {
                count++;
                final double value = NumericUtils.sortableLongToDouble(docValues.get(doc));
                if (value > result)
                    result = value;
            }
        }
    }

    public static class MaxFloat extends MaxNumericCollector<Float> {

        private float result;

        public MaxFloat(final String collectorName, final String fieldName) {
            super(collectorName, fieldName);
            result = Float.MIN_VALUE;
        }

        @Override
        public Float getResult() {
            return count == 0 ? null : result;
        }

        @Override
        protected LeafCollector newLeafCollector(final LeafReader leafReader, final NumericDocValues docValues)
                throws IOException {
            return new Leaf(docValues);
        }

        private class Leaf extends DocValuesLeafCollector.Numeric {

            private Leaf(NumericDocValues docValues) throws IOException {
                super(docValues);
            }

            @Override
            final public void collect(final int doc) {
                count++;
                final float value = NumericUtils.sortableIntToFloat((int) docValues.get(doc));
                if (value > result)
                    result = value;
            }
        }
    }

}
