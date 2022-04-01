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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

class FullContent extends AddressContent {

    public final Set<String> groups;
    public final Set<String> services;

    public FullContent() {
        groups = new HashSet<>();
        services = new HashSet<>();
    }

    FullContent(final String address, final UUID nodeLiveId, final Set<String> groups, final Set<String> services) {
        super(address, nodeLiveId);
        this.groups = groups;
        this.services = services;
    }

    private static void writeCollection(final Collection<String> collection, final ObjectOutput out)
            throws IOException {
        if (collection != null) {
            out.writeInt(collection.size());
            for (String s : collection)
                out.writeUTF(s);
        } else
            out.writeInt(0);
    }

    @Override
    final public void writeExternal(final ObjectOutput out) throws IOException {
        super.writeExternal(out);
        writeCollection(groups, out);
        writeCollection(services, out);
    }

    @Override
    final public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        int size = in.readInt();
        while (size-- > 0)
            groups.add(in.readUTF());
        size = in.readInt();
        while (size-- > 0)
            services.add(in.readUTF());
    }

    @Override
    final public String toString() {
        return super.toString() + " " + groups.size() + "/" + services.size();
    }

}
