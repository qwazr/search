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
package com.qwazr.utils;

import org.apache.commons.text.CharacterPredicate;
import org.apache.commons.text.RandomStringGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class RandomUtils extends org.apache.commons.lang3.RandomUtils {

	public enum Predicates implements CharacterPredicate {

		LETTER_OR_DIGIT {
			@Override
			public boolean test(int codePoint) {
				return Character.isLetterOrDigit(codePoint);
			}
		}
	}

	final public static RandomStringGenerator LETTER_OR_DIGIT =
			new RandomStringGenerator.Builder().filteredBy(Predicates.LETTER_OR_DIGIT).withinRange(31, 127).build();

	public static String alphanumeric(int length) {
		return LETTER_OR_DIGIT.generate(length);
	}

	public static short nextShort() {
		return (short) nextInt();
	}

	public static byte nextByte() {
		return nextBytes(1)[0];
	}

	public static char nextAlphanumericChar() {
		return alphanumeric(1).charAt(0);
	}

	public static Date nextFutureDate(int fromDay, int toDay) {
		return Date.from(Instant.now().plus(Duration.ofDays(RandomUtils.nextInt(fromDay, toDay))));
	}

	public static Date nextPastDate(int fromDay, int toDay) {
		return Date.from(Instant.now().minus(Duration.ofDays(RandomUtils.nextInt(fromDay, toDay))));
	}
}
