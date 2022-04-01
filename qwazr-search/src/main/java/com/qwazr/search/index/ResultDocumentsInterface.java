/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

public interface ResultDocumentsInterface {

    /**
     * @param searcher the IndexSearcher used for the query
     * @param pos      the position of the current document
     * @param scoreDoc the ScoreDoc defining the document
     * @throws IOException if any I/O error occurs
     */
    default void doc(IndexSearcher searcher, int pos, ScoreDoc scoreDoc) throws IOException {
    }

    /**
     * @param pos     the position of the document
     * @param name    the name of the snippet
     * @param snippet the extracted snippet
     */
    default void highlight(int pos, String name, String snippet) {
    }

    ResultDocumentsInterface NOPE = new ResultDocumentsInterface() {
    };

    final class ForScoreDoc implements ResultDocumentsInterface {

        private final List<ScoreDoc> scoredocs;

        public ForScoreDoc() {
            this.scoredocs = new ArrayList<>();
        }

        final public void doc(final IndexSearcher searcher, final int pos, final ScoreDoc scoreDoc) {
            scoredocs.add(scoreDoc);
        }

        public float getScore(int pos) {
            return scoredocs.get(pos).score;
        }

        int getDoc(int pos) {
            return scoredocs.get(pos).doc;
        }
    }
}
