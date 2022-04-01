/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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
package com.qwazr.cluster;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

class AddressContent implements Externalizable {

    /**
     * The address of the node
     */
    private String address;
    /**
     * The UUID of the node
     */
    private UUID nodeLiveId;

    public AddressContent() {
    }

    protected AddressContent(final String address, final UUID nodeLiveId) {
        this.address = address;
        this.nodeLiveId = nodeLiveId;
    }

    final String getAddress() {
        return address;
    }

    final UUID getNodeLiveId() {
        return nodeLiveId;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(address);
        out.writeLong(nodeLiveId.getMostSignificantBits());
        out.writeLong(nodeLiveId.getLeastSignificantBits());
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        this.address = in.readUTF();
        this.nodeLiveId = new UUID(in.readLong(), in.readLong());
    }

    @Override
    public String toString() {
        return address + " " + nodeLiveId;
    }
}
