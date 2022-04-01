/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

public class CharsetUtils {

	public final static String charsetDetector(InputStream inputStream) throws IOException {
		CharsetDetector detector = new CharsetDetector();
		detector.setText(inputStream);
		CharsetMatch match = detector.detect();
		return match == null ? null : match.getName();
	}

	public final static String charsetDetector(byte[] bytes) {
		CharsetDetector detector = new CharsetDetector();
		detector.setText(bytes);
		CharsetMatch match = detector.detect();
		return match == null ? null : match.getName();
	}

	public final static CharsetEncoder newUTF8Encoder() {
		synchronized (StandardCharsets.UTF_8) {
			return StandardCharsets.UTF_8.newEncoder();
		}
	}

	public final static byte[] encodeUtf8(String string) {
		if (string == null)
			return null;
		return string.getBytes(StandardCharsets.UTF_8);
	}

	public final static String decodeUtf8(byte[] bytes) {
		if (bytes == null)
			return null;
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public final static String decodeUtf8(ByteBuffer bytes) throws IOException {
		if (bytes == null)
			return null;
		return new String(StandardCharsets.UTF_8.newDecoder().decode(bytes).array());
	}

}
