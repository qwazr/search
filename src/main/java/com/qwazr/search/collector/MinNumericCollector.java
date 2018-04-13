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

public abstract class MinNumericCollector<R extends Comparable<R>> extends DocValuesCollector.Numeric<R> implements NumericCollector<R> {

    public MinNumericCollector(final String collectorName, final String fieldName) {
        super(collectorName, fieldName);
    }

    @Override
    public boolean keepNewValue(final R previousValue, final R newValue) {
        return previousValue.compareTo(newValue) > 0;
    }

    @Override
    public boolean needsScores() {
        return false;
    }

    public static class MinLong extends MinNumericCollector<Long> {

        private long result;

        public MinLong(final String collectorName, final String fieldName) {
            super(collectorName, fieldName);
            result = Long.MAX_VALUE;
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
                if (value < result)
                    result = value;
            }
        }
    }

    public static class MinInteger extends MinNumericCollector<Integer> {

        private int result;

        public MinInteger(final String collectorName, final String fieldName) {
            super(collectorName, fieldName);
            result = Integer.MAX_VALUE;
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
                if (value < result)
                    result = value;
            }
        }
    }

    public static class MinDouble extends MinNumericCollector<Double> {

        private double result;

        public MinDouble(final String collectorName, final String fieldName) {
            super(collectorName, fieldName);
            result = Double.MAX_VALUE;
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
                if (value < result)
                    result = value;
            }
        }
    }

    public static class MinFloat extends MinNumericCollector<Float> {

        private float result;

        public MinFloat(final String collectorName, final String fieldName) {
            super(collectorName, fieldName);
            result = Float.MAX_VALUE;
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
                if (value < result)
                    result = value;
            }
        }
    }

}
