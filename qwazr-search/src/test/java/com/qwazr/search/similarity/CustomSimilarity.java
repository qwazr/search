package com.qwazr.search.similarity;

import org.apache.lucene.search.similarities.BM25Similarity;

public class CustomSimilarity extends BM25Similarity {

    public static final String CUSTOM_SIMILARITY = "customSimilarity";

    public CustomSimilarity(final float k1) {
        super(k1, 0.75f);
    }
}
