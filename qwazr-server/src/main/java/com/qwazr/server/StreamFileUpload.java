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
package com.qwazr.server;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class StreamFileUpload extends FileUpload {

	public StreamFileUpload(final File repository) {
		super(new DiskFileItemFactory(0, repository));
	}

	public List<FileItem> parse(final String contentType, final long contentLength, final String encoding,
			final InputStream inputStream) throws FileUploadException {
		return parseRequest(new StreamRequestContext(contentType, contentLength, encoding, inputStream));
	}

}

