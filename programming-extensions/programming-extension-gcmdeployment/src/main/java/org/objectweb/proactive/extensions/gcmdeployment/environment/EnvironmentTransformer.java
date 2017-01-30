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
package org.objectweb.proactive.extensions.gcmdeployment.environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.core.xml.VariableContract;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * 
 * 
 * @author ProActive team
 * @since  ProActive 5.2.0
 */
public class EnvironmentTransformer {
    protected URL source;

    final private VariableContract vc;

    final private VariableReplacer replacer;

    public EnvironmentTransformer(VariableContract vc, URL source) {
        this.vc = vc;
        this.replacer = new VariableReplacer();
        this.source = source;
    }

    public void transform(OutputStream output)
            throws XPathExpressionException, SAXException, TransformerException, URISyntaxException, IOException {
        InputSource is = new InputSource(source.openStream());

        XMLReader parser = XMLReaderFactory.createXMLReader();
        CustomFilter filter = new CustomFilter();
        filter.setParent(parser);
        filter.setContentHandler(new DefaultHandler());
        SAXSource source = new SAXSource(filter, is);

        StreamResult result = new StreamResult(output);

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
    }

    private class CustomFilter extends XMLFilterImpl {
        public void setContentHandler(ContentHandler handler) {
            super.setContentHandler(new CustomContentHandler(handler));
        }
    }

    private class CustomContentHandler implements ContentHandler {
        private ContentHandler parent;

        public CustomContentHandler(ContentHandler parent) {
            this.parent = parent;
        }

        public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attrs)
                throws SAXException {
            AttributesImpl a = new AttributesImpl(attrs);
            for (int i = 0; i < a.getLength(); i++) {
                String value = a.getValue(i);
                a.setValue(i, replacer.replaceAll(value));
            }

            parent.startElement(namespaceURI, localName, qualifiedName, a);
        }

        public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
            parent.endElement(namespaceURI, localName, qualifiedName);
        }

        public void startDocument() throws SAXException {
            parent.startDocument();
        }

        public void endDocument() throws SAXException {
            parent.endDocument();
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            parent.startPrefixMapping(prefix, uri);
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            parent.endPrefixMapping(prefix);
        }

        public void characters(char[] text, int start, int length) throws SAXException {
            char[] newChars = replacer.replaceAll(new String(text, start, length)).toCharArray();
            parent.characters(newChars, 0, newChars.length);
        }

        public void ignorableWhitespace(char[] text, int start, int length) throws SAXException {
            parent.ignorableWhitespace(text, start, length);
        }

        public void processingInstruction(String target, String data) throws SAXException {
            parent.processingInstruction(target, data);
        }

        public void skippedEntity(String name) throws SAXException {
            parent.skippedEntity(name);
        }

        public void setDocumentLocator(Locator locator) {
            parent.setDocumentLocator(locator);
        }
    }

    /**
     * Replace variables by their value.
     * 
     */
    private class VariableReplacer {

        public VariableReplacer() {
        }

        /**
         * Replace all variables in input by their value.
         * 
         * @param input
         *    The string to be modified
         * @return
         *    input with variable 
         */
        public String replaceAll(String input) {
            StringBuilder sb = new StringBuilder();

            Pattern pattern = Pattern.compile("\\$\\{[A-Za-z_0-9.]+\\}");
            Matcher matcher = pattern.matcher(input);

            int start = 0;
            int end = 0;

            while (matcher.find()) {
                final int old_start = start;
                final int old_end = end;
                start = matcher.start();
                end = matcher.end();

                // copy before the variable
                sb.append(input.substring(old_end, start));

                //replace the variable by its value
                String varName = input.substring(start + 2, end - 1);
                String varValue = vc.getValue(varName);
                if (varValue != null) {
                    sb.append(vc.getValue(varName));
                } else {
                    throw new IllegalStateException("Unknown variable: " + varName);
                }
            }

            // copy remaining
            sb.append(input.substring(end, input.length()));

            if (start == 0) {
                return sb.toString();
            } else {
                return this.replaceAll(sb.toString());
            }
        }
    }
}
