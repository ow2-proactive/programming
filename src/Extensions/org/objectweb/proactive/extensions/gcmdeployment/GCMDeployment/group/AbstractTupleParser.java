/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public abstract class AbstractTupleParser implements GroupParser {

    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        String id = GCMParserHelper.getAttributeValue(groupNode, "id");

        AbstractGroup group = createGroup();

        group.setId(id);

        String commandPath = GCMParserHelper.getAttributeValue(groupNode, "commandPath");
        if (commandPath != null) {
            group.setCommandPath(commandPath);
        }

        try {
            Node environmentNode = (Node) xpath.evaluate("dep:environment", groupNode, XPathConstants.NODE);

            if (environmentNode != null) {
                Map<String, String> envVars = new HashMap<String, String>();

                NodeList argNodes = (NodeList) xpath.evaluate("dep:variable", environmentNode,
                        XPathConstants.NODESET);

                for (int i = 0; i < argNodes.getLength(); ++i) {
                    Node argNode = argNodes.item(i);
                    String name = GCMParserHelper.getAttributeValue(argNode, "name");
                    String value = GCMParserHelper.getAttributeValue(argNode, "value");
                    envVars.put(name, value);
                }

                group.setEnvironment(envVars);
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }

        return group;
    }

    public abstract AbstractGroup createGroup();

}
