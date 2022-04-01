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
package com.qwazr.utils;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Language {

	private static final String[] LANG_LIST =
			{ "af", "ar", "bg", "bn", "cs", "da", "de", "el", "en", "es", "et", "fa", "fi", "fr", "gu", "he", "hi",
					"hr", "hu", "id", "it", "ja", "kn", "ko", "lt", "lv", "mk", "ml", "mr", "ne", "nl", "no", "pa",
					"pl", "pt", "ro", "ru", "sk", "sl", "so", "sq", "sv", "sw", "ta", "te", "th", "tl", "tr", "uk",
					"ur", "vi", "zh-cn", "zh-tw" };

	static {
		try {
			List<String> langList = DetectorFactory.getLangList();
			List<String> profiles = new ArrayList<String>(langList.size());
			for (String lang : LANG_LIST) {
				InputStream is = com.cybozu.labs.langdetect.Detector.class.getResourceAsStream("/profiles/" + lang);
				profiles.add(IOUtils.toString(is, Charset.defaultCharset()));
				is.close();
			}
			DetectorFactory.loadProfile(profiles);
		} catch (LangDetectException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Try to detect the language of a given text.
	 *
	 * @param text   the text to identify
	 * @param length the number of characters used for the language detection
	 * @return the code of the detected language
	 * @throws LangDetectException from langDetect package
	 */
	public static final String detect(String text, int length) throws LangDetectException {
		if (StringUtils.isEmpty(text))
			return null;
		Detector detector = DetectorFactory.create();
		detector.setMaxTextLength(length);
		detector.append(text);
		return detector.detect();
	}

	/**
	 * Try to detect the language of a given text.
	 *
	 * @param text   the text to identify
	 * @param length the number of characters used for the language detection
	 * @return the code of the detected language, null if the language is not
	 * detected
	 */
	public static final String quietDetect(String text, int length) {
		try {
			return detect(text, length);
		} catch (LangDetectException e) {
			return null;
		}
	}
}
