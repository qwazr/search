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

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {

	private final static Pattern removeTagPattern = Pattern.compile("<[^>]*>");
	private final static Pattern removeBrPattern1 =
			Pattern.compile("\\.\\p{Space}+<br\\p{Space}*/?>", Pattern.CASE_INSENSITIVE);
	private final static Pattern removeEndTagBlockPattern1 = Pattern.compile(
			"\\.\\p{Space}+</(p|div|h1|h2|h3|h4|h5|h6|hr|li|option|pre|select|table|tbody|td|textarea|tfoot|thead|th|title|tr|ul)>",
			Pattern.CASE_INSENSITIVE);
	private final static Pattern removeEndTagBlockPattern2 = Pattern.compile(
			"</(p|div|h1|h2|h3|h4|h5|h6|hr|li|option|pre|select|table|tbody|td|textarea|tfoot|thead|th|title|tr|ul)>",
			Pattern.CASE_INSENSITIVE);
	private final static Pattern removeBrPattern2 = Pattern.compile("<br\\p{Space}*/?>", Pattern.CASE_INSENSITIVE);
	private final static Pattern removeScriptObjectStylePattern =
			Pattern.compile("<(script|object|style)[^>]*>[^<]*</(script|object|style)>", Pattern.CASE_INSENSITIVE);

	public static final String removeTag(String text) {
		if (StringUtils.isEmpty(text))
			return text;
		text = StringUtils.replaceConsecutiveSpaces(text, " ");
		synchronized (removeScriptObjectStylePattern) {
			text = removeScriptObjectStylePattern.matcher(text).replaceAll("");
		}
		synchronized (removeBrPattern1) {
			text = removeBrPattern1.matcher(text).replaceAll("</p>");
		}
		synchronized (removeEndTagBlockPattern1) {
			text = removeEndTagBlockPattern1.matcher(text).replaceAll("</p>");
		}
		synchronized (removeEndTagBlockPattern2) {
			text = removeEndTagBlockPattern2.matcher(text).replaceAll(". ");
		}
		synchronized (removeBrPattern2) {
			text = removeBrPattern2.matcher(text).replaceAll(". ");
		}
		synchronized (removeTagPattern) {
			text = removeTagPattern.matcher(text).replaceAll("");
		}
		text = StringUtils.replaceConsecutiveSpaces(text, " ");
		return text;
	}

	public static final String removeTag(String text, String[] allowedTags) {
		if (allowedTags == null)
			text = StringUtils.replaceConsecutiveSpaces(text, " ");
		StringBuffer sb = new StringBuffer();
		Matcher matcher;
		synchronized (removeTagPattern) {
			matcher = removeTagPattern.matcher(text);
		}
		while (matcher.find()) {
			boolean allowed = false;
			String group = matcher.group();
			if (allowedTags != null) {
				for (String tag : allowedTags) {
					if (tag.equals(group)) {
						allowed = true;
						break;
					}
				}
			}
			matcher.appendReplacement(sb, allowed ? group : "");
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public final static String htmlWrap(String text, int wrapLength) {
		if (StringUtils.isEmpty(text))
			return text;
		if (text.length() < wrapLength)
			return text;
		text = StringUtils.replace(text, "&shy;", "");
		return WordUtils.wrap(text, wrapLength, "&shy;", true);
	}

	public final static String htmlWrapReduce(String text, int wrapLength, int maxSize) {
		if (StringUtils.isEmpty(text))
			return text;
		if (text.length() < maxSize)
			return text;
		text = StringUtils.replace(text, "&shy;", "");
		text = WordUtils.wrap(text, wrapLength, "\u00AD", true);
		String[] frags = StringUtils.split(text, '\u00AD');
		StringBuilder sb = new StringBuilder();
		int l = frags[0].length();
		for (int i = frags.length - 1; i > 0; i--) {
			String frag = frags[i];
			l += frag.length();
			if (l >= maxSize)
				break;
			sb.insert(0, frag);
		}
		sb.insert(0, '…');
		sb.insert(0, frags[0]);
		return sb.toString();
	}

	private final static HashSet<String> sentenceTagSet = new HashSet<>(
			Arrays.asList("p", "td", "div", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "li", "option", "pre", "select",
					"table", "tbody", "td", "textarea", "tfoot", "thead", "th", "title", "tr", "ul"));

	private final static HashSet<String> excludedTagSet = new HashSet<>(Arrays.asList("script", "style", "object"));

	private static void domTextExtractor(final Node node, int recursion, HashSet<Node> loopProtection,
			final StringBuffer buffer, final Consumer<String> output) {

		if (loopProtection.contains(node))
			return;
		loopProtection.add(node);

		if (recursion == 0)
			return;

		final short nodeType = node.getNodeType();

		if (nodeType == Node.COMMENT_NODE)
			return;

		final String s = node.getNodeName();
		final String nodeName = s == null ? null : s.toLowerCase();

		if (excludedTagSet.contains(nodeName))
			return;

		if (nodeType == Node.TEXT_NODE) {
			String text = node.getTextContent();
			if (text != null && !text.isEmpty()) {
				text = text.replace('\r', ' ').replace('\n', ' ');
				text = StringUtils.replaceConsecutiveSpaces(text, " ");
				text = text.trim();
				if (!text.isEmpty()) {
					text = StringEscapeUtils.unescapeHtml4(text);
					if (buffer.length() > 0)
						buffer.append(' ');
					buffer.append(text);
				}
			}
		}

		final int nextRecursion = recursion - 1;
		DomUtils.forEach(node.getChildNodes(),
				child -> domTextExtractor(child, nextRecursion, loopProtection, buffer, output));

		if (nodeName == null || buffer.length() == 0)
			return;

		if (sentenceTagSet.contains(nodeName)) {
			output.accept(buffer.toString());
			buffer.setLength(0);
		}
	}

	public final static int DEFAULT_MAX_RECURSION = 768;

	public static void domTextExtractor(final Node node, final int maxRecursion, Consumer<String> consumer) {
		final StringBuffer buffer = new StringBuffer();
		final HashSet<Node> loopProtection = new HashSet<>();
		domTextExtractor(node, maxRecursion, loopProtection, buffer, consumer);
		if (buffer.length() > 0)
			consumer.accept(buffer.toString());
	}

	public static void domTextExtractor(final Node node, Consumer<String> consumer) {
		domTextExtractor(node, DEFAULT_MAX_RECURSION, consumer);
	}
}
