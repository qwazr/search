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
package com.qwazr.utils.test;

import com.qwazr.utils.HtmlUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static com.qwazr.utils.HtmlUtils.htmlWrap;
import static com.qwazr.utils.HtmlUtils.htmlWrapReduce;

public class HtmlUtilsTest {

	private final static String[] TEST_URLS = {
			"file://&shy;Users/ekeller/Moteur/infotoday_enterprisesearchsourcebook08/Open_on_Windows.exe",
			"file://Users/ekeller/Moteur/infotoday_enterprisesearchsourcebook08/Open_on_Windows.exe?test=2" };

	private final static String[] WRAP_RESULTS = {
			"file://Users/ekeller&shy;/Moteur/infotoday_en&shy;terprisesearchsource&shy;book08/Open_on_Windo&shy;ws.exe",
			"file://Users/ekeller&shy;/Moteur/infotoday_en&shy;terprisesearchsource&shy;book08/Open_on_Windo&shy;ws.exe?test=2" };

	@Test
	public void testWrap() {
		int i = 0;
		for (String url : TEST_URLS)
			Assert.assertEquals(WRAP_RESULTS[i++], htmlWrap(url, 20));
	}

	private final static String[] WRAP_REDUCE_RESULTS = {
			"file://Users/ekeller…terprisesearchsourcebook08/Open_on_Windows.exe",
			"file://Users/ekeller…terprisesearchsourcebook08/Open_on_Windows.exe?test=2" };

	@Test
	public void testWrapReduce() {
		int i = 0;
		for (String url : TEST_URLS)
			Assert.assertEquals(WRAP_REDUCE_RESULTS[i++], htmlWrapReduce(url, 20, 80));

	}

	static final String html =
			"<html>" + "<head>\n" + "<meta name=\"metacontent\"/>\n" + "<title>Title Test</title>\n" + "</head>\n"
					+ "<body>\n" + "<h1>My title</h1>\n" + "<p>The content <span>of</span> the\narticle</p>\n"
					+ "</body>\n" + "</html>";

	public static Document getHtmlDocument(final String xml)
			throws IOException, SAXException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(new InputSource(new StringReader(xml)));
	}

	@Test
	public void testDomTextExtractor() throws ParserConfigurationException, SAXException, IOException {
		final Document document = getHtmlDocument(html);
		List<String> result = new ArrayList<>();
		HtmlUtils.domTextExtractor(document, result::add);
		Assert.assertArrayEquals(new String[] { "Title Test", "My title", "The content of the article" },
				result.toArray());
	}

}
