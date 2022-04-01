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

import java.io.IOException;

public class SizeExceededException extends IOException {

	private static final long serialVersionUID = 5299460775637095407L;

	public SizeExceededException(long currentSize, long sizeLimit) {
		super("Size limit exceeded: " + currentSize + " - Limited to " + sizeLimit + " bytes");
	}
}
