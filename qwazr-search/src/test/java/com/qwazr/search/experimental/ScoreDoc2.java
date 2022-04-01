/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

package com.qwazr.search.experimental;

import org.apache.lucene.search.ScoreDoc;

import java.util.Objects;

/**
 * ScoreDoc2 extends ScoreDoc by adding hashCode and equals implementation
 */
public class ScoreDoc2 extends ScoreDoc {

    private final int hashCode;

    public ScoreDoc2(int doc, float score, int shardIndex) {
        super(doc, score, shardIndex);
        hashCode = Objects.hash(doc, score, shardIndex);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ScoreDoc2))
            return false;
        final ScoreDoc2 sc = (ScoreDoc2) o;
        return sc.doc == doc && sc.score == score && sc.shardIndex == shardIndex;
    }
}
