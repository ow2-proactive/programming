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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupCCSParser extends AbstractGroupSchedulerParser {

    private static final String NODE_NAME = "ccsGroup";

    private static final String NODE_NAME_RESOURCES = "resources";

    private static final String NODE_NAME_STDOUT = "stdout";

    private static final String NODE_NAME_STDERR = "stderr";

    private static final String ATTR_RESOURCES_CPUS = "cpus";

    private static final String ATTR_RESOURCES_RUNTIME = "runtime";

    private static final String NODE_NAME_PRECMD = "preCommand";

    @Override
    public AbstractGroup createGroup() {
        return new GroupCCS();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupCCS ccsGroup = (GroupCCS) super.parseGroupNode(groupNode, xpath);

        NodeList childNodes = groupNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = childNode.getNodeName();
            String nodeValue = GCMParserHelper.getElementValue(childNode);

            if (nodeName.equals(NODE_NAME_RESOURCES)) {
                String cpus = GCMParserHelper.getAttributeValue(childNode, ATTR_RESOURCES_CPUS);
                if (cpus != null) {
                    ccsGroup.setCpus(Integer.parseInt(cpus));
                }
                String runtime = GCMParserHelper.getAttributeValue(childNode, ATTR_RESOURCES_RUNTIME);
                if (runtime != null) {
                    ccsGroup.setRunTime(runtime);
                }
            } else if (nodeName.equals(NODE_NAME_STDOUT)) {
                ccsGroup.setStdout(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDERR)) {
                ccsGroup.setStderr(nodeValue);
            } else if (nodeName.equals(NODE_NAME_PRECMD)) {
                ccsGroup.setPreCommand(nodeValue);
            }
        }
        return ccsGroup;
    }
}
