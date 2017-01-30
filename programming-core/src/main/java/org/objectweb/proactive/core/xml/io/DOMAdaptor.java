/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.xml.io;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 *
 * Adaptor between a DOM and the XMLHandler
 *
 * @author The ProActive Team
 * @version      0.91
 *
 */
public class DOMAdaptor {
    private XMLHandler targetHandler;

    public DOMAdaptor(XMLHandler targetHandler) {
        this.targetHandler = targetHandler;
    }

    //
    // -- PUBLIC METHOD ------------------------------------------------------
    //
    public void read(Element rootElement) throws java.io.IOException {
        try {
            domWalker(rootElement);
        } catch (org.xml.sax.SAXException e) {
            throw new java.io.IOException(e.getMessage());
        }
    }

    //
    // -- PRIVATE METHODS ------------------------------------------------------
    //
    private void domWalker(Node node) throws org.xml.sax.SAXException {
        String localName = node.getNodeName();
        NamedNodeMap nodeMap = node.getAttributes();
        java.util.Vector<String> prefixes = null;
        if (nodeMap == null) {
            targetHandler.startElement(localName, new EmptyAttributesImpl());
        } else {
            prefixes = notifyStartPrefixMapping(nodeMap);
            targetHandler.startElement(localName, new AttributesImpl(nodeMap));
        }
        processChilds(node);
        targetHandler.endElement(localName);
        if (prefixes != null) {
            notifyEndPrefixMapping(prefixes);
        }
    }

    private void processChilds(Node node) throws org.xml.sax.SAXException {
        StringBuilder sb = null;
        Node child = node.getFirstChild();
        while (child != null) {
            switch (child.getNodeType()) {
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE:
                    if (sb == null) {
                        sb = new StringBuilder();
                    }
                    sb.append(((org.w3c.dom.CharacterData) child).getData());
                    break;
                default:
                    domWalker(child);
                    break;
            }
            child = child.getNextSibling();
        }
        if (sb != null) {
            targetHandler.readValue(sb.toString());
        }
    }

    private java.util.Vector<String> notifyStartPrefixMapping(NamedNodeMap nodeMap) throws org.xml.sax.SAXException {
        java.util.Vector<String> prefixes = null;
        int n = nodeMap.getLength();
        for (int i = 0; i < n; i++) {
            Node attributeNode = nodeMap.item(i);
            String attributeName = attributeNode.getNodeName();
            if (attributeName.startsWith("xmlns:")) {
                // found a namespace attribute
                if (prefixes == null) {
                    prefixes = new java.util.Vector<String>();
                }
                String prefix = attributeName.substring(6);
                String URI = attributeNode.getNodeValue();
                prefixes.addElement(prefix);
                targetHandler.startPrefixMapping(prefix, URI);
            }
        }
        return prefixes;
    }

    private void notifyEndPrefixMapping(java.util.Vector<String> prefixes) throws org.xml.sax.SAXException {
        int n = prefixes.size();
        for (int i = 0; i < n; i++) {
            targetHandler.endPrefixMapping((String) prefixes.elementAt(i));
        }
    }

    //
    // -- INNER CLASSES ------------------------------------------------------
    //
    private class AttributesImpl implements Attributes {
        private NamedNodeMap attributes;

        AttributesImpl(NamedNodeMap attributes) {
            this.attributes = attributes;
        }

        public String getValue(int index) {
            Node node = attributes.item(index);
            if (node == null) {
                return null;
            }
            return node.getNodeValue();
        }

        public String getValue(String qName) {
            Node node = attributes.getNamedItem(qName);
            if (node == null) {
                return null;
            }
            return node.getNodeValue();
        }

        public String getValue(String uri, String localPart) {
            Node node = attributes.getNamedItemNS(uri, localPart);
            if (node == null) {
                return null;
            }
            return node.getNodeValue();
        }

        public int getLength() {
            return attributes.getLength();
        }
    }

    protected class EmptyAttributesImpl implements Attributes {
        EmptyAttributesImpl() {
        }

        public String getValue(int index) {
            return null;
        }

        public String getValue(String qName) {
            return null;
        }

        public String getValue(String uri, String localPart) {
            return null;
        }

        public int getLength() {
            return 0;
        }
    }
}
