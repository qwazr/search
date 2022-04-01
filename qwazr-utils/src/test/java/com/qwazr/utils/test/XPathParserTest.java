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

import com.qwazr.utils.DomUtils;
import com.qwazr.utils.XPathParser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class XPathParserTest {

	static final String xml =
			"<html>" + "<head>" + "<meta name=\"metacontent\"/>" + "<title>Title Test</title>" + "</head>" + "<body>" +
					"<p>12</p>" + "<p>true</p>" + "</body>" + "</html>";

	private static Document xmlDocument;

	@BeforeClass
	public static void prepareXml() throws IOException, SAXException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		xmlDocument = db.parse(new InputSource(new StringReader(xml)));
	}

	@Test
	public void testEvaluateString()
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		XPathParser xpp = new XPathParser();
		Assert.assertEquals("Title Test", xpp.evaluateString(xmlDocument, "/html/head/title/text()"));
		Assert.assertEquals("Title Test", xpp.evaluateString(xmlDocument, "/html/head/title/text()"));
	}

	@Test
	public void testEvaluateNumber()
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		XPathParser xpp = new XPathParser();
		Assert.assertEquals(12D, xpp.evaluateNumber(xmlDocument, "/html/body/p"));
	}

	@Test
	public void testEvaluateBoolean()
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		XPathParser xpp = new XPathParser();
		Assert.assertEquals(true, xpp.evaluateBoolean(xmlDocument, "boolean(/html/body/p[2])"));
	}

	@Test
	public void testEvaluateNode()
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		XPathParser xpp = new XPathParser();
		Node node = xpp.evaluateNode(xmlDocument, "/html/body");
		Assert.assertNotNull(node);
		Assert.assertEquals("body", node.getNodeName());
	}

	class Evaluator implements XPathParser.Consumer {

		final List<Node> nodeList = new ArrayList<>();
		final List<Boolean> booleanList = new ArrayList<>();
		final List<String> stringList = new ArrayList<>();
		final List<Number> numberList = new ArrayList<>();

		public void accept(Node object) {
			nodeList.add(object);
		}

		public void accept(Boolean object) {
			booleanList.add(object);
		}

		public void accept(String object) {
			stringList.add(object);
		}

		public void accept(Number object) {
			numberList.add(object);
		}
	}

	@Test
	public void testEvaluateNodes()
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		XPathParser xpp = new XPathParser();
		Evaluator evaluator = new Evaluator();
		DomUtils.forEach(xpp.evaluateNodes(xmlDocument, "//p"), evaluator::accept);
		Assert.assertEquals(2, evaluator.nodeList.size());
	}

	@Test
	public void testEvaluatesNodes()
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		XPathParser xpp = new XPathParser();
		Evaluator evaluator = new Evaluator();
		xpp.evaluate(xmlDocument, "//p", evaluator);
		Assert.assertEquals(2, evaluator.nodeList.size());
	}

	@Test
	public void testEvaluatesNode()
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		XPathParser xpp = new XPathParser();
		Evaluator evaluator = new Evaluator();
		xpp.evaluate(xmlDocument, "//p[1]", evaluator);
		Assert.assertEquals(1, evaluator.nodeList.size());
	}

	@Test
	public void testEvaluatesString()
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		XPathParser xpp = new XPathParser();
		Evaluator evaluator = new Evaluator();
		xpp.evaluate(xmlDocument, "string(/html/body/p)", evaluator);
		Assert.assertEquals(1, evaluator.stringList.size());
	}

	@Test
	public void testAttributes()
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		XPathParser xpp = new XPathParser();
		Assert.assertEquals("metacontent",
				DomUtils.getAttributeString(xpp.evaluateNode(xmlDocument, "/html/head/meta"), "name"));
	}

}
