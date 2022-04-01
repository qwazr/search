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
 **/
package com.qwazr.search.index;

import com.qwazr.search.field.converters.ValueConverter;
import org.apache.lucene.search.ScoreDoc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

interface ResultDocumentBuilder<T extends ResultDocumentAbstract> {

    int pos();

    ScoreDoc scoreDoc();

    Map<String, String> highlights();

    default void setDocValuesField(final String fieldName, final ValueConverter<?> converter) {
    }

    default void setStoredFieldString(final String fieldName, final String value) {
    }

    default void setStoredFieldString(final String fieldName, final List<String> values) {
    }

    default void setStoredFieldBytes(final String fieldName, final byte[] value) {
    }

    default void setStoredFieldBytes(final String fieldName, final List<byte[]> values) {
    }

    default void setStoredFieldInteger(final String fieldName, final int value) {
    }

    default void setStoredFieldInteger(final String fieldName, final int[] values) {
    }

    default void setStoredFieldLong(final String fieldName, final long value) {
    }

    default void setStoredFieldLong(final String fieldName, final long[] values) {
    }

    default void setStoredFieldFloat(final String fieldName, final float value) {
    }

    default void setStoredFieldFloat(final String fieldName, final float[] values) {
    }

    default void setStoredFieldDouble(final String fieldName, final double value) {
    }

    default void setStoredFieldDouble(final String fieldName, final double[] values) {
    }

    void setHighlight(final String name, final String snippet);

    T build();

    abstract class Base<T extends ResultDocumentAbstract> implements ResultDocumentBuilder<T> {

        protected final int pos;
        protected final ScoreDoc scoreDoc;
        protected Map<String, String> highlights;

        Base(final int pos, final ScoreDoc scoreDoc) {
            this.pos = pos;
            this.scoreDoc = scoreDoc;
        }

        @Override
        public final int pos() {
            return pos;
        }

        @Override
        public final ScoreDoc scoreDoc() {
            return scoreDoc;
        }

        @Override
        public final Map<String, String> highlights() {
            return highlights;
        }

        @Override
        public final void setHighlight(final String name, final String snippet) {
            if (name == null || snippet == null)
                return;
            if (highlights == null)
                highlights = new LinkedHashMap<>();
            highlights.put(name, snippet);
        }
    }

}
