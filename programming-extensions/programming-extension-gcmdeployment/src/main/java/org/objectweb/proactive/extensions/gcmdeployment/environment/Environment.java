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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserConstants;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class Environment {

    /*
     * Since XSLT 1.0 & Java static methods are used to perform the variable replacement, it is not
     * thread safe
     */
    synchronized public static InputSource replaceVariables(URL descriptor, VariableContractImpl vContract, XPath xpath,
            String namespace)
            throws IOException, SAXException, XPathExpressionException, TransformerException, URISyntaxException {

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        domFactory.setIgnoringComments(true);

        // Get the variable map
        EnvironmentParser environmentParser;
        environmentParser = new EnvironmentParser(descriptor, vContract, domFactory, xpath, namespace);

        DocumentBuilder newDocumentBuilder = GCMParserHelper.getNewDocumentBuilder(domFactory);

        Document baseDocument = newDocumentBuilder.parse(descriptor.openStream());

        // sanity check on the document's namespace
        // we have to do this because we have no schema validation at this stage 
        //
        String expectedNamespace = namespace.equals(GCMParserConstants.GCM_APPLICATION_NAMESPACE_PREFIX) ? GCMParserConstants.GCM_APPLICATION_NAMESPACE
                                                                                                         : GCMParserConstants.GCM_DEPLOYMENT_NAMESPACE;
        NamedNodeMap rootNodeAttributes = baseDocument.getFirstChild().getAttributes();
        if (rootNodeAttributes != null) {
            Node attr = rootNodeAttributes.getNamedItem("xmlns");
            if (attr == null || !attr.getNodeValue().equals(expectedNamespace)) {
                if (attr != null && attr.getNodeValue().equals("urn:proactive:deployment:3.3")) {
                    throw new SAXException("descriptor is using old format - expected namespace is " +
                                           expectedNamespace);
                } else {
                    throw new SAXException("document has wrong namespace or no namespace - must be in " +
                                           expectedNamespace);
                }
            }
        } else {
            throw new SAXException("couldn't check document's namespace");
        }

        EnvironmentTransformer environmentTransformer;
        environmentTransformer = new EnvironmentTransformer(environmentParser.getVariableContract(), descriptor);

        // We get the file name from the url
        // TODO test it on linux / windows
        File abstractFile = new File(descriptor.getFile());
        File tempFile = File.createTempFile(abstractFile.getName(), null);
        tempFile.deleteOnExit();

        OutputStream outputStream = new FileOutputStream(tempFile);
        environmentTransformer.transform(outputStream);
        outputStream.close();

        InputSource inputSource = new InputSource(new FileInputStream(tempFile));
        return inputSource;
    }

}
