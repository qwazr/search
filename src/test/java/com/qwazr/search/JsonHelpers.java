/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

package com.qwazr.search;

import com.qwazr.utils.ObjectMappers;
import org.junit.Assert;

import java.io.IOException;

public class JsonHelpers {

    public static <T> T serializeDeserializeAndCheckEquals(T source, Class<T> type) throws IOException {
        T target = ObjectMappers.JSON.readValue(ObjectMappers.JSON.writeValueAsString(source), type);
        Assert.assertEquals(source, target);
        Assert.assertEquals(source.hashCode(), target.hashCode());
        return target;
    }
}
