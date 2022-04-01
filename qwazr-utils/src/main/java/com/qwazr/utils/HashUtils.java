/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.google.common.primitives.Longs;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MurmurHash3;
import org.bitcoinj.core.Base58;

public interface HashUtils {

    static int getMurmur3Hash32(final String stringToHash, final int mod) {
        return (Math.abs(MurmurHash3.hash32x86(stringToHash.getBytes())) % mod);
    }

    static String getMurmur3Hash128Hex(final String stringToHash) {
        final long[] hash = MurmurHash3.hash128x64(stringToHash.getBytes());
        return Long.toHexString(hash[0]) + Long.toHexString(hash[1]);
    }

    static String getMurmur3Hash32Hex(final String stringToHash) {
        return Integer.toHexString(MurmurHash3.hash32x86(stringToHash.getBytes()));
    }

    /**
     * Compute the MD5 hash from a file and return the hexa representation
     *
     * @param filePath path to a regular file
     * @return an hexa representation of the md5
     * @throws IOException if any I/O exception occurs
     */
    static String md5Hex(final Path filePath) throws IOException {
        try (final InputStream in = Files.newInputStream(filePath);
             final BufferedInputStream bIn = new BufferedInputStream(in)) {
            return DigestUtils.md5Hex(bIn);
        }
    }

    static String md5Hex(String text) {
        return DigestUtils.md5Hex(text);
    }

    TimeBasedGenerator UUID_GENERATOR = Generators.timeBasedGenerator(EthernetAddress.fromInterface());

    static UUID newTimeBasedUUID() {
        return UUID_GENERATOR.generate();
    }

    long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;

    // This method comes from Hector's TimeUUIDUtils class:
    // https://github.com/rantav/hector/blob/master/core/src/main/java/me/prettyprint/cassandra/utils/TimeUUIDUtils.java
    static long getTimeFromUUID(UUID uuid) {
        return (uuid.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH) / 10000;
    }

    static void toByteArray(long value, final byte[] bytes, final int offset) {
        for (int i = offset + 7; i >= offset; i--) {
            bytes[i] = (byte) (value & 0xffL);
            value >>= 8;
        }
    }

    static void toByteArray(final UUID uuid, final byte[] bytes, final int offset) {
        toByteArray(uuid.getMostSignificantBits(), bytes, offset);
        toByteArray(uuid.getLeastSignificantBits(), bytes, offset + 8);
    }

    static long fromByteArray(final byte[] bytes, int offset) {
        return Longs.fromBytes(
                bytes[offset],
                bytes[++offset],
                bytes[++offset],
                bytes[++offset],
                bytes[++offset],
                bytes[++offset],
                bytes[++offset],
                bytes[++offset]);
    }

    static byte[] getBase58buffer(int uuidCount) {
        return new byte[uuidCount * 16];
    }

    /**
     * @param uuid        the uuid to encode
     * @param bytesBuffer a 16 bytes long bytes buffer
     * @return the Base58 encoded string
     */
    static String base58encode(final UUID uuid, byte[] bytesBuffer) {
        toByteArray(uuid, bytesBuffer, 0);
        return Base58.encode(bytesBuffer);
    }

    static String base58encode(final UUID uuid) {
        return base58encode(uuid, getBase58buffer(1));
    }

    static String base58encode(byte[] bytesBuffer, final UUID... uuids) {
        int offset = 0;
        for (final UUID uuid : uuids) {
            toByteArray(uuid, bytesBuffer, offset);
            offset += 16;
        }
        return Base58.encode(bytesBuffer);
    }

    static String base58encode(final UUID... uuids) {
        return base58encode(getBase58buffer(uuids.length), uuids);
    }

    static UUID base58decode(final String encoded) {
        final byte[] bytes = Base58.decode(encoded);
        return new UUID(fromByteArray(bytes, 0), fromByteArray(bytes, 8));
    }

    static void base58decode(final String encoded, final Consumer<UUID> uuidConsumer) {
        final byte[] bytes = Base58.decode(encoded);
        int offset = 0;
        while (offset < bytes.length) {
            uuidConsumer.accept(new UUID(fromByteArray(bytes, offset), fromByteArray(bytes, offset + 8)));
            offset += 16;
        }
    }
}
