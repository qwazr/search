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

package com.qwazr.search.replication;

import com.qwazr.search.index.SnapshotDirectoryTaxonomyWriter;
import com.qwazr.utils.HashUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.SnapshotDeletionPolicy;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public interface MasterNode extends Closeable {

    ReplicationSession newSession() throws IOException;

    InputStream getItem(String sessionId, ReplicationProcess.Source source, String itemName)
            throws FileNotFoundException;

    void releaseSession(String sessionId) throws IOException;

    abstract class Base implements MasterNode {

        private final String masterUuid;

        protected Base(final String masterUuid) {
            this.masterUuid = masterUuid;
        }

        protected abstract void fillSession(final String sessionUuid,
                                            final Map<String, Map<String, ReplicationSession.Item>> sessionMap) throws IOException;

        final public ReplicationSession newSession() throws IOException {
            final Map<String, Map<String, ReplicationSession.Item>> sessionMap = new HashMap<>();
            final String sessionUuid = HashUtils.newTimeBasedUUID().toString();
            fillSession(sessionUuid, sessionMap);
            return new ReplicationSession(masterUuid, sessionUuid, sessionMap);
        }
    }

    class WithMetadata extends Base {

        private final Path metadataDirectory;
        private final String[] metadataItems;
        private final HashMap<String, SourceView.FromPathFiles> metadataSessions;

        public WithMetadata(final String masterUuid, final Path metadataDirectory, final String... metadataItems) {
            super(masterUuid);
            this.metadataDirectory = metadataDirectory;
            this.metadataItems = metadataItems;
            this.metadataSessions = new HashMap<>();
        }

        @Override
        protected void fillSession(final String sessionId,
                                   final Map<String, Map<String, ReplicationSession.Item>> sessionMap) throws IOException {
            synchronized (metadataSessions) {
                final SourceView.FromPathFiles sourceView =
                        new SourceView.FromPathFiles(metadataDirectory, metadataItems);
                metadataSessions.put(sessionId, sourceView);
                sessionMap.put(ReplicationProcess.Source.metadata.name(), sourceView.getItems());
            }
        }

        @Override
        public InputStream getItem(final String sessionId, ReplicationProcess.Source source, final String itemName)
                throws FileNotFoundException {
            if (source != null && source != ReplicationProcess.Source.metadata)
                return null;
            final SourceView.FromPathFiles sourceView;
            synchronized (metadataSessions) {
                sourceView = metadataSessions.get(sessionId);
            }
            return sourceView == null ? null : sourceView.getItem(itemName);
        }

        @Override
        public void releaseSession(final String sessionId) throws IOException {
            synchronized (metadataSessions) {
                metadataSessions.remove(sessionId);
            }
        }

        @Override
        public void close() throws IOException {
        }
    }

    class WithResources extends WithMetadata {

        private final Path resourcesPath;
        private final HashMap<String, SourceView.FromPathDirectory> resourcesSessions;

        public WithResources(final String masterUuid, final Path resourcesPath, final Path metadataDirectory,
                             final String... metadataItems) {
            super(masterUuid, metadataDirectory, metadataItems);
            this.resourcesPath = resourcesPath;
            this.resourcesSessions = new HashMap<>();
        }

        @Override
        protected void fillSession(final String sessionId,
                                   final Map<String, Map<String, ReplicationSession.Item>> sessionMap) throws IOException {
            super.fillSession(sessionId, sessionMap);
            synchronized (resourcesSessions) {
                final SourceView.FromPathDirectory indexView = new SourceView.FromPathDirectory(resourcesPath);
                resourcesSessions.put(sessionId, indexView);
                sessionMap.put(ReplicationProcess.Source.resources.name(), indexView.getItems());
            }
        }

        @Override
        public InputStream getItem(final String sessionId, ReplicationProcess.Source source, final String itemName)
                throws FileNotFoundException {
            if (source != null && source != ReplicationProcess.Source.resources)
                return super.getItem(sessionId, source, itemName);
            final SourceView.FromPathDirectory sourceView;
            synchronized (resourcesSessions) {
                sourceView = resourcesSessions.get(sessionId);
            }
            return sourceView == null ? null : sourceView.getItem(itemName);
        }

        @Override
        public void releaseSession(final String sessionId) throws IOException {
            super.releaseSession(sessionId);
            synchronized (resourcesSessions) {
                resourcesSessions.remove(sessionId);
            }
        }

        @Override
        public void close() throws IOException {
            super.close();
        }
    }

    class WithIndex extends WithResources {

        private final Path indexDirectoryPath;
        private final SnapshotDeletionPolicy indexSnapshot;
        private final HashMap<String, SourceView.FromCommit> indexSessions;

        public WithIndex(final String masterUuid, final Path resourcesPath, final Path indexDirectoryPath,
                         final IndexWriter indexWriter, final Path metadataDirectory, final String... metadataItems) {
            super(masterUuid, resourcesPath, metadataDirectory, metadataItems);
            this.indexDirectoryPath = indexDirectoryPath;
            this.indexSnapshot = (SnapshotDeletionPolicy) indexWriter.getConfig().getIndexDeletionPolicy();
            this.indexSessions = new HashMap<>();
        }

        @Override
        protected void fillSession(final String sessionId,
                                   final Map<String, Map<String, ReplicationSession.Item>> sessionMap) throws IOException {
            super.fillSession(sessionId, sessionMap);
            synchronized (indexSessions) {
                final SourceView.FromCommit indexView = new SourceView.FromCommit(indexDirectoryPath, indexSnapshot);
                indexSessions.put(sessionId, indexView);
                sessionMap.put(ReplicationProcess.Source.data.name(), indexView.getItems());
            }
        }

        @Override
        public InputStream getItem(final String sessionId, ReplicationProcess.Source source, final String itemName)
                throws FileNotFoundException {
            if (source == null || source != ReplicationProcess.Source.data)
                return super.getItem(sessionId, source, itemName);
            final SourceView.FromCommit sourceView;
            synchronized (indexSessions) {
                sourceView = indexSessions.get(sessionId);
            }
            return sourceView == null ? null : sourceView.getItem(itemName);
        }

        @Override
        public void releaseSession(final String sessionId) throws IOException {
            super.releaseSession(sessionId);
            synchronized (indexSessions) {
                final SourceView.FromCommit sourceView = indexSessions.remove(sessionId);
                if (sourceView != null)
                    sourceView.close();
            }
        }

        @Override
        public void close() throws IOException {
            super.close();
            synchronized (indexSessions) {
                for (SourceView.FromCommit sourceView : indexSessions.values())
                    sourceView.close();
            }
        }
    }

    class WithIndexAndTaxo extends WithIndex {

        private final Path taxoDirectoryPath;
        private final SnapshotDeletionPolicy taxoSnapshots;
        private final HashMap<String, SourceView.FromCommit> taxoSessions;

        public WithIndexAndTaxo(final String masterUuid, final Path resourcesPath, final Path indexDirectoryPath,
                                final IndexWriter indexWriter, final Path taxoDirectoryPath,
                                final SnapshotDirectoryTaxonomyWriter taxonomyWriter, final Path metadataDirectory,
                                final String... metadataItems) throws IOException {
            super(masterUuid, resourcesPath, indexDirectoryPath, indexWriter, metadataDirectory, metadataItems);
            this.taxoDirectoryPath = taxoDirectoryPath;
            this.taxoSnapshots = taxonomyWriter.getDeletionPolicy();
            this.taxoSessions = new HashMap<>();
        }

        @Override
        protected void fillSession(final String sessionId,
                                   final Map<String, Map<String, ReplicationSession.Item>> sessionMap) throws IOException {
            super.fillSession(sessionId, sessionMap);
            synchronized (taxoSessions) {
                final SourceView.FromCommit indexView = new SourceView.FromCommit(taxoDirectoryPath, taxoSnapshots);
                taxoSessions.put(sessionId, indexView);
                sessionMap.put(ReplicationProcess.Source.taxonomy.name(), indexView.getItems());
            }
        }

        @Override
        public InputStream getItem(final String sessionId, final ReplicationProcess.Source source,
                                   final String itemName) throws FileNotFoundException {
            if (source == null || source != ReplicationProcess.Source.taxonomy)
                return super.getItem(sessionId, source, itemName);
            final SourceView.FromCommit sourceView;
            synchronized (taxoSessions) {
                sourceView = taxoSessions.get(sessionId);
            }
            return sourceView == null ? null : sourceView.getItem(itemName);
        }

        @Override
        public void releaseSession(final String sessionId) throws IOException {
            super.releaseSession(sessionId);
            synchronized (taxoSessions) {
                final SourceView.FromCommit sourceView = taxoSessions.remove(sessionId);
                if (sourceView != null)
                    sourceView.close();
            }
        }

        @Override
        public void close() throws IOException {
            super.close();
            synchronized (taxoSessions) {
                for (SourceView.FromCommit sourceView : taxoSessions.values())
                    sourceView.close();
            }
        }
    }
}
