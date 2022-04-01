/*
 *  Copyright 2015-2018 Emmanuel Keller / QWAZR
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.qwazr.search.query.lucene;

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

import java.util.Collection;

final class FakeScorer extends Scorer {

    float score;
    int doc;

    FakeScorer(final Weight weight) {
        super(weight);
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
    public float getMaxScore(int upTo) {
        return score;
    }

    @Override
    public Weight getWeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Scorable.ChildScorable> getChildren() {
        throw new UnsupportedOperationException();
    }
}
