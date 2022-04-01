/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.RegExpUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpUtilsTest {

	@Test
	public void anyMatchTest() {
		List<Matcher> list = Arrays.asList(Pattern.compile("http://.*.qwazr.*").matcher(""),
				Pattern.compile("https://.*.qwazr.*").matcher(""));
		Assert.assertTrue(RegExpUtils.anyMatch("https://www.qwazr.com", list));
		Assert.assertFalse(RegExpUtils.anyMatch("https://www.opensearchserver.com", list));
	}
}
