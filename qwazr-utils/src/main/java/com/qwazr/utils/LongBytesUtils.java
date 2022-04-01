/*
 * Copyright 2016-2018 Emmanuel Keller / QWAZR
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
package com.qwazr.utils;

public class LongBytesUtils {

    public static void longToByteArray(long l, final byte[] b) {
        b[7] = (byte) (l);
        l >>>= 8;
        b[6] = (byte) (l);
        l >>>= 8;
        b[5] = (byte) (l);
        l >>>= 8;
        b[4] = (byte) (l);
        l >>>= 8;
        b[3] = (byte) (l);
        l >>>= 8;
        b[2] = (byte) (l);
        l >>>= 8;
        b[1] = (byte) (l);
        l >>>= 8;
        b[0] = (byte) (l);
    }

    public static long byteArrayToLong(byte[] b) {
        return (((long) b[7]) & 0xFF) +
                ((((long) b[6]) & 0xFF) << 8) +
                ((((long) b[5]) & 0xFF) << 16) +
                ((((long) b[4]) & 0xFF) << 24) +
                ((((long) b[3]) & 0xFF) << 32) +
                ((((long) b[2]) & 0xFF) << 40) +
                ((((long) b[1]) & 0xFF) << 48) +
                ((((long) b[0]) & 0xFF) << 56);
    }

}
