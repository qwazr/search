/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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
package com.qwazr.binder.impl;

import com.qwazr.utils.RandomUtils;

public interface BaseTestInterface {

	default Long nextLong() {
		return RandomUtils.nextLong(0, Byte.MAX_VALUE);
	}

	default Short nextShort() {
		return (short) RandomUtils.nextInt(0, Byte.MAX_VALUE);
	}

	default Integer nextInt() {
		return RandomUtils.nextInt(0, Byte.MAX_VALUE);
	}

	default Double nextDouble() {
		return RandomUtils.nextDouble(0, Byte.MAX_VALUE);
	}

	default Float nextFloat() {
		return RandomUtils.nextFloat(0, Byte.MAX_VALUE);
	}

	default Byte nextByte() {
		return (byte) RandomUtils.nextInt(0, Byte.MAX_VALUE);
	}

	default Character nextChar() {
		return (char) (byte) nextByte();
	}

	default Boolean nextBoolean() {
		return RandomUtils.nextBoolean();
	}
}
