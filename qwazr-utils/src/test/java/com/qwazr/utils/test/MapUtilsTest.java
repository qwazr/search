/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.MapUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import org.junit.Test;

public class MapUtilsTest {

    @Test
    public void testLinkedHashMapCopy() {
        final LinkedHashMap<String, Integer> source = new LinkedHashMap<>();
        source.put("2", 2);
        source.put("1", 1);
        final Map<String, Integer> copy = MapUtils.copyOf(source);
        assertThat(copy, equalTo(source));
        final Iterator<String> iterator = copy.keySet().iterator();
        assertThat(iterator.next(), equalTo("2"));
        assertThat(iterator.next(), equalTo("1"));
    }

    @Test
    public void testTreeMapCopy() {
        final TreeMap<String, Integer> source = new TreeMap<>();
        source.put("2", 2);
        source.put("1", 1);
        final SortedMap<String, Integer> copy = MapUtils.copyOf(source);
        assertThat(copy, equalTo(source));
        assertThat(copy, instanceOf(SortedMap.class));
        final Iterator<String> iterator = copy.keySet().iterator();
        assertThat(iterator.next(), equalTo("1"));
        assertThat(iterator.next(), equalTo("2"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testLinkedHashMapCopyIsUnmodifiable() {
        MapUtils.copyOf(new LinkedHashMap<>()).put("1", 1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testLTreeHashMapCopyIsUnmodifiable() {
        MapUtils.copyOf(new TreeMap<>()).put("1", 1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testLHashMapCopyIsUnmodifiable() {
        MapUtils.copyOf(new HashMap<>()).put("1", 1);
    }

    @Test
    public void testNull() {
        assertThat(MapUtils.copyOf((LinkedHashMap<?, ?>) null), nullValue());
        assertThat(MapUtils.copyOf((TreeMap<?, ?>) null), nullValue());
        assertThat(MapUtils.copyOf((HashMap<?, ?>) null), nullValue());
    }
}
