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


public class GroupLSFParser extends AbstractGroupSchedulerParser {
    private static final String NODE_NAME_RESOURCES = "resources";

    private static final String ATTR_RESOURCES_WALLTIME = "walltime";

    private static final String ATTR_RESOURCES_PROCESSOR_NUMBER = "processorNumber";

    private static final String NODE_NAME = "lsfGroup";

    private static final String NODE_NAME_STDOUT = "stdout";

    private static final Object NODE_NAME_STDERR = "stderr";

    private static final String ATTR_INTERACTIVE = "interactive";

    private static final String ATTR_JOBNAME = "jobName";

    private static final String ATTR_QUEUE = "queue";

    @Override
    public AbstractGroup createGroup() {
        return new GroupLSF();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupLSF lsfGroup = (GroupLSF) super.parseGroupNode(groupNode, xpath);

        String interactive = GCMParserHelper.getAttributeValue(groupNode, ATTR_INTERACTIVE);
        lsfGroup.setInteractive(interactive);

        String queueName = GCMParserHelper.getAttributeValue(groupNode, ATTR_QUEUE);
        lsfGroup.setQueueName(queueName);

        String jobName = GCMParserHelper.getAttributeValue(groupNode, ATTR_JOBNAME);
        lsfGroup.setJobName(jobName);

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
                    lsfGroup.setResources(nodeValue);
                    if (childNode.hasAttributes()) {
                        GCMD_LOGGER.warn(NODE_NAME_RESOURCES +
                                         "tag has both attributes and value. It's probably a mistake. Attributes are IGNORED");
                    }
                } else {

                    String processorNumber = GCMParserHelper.getAttributeValue(childNode,
                                                                               ATTR_RESOURCES_PROCESSOR_NUMBER);
                    if (processorNumber != null) {
                        lsfGroup.setProcessorNumber(Integer.parseInt(processorNumber));
                    }

                    String walltime = GCMParserHelper.getAttributeValue(childNode, ATTR_RESOURCES_WALLTIME);
                    if (walltime != null) {
                        lsfGroup.setWallTime(walltime);
                    }
                }
            } else if (nodeName.equals(NODE_NAME_STDOUT)) {
                lsfGroup.setStdout(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDERR)) {
                lsfGroup.setStderr(nodeValue);
            }
        }
        return lsfGroup;
    }
}
