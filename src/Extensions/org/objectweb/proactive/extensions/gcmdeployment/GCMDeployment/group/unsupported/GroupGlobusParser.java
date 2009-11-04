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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.unsupported;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.AbstractGroup;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.AbstractGroupSchedulerParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupGlobusParser extends AbstractGroupSchedulerParser {
    private static final String ATTR_QUEUE = "queue";
    private static final String ATTR_HOSTNAME = "hostname";
    private static final String NODE_NAME_STDERR = "stderr";
    private static final String NODE_NAME_STDOUT = "stdout";
    private static final String NODE_NAME_STDIN = "stdin";
    private static final String NODE_NAME_DIRECTORY = "directory";
    private static final String NODE_NAME_MAX_TIME = "maxTime";
    private static final String NODE_NAME_COUNT = "count";
    private static final String NODE_NAME = "globusGroup";

    @Override
    public AbstractGroup createGroup() {
        return new GroupGlobus();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupGlobus globusGroup = (GroupGlobus) super.parseGroupNode(groupNode, xpath);

        String hostname = GCMParserHelper.getAttributeValue(groupNode, ATTR_HOSTNAME);

        globusGroup.setHostname(hostname);

        String queue = GCMParserHelper.getAttributeValue(groupNode, ATTR_QUEUE);

        globusGroup.setQueue(queue);

        NodeList childNodes = groupNode.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); ++j) {
            Node child = childNodes.item(j);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeValue = GCMParserHelper.getElementValue(child);
            String nodeName = child.getNodeName();
            if (nodeName.equals(NODE_NAME_COUNT)) {
                globusGroup.setCount(nodeValue);
            } else if (nodeName.equals(NODE_NAME_MAX_TIME)) {
                globusGroup.setMaxTime(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDOUT)) {
                globusGroup.setStdout(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDERR)) {
                globusGroup.setStderr(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDIN)) {
                globusGroup.setStdin(nodeValue);
            } else if (nodeName.equals(NODE_NAME_DIRECTORY)) {
                globusGroup.setDirectory(nodeValue);
            }
        }

        return globusGroup;
    }
}
