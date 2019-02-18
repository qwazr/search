/**
 * Copyright 2017 Emmanuel Keller / QWAZR
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
package com.qwazr.search.query.lucene;

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

import java.util.Collection;

final class FakeScorer extends Scorer {

    float score;
    int doc;

    FakeScorer() {
        super(null);
    }

    @Override
    public int docID() {
        return doc;
    }

    @Override
    public float score() {
        return score;
    }

    @Override
    public DocIdSetIterator iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Weight getWeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<ChildScorer> getChildren() {
        throw new UnsupportedOperationException();
    }
}
