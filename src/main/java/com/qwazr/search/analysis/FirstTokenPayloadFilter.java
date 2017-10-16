/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qwazr.search.analysis;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public class FirstTokenPayloadFilter extends FilteringTokenFilter {

	private BytesRef payload;

	private final PayloadAttribute payAtt = addAttribute(PayloadAttribute.class);
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

	public FirstTokenPayloadFilter(TokenStream input) {
		super(input);
		payload = null;
	}

	@Override
	final public void reset() throws IOException {
		super.reset();
		payload = null;
	}

	@Override
	protected boolean accept() throws IOException {
		if (payload == null) {
			payload = computePayload();
			return false;
		}
		return true;
	}

	BytesRef computePayload() {
		char c = termAtt.charAt(0);
		assert c >= 48 && termAtt.length() == 1;
		return new BytesRef(new byte[] { (byte) (c - 48) });
	}

	/**
	 * Put the second pass filter at the end of the analyzer to set the payload.
	 *
	 * @param input the stream to analyze
	 * @return a new PayloadSetter
	 */
	final public PayloadSetter newSetterFilter(TokenStream input) {
		return new PayloadSetter(input);
	}

	public final class PayloadSetter extends TokenFilter {

		PayloadSetter(TokenStream input) {
			super(input);
		}

		@Override
		final public boolean incrementToken() throws IOException {
			if (!input.incrementToken())
				return false;
			assert payload != null;
			payAtt.setPayload(payload);
			return true;
		}
	}
}