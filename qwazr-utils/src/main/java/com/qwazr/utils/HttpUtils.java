/*
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

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpUtils {

	/**
	 * Extract the parameters from a header content.
	 *
	 * @param headerContent The header value
	 * @return a map with all parameters
	 */
	public static Map<String, String> getHeaderParameters(final String headerContent) {
		if (headerContent == null)
			return null;
		final String[] params = StringUtils.split(headerContent, ';');
		if (params == null || params.length == 0)
			return null;
		final Map<String, String> nameValues = new LinkedHashMap<>();
		for (String param : params) {
			if (param == null)
				continue;
			String[] nameValue = StringUtils.split(param, "=");
			if (nameValue == null || nameValue.length != 2)
				continue;
			String value = nameValue[1].trim();
			if (value.startsWith("\"") && value.endsWith("\""))
				value = value.substring(1, value.length() - 1);
			nameValues.put(nameValue[0].trim().toLowerCase(), value);
		}
		return nameValues;
	}

	/**
	 * Extract the given parameter from a header content.
	 *
	 * @param headerContent the header value
	 * @param paramName     the requested parameter
	 * @return the parameter or null if it is not found
	 */
	public static String getHeaderParameter(final String headerContent, final String paramName) {
		if (headerContent == null)
			return null;
		Map<String, String> headerParams = getHeaderParameters(headerContent);
		if (headerParams == null)
			return null;
		return headerParams.get(paramName.trim().toLowerCase());
	}


}
