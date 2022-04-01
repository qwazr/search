/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

package com.qwazr.search.index;

import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.facet.taxonomy.writercache.TaxonomyWriterCache;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.store.Directory;

import java.io.IOException;

/**
 * Copied  from: org.apache.lucene.replicator (we don't want to import the full replication module
 */
public final class SnapshotDirectoryTaxonomyWriter extends DirectoryTaxonomyWriter {

    private SnapshotDeletionPolicy sdp;
    private IndexWriter writer;

    /**
     * @param directory the directory to use
     * @param openMode  the opening mode to use
     * @param cache     the cache to use
     * @throws IOException if any I/O error occurs
     * @see DirectoryTaxonomyWriter#DirectoryTaxonomyWriter(Directory, IndexWriterConfig.OpenMode, TaxonomyWriterCache)
     */
    public SnapshotDirectoryTaxonomyWriter(Directory directory, IndexWriterConfig.OpenMode openMode,
                                           TaxonomyWriterCache cache) throws IOException {
        super(directory, openMode, cache);
    }

    /**
     * @param directory the directory to use
     * @param openMode  the opening mode to use
     * @throws IOException if any I/O error occurs
     * @see DirectoryTaxonomyWriter#DirectoryTaxonomyWriter(Directory, IndexWriterConfig.OpenMode)
     */
    public SnapshotDirectoryTaxonomyWriter(Directory directory, IndexWriterConfig.OpenMode openMode)
            throws IOException {
        super(directory, openMode);
    }

    /**
     * @param d the directory to use
     * @throws IOException if any I/O error occurs
     * @see DirectoryTaxonomyWriter#DirectoryTaxonomyWriter(Directory)
     */
    public SnapshotDirectoryTaxonomyWriter(Directory d) throws IOException {
        super(d);
    }

    @Override
    protected IndexWriterConfig createIndexWriterConfig(IndexWriterConfig.OpenMode openMode) {
        IndexWriterConfig conf = super.createIndexWriterConfig(openMode);
        sdp = new SnapshotDeletionPolicy(conf.getIndexDeletionPolicy());
        conf.setIndexDeletionPolicy(sdp);
        return conf;
    }

    @Override
    protected IndexWriter openIndexWriter(Directory directory, IndexWriterConfig config) throws IOException {
        writer = super.openIndexWriter(directory, config);
        return writer;
    }

    /**
     * @return the {@link SnapshotDeletionPolicy} used by the underlying {@link IndexWriter}.
     */
    public SnapshotDeletionPolicy getDeletionPolicy() {
        return sdp;
    }

    /**
     * @return the {@link IndexWriter} used by this {@link DirectoryTaxonomyWriter}.
     */
    public IndexWriter getIndexWriter() {
        return writer;
    }

}
