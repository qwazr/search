/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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
package com.qwazr.utils.inputstream;

import org.apache.commons.io.input.CountingInputStream;

import java.io.IOException;
import java.io.InputStream;

final public class LimiterInputStream extends CountingInputStream {

	private final long sizeLimit;

	public LimiterInputStream(final InputStream input, final long sizeLimit) {
		super(input);
		this.sizeLimit = sizeLimit;
	}

	@Override
	final public int read() throws IOException {
		final int c = super.read();
		if (c == -1)
			return -1;
		checkSizeLimit();
		return c;
	}

	@Override
	final public int read(final byte b[], final int off, final int len) throws IOException {
		final int c = super.read(b, off, len);
		switch (c) {
			case -1:
				return -1;
			case 0:
				return 0;
			default:
				checkSizeLimit();
				return c;
		}
	}

	@Override
	final public long skip(final long n) throws IOException {
		final long c = super.skip(n);
		checkSizeLimit();
		return c;
	}

	private void checkSizeLimit() throws IOException {
		final long count = getByteCount();
		if (count > sizeLimit)
			throw new SizeExceededException(count, sizeLimit);
	}
}
