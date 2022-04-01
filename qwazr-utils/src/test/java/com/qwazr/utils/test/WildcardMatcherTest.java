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

package com.qwazr.utils.test;

import com.qwazr.utils.WildcardMatcher;
import org.junit.Assert;
import org.junit.Test;

public class WildcardMatcherTest {

	@Test
	public void matcherTest() {
		Assert.assertTrue(new WildcardMatcher("http://*.qwazr*").match("http://www.qwazr.com"));
		Assert.assertFalse(new WildcardMatcher("http://*.qwazr*").match("http://www.opensearchserver.com"));
		Assert.assertEquals(new WildcardMatcher("http://*.qwazr*").getPattern(), "http://*.qwazr*");
	}

}
