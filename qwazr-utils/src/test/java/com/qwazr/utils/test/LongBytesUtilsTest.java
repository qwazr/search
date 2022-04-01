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

import com.qwazr.utils.LongBytesUtils;
import com.qwazr.utils.RandomUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.Test;

public class LongBytesUtilsTest {

    @Test
    public void testLongSerialization() {
        final long longValue = RandomUtils.nextLong();
        final byte[] byteArray = new byte[8];
        LongBytesUtils.longToByteArray(longValue, byteArray);
        assertThat(LongBytesUtils.byteArrayToLong(byteArray), equalTo(longValue));
    }
}
