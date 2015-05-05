/**
 * Copyright 2014-2015 OpenSearchServer Inc.
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
package com.qwazr.search.index.osse;

public class OsseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5875269000359657689L;

	private final Integer errorCode;

	public OsseException(String message, Integer errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public boolean isError(int... errorCodes) {
		if (errorCode == null)
			return false;
		if (errorCodes == null)
			return false;
		for (int code : errorCodes)
			if (errorCode == code)
				return true;
		return false;
	}

}
