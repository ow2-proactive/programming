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

import static org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers.GCMD_LOGGER;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupOARParser extends AbstractGroupSchedulerParser {
    private static final String NODE_NAME_RESOURCES = "resources";

    private static final String NODE_NAME_WALLTIME = "wallTime";

    private static final String NODE_NAME_DIRECTORY = "directory";

    private static final String NODE_NAME_STDOUT = "stdout";

    private static final String NODE_NAME_STDERR = "stderr";

    private static final String ATTR_QUEUE = "queue";

    private static final String ATTR_INTERACTIVE = "interactive";

    private static final String ATTR_TYPE = "type";

    private static final String ATTR_RESOURCES_NODES = "nodes";

    private static final String ATTR_RESOURCES_CPU = "cpu";

    private static final String ATTR_RESOURCES_CORE = "core";

    private static final String ATTR_RESOURCES_WALLTIME = "walltime";

    private static final String NODE_NAME = "oarGroup";

    @Override
    public AbstractGroup createGroup() {
        return new GroupOAR();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupOAR oarGroup = (GroupOAR) super.parseGroupNode(groupNode, xpath);

        String interactive = GCMParserHelper.getAttributeValue(groupNode, ATTR_INTERACTIVE);

        if (interactive != null) {
            oarGroup.setInteractive(interactive);
        }

        String queueName = GCMParserHelper.getAttributeValue(groupNode, ATTR_QUEUE);

        if (queueName != null) {
            oarGroup.setQueueName(queueName);
        }

        String type = GCMParserHelper.getAttributeValue(groupNode, ATTR_TYPE);
        if (type != null) {
            oarGroup.setType(type);
        }

        //
        // Parse child nodes
        //
        NodeList childNodes = groupNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = childNode.getNodeName();
            String nodeValue = GCMParserHelper.getElementValue(childNode);

            if (nodeName.equals(NODE_NAME_RESOURCES)) {
                if ((nodeValue != null) && (nodeValue.trim().length() != 0)) {
                    oarGroup.setResources(nodeValue);
                    if (childNode.hasAttributes()) {
                        GCMD_LOGGER.warn(NODE_NAME_RESOURCES +
                                         "tag has both attributes and value. It's probably a mistake. Attributes are IGNORED");
                    }
                } else {
                    String nodes = GCMParserHelper.getAttributeValue(childNode, ATTR_RESOURCES_NODES);
                    if (nodes != null) {
                        oarGroup.setNodes(Integer.parseInt(nodes));
                    }
                    String cpu = GCMParserHelper.getAttributeValue(childNode, ATTR_RESOURCES_CPU);
                    if (cpu != null) {
                        oarGroup.setCpu(Integer.parseInt(cpu));
                    }
                    String core = GCMParserHelper.getAttributeValue(childNode, ATTR_RESOURCES_CORE);
                    if (core != null) {
                        oarGroup.setCore(Integer.parseInt(core));
                    }
                    String walltime = GCMParserHelper.getAttributeValue(childNode, ATTR_RESOURCES_WALLTIME);
                    if (walltime != null) {
                        oarGroup.setWallTime(walltime);
                    }
                }
            } else if (nodeName.equals(NODE_NAME_DIRECTORY)) {
                oarGroup.setDirectory(nodeValue);
            } else if (nodeName.equals(NODE_NAME_WALLTIME)) {
                oarGroup.setWallTime(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDOUT)) {
                oarGroup.setStdout(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDERR)) {
                oarGroup.setStderr(nodeValue);
            }
        }

        return oarGroup;
    }
}
