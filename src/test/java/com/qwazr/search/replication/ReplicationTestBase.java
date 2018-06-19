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

package com.qwazr.search.replication;

import com.qwazr.search.annotations.AnnotatedIndexService;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexStatus;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.search.index.ReplicationStatus;
import com.qwazr.search.query.MatchAllDocsQuery;
import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.FileUtils;
import org.junit.Assert;
import org.junit.Before;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class ReplicationTestBase<T> {

    protected AnnotatedIndexService<T> master;
    protected List<AnnotatedIndexService<T>> slaves;

    protected final IndexServiceInterface service;
    final Class<T> recordClass;

    protected ReplicationTestBase(IndexServiceInterface service, Class<T> recordClass) {
        this.service = service;
        this.recordClass = recordClass;
    }

    @Before
    public void setup() throws URISyntaxException {

        // Create the master index
        master = getMaster();
        master.createUpdateSchema();
        master.createUpdateIndex();
        master.createUpdateFields();

        // Create the slaves
        slaves = getSlaves();
        for (AnnotatedIndexService<T> slave : slaves)
            slave.createUpdateIndex();
    }

    public abstract AnnotatedIndexService<T> getMaster() throws URISyntaxException;

    public abstract List<AnnotatedIndexService<T>> getSlaves() throws URISyntaxException;

    /**
     * Check if the status of the slave equals the master status
     */
    public void checkSlaveStatusEqualsMasterStatus() {
        final IndexStatus masterStatus = master.getIndexStatus();
        for (AnnotatedIndexService<T> slave : slaves) {
            final IndexStatus slaveStatus = slave.getIndexStatus();
            Assert.assertTrue(CollectionsUtils.equals(masterStatus.commitUserData, slaveStatus.commitUserData));
            Assert.assertEquals(masterStatus.numDocs, slaveStatus.numDocs);
            Assert.assertEquals(1, slaveStatus.activeQueryAnalyzers, 0);
            Assert.assertEquals(1, slaveStatus.activeIndexAnalyzers, 0);
            Assert.assertEquals(1, masterStatus.activeQueryAnalyzers, 0);
            Assert.assertEquals(1, masterStatus.activeIndexAnalyzers, 0);
        }
    }

    /**
     * Check the content of the replication status
     *
     * @param replicationStatus
     */
    public ReplicationStatus checkReplicationStatus(ReplicationStatus replicationStatus,
        ReplicationStatus.Strategy expectedStrategy, Integer expectedRatio) {
        Assert.assertNotNull(replicationStatus);
        Assert.assertNotNull(replicationStatus.start);
        Assert.assertNotNull(replicationStatus.end);
        Assert.assertEquals(replicationStatus.time,
            replicationStatus.end.getTime() - replicationStatus.start.getTime());
        Assert.assertNotNull(replicationStatus.size);
        Assert.assertEquals(FileUtils.byteCountToDisplaySize(replicationStatus.bytes), replicationStatus.size);
        if (expectedStrategy != null)
            Assert.assertEquals(expectedStrategy, replicationStatus.strategy);
        if (expectedRatio != null)
            Assert.assertEquals(expectedRatio, replicationStatus.ratio, 0);
        return replicationStatus;
    }

    /**
     * Check if everyrecords are identical between master and slaves.
     * By default this method check the equality.
     *
     * @param recordsChecker any user additional check
     */
    public void compareMasterAndSlaveRecords(BiConsumer<T, T> recordsChecker) {

        final QueryDefinition queryIterator = QueryDefinition.of(new MatchAllDocsQuery())
            .returnedField("*")
            .sort("sortId", QueryDefinition.SortEnum.ascending)
            .build();
        final Iterator<T> masterIterator = master.searchIterator(queryIterator, recordClass);
        final List<Iterator<T>> slavesIterators = new ArrayList<>();
        for (AnnotatedIndexService<T> slave : slaves)
            slavesIterators.add(slave.searchIterator(queryIterator, recordClass));

        masterIterator.forEachRemaining(masterRecord -> slavesIterators.forEach(slaveIterator -> {
            final T slaveRecord = slaveIterator.next();
            Assert.assertEquals(masterRecord, slaveRecord);
            if (recordsChecker != null)
                recordsChecker.accept(masterRecord, slaveRecord);
        }));
    }
}
