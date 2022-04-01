/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.search.reindex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.RandomUtils;
import java.util.Map;
import org.junit.BeforeClass;

public class ReindexWithId extends AbstractReindexTest {

    @BeforeClass
    public static void setup() throws JsonProcessingException {
        setup(ReindexWithId::getRandomDoc);
    }

    public static Map<String, Object> getRandomDoc() {
        return Map.of(
            "id", HashUtils.newTimeBasedUUID().toString(),
            "text", RandomUtils.alphanumeric(100),
            "value", RandomUtils.nextInt(0, 10));
    }


}
