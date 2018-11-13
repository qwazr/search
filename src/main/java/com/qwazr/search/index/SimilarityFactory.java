package com.qwazr.search.index;

import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;

public interface SimilarityFactory {

    Similarity createSimilarity(final ResourceLoader resourceLoader) throws IOException, ReflectiveOperationException;
}
