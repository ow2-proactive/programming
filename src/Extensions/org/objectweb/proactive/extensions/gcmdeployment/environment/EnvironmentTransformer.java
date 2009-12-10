/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.gcmdeployment.environment;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;


class EnvironmentTransformer {
    Map<String, String> vmap;
    protected Document document;

    public EnvironmentTransformer(Map<String, String> vmap, Document document) {
        this.vmap = vmap;
        this.document = document;
    }

    public void transform(OutputStream output) throws XPathExpressionException, SAXException,
            TransformerException {
        String[] nameList = vmap.keySet().toArray(new String[0]);

        String[] valueList = new String[nameList.length];
        for (int i = 0; i < nameList.length; i++) {
            valueList[i] = vmap.get(nameList[i]);
        }

        // Escape \ and $
        for (int i = 0; i < valueList.length; i++) {
            valueList[i] = valueList[i].replaceAll("\\\\", "\\\\\\\\");
            valueList[i] = valueList[i].replaceAll("\\$", "\\\\\\$");
        }

        String nameListStr = "";
        String valueListStr = "";
        String sep = "" + ((char) 5);
        if (nameList.length > 0) {
            for (int i = 0; i < nameList.length - 1; i++) {
                nameListStr += nameList[i] + sep;
                valueListStr += valueList[i] + sep;
            }
            nameListStr += nameList[nameList.length - 1];
            valueListStr += valueList[nameList.length - 1];
        }

        //PAProperties.JAVAX_XML_TRANSFORM_TRANSFORMERFACTORY.setValue("net.sf.saxon.TransformerFactoryImpl");
        DOMSource domSource = new DOMSource(document);
        TransformerFactory tfactory = TransformerFactory.newInstance();

        InputStream variablesIS = this.getClass().getResourceAsStream("variables.xsl");
        Source stylesheetSource = new StreamSource(variablesIS);

        Transformer transformer = null;
        try {
            transformer = tfactory.newTransformer(stylesheetSource);
            transformer.setParameter("nameList", nameListStr);
            transformer.setParameter("valueList", valueListStr);
            StreamResult result = new StreamResult(output);
            transformer.transform(domSource, result);
        } catch (TransformerException e) {
            throw e;
        }
    }
}
