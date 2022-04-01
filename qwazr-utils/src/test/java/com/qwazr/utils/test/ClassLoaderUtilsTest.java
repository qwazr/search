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

import com.qwazr.utils.ClassLoaderUtils;
import org.junit.Assert;
import org.junit.Test;

public class ClassLoaderUtilsTest {

	@Test
	public void findClassNoClassLoader() throws ClassNotFoundException {
		Assert.assertEquals(String.class, ClassLoaderUtils.findClass(String.class.getName()));
	}

	@Test
	public void findClass() throws ClassNotFoundException {
		Assert.assertEquals(ClassLoaderUtilsTest.class,
				ClassLoaderUtils.findClass(ClassLoaderUtilsTest.class.getName()));
	}

	@Test
	public void findClassList() throws ClassNotFoundException {
		String[] prefixes = { "xxx.yyy.", "com.qwazr.utils.test." };
		Assert.assertEquals(ClassLoaderUtilsTest.class, ClassLoaderUtils.findClass("ClassLoaderUtilsTest", prefixes));
	}

}
