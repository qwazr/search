/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.WordCount;
import org.junit.Assert;
import org.junit.Test;

public class WordCountTest {

	public final static String[] text1 = { "Copyright 2014-2016 Emmanuel Keller / QWAZR",
			"Licensed under the Apache License, Version 2.0 (the \"License\");",
			"Unless required by applicable law or agreed to in writing, software",
			"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied",
			"See the License for the specific language governing permissions and", "limitations under the License" };

	public final static String[] text2 = { "See the License for the specific language governing permissions and",
			"Copyright 2014-2016 Emmanuel Keller / QWAZR",
			"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied", "limitations under the License",
			"Licensed under the Apache License, Version 2.0 (the \"License\");",
			"Unless required by applicable law or agreed to in writing, software", };

	public final static String[] text3 = { "See the License",
			"Unless required by applicable law or agreed to in writing, software" };

	public final static String[] text4 = { "Copyright 2014-2016 Emmanuel Keller / QWAZR",
			"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND",
			"Licensed under the Apache License, Version 2.0 (the \"License\");",
			"Unless required by applicable law or agreed to in writing, software", };

	@Test
	public void testSameText() {
		final WordCount dic1 = new WordCount(text1);
		final WordCount dic2 = new WordCount(text1);
		Assert.assertEquals(1.0F, WordCount.compare(dic1, dic2), 0);
	}

	@Test
	public void testUnorderedIdenticalText() {
		final WordCount dic1 = new WordCount(text1);
		final WordCount dic2 = new WordCount(text2);
		Assert.assertEquals(1.0F, WordCount.compare(dic1, dic2), 0);
	}

	@Test
	public void testPartialLowText() {
		final WordCount dic1 = new WordCount(text1);
		final WordCount dic3 = new WordCount(text3);
		Assert.assertEquals(0.3F, WordCount.compare(dic1, dic3), 0.1F);
	}

	@Test
	public void testPartialHighText() {
		final WordCount dic1 = new WordCount(text1);
		final WordCount dic4 = new WordCount(text4);
		Assert.assertEquals(0.7F, WordCount.compare(dic1, dic4), 0.1F);
	}

	@Test
	public void testOneEmpty() {
		final WordCount dic1 = new WordCount(text1);
		final WordCount dic = new WordCount();
		Assert.assertEquals(0, WordCount.compare(dic1, dic), 0);
	}

	@Test
	public void testBothEmpty() {
		final WordCount dic1 = new WordCount();
		final WordCount dic2 = new WordCount();
		Assert.assertEquals(Float.NaN, WordCount.compare(dic1, dic2), 0);
	}
}
