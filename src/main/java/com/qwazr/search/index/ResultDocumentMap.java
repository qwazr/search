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
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.qwazr.search.field.converters.ValueConverter;
import com.qwazr.server.ServerException;
import com.qwazr.utils.ObjectMappers;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResultDocumentMap extends ResultDocumentAbstract {

    final public Map<String, Object> fields;

    @JsonCreator
    public ResultDocumentMap(@JsonProperty("score") Float score, @JsonProperty("pos") Integer pos,
                             @JsonProperty("doc") Integer doc, @JsonProperty("shard_index") Integer shardIndex,
                             @JsonProperty("highlights") Map<String, String> highlights,
                             @JsonProperty("fields") Map<String, Object> fields) {
        super(score, pos, doc, shardIndex, highlights);
        this.fields = fields;
    }

    private ResultDocumentMap(final ResultDocumentBuilder.Base<ResultDocumentMap> builder,
                              final Map<String, Object> fields) {
        super(builder);
        this.fields = fields;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    final static class ForFields extends ResultDocumentBuilder.Base<ResultDocumentMap> {

        private final Map<String, Object> fields;

        ForFields(final int pos, final ScoreDoc scoreDoc) {
            super(pos, scoreDoc);
            this.fields = new LinkedHashMap<>();
        }

        @Override
        public final ResultDocumentMap build() {
            return new ResultDocumentMap(this, Collections.unmodifiableMap(fields));
        }

        @Override
        public final void setDocValuesField(final String fieldName, final ValueConverter<?> converter) {
            try {
                fields.put(fieldName, converter.convert(scoreDoc.doc));
            } catch (IOException e) {
                throw ServerException.of("Conversion failure on field '" + fieldName + "' : " + e.getMessage(), e);
            }
        }

        @Override
        public void setStoredFieldString(final String fieldName, final String value) {
            fields.put(fieldName, value);
        }

        @Override
        public void setStoredFieldString(final String fieldName, final List<String> values) {
            fields.put(fieldName, values);
        }

        @Override
        public void setStoredFieldBytes(final String fieldName, final byte[] value) {
            fields.put(fieldName, value);
        }

        @Override
        public void setStoredFieldBytes(final String fieldName, final List<byte[]> values) {
            fields.put(fieldName, values);
        }

        @Override
        public void setStoredFieldInteger(final String fieldName, final int value) {
            fields.put(fieldName, value);
        }

        @Override
        public void setStoredFieldInteger(final String fieldName, final int[] values) {
            fields.put(fieldName, values);
        }

        @Override
        public void setStoredFieldLong(final String fieldName, final long value) {
            fields.put(fieldName, value);
        }

        @Override
        public void setStoredFieldLong(final String fieldName, final long[] values) {
            fields.put(fieldName, values);
        }

        @Override
        public void setStoredFieldFloat(final String fieldName, final float value) {
            fields.put(fieldName, value);
        }

        @Override
        public void setStoredFieldFloat(final String fieldName, final float[] values) {
            fields.put(fieldName, values);
        }

        @Override
        public void setStoredFieldDouble(final String fieldName, final double value) {
            fields.put(fieldName, value);
        }

        @Override
        public void setStoredFieldDouble(final String fieldName, final double[] values) {
            fields.put(fieldName, values);
        }
    }

    private final static TypeReference<Map<String, Object>> MapStringObjectType = new TypeReference<>() {
    };

    final static class ForRecord extends ResultDocumentBuilder.Base<ResultDocumentMap> {

        private Map<String, Object> fields;

        ForRecord(final int pos, final ScoreDoc scoreDoc) {
            super(pos, scoreDoc);
        }

        @Override
        public final ResultDocumentMap build() {
            return new ResultDocumentMap(this, fields);
        }

        @Override
        public void setStoredFieldBytes(final String fieldName, final byte[] value) {
            try {
                fields = Collections.unmodifiableMap(ObjectMappers.SMILE.readValue(value, MapStringObjectType));
            } catch (IOException e) {
                throw ServerException.of("Conversion failure on field " + fieldName + " : " + e.getMessage(), e);
            }
        }
    }

    final static class ForNone extends ResultDocumentBuilder.Base<ResultDocumentMap> {

        ForNone(final int pos, final ScoreDoc scoreDoc) {
            super(pos, scoreDoc);
        }

        @Override
        public final ResultDocumentMap build() {
            return new ResultDocumentMap(this, null);
        }

    }
}
