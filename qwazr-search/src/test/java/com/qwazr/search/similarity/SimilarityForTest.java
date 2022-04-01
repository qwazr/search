package com.qwazr.search.similarity;

import org.apache.lucene.search.similarities.BM25Similarity;

public class SimilarityForTest extends BM25Similarity {

    public SimilarityForTest() {
        super(1.2f, 0.75f);
    }
}
