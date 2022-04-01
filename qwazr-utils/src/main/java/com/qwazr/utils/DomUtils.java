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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.function.Consumer;

public class DomUtils {

    static Node getAttribute(final Node node, final String attributeName) {
        final NamedNodeMap attributes = node.getAttributes();
        return attributes == null ? null : attributes.getNamedItem(attributeName);
    }

    public static String getAttributeString(final Node node, final String attributeName) {
        final Node attrNode = getAttribute(node, attributeName);
        return attrNode == null ? null : attrNode.getTextContent();
    }

    public static void extractText(final Node node, final StringBuilder sb) {
        switch (node.getNodeType()) {
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                sb.append(node.getNodeValue());
                break;
            default:
                break;
        }
        forEach(node.getChildNodes(), n -> extractText(n, sb));
    }

    public static String getText(final Node node) {
        final StringBuilder sb = new StringBuilder();
        extractText(node, sb);
        return sb.toString();
    }

    public static void forEach(final NodeList nodeList, final Consumer<Node> nodeConsumer) {
        if (nodeList == null || nodeConsumer == null)
            return;
        final int length = nodeList.getLength();
        for (int i = 0; i < length; i++)
            nodeConsumer.accept(nodeList.item(i));
    }

}
