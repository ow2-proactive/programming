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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.unsupported;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.AbstractGroup;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.AbstractGroupSchedulerParser;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupCGSPParser extends AbstractGroupSchedulerParser {
    private static final String NODE_NAME_STDERR = "stderr";

    private static final String NODE_NAME_STDOUT = "stdout";

    private static final String NODE_NAME_DIRECTORY = "directory";

    private static final String NODE_NAME_COUNT = "count";

    private static final String ATTR_QUEUE = "queue";

    private static final String ATTR_HOSTNAME = "hostname";

    private static final String NODE_NAME = "cgspGroup";

    @Override
    public AbstractGroup createGroup() {
        return new GroupCGSP();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupCGSP cgspGroup = (GroupCGSP) super.parseGroupNode(groupNode, xpath);

        String hostname = GCMParserHelper.getAttributeValue(groupNode, ATTR_HOSTNAME);
        cgspGroup.setHostName(hostname);

        String queue = GCMParserHelper.getAttributeValue(groupNode, ATTR_QUEUE);
        cgspGroup.setQueue(queue);

        groupNode.getChildNodes();

        NodeList childNodes = groupNode.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); ++j) {
            Node child = childNodes.item(j);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = child.getNodeName();
            String nodeValue = GCMParserHelper.getElementValue(child);

            if (nodeName.equals(NODE_NAME_COUNT)) {
                cgspGroup.setCount(nodeValue);
            } else if (nodeName.equals(NODE_NAME_DIRECTORY)) {
                cgspGroup.setDirectory(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDOUT)) {
                cgspGroup.setStdout(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDERR)) {
                cgspGroup.setStderr(nodeValue);
            }
        }

        return cgspGroup;
    }
}
