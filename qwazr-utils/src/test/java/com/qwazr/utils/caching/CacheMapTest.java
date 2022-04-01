/*
 * Copyright 2015-2019 Emmanuel Keller / QWAZR
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
package com.qwazr.utils.caching;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class CacheMapTest {

    @Test
    public void cacheMapTest() {
        final CacheMap<String, Integer> cache = new CacheMap<>(10);
        assertThat(cache.getMaxSize(), equalTo(10));
        for (int i = 1; i <= 10; i++) {
            synchronized (cache) {
                cache.put("k" + i, i);
            }
            assertThat(cache.size(), equalTo(i));
            assertThat(cache.values().iterator().next(), equalTo(1));
        }
        for (int i = 11; i <= 20; i++) {
            synchronized (cache) {
                cache.put("k" + i, i);
            }
            assertThat(cache.size(), equalTo(10));
            assertThat(cache.values().iterator().next(), equalTo(i - 9));
        }
    }
}
