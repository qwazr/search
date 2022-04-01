/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.utils.concurrent;

import java.util.Map;

public class ConcurrentUtils {

	public static <K, V, E extends Exception> void forEachEx(Map<K, V> map, BiConsumerEx<K, V, E> consumer) throws E {
		for (Map.Entry<K, V> entry : map.entrySet())
			consumer.accept(entry.getKey(), entry.getValue());
	}

}
