/*
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationUtils {

	public static class IteratorEnumeration<E> implements Enumeration<E> {
		private final Iterator<E> iterator;

		public IteratorEnumeration(Iterator<E> iterator) {
			this.iterator = iterator;
		}

		@Override
		public E nextElement() {
			return iterator.next();
		}

		@Override
		public boolean hasMoreElements() {
			return iterator.hasNext();
		}

	}
}
