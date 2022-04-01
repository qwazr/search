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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.lucene.search.ScoreDoc;

import java.util.Map;

public abstract class ResultDocumentAbstract {

    final public float score;
    final public int pos;
    final public Map<String, String> highlights;

    @JsonCreator
    ResultDocumentAbstract(@JsonProperty("score") Float score,
                           @JsonProperty("pos") Integer pos,
                           @JsonProperty("highlights") Map<String, String> highlights) {
        this.score = score == null ? 1.0F : score;
        this.pos = pos == null ? -1 : pos;
        this.highlights = highlights;
    }

    protected ResultDocumentAbstract(final ResultDocumentBuilder<?> builder) {
        final ScoreDoc scoreDoc = builder.scoreDoc();
        this.score = scoreDoc.score;
        this.pos = builder.pos();
        this.highlights = builder.highlights();
    }

    protected ResultDocumentAbstract(final ResultDocumentAbstract src) {
        this(src.score, src.pos, src.highlights);
    }

    final public float getScore() {
        return score;
    }

    final public int getPos() {
        return pos;
    }
    
    final public Map<String, String> getHighlights() {
        return highlights;
    }

    @JsonIgnore
    final public Map<String, String> getSnippets() {
        return highlights;
    }

}
