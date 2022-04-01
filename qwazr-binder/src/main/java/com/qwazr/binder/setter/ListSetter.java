/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.binder.setter;

import java.util.List;

public interface ListSetter extends ErrorSetter {

	default void fromString(List<String> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromDouble(List<Double> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromFloat(List<Float> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromLong(List<Long> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromInteger(List<Integer> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromShort(List<Short> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromChar(List<Character> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromByte(List<Byte> values, Object object) {
		throw error("Not supported ", object);
	}

	default void fromBoolean(List<Boolean> values, Object object) {
		throw error("Not supported ", object);
	}

}
