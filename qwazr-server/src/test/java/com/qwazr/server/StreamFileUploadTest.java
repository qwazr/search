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
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class StreamFileUploadTest {

	private final static String MULTIPART_TEST = "multipart_test.out";
	private final static String MIME_TYPE = "multipart/form; boundary=1550add082a";

	@Test
	public void extract() throws IOException, FileUploadException {
		final InputStream input = StreamFileUploadTest.class.getResourceAsStream(MULTIPART_TEST);
		final File tmpDirectory = Files.createTempDirectory(StreamFileUploadTest.class.getName()).toFile();
		final StreamFileUpload parser = new StreamFileUpload(tmpDirectory);
		final List<FileItem> files = parser.parse(MIME_TYPE, -1, "UTF-8", input);
		Assert.assertNotNull(files);
		Assert.assertEquals(2, files.size());
		for (FileItem fileItem : files)
			Assert.assertEquals(fileItem.getSize(), IOUtils.skip(fileItem.getInputStream(), fileItem.getSize() + 1));
	}

}