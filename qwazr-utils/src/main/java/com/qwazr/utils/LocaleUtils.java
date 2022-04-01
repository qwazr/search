/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils;

import java.util.Locale;

public class LocaleUtils {

	public static Locale findLocaleISO639(String lang) {
		if (lang == null)
			return null;
		int l = lang.indexOf('-');
		if (l != -1)
			lang = lang.substring(0, l);
		lang = new Locale(lang).getLanguage();
		Locale[] locales = Locale.getAvailableLocales();
		for (Locale locale : locales)
			if (locale.getLanguage().equalsIgnoreCase(lang))
				return locale;
		return null;
	}

	public static final Locale findLocaleDescription(String language) {
		if (StringUtils.isEmpty(language))
			return null;
		Locale[] locales = Locale.getAvailableLocales();
		for (Locale locale : locales)
			if (locale.getLanguage().equalsIgnoreCase(language))
				return locale;
		for (Locale locale : locales)
			if (locale.getDisplayName(Locale.ENGLISH)
					.equalsIgnoreCase(language))
				return locale;
		for (Locale locale : locales)
			if (locale.getDisplayLanguage(Locale.ENGLISH).equalsIgnoreCase(
					language))
				return locale;
		for (Locale locale : locales)
			if (locale.getDisplayName().equalsIgnoreCase(language))
				return locale;
		for (Locale locale : locales)
			if (locale.getDisplayLanguage().equalsIgnoreCase(language))
				return locale;
		return null;
	}

}
