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
package com.qwazr.utils.test;

import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

public class RandomUtilsTest {

	@Test
	public void testAlphanumeric() {
		String s = RandomUtils.alphanumeric(10);
		Assert.assertNotNull(s);
		Assert.assertEquals(10, s.length());
		for (Character c : s.toCharArray())
			if (!Character.isDigit(c) && !Character.isLetter(c))
				Assert.fail("Bad character: " + s);
	}

	@Test
	public void testRandoms() {
		RandomUtils.nextShort();
		RandomUtils.nextByte();
		RandomUtils.nextAlphanumericChar();
	}

	@Test
	public void testDate() {
		Assert.assertTrue(RandomUtils.nextFutureDate(1, 10).getTime() > System.currentTimeMillis());
		Assert.assertTrue(RandomUtils.nextPastDate(1, 10).getTime() < System.currentTimeMillis());
	}
}
