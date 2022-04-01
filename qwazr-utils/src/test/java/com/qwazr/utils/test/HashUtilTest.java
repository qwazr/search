/*
 * Copyright 2016-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.utils.test;

import com.qwazr.utils.HashUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.RandomUtils;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import org.junit.Assert;
import org.junit.Test;

public class HashUtilTest {

    @Test
    public void timeBasedUuid() {
        final HashSet<UUID> set = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            final UUID uuid = HashUtils.newTimeBasedUUID();
            if (!set.add(uuid))
                Assert.fail("The UUID is not unique");
        }
    }

    @Test
    public void md5Test() throws IOException {
        final String content = RandomUtils.alphanumeric(1000);
        final String md5a = HashUtils.md5Hex(content);
        Assert.assertNotNull(md5a);

        final Path file = Files.createTempFile("hashUtils", ".txt");
        IOUtils.writeStringToPath(content, Charset.defaultCharset(), file);
        final String md5b = HashUtils.md5Hex(file);

        Assert.assertEquals(md5a, md5b);

    }

    @Test
    public void timeConversion() {
        UUID uuid = HashUtils.newTimeBasedUUID();
        long time = HashUtils.getTimeFromUUID(uuid);
        Assert.assertEquals(time, (uuid.timestamp() - 0x01b21dd213814000L) / 10000);
    }

    private void checkBase58Uuid(final UUID uuid) {
        final String encoded = HashUtils.base58encode(uuid);
        final UUID decodedUui = HashUtils.base58decode(encoded);
        assertThat(decodedUui, equalTo(uuid));
    }

    private void checkBase58Uuids(final UUID... uuids) {
        final String encoded = HashUtils.base58encode(uuids);
        final List<UUID> decodedUuids = new ArrayList<>();
        HashUtils.base58decode(encoded, decodedUuids::add);
        assertThat(uuids, arrayWithSize(decodedUuids.size()));
        assertThat(uuids, equalTo(decodedUuids.toArray(new UUID[0])));
    }

    @Test
    public void base58UuidTimeBasesEncodingTest() {
        checkBase58Uuids(HashUtils.newTimeBasedUUID());
        checkBase58Uuids(HashUtils.newTimeBasedUUID(), HashUtils.newTimeBasedUUID());
        checkBase58Uuids(HashUtils.newTimeBasedUUID(), HashUtils.newTimeBasedUUID(), HashUtils.newTimeBasedUUID());
        checkBase58Uuid(HashUtils.newTimeBasedUUID());
    }

    @Test
    public void base58UuidRandomEncodingTest() {
        checkBase58Uuids(UUID.randomUUID());
        checkBase58Uuids(UUID.randomUUID(), UUID.randomUUID());
        checkBase58Uuids(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        checkBase58Uuid(UUID.randomUUID());
    }

    @Test
    public void longGetMurmur3Hash32() {
        Assert.assertEquals(0, HashUtils.getMurmur3Hash32("a", 10));
        Assert.assertEquals(1, HashUtils.getMurmur3Hash32("b", 10));
        Assert.assertEquals(7, HashUtils.getMurmur3Hash32("c", 10));
        Assert.assertEquals(9, HashUtils.getMurmur3Hash32("d", 10));
        Assert.assertEquals(9, HashUtils.getMurmur3Hash32("e", 10));
        Assert.assertEquals(3, HashUtils.getMurmur3Hash32("f", 10));
    }

    @Test
    public void longGetMurmur3Hash128Hex() {
        Assert.assertEquals("e47d86bfaca3bf55b07109993321845c", HashUtils.getMurmur3Hash128Hex("abcdef"));
        Assert.assertEquals("a6cd2f9fc09ee4991c3aa23ab155bbb6", HashUtils.getMurmur3Hash128Hex("abcdefg"));
    }

    @Test
    public void longGetMurmur3Hash32Hex() {
        Assert.assertEquals("6181c085", HashUtils.getMurmur3Hash32Hex("abcdef"));
        Assert.assertEquals("883c9b06", HashUtils.getMurmur3Hash32Hex("abcdefg"));
    }
}
