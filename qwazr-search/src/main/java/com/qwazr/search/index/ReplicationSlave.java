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

import com.qwazr.search.replication.ReplicationProcess;
import com.qwazr.search.replication.ReplicationSession;
import com.qwazr.search.replication.SlaveNode;
import com.qwazr.server.ServerException;
import com.qwazr.utils.IOUtils;
import org.apache.lucene.store.Directory;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

class ReplicationSlave extends ReplicationClient {

    private final File masterUuidFile;
    private volatile UUID clientMasterUuid;
    private final IndexServiceInterface indexService;
    private final RemoteIndex master;

    ReplicationSlave(final File masterUuidFile, final IndexServiceInterface localService, final RemoteIndex master,
                     final SlaveNode slaveNode) throws IOException {
        super(slaveNode);
        this.masterUuidFile = masterUuidFile;
        this.master = master;
        this.indexService = master == null ? null : master.host == null ? localService : new IndexSingleClient(master);
        getClientMasterUuid();
    }

    UUID getClientMasterUuid() throws IOException {
        if (masterUuidFile.exists() && masterUuidFile.length() > 0)
            clientMasterUuid = UUID.fromString(IOUtils.readFileAsString(masterUuidFile));
        else
            clientMasterUuid = null;
        return clientMasterUuid;
    }

    void setClientMasterUuid(final UUID remoteMasterUuid) throws IOException {
        if (remoteMasterUuid.equals(clientMasterUuid))
            return;
        IOUtils.writeStringToFile(remoteMasterUuid.toString(), masterUuidFile);
        clientMasterUuid = remoteMasterUuid;
    }

    private IndexServiceInterface checkService() {
        if (indexService == null)
            throw new ServerException(Response.Status.NOT_ACCEPTABLE, "The remote master has not been set");
        return indexService;
    }

    @Override
    public InputStream getItem(final String sessionId, final ReplicationProcess.Source source, final String file) {
        return checkService().replicationObtain(master.index, sessionId, source.name(), file);
    }

    ReplicationStatus replicate(final Switcher switcher) throws IOException {
        final ReplicationSession session = checkService().replicationUpdate(master.index, null);
        try {
            return replicate(session, getClientMasterUuid(), switcher);
        } finally {
            checkService().replicationRelease(master.index, session.sessionUuid);
        }
    }

    static ReplicationSlave withIndexAndTaxo(final IndexFileSet fileSet, final IndexServiceInterface localService,
                                             final RemoteIndex master, final Directory dataDirectory, final Directory taxonomyDirectory)
        throws IOException {
        return new ReplicationSlave(fileSet.uuidMasterFile, localService, master,
            new SlaveNode.WithIndexAndTaxo(fileSet.resourcesDirectoryPath, dataDirectory, fileSet.dataDirectory,
                taxonomyDirectory, fileSet.taxonomyDirectory, fileSet.replWorkPath, fileSet.mainDirectory));
    }

    static ReplicationSlave withIndex(final IndexFileSet fileSet, final IndexServiceInterface localService,
                                      final RemoteIndex master, final Directory dataDirectory) throws IOException {
        return new ReplicationSlave(fileSet.uuidMasterFile, localService, master,
            new SlaveNode.WithIndex(fileSet.resourcesDirectoryPath, dataDirectory, fileSet.dataDirectory,
                fileSet.replWorkPath, fileSet.mainDirectory));
    }

}
