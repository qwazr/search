/*
 * Copyright 2016-2018 Emmanuel Keller / QWAZR
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

import org.junit.Assert;
import org.junit.Test;

public class PagingTest {

	private void checkResult(Long totalHit, long currentStart, Integer expectedPrev, Integer expectedCurrent,
			Integer expectedNext, int... expectedPages) {
		final Paging paging = new Paging(totalHit, currentStart, 10, 10);
		if (expectedPages.length == 0) {
			Assert.assertNull(paging.getPages());
			Assert.assertNull(paging.getTotalPage());
		} else {
			Assert.assertEquals(totalHit == 0 ? 0 : (totalHit + 9) / 10, paging.getTotalPage(), 0);
			Assert.assertEquals(paging.getPages().size(), expectedPages.length, 0);
			int pos = 0;
			for (int expectedPageNumber : expectedPages) {
				Paging.Page page = paging.getPages().get(pos++);
				Assert.assertEquals(expectedPageNumber, page.getNumber());
				Assert.assertEquals((expectedPageNumber - 1) * 10, page.getStart());
			}
		}
		if (expectedPrev == null)
			Assert.assertNull(paging.getPrev());
		else
			Assert.assertEquals(paging.of(expectedPrev - 1), paging.getPrev());
		if (expectedNext == null)
			Assert.assertNull(paging.getNext());
		else
			Assert.assertEquals(paging.of(expectedNext - 1), paging.getNext());
		if (expectedCurrent == null)
			Assert.assertNull(paging.getCurrent());
		else
			Assert.assertEquals(paging.of(expectedCurrent - 1), paging.getCurrent());
	}

	@Test
	public void testEmpty() {
		checkResult(null, 0, null, null, null);
		checkResult(-10L, 0, null, null, null);
		checkResult(0L, 0, null, null, null);
		checkResult(0L, 15, null, null, null);
	}

	@Test
	public void testOnePage() {
		checkResult(1L, 0, null, 1, null, 1);
		checkResult(1L, 5, null, 1, null, 1);
		checkResult(1L, 10, null, 1, null, 1);
		checkResult(1L, 15, null, 1, null, 1);
		checkResult(9L, 0, null, 1, null, 1);
		checkResult(9L, 5, null, 1, null, 1);
		checkResult(9L, 10, null, 1, null, 1);
		checkResult(9L, 15, null, 1, null, 1);
	}

	@Test
	public void testTwoPages() {
		checkResult(15L, 12, 1, 2, null, 1, 2);
		checkResult(15L, 10, 1, 2, null, 1, 2);
		checkResult(15L, 19, 1, 2, null, 1, 2);

	}

	@Test
	public void testMiddlePage() {
		checkResult(30L, 10, 1, 2, 3, 1, 2, 3);
		checkResult(30L, 11, 1, 2, 3, 1, 2, 3);
		checkResult(30L, 19, 1, 2, 3, 1, 2, 3);
	}

	@Test
	public void testLastPage() {
		checkResult(30L, 20, 2, 3, null, 1, 2, 3);
		checkResult(30L, 21, 2, 3, null, 1, 2, 3);
		checkResult(30L, 29, 2, 3, null, 1, 2, 3);
		checkResult(30L, 30, 2, 3, null, 1, 2, 3);
	}

	@Test
	public void testTenPages() {
		checkResult(1000L, 10, 1, 2, 3, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		checkResult(1000L, 50, 5, 6, 7, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		checkResult(1000L, 60, 6, 7, 8, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
		checkResult(1000L, 100, 10, 11, 12, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
	}
}
